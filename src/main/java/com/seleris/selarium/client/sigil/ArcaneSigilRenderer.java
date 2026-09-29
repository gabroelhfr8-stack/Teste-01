package com.seleris.selarium.client.sigil;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.seleris.selarium.Selarium;
import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.client.render.SelariumRenderUtil;
import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.dust.DustPurity;
import com.seleris.selarium.dust.DustType;
import com.seleris.selarium.ward.WardType;
import com.mojang.math.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class ArcaneSigilRenderer implements BlockEntityRenderer<ArcaneSigilBlockEntity> {
    private static final ResourceLocation BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID, "textures/block/arcane_sigil.png");
    private static final ResourceLocation ACTIVE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID, "textures/block/arcane_sigil_active.png");
    private static final float BASE_Y = 0.071F;
    private static final float LAYER_STEP = 0.002F;
    private static final float FULL_QUAD_SCALE = 1.0F;
    private static final float OVERLAY_SCALE = 0.92F;
    private static final float ACTIVE_SCALE = 1.0F;

    private final Map<Long, VisualState> visualStates = new HashMap<>();

    public ArcaneSigilRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ArcaneSigilBlockEntity sigil, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        boolean animationsEnabled = SelariumCommonConfig.SIGIL_ANIMATIONS_ENABLED.get();
        boolean active = sigil.isActive();
        float time = getAnimationTime(sigil, partialTick);
        float phase = getPhaseOffset(sigil.getBlockPos());
        VisualState visualState = getVisualState(sigil, time);
        visualState.update(time, active, animationsEnabled);

        float activationProgress = animationsEnabled ? smoothstep(visualState.activationProgress) : active ? 1.0F : 0.0F;
        float activeLift = animationsEnabled ? getActiveLift(time, phase, activationProgress) : 0.0F;
        int baseAlpha = blendAlpha(215, pulseAlpha(time, phase, 215, 235), activationProgress);

        maybeSpawnParticles(sigil, visualState, time, phase, activeLift, activationProgress);

        drawLayer(poseStack, bufferSource, BASE_TEXTURE, BASE_Y + activeLift, 0.0F, FULL_QUAD_SCALE, packedLight, baseAlpha);

        int light = activationProgress > 0.2F ? LightTexture.FULL_BRIGHT : packedLight;
        WardType preview = ClientSigilSelection.previewFor(sigil);
        SigilVisualPlan visualPlan = SigilVisualResolver.resolve(sigil,
                preview == WardType.NONE ? sigil.getWardType() : preview);
        int visibleComponents = Math.min(4, visualPlan.componentLayers().size());
        for (int index = 0; index < visibleComponents; index++) {
            SigilVisualPlan.ComponentLayer componentLayer = visualPlan.componentLayers().get(index);
            float angle = (float) (index * Math.PI * 2.0 / visibleComponents - Math.PI / 2.0);
            float offsetX = Mth.cos(angle) * 0.36F;
            float offsetZ = Mth.sin(angle) * 0.36F;
            int alpha = blendAlpha(125, 170, activationProgress);
            drawLayer(poseStack, bufferSource, componentLayer.layer().texture(),
                    BASE_Y + activeLift + LAYER_STEP, 0.0F, 0.28F, light, alpha, offsetX, offsetZ);
        }

        if (visualPlan.wardLayer().isPresent()) {
            ArcaneSigilVisualLayer wardLayer = visualPlan.wardLayer().get();
            int inactiveAlpha = Math.max(165, Math.min(225, SelariumCommonConfig.SIGIL_WARD_LAYER_ALPHA.get()));
            int alpha = blendAlpha(inactiveAlpha, 235, activationProgress);
            drawLayer(poseStack, bufferSource, wardLayer.texture(), BASE_Y + activeLift + 2 * LAYER_STEP,
                    0.0F, OVERLAY_SCALE, light, alpha);
        }

        if (active || activationProgress > 0.01F) {
            int alpha = blendAlpha(0, pulseAlpha(time, phase + 3.0F, 55, 95), activationProgress);
            drawLayer(poseStack, bufferSource, ACTIVE_TEXTURE, BASE_Y + activeLift + 3 * LAYER_STEP,
                    0.0F, ACTIVE_SCALE, LightTexture.FULL_BRIGHT, alpha);
        }
    }

    private static float getAnimationTime(ArcaneSigilBlockEntity sigil, float partialTick) {
        Level level = sigil.getLevel();
        if (level == null) {
            return partialTick;
        }

        long created = Math.max(0L, sigil.getCreatedGameTime());
        return Math.max(0.0F, level.getGameTime() - created + partialTick);
    }

    private static float getPhaseOffset(BlockPos pos) {
        long hash = pos.asLong();
        return (hash & 1023L) * 0.015F;
    }

    private VisualState getVisualState(ArcaneSigilBlockEntity sigil, float time) {
        long key = sigil.getBlockPos().asLong();
        long created = sigil.getCreatedGameTime();
        VisualState visualState = visualStates.get(key);
        if (visualState == null || visualState.createdGameTime != created || time < visualState.lastRenderTime) {
            long gameTime = sigil.getLevel() == null ? 0L : sigil.getLevel().getGameTime();
            visualState = new VisualState(created, sigil.isActive() ? 1.0F : 0.0F, sigil.isActive(), getVisualComponentCount(sigil), gameTime, time);
            visualStates.put(key, visualState);
        }
        return visualState;
    }

    private static float getActiveLift(float time, float phase, float activationProgress) {
        float lift = SelariumCommonConfig.SIGIL_FLOATING_WHEN_ACTIVE.get()
                ? SelariumCommonConfig.SIGIL_ACTIVE_FLOAT_HEIGHT.get().floatValue() * activationProgress
                : 0.0F;
        if (SelariumCommonConfig.SIGIL_ACTIVE_BOBBING_ENABLED.get()) {
            lift += Mth.sin(time * SelariumCommonConfig.SIGIL_ACTIVE_BOBBING_SPEED.get().floatValue() + phase)
                    * SelariumCommonConfig.SIGIL_ACTIVE_BOBBING_AMPLITUDE.get().floatValue()
                    * activationProgress;
        }
        return lift;
    }

    private static int pulseAlpha(float time, float phase, int minAlpha, int maxAlpha) {
        float normalized = (Mth.sin(time * 0.12F + phase) + 1.0F) * 0.5F;
        return Mth.clamp(Math.round(Mth.lerp(normalized, minAlpha, maxAlpha)), 0, 255);
    }

    private static int blendAlpha(int inactiveAlpha, int activeAlpha, float activationProgress) {
        return Mth.clamp(Math.round(Mth.lerp(activationProgress, inactiveAlpha, activeAlpha)), 0, 255);
    }

    private static float smoothstep(float value) {
        float clamped = Mth.clamp(value, 0.0F, 1.0F);
        return clamped * clamped * (3.0F - 2.0F * clamped);
    }

    private void maybeSpawnParticles(ArcaneSigilBlockEntity sigil, VisualState visualState, float time, float phase, float activeLift, float activationProgress) {
        if (!SelariumCommonConfig.SIGIL_ANIMATIONS_ENABLED.get() || !SelariumCommonConfig.SIGIL_ENABLE_PARTICLES.get()) {
            visualState.componentCount = getVisualComponentCount(sigil);
            visualState.active = sigil.isActive();
            return;
        }

        Level level = sigil.getLevel();
        if (level == null || !level.isClientSide) {
            return;
        }

        long gameTime = level.getGameTime();
        int componentCount = getVisualComponentCount(sigil);
        boolean active = sigil.isActive();

        if (componentCount > visualState.componentCount) {
            spawnBurst(level, sigil.getBlockPos(), 5, BASE_Y + activeLift + 0.05F, false);
        }
        if (active && !visualState.active) {
            spawnBurst(level, sigil.getBlockPos(), SelariumCommonConfig.SIGIL_ACTIVATION_PARTICLE_BURST.get(), BASE_Y + activeLift + 0.08F, true);
        }
        if (!active && visualState.active) {
            spawnBurst(level, sigil.getBlockPos(), SelariumCommonConfig.SIGIL_DEACTIVATION_PARTICLE_BURST.get(), BASE_Y + activeLift + 0.05F, false);
        }
        if (activationProgress > 0.15F && gameTime - visualState.lastActiveParticleTick >= SelariumCommonConfig.SIGIL_ACTIVE_PARTICLE_INTERVAL.get()) {
            spawnAmbient(level, sigil.getBlockPos(), BASE_Y + activeLift + 0.07F, time, phase);
            visualState.lastActiveParticleTick = gameTime;
        }

        visualState.componentCount = componentCount;
        visualState.active = active;
    }

    private static int getVisualComponentCount(ArcaneSigilBlockEntity sigil) {
        return Math.max(0, sigil.getTotalComponents() - sigil.getComponentCount(DustType.ARCANE, DustPurity.BASIC));
    }

    private static void spawnBurst(Level level, BlockPos pos, int count, float yOffset, boolean active) {
        RandomSource random = level.random;
        for (int index = 0; index < count; index++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = active ? 0.28D + random.nextDouble() * 0.38D : 0.18D + random.nextDouble() * 0.28D;
            double x = pos.getX() + 0.5D + Math.cos(angle) * radius;
            double y = pos.getY() + yOffset + random.nextDouble() * 0.05D;
            double z = pos.getZ() + 0.5D + Math.sin(angle) * radius;
            level.addParticle(ParticleTypes.ENCHANT, x, y, z, -Math.cos(angle) * 0.015D, 0.025D, -Math.sin(angle) * 0.015D);
        }
    }

    private static void spawnAmbient(Level level, BlockPos pos, float yOffset, float time, float phase) {
        double angle = time * 0.12D + phase;
        double radius = 0.44D;
        double x = pos.getX() + 0.5D + Math.cos(angle) * radius;
        double y = pos.getY() + yOffset;
        double z = pos.getZ() + 0.5D + Math.sin(angle) * radius;
        level.addParticle(ParticleTypes.ENCHANT, x, y, z, -Math.cos(angle) * 0.01D, 0.018D, -Math.sin(angle) * 0.01D);
    }

    private static void drawLayer(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, float y, float rotationDegrees, float scale, int light, int alpha) {
        drawLayer(poseStack, bufferSource, texture, y, rotationDegrees, scale, light, alpha, 0.0F, 0.0F);
    }

    private static void drawLayer(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture,
                                  float y, float rotationDegrees, float scale, int light, int alpha,
                                  float offsetX, float offsetZ) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
        poseStack.pushPose();
        poseStack.translate(0.5F + offsetX, y, 0.5F + offsetZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees));
        poseStack.scale(scale, 1.0F, scale);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        SelariumRenderUtil.vertex(consumer, matrix, normal, -0.5F, 0.0F, -0.5F, 0.0F, 0.0F, light, 255, 255, 255, alpha, 0.0F, 1.0F, 0.0F);
        SelariumRenderUtil.vertex(consumer, matrix, normal, -0.5F, 0.0F, 0.5F, 0.0F, 1.0F, light, 255, 255, 255, alpha, 0.0F, 1.0F, 0.0F);
        SelariumRenderUtil.vertex(consumer, matrix, normal, 0.5F, 0.0F, 0.5F, 1.0F, 1.0F, light, 255, 255, 255, alpha, 0.0F, 1.0F, 0.0F);
        SelariumRenderUtil.vertex(consumer, matrix, normal, 0.5F, 0.0F, -0.5F, 1.0F, 0.0F, light, 255, 255, 255, alpha, 0.0F, 1.0F, 0.0F);
        poseStack.popPose();
    }

    private static final class VisualState {
        private final long createdGameTime;
        private float activationProgress;
        private float lastRenderTime;
        private int componentCount;
        private boolean active;
        private long lastActiveParticleTick;

        private VisualState(long createdGameTime, float activationProgress, boolean active, int componentCount, long lastActiveParticleTick, float lastRenderTime) {
            this.createdGameTime = createdGameTime;
            this.activationProgress = activationProgress;
            this.lastRenderTime = lastRenderTime;
            this.active = active;
            this.componentCount = componentCount;
            this.lastActiveParticleTick = lastActiveParticleTick;
        }

        private void update(float time, boolean active, boolean animationsEnabled) {
            float delta = Mth.clamp(time - lastRenderTime, 0.0F, 3.0F);
            lastRenderTime = time;

            if (!animationsEnabled) {
                activationProgress = active ? 1.0F : 0.0F;
                return;
            }

            float target = active ? 1.0F : 0.0F;
            int transitionTicks = active
                    ? SelariumCommonConfig.SIGIL_ACTIVATION_TRANSITION_TICKS.get()
                    : SelariumCommonConfig.SIGIL_DEACTIVATION_TRANSITION_TICKS.get();
            activationProgress = approach(activationProgress, target, delta / Math.max(1, transitionTicks));
        }

        private static float approach(float current, float target, float step) {
            if (current < target) {
                return Math.min(target, current + step);
            }
            return Math.max(target, current - step);
        }

    }
}
