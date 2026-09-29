package com.seleris.selarium.client.sigil;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.seleris.selarium.Selarium;
import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.client.render.SelariumRenderUtil;
import com.seleris.selarium.client.vfx.SelariumRenderTypes;
import com.seleris.selarium.client.vfx.VfxDraw;
import com.seleris.selarium.client.vfx.VoxelField;
import com.seleris.selarium.client.vfx.WardShellRenderer;
import com.seleris.selarium.config.SelariumClientConfig;
import com.seleris.selarium.dust.DustDefinition;
import com.seleris.selarium.dust.DustType;
import com.seleris.selarium.ward.WardShellStyle;
import com.seleris.selarium.ward.WardStyles;
import com.seleris.selarium.ward.WardType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Draws the ritual circle of an Arcane Sigil: the chalk base, dust component marks, the ward glyph, and
 * (while active) light rings, orbiting runes, a floating focus crystal, a column of light that rises to the top of the
 * field, and the field itself. Everything on the ground is pixel art on the block's 32x32 grid: rings turn in quarter
 * steps like the hand of a clock, marks and runes are snapped to the grid, and nothing is rotated by an odd angle.
 * Layers are ordered bottom to top by tiny vertical offsets.
 */
public class ArcaneSigilRenderer implements BlockEntityRenderer<ArcaneSigilBlockEntity> {
    private static final ResourceLocation BASE = tex("vfx/sigil/base_circle.png");
    private static final ResourceLocation RING_OUTER = tex("vfx/sigil/ring_outer.png");
    private static final ResourceLocation RING_RUNES = tex("vfx/sigil/ring_runes.png");
    private static final ResourceLocation RING_STAR = tex("vfx/sigil/ring_star.png");
    private static final ResourceLocation RING_DASH = tex("vfx/sigil/ring_dash.png");
    private static final ResourceLocation GLOW = tex("vfx/glow.png");
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
    /** The chalk texture is 32 pixels per block. */
    private static final float PIXEL = 1.0F / 32.0F;
    private static final int INERT_COLOR = 0x7A8494;
    /** Height of the light column above wards that are made of real blocks and have no dome to reach. */
    private static final int WALL_BEAM_HEIGHT = 6;

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
        boolean inert = sigil.isInert();
        // an inert sigil keeps its rings but they burn low and grey: the ward is on and asleep
        float energy = activation * (inert ? 0.3F : 1.0F);

        WardType preview = ClientSigilSelection.previewFor(sigil);
        WardType shown = preview != WardType.NONE ? preview : sigil.getWardType();
        WardStyles.Style style = WardStyles.of(shown);
        int primary = tint(style.primary(), inert);
        int secondary = tint(style.secondary(), inert);
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

        // 2. dust component marks around the ring, each on its own pixel of the grid
        if (!marks.isEmpty()) {
            float radius = 0.305F;
            for (int i = 0; i < marks.size(); i++) {
                DustType type = marks.get(i);
                double angle = Math.toRadians(-90.0D + i * 360.0D / marks.size());
                float ox = snap((float) Math.cos(angle) * radius, PIXEL);
                float oz = snap((float) Math.sin(angle) * radius, PIXEL);
                poseStack.pushPose();
                poseStack.translate(ox, 0.0F, oz);
                VertexConsumer consumer = buffers.getBuffer(SelariumRenderTypes.decal(componentTexture(type)));
                VfxDraw.groundQuad(consumer, poseStack, 4.0F * PIXEL, y + STEP, 0.0F, DustPalette.color(type), VfxDraw.alpha(0.9F), lit);
                poseStack.popPose();
            }
            layer++;
        }

