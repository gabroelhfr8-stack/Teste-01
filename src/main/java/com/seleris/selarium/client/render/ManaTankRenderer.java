package com.seleris.selarium.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.seleris.selarium.Selarium;
import com.seleris.selarium.blockentity.ManaTankBlockEntity;
import com.seleris.selarium.client.vfx.SelariumRenderTypes;
import com.seleris.selarium.client.vfx.VfxDraw;
import com.seleris.selarium.config.SelariumClientConfig;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Draws the swirling mana inside the tank's glass; its height follows the stored mana. */
public class ManaTankRenderer implements BlockEntityRenderer<ManaTankBlockEntity> {
    private static final ResourceLocation FLUID = ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID, "textures/vfx/mana_fluid.png");
    private static final ResourceLocation GLOW = ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID, "textures/vfx/glow.png");

    /** Outline of the glass body in 1/16 blocks: two overlapping boxes forming a chamfered square. */
    private static final float[][] OUTLINE = {
            {4, 3}, {12, 3}, {12, 4}, {13, 4}, {13, 12}, {12, 12}, {12, 13}, {4, 13}, {4, 12}, {3, 12}, {3, 4}, {4, 4}};
    private static final float MIN_Y = 4.2F / 16.0F;
    private static final float MAX_Y = 12.8F / 16.0F;
    private static final float INSET = 0.985F;

    public ManaTankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ManaTankBlockEntity tank, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        float ratio = tank.getVisualFillRatio();
        Level level = tank.getLevel();
        if (ratio <= 0.0F || level == null) {
            return;
        }
        boolean animate = SelariumClientConfig.vfxEnabled();
        float time = animate ? level.getGameTime() + partialTick : 0.0F;
        float top = MIN_Y + (MAX_Y - MIN_Y) * ratio;

        VertexConsumer consumer = bufferSource.getBuffer(SelariumRenderTypes.glow(FLUID));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        float scrollU = time * 0.004F;
        float scrollV = time * -0.006F;
        int alpha = 215;

        float perimeter = 0.0F;
        for (int i = 0; i < OUTLINE.length; i++) {
            float[] a = OUTLINE[i];
            float[] b = OUTLINE[(i + 1) % OUTLINE.length];
            float ax = point(a[0]), az = point(a[1]), bx = point(b[0]), bz = point(b[1]);
            float length = (float) Math.hypot(bx - ax, bz - az);
            float u0 = perimeter * 1.4F + scrollU;
            float u1 = (perimeter + length) * 1.4F + scrollU;
            float v0 = MIN_Y * 1.6F + scrollV;
            float v1 = top * 1.6F + scrollV;
            float nx = bz - az, nz = -(bx - ax);
            float nl = Math.max(1.0E-4F, (float) Math.hypot(nx, nz));
            nx /= nl;
            nz /= nl;
            SelariumRenderUtil.vertex(consumer, matrix, normal, ax, MIN_Y, az, u0, v0, LightTexture.FULL_BRIGHT, 255, 255, 255, alpha, nx, 0.0F, nz);
            SelariumRenderUtil.vertex(consumer, matrix, normal, bx, MIN_Y, bz, u1, v0, LightTexture.FULL_BRIGHT, 255, 255, 255, alpha, nx, 0.0F, nz);
            SelariumRenderUtil.vertex(consumer, matrix, normal, bx, top, bz, u1, v1, LightTexture.FULL_BRIGHT, 255, 255, 255, alpha, nx, 0.0F, nz);
            SelariumRenderUtil.vertex(consumer, matrix, normal, ax, top, az, u0, v1, LightTexture.FULL_BRIGHT, 255, 255, 255, alpha, nx, 0.0F, nz);
            perimeter += length;
        }

        // surface: centre rectangle plus the two side strips (no overlap, so no double-blended patches)
        topRect(consumer, matrix, normal, 4, 3, 12, 13, top, scrollU, scrollV, 255);
        topRect(consumer, matrix, normal, 3, 4, 4, 12, top, scrollU, scrollV, 255);
        topRect(consumer, matrix, normal, 12, 4, 13, 12, top, scrollU, scrollV, 255);

        // a soft halo on the surface makes a nearly full tank read as "charged"
        if (animate && ratio > 0.55F) {
            VertexConsumer halo = bufferSource.getBuffer(SelariumRenderTypes.additive(GLOW));
            float pulse = 0.6F + 0.4F * (float) Math.sin(time * 0.1F);
            VfxDraw.groundQuad(halo, poseStack, 0.30F, top + 0.004F, 0.0F, 0x7EE6F2,
                    VfxDraw.alpha((ratio - 0.55F) * 0.9F * pulse), LightTexture.FULL_BRIGHT);
        }
    }

    private static float point(float pixels) {
        return 0.5F + (pixels / 16.0F - 0.5F) * INSET;
    }

    private static void topRect(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float x0, float z0, float x1, float z1,
                                float y, float scrollU, float scrollV, int brightness) {
        float ax = point(x0), az = point(z0), bx = point(x1), bz = point(z1);
        SelariumRenderUtil.vertex(consumer, matrix, normal, ax, y, az, ax * 1.6F + scrollU, az * 1.6F + scrollV, LightTexture.FULL_BRIGHT, brightness, brightness, brightness, 235, 0.0F, 1.0F, 0.0F);
        SelariumRenderUtil.vertex(consumer, matrix, normal, ax, y, bz, ax * 1.6F + scrollU, bz * 1.6F + scrollV, LightTexture.FULL_BRIGHT, brightness, brightness, brightness, 235, 0.0F, 1.0F, 0.0F);
        SelariumRenderUtil.vertex(consumer, matrix, normal, bx, y, bz, bx * 1.6F + scrollU, bz * 1.6F + scrollV, LightTexture.FULL_BRIGHT, brightness, brightness, brightness, 235, 0.0F, 1.0F, 0.0F);
        SelariumRenderUtil.vertex(consumer, matrix, normal, bx, y, az, bx * 1.6F + scrollU, az * 1.6F + scrollV, LightTexture.FULL_BRIGHT, brightness, brightness, brightness, 235, 0.0F, 1.0F, 0.0F);
    }
}
