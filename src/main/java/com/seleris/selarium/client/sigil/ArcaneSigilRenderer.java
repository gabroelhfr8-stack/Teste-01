package com.seleris.selarium.client.sigil;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.seleris.selarium.Selarium;
import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.client.vfx.SelariumRenderTypes;
import com.seleris.selarium.client.vfx.VfxDraw;
import com.seleris.selarium.client.vfx.WardShellRenderer;
import com.seleris.selarium.config.SelariumClientConfig;
import com.seleris.selarium.dust.DustDefinition;
import com.seleris.selarium.dust.DustType;
import com.seleris.selarium.util.Facets;
import com.seleris.selarium.ward.WardStyles;
import com.seleris.selarium.ward.WardType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Draws the ritual circle of an Arcane Sigil: the chalk base, dust component marks, the ward glyph, and
 * (while active) rotating light rings, orbiting runes, a floating focus crystal, a light column and the
 * ward's field shell. Layers are ordered bottom to top by tiny vertical offsets.
 */
public class ArcaneSigilRenderer implements BlockEntityRenderer<ArcaneSigilBlockEntity> {
    private static final ResourceLocation BASE = tex("vfx/sigil/base_circle.png");
    private static final ResourceLocation RING_OUTER = tex("vfx/sigil/ring_outer.png");
    private static final ResourceLocation RING_RUNES = tex("vfx/sigil/ring_runes.png");
    private static final ResourceLocation RING_STAR = tex("vfx/sigil/ring_star.png");
    private static final ResourceLocation RING_DASH = tex("vfx/sigil/ring_dash.png");
    private static final ResourceLocation GLOW = tex("vfx/glow.png");
    private static final ResourceLocation BEAM = tex("vfx/beam.png");
    private static final ResourceLocation CRYSTAL = tex("block/crystal_cyan.png");
    private static final ResourceLocation[] ORBIT_RUNES = new ResourceLocation[8];

    static {
        for (int i = 0; i < ORBIT_RUNES.length; i++) {
            ORBIT_RUNES[i] = tex("particle/rune_" + i + ".png");
        }
    }

    private static final float BASE_Y = 0.024F;
    private static final float STEP = 0.0025F;
    private static final int ACTIVATE_TICKS = 24;
    private static final int DEACTIVATE_TICKS = 28;
    private static final int MAX_MARKS = 8;

    private final Map<Long, State> states = new HashMap<>();
    private long lastPrune;