        // 3. ward glyph (resolved, previewed, or the incomplete marker): it never turns, it only glows
        String glyph = shown != WardType.NONE ? shown.getSerializedName() : (marks.isEmpty() ? null : "incomplete");
        if (glyph != null) {
            int glyphColor = shown != WardType.NONE ? primary : 0xC9C2EE;
            float glyphAlpha = preview != WardType.NONE
                    ? 0.55F + 0.35F * pulse
                    : Mth.lerp(activation, 0.72F + 0.12F * pulse, inert ? 0.6F : 0.95F);
            VfxDraw.groundQuad(buffers.getBuffer(SelariumRenderTypes.decal(glyphTexture(glyph))), poseStack, 0.5F,
                    y + (2 + layer) * STEP, 0.0F, glyphColor, VfxDraw.alpha(glyphAlpha), lit);
            if (energy > 0.02F) {
                VfxDraw.groundQuad(buffers.getBuffer(SelariumRenderTypes.additive(glyphTexture(glyph))), poseStack, 0.5F,
                        y + (3 + layer) * STEP, 0.0F, secondary, VfxDraw.alpha(energy * (0.35F + 0.35F * pulse)),
                        LightTexture.FULL_BRIGHT);
            }
        }

        // 4. active light rings: they tick round a quarter turn at a time, so their pixels stay on the grid
        if (energy > 0.02F && quality != SelariumClientConfig.VfxQuality.OFF) {
            float ringY = y + 6 * STEP;
            float a = animate ? 1.0F : 0.0F;
            VfxDraw.groundQuad(buffers.getBuffer(SelariumRenderTypes.additive(RING_OUTER)), poseStack, 0.5F, ringY,
                    quarterTurns(time, 36.0F, 1) * a, tint(0xFFE9A8, inert), VfxDraw.alpha(energy * 0.8F), LightTexture.FULL_BRIGHT);
            VfxDraw.groundQuad(buffers.getBuffer(SelariumRenderTypes.additive(RING_RUNES)), poseStack, 0.5F, ringY + STEP,
                    quarterTurns(time, 24.0F, -1) * a, secondary, VfxDraw.alpha(energy * 0.85F), LightTexture.FULL_BRIGHT);
            VfxDraw.groundQuad(buffers.getBuffer(SelariumRenderTypes.additive(RING_STAR)), poseStack, 0.5F, ringY + 2 * STEP,
                    quarterTurns(time, 15.0F, 1) * a, primary, VfxDraw.alpha(energy * (0.55F + 0.3F * pulse)), LightTexture.FULL_BRIGHT);
            if (quality.atLeast(SelariumClientConfig.VfxQuality.MEDIUM)) {
                VfxDraw.groundQuad(buffers.getBuffer(SelariumRenderTypes.additive(RING_DASH)), poseStack, 0.5F, ringY + 3 * STEP,
                        quarterTurns(time, 10.0F, -1) * a, secondary, VfxDraw.alpha(energy * 0.5F), LightTexture.FULL_BRIGHT);
            }
        }

        // 5. orbiting runes and the floating focus crystal
        if (energy > 0.05F && animate && SelariumClientConfig.SIGIL_FLOATING_RUNES.get()
                && quality.atLeast(SelariumClientConfig.VfxQuality.MEDIUM)) {
            drawOrbit(poseStack, buffers, time, phase, energy, secondary, quality.atLeast(SelariumClientConfig.VfxQuality.HIGH) ? 6 : 4);
            drawFocusCrystal(poseStack, buffers, time, phase, energy, primary, Math.min(marks.size(), 6));
        }

        // 6. the column of light: it climbs from the crystal to the top of the field as the sigil wakes
        WardType fieldType = sigil.getWardType();
        if (energy > 0.05F && !inert && animate && fieldType != WardType.NONE && SelariumClientConfig.SIGIL_LIGHT_BEAM.get()
                && quality.atLeast(SelariumClientConfig.VfxQuality.MEDIUM)) {
            drawBeam(poseStack, buffers, partialTick, level.getGameTime(), activation, beamHeight(sigil, fieldType), primary);
        }

        // 7. the field itself
        if (sigil.isActive() || activation > 0.05F) {
            if (fieldType != WardType.NONE) {
                Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                Vec3 viewer = camera.subtract(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
                WardShellRenderer.draw(poseStack, buffers, fieldType, Math.max(1, sigil.getRange()), activation, time, viewer,
                        pos.asLong(), inert);
            }
        }

        if (time - lastPrune > 400.0F) {
            lastPrune = (long) time;
            states.values().removeIf(entry -> time - entry.lastTime > 200.0F);
        }
    }

    /** A ring's angle: a whole number of quarter turns, advancing every {@code period} ticks. */
    private static float quarterTurns(float time, float period, int direction) {
        return direction * 90.0F * (((int) Math.floor(time / period)) & 3);
    }

    private static float snap(float value, float grid) {
        return Math.round(value / grid) * grid;
    }

    private static int tint(int rgb, boolean inert) {
        return inert ? VfxDraw.mix(rgb, INERT_COLOR, 0.75F) : rgb;
    }

    private void drawOrbit(PoseStack poseStack, MultiBufferSource buffers, float time, float phase, float energy, int rgb, int count) {
        Quaternionf camera = Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation();
        for (int i = 0; i < count; i++) {
            float angle = (float) Math.toRadians(time * 1.7F) + i * (float) (Math.PI * 2.0D / count);
            float bob = Mth.sin(time * 0.09F + i * 1.7F + phase) * 0.035F;
            // the runes drift round a circle, but always sit on the 1/16 grid of the block
            float rx = 0.5F + snap(Mth.cos(angle) * 0.4F, 0.0625F);
            float rz = 0.5F + snap(Mth.sin(angle) * 0.4F, 0.0625F);
            float ry = snap(0.42F + bob + 0.06F * Mth.sin(i * 2.1F), 0.0625F);
            VertexConsumer consumer = buffers.getBuffer(SelariumRenderTypes.additive(ORBIT_RUNES[i % ORBIT_RUNES.length]));
            VfxDraw.billboard(consumer, poseStack, camera, rx, ry, rz, 0.09F, 0.0F, rgb,
                    VfxDraw.alpha(energy * 0.85F), LightTexture.FULL_BRIGHT);
        }
    }