    public ArcaneSigilRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID, "textures/" + path);
    }

    @Override
    public boolean shouldRenderOffScreen(ArcaneSigilBlockEntity sigil) {
        return sigil.isActive();
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public void render(ArcaneSigilBlockEntity sigil, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
                       int packedLight, int packedOverlay) {
        Level level = sigil.getLevel();
        if (level == null) {
            return;
        }
        SelariumClientConfig.VfxQuality quality = SelariumClientConfig.VFX_QUALITY.get();
        boolean animate = quality != SelariumClientConfig.VfxQuality.OFF && SelariumClientConfig.SIGIL_ANIMATIONS.get();
        float time = level.getGameTime() + partialTick;
        State state = stateFor(sigil, time);
        state.update(time, sigil.isActive(), animate);
        float activation = state.activation * state.activation * (3.0F - 2.0F * state.activation);

        WardType preview = ClientSigilSelection.previewFor(sigil);
        WardType shown = preview != WardType.NONE ? preview : sigil.getWardType();
        WardStyles.Style style = WardStyles.of(shown);
        List<DustType> marks = visibleMarks(sigil);

        BlockPos pos = sigil.getBlockPos();
        float phase = (pos.asLong() & 1023L) * 0.015F;
        float pulse = 0.5F + 0.5F * Mth.sin(time * 0.09F + phase);
        float lift = activation * 0.05F + (animate ? Mth.sin(time * 0.11F + phase) * 0.008F * activation : 0.0F);
        float y = BASE_Y + lift;
        int dustLight = glowing(packedLight);
        int lit = activation > 0.15F ? LightTexture.FULL_BRIGHT : dustLight;
        int layer = 0;

        // 1. chalk base
        VfxDraw.groundQuad(buffers.getBuffer(SelariumRenderTypes.decal(BASE)), poseStack, 0.5F, y, 0.0F, 0xFFFFFF,
                VfxDraw.alpha(0.93F), dustLight);

        // 2. dust component marks around the ring
        if (!marks.isEmpty()) {
            float ringSpin = animate ? time * (0.2F + 0.4F * activation) : 0.0F;
            float radius = 0.305F;
            for (int i = 0; i < marks.size(); i++) {
                DustType type = marks.get(i);
                float angle = (float) (i * Math.PI * 2.0D / marks.size()) + (float) Math.toRadians(ringSpin);
                float reach = radius * Facets.polygonRadius(angle, Facets.SIDES);      // marks travel along an octagon
                float ox = Mth.cos(angle) * reach;
                float oz = Mth.sin(angle) * reach;
                poseStack.pushPose();
                poseStack.translate(ox, 0.0F, oz);
                VertexConsumer consumer = buffers.getBuffer(SelariumRenderTypes.decal(componentTexture(type)));
                VfxDraw.groundQuad(consumer, poseStack, 0.085F, y + STEP, -(float) Math.toDegrees(angle) - 90.0F,
                        DustPalette.color(type), VfxDraw.alpha(0.9F), lit);
                poseStack.popPose();
            }
            layer++;
        }

        // 3. ward glyph (resolved, previewed, or the incomplete marker)
        String glyph = shown != WardType.NONE ? shown.getSerializedName() : (marks.isEmpty() ? null : "incomplete");
        if (glyph != null) {
            int glyphColor = shown != WardType.NONE ? style.primary() : 0xC9C2EE;
            float glyphAlpha = preview != WardType.NONE
                    ? 0.55F + 0.35F * pulse
                    : Mth.lerp(activation, 0.72F + 0.12F * pulse, 0.95F);
            float spin = animate && activation > 0.0F ? time * 0.25F * activation : 0.0F;
            VfxDraw.groundQuad(buffers.getBuffer(SelariumRenderTypes.decal(glyphTexture(glyph))), poseStack, 0.5F,
                    y + (2 + layer) * STEP, spin, glyphColor, VfxDraw.alpha(glyphAlpha), lit);
            if (activation > 0.02F) {
                VfxDraw.groundQuad(buffers.getBuffer(SelariumRenderTypes.additive(glyphTexture(glyph))), poseStack, 0.5F,
                        y + (3 + layer) * STEP, spin, style.secondary(), VfxDraw.alpha(activation * (0.35F + 0.35F * pulse)),
                        LightTexture.FULL_BRIGHT);
            }
        }

        // 4. active light rings
        if (activation > 0.02F && quality != SelariumClientConfig.VfxQuality.OFF) {
            float unfold = 0.72F + 0.28F * activation;
            float ringY = y + 6 * STEP;
            VfxDraw.groundQuad(buffers.getBuffer(SelariumRenderTypes.additive(RING_OUTER)), poseStack, 0.5F * unfold, ringY,
                    animate ? time * 0.28F : 0.0F, 0xFFE9A8, VfxDraw.alpha(activation * 0.8F), LightTexture.FULL_BRIGHT);
            VfxDraw.groundQuad(buffers.getBuffer(SelariumRenderTypes.additive(RING_RUNES)), poseStack, 0.5F * unfold, ringY + STEP,
                    animate ? time * 0.7F : 0.0F, style.secondary(), VfxDraw.alpha(activation * 0.85F), LightTexture.FULL_BRIGHT);
            VfxDraw.groundQuad(buffers.getBuffer(SelariumRenderTypes.additive(RING_STAR)), poseStack, 0.5F * unfold, ringY + 2 * STEP,
                    animate ? -time * 1.05F : 0.0F, style.primary(), VfxDraw.alpha(activation * (0.55F + 0.3F * pulse)), LightTexture.FULL_BRIGHT);
            if (quality.atLeast(SelariumClientConfig.VfxQuality.MEDIUM)) {
                VfxDraw.groundQuad(buffers.getBuffer(SelariumRenderTypes.additive(RING_DASH)), poseStack, 0.5F * unfold, ringY + 3 * STEP,
                        animate ? time * 1.6F : 0.0F, style.secondary(), VfxDraw.alpha(activation * 0.5F), LightTexture.FULL_BRIGHT);
            }
        }

        // 5. orbiting runes and the floating focus crystal
        if (activation > 0.05F && animate && SelariumClientConfig.SIGIL_FLOATING_RUNES.get()
                && quality.atLeast(SelariumClientConfig.VfxQuality.MEDIUM)) {
            drawOrbit(poseStack, buffers, time, phase, activation, style, quality.atLeast(SelariumClientConfig.VfxQuality.HIGH) ? 6 : 4);
            drawFocusCrystal(poseStack, buffers, time, phase, activation, style, Math.min(marks.size(), 6));
        }

        // 6. light column
        if (activation > 0.05F && animate && SelariumClientConfig.SIGIL_LIGHT_BEAM.get()
                && quality.atLeast(SelariumClientConfig.VfxQuality.HIGH)) {
            drawBeam(poseStack, buffers, time, activation, style.primary());
        }

        // 7. the field itself
        if (sigil.isActive() || activation > 0.05F) {
            WardType fieldType = sigil.getWardType();
            if (fieldType != WardType.NONE) {
                Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                Vec3 viewer = camera.subtract(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
                WardShellRenderer.draw(poseStack, buffers, fieldType, Math.max(1, sigil.getRange()), activation, time, viewer, pos.asLong());
            }
        }

        if (time - lastPrune > 400.0F) {
            lastPrune = (long) time;
            states.values().removeIf(entry -> time - entry.lastTime > 200.0F);
        }
    }

    private void drawOrbit(PoseStack poseStack, MultiBufferSource buffers, float time, float phase, float activation,
                           WardStyles.Style style, int count) {
        Quaternionf camera = Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation();
        for (int i = 0; i < count; i++) {
            float angle = (float) Math.toRadians(time * 1.7F) + i * (float) (Math.PI * 2.0D / count);
            float bob = Mth.sin(time * 0.09F + i * 1.7F + phase) * 0.035F;
            float reach = 0.4F * Facets.polygonRadius(angle, Facets.SIDES);         // an octagonal orbit, not a circle
            float rx = 0.5F + Mth.cos(angle) * reach;
            float rz = 0.5F + Mth.sin(angle) * reach;
            float ry = 0.42F + bob + 0.06F * Mth.sin(i * 2.1F);
            VertexConsumer consumer = buffers.getBuffer(SelariumRenderTypes.additive(ORBIT_RUNES[i % ORBIT_RUNES.length]));
            VfxDraw.billboard(consumer, poseStack, camera, rx, ry, rz, 0.07F, 0.0F, style.secondary(),
                    VfxDraw.alpha(activation * 0.85F), LightTexture.FULL_BRIGHT);
        }
    }

    private void drawFocusCrystal(PoseStack poseStack, MultiBufferSource buffers, float time, float phase, float activation,
                                  WardStyles.Style style, int marks) {
        float size = (0.055F + 0.012F * marks) * (0.6F + 0.4F * activation);
        float height = size * 1.55F;
        float cy = 0.62F + Mth.sin(time * 0.07F + phase) * 0.03F;
        int tint = VfxDraw.mix(0xFFFFFF, style.primary(), 0.55F);
        int alpha = VfxDraw.alpha(0.92F * activation);

        Quaternionf camera = Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation();
        VfxDraw.billboard(buffers.getBuffer(SelariumRenderTypes.additive(GLOW)), poseStack, camera, 0.5F, cy, 0.5F,
                0.26F + 0.03F * Mth.sin(time * 0.13F), time * 0.8F, style.primary(), VfxDraw.alpha(0.5F * activation), LightTexture.FULL_BRIGHT);

        poseStack.pushPose();
        poseStack.translate(0.5F, cy, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.6F));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(time * 0.05F) * 8.0F));
        VertexConsumer consumer = buffers.getBuffer(SelariumRenderTypes.glow(CRYSTAL));
        PoseStack.Pose pose = poseStack.last();
        float[][] equator = {{size, 0, 0}, {0, 0, size}, {-size, 0, 0}, {0, 0, -size}};
        for (int i = 0; i < 4; i++) {
            float[] a = equator[i];
            float[] b = equator[(i + 1) % 4];
            float shadeTop = 0.72F + 0.28F * (i % 2);
            float shadeBottom = 0.5F + 0.25F * (i % 2);
            int top = VfxDraw.mix(0x000000, tint, shadeTop);
            int bottom = VfxDraw.mix(0x000000, tint, shadeBottom);
            VfxDraw.triangle(consumer, pose, 0, height, 0, 0.5F, 0.0F, a[0], a[1], a[2], 0.0F, 1.0F, b[0], b[1], b[2], 1.0F, 1.0F,
                    top, alpha, LightTexture.FULL_BRIGHT);
            VfxDraw.triangle(consumer, pose, 0, -height, 0, 0.5F, 1.0F, b[0], b[1], b[2], 1.0F, 0.0F, a[0], a[1], a[2], 0.0F, 0.0F,
                    bottom, alpha, LightTexture.FULL_BRIGHT);
        }
        poseStack.popPose();
    }

    private void drawBeam(PoseStack poseStack, MultiBufferSource buffers, float time, float activation, int rgb) {
        VertexConsumer consumer = buffers.getBuffer(SelariumRenderTypes.additive(BEAM));
        int alpha = VfxDraw.alpha(0.42F * activation);
        for (int i = 0; i < 2; i++) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.05F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(time * 0.6F + i * 90.0F));
            VfxDraw.verticalQuad(consumer, poseStack.last(), 0.2F, 2.6F, rgb, alpha, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
        }
    }

    // ---------------------------------------------------------------------------- helpers
    /** Sigil dust glows faintly, so a drawn circle stays readable at night and in caves. */
    private static int glowing(int packedLight) {
        return LightTexture.pack(Math.max(LightTexture.block(packedLight), 6), Math.max(LightTexture.sky(packedLight), 2));
    }

    private static ResourceLocation glyphTexture(String name) {
        return tex("vfx/sigil/glyph/" + name + ".png");
    }

    private static ResourceLocation componentTexture(DustType type) {
        return tex("vfx/sigil/component/" + type.getSerializedName() + ".png");
    }

    /** Distinct dust types shown as marks; the base Arcane dust placed at creation is not drawn on its own. */
    private static List<DustType> visibleMarks(ArcaneSigilBlockEntity sigil) {
        Map<DustType, Integer> counts = new EnumMap<>(DustType.class);
        for (Map.Entry<DustDefinition, Integer> entry : sigil.getComponents().entrySet()) {
            if (entry.getValue() > 0) {
                counts.merge(entry.getKey().type(), entry.getValue(), Integer::sum);
            }
        }
        int arcane = counts.getOrDefault(DustType.ARCANE, 0);
        if (arcane <= 1) {
            counts.remove(DustType.ARCANE);
        } else {
            counts.put(DustType.ARCANE, arcane - 1);
        }
        List<DustType> types = new ArrayList<>(counts.keySet());
        types.sort(Comparator.comparingInt(DustPalette::priority));
        return types.size() > MAX_MARKS ? types.subList(0, MAX_MARKS) : types;
    }

    private State stateFor(ArcaneSigilBlockEntity sigil, float time) {
        long key = sigil.getBlockPos().asLong();
        State state = states.get(key);
        if (state == null || time < state.lastTime) {
            state = new State(sigil.isActive() ? 1.0F : 0.0F, time);
            states.put(key, state);
        }
        return state;
    }

    /** Smoothed activation (0..1) so rings unfold and fade instead of popping. */
    private static final class State {
        private float activation;
        private float lastTime;

        private State(float activation, float time) {
            this.activation = activation;
            this.lastTime = time;
        }

        private void update(float time, boolean active, boolean animate) {
            float delta = Mth.clamp(time - lastTime, 0.0F, 3.0F);
            lastTime = time;
            if (!animate) {
                activation = active ? 1.0F : 0.0F;
                return;
            }
            float target = active ? 1.0F : 0.0F;
            float step = delta / (active ? ACTIVATE_TICKS : DEACTIVATE_TICKS);
            activation = activation < target ? Math.min(target, activation + step) : Math.max(target, activation - step);
        }
    }
}