    /** A double-pointed crystal, like a cut amethyst: a short square shaft with a pyramid on each end. */
    private void drawFocusCrystal(PoseStack poseStack, MultiBufferSource buffers, float time, float phase, float energy, int rgb,
                                  int marks) {
        float half = (0.06F + 0.012F * marks) * (0.6F + 0.4F * energy);
        float shaft = half * 0.8F;
        float tip = half * 1.35F;
        float cy = 0.62F + Mth.sin(time * 0.07F + phase) * 0.03F;
        int tint = VfxDraw.mix(0xFFFFFF, rgb, 0.55F);
        int alpha = VfxDraw.alpha(0.92F * energy);

        Quaternionf camera = Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation();
        VfxDraw.billboard(buffers.getBuffer(SelariumRenderTypes.additive(GLOW)), poseStack, camera, 0.5F, cy, 0.5F,
                0.26F + 0.03F * Mth.sin(time * 0.13F), 0.0F, rgb, VfxDraw.alpha(0.5F * energy), LightTexture.FULL_BRIGHT);

        poseStack.pushPose();
        poseStack.translate(0.5F, cy, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.6F));
        VertexConsumer consumer = buffers.getBuffer(SelariumRenderTypes.glow(CRYSTAL));
        PoseStack.Pose pose = poseStack.last();
        float[][] ring = {{half, 0, 0}, {0, 0, half}, {-half, 0, 0}, {0, 0, -half}};
        for (int i = 0; i < 4; i++) {
            float[] a = ring[i];
            float[] b = ring[(i + 1) % 4];
            int top = VfxDraw.mix(0x000000, tint, 0.72F + 0.28F * (i % 2));
            int mid = VfxDraw.mix(0x000000, tint, 0.86F - 0.2F * (i % 2));
            int bottom = VfxDraw.mix(0x000000, tint, 0.5F + 0.25F * (i % 2));
            VfxDraw.triangle(consumer, pose, 0, shaft + tip, 0, 0.5F, 0.0F, a[0], shaft, a[2], 0.0F, 1.0F, b[0], shaft, b[2], 1.0F, 1.0F,
                    top, alpha, LightTexture.FULL_BRIGHT);
            quad(consumer, pose, a[0], shaft, a[2], b[0], shaft, b[2], b[0], -shaft, b[2], a[0], -shaft, a[2], mid, alpha);
            VfxDraw.triangle(consumer, pose, 0, -shaft - tip, 0, 0.5F, 1.0F, b[0], -shaft, b[2], 1.0F, 0.0F, a[0], -shaft, a[2], 0.0F, 0.0F,
                    bottom, alpha, LightTexture.FULL_BRIGHT);
        }
        poseStack.popPose();
    }

    /** One textured quad (the whole tile), with its normal taken from the first three corners. */
    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz, int rgb, int alpha) {
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        float ux = bx - ax, uy = by - ay, uz = bz - az;
        float vx = cx - ax, vy = cy - ay, vz = cz - az;
        float nx = uy * vz - uz * vy, ny = uz * vx - ux * vz, nz = ux * vy - uy * vx;
        float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        if (length > 1.0E-6F) {
            nx /= length;
            ny /= length;
            nz /= length;
        }
        int r = VfxDraw.red(rgb), g = VfxDraw.green(rgb), b = VfxDraw.blue(rgb);
        SelariumRenderUtil.vertex(consumer, matrix, normal, ax, ay, az, 0.0F, 0.0F, LightTexture.FULL_BRIGHT, r, g, b, alpha, nx, ny, nz);
        SelariumRenderUtil.vertex(consumer, matrix, normal, bx, by, bz, 1.0F, 0.0F, LightTexture.FULL_BRIGHT, r, g, b, alpha, nx, ny, nz);
        SelariumRenderUtil.vertex(consumer, matrix, normal, cx, cy, cz, 1.0F, 1.0F, LightTexture.FULL_BRIGHT, r, g, b, alpha, nx, ny, nz);
        SelariumRenderUtil.vertex(consumer, matrix, normal, dx, dy, dz, 0.0F, 1.0F, LightTexture.FULL_BRIGHT, r, g, b, alpha, nx, ny, nz);
    }

    /** Whole blocks from the crystal up to the top of the field (a few for wards that are made of real blocks). */
    private static int beamHeight(ArcaneSigilBlockEntity sigil, WardType type) {
        if (WardStyles.of(type).shell() == WardShellStyle.NONE) {
            return WALL_BEAM_HEIGHT;
        }
        return Math.max(2, (int) Math.ceil(VoxelField.of(Math.max(1, sigil.getRange())).topHeight() - BEAM_START));
    }

    /** Where the column starts above the sigil's floor: just under the focus crystal. */
    private static final float BEAM_START = 0.3F;

    /** The vanilla beacon beam, thin and tinted with the ward's colour; it grows with the sigil's activation. */
    private void drawBeam(PoseStack poseStack, MultiBufferSource buffers, float partialTick, long gameTime, float rise, int height,
                          int rgb) {
        int light = VfxDraw.mix(rgb, 0xFFFFFF, 0.35F);
        float[] colors = {VfxDraw.red(light) / 255.0F, VfxDraw.green(light) / 255.0F, VfxDraw.blue(light) / 255.0F};
        poseStack.pushPose();
        poseStack.translate(0.0F, BEAM_START, 0.0F);
        poseStack.scale(1.0F, Math.max(0.02F, rise), 1.0F);
        BeaconRenderer.renderBeaconBeam(poseStack, buffers, BeaconRenderer.BEAM_LOCATION, partialTick, 1.0F, gameTime, 0, height,
                colors, 0.11F, 0.17F);
        poseStack.popPose();
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

    /** Smoothed activation (0..1) so rings fade and the column climbs instead of popping. */
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
