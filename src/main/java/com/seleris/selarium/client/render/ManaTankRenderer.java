package com.seleris.selarium.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.seleris.selarium.Selarium;
import com.seleris.selarium.blockentity.ManaTankBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class ManaTankRenderer implements BlockEntityRenderer<ManaTankBlockEntity> {
    private static final ResourceLocation FILL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID, "textures/block/mana_tank_fill.png");
    private static final float MIN = 0.21F;
    private static final float MAX = 0.79F;
    private static final float MIN_Y = 0.19F;
    private static final float MAX_Y = 0.81F;
    private static final float OFFSET = 0.004F;

    public ManaTankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ManaTankBlockEntity tank, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float ratio = tank.getVisualFillRatio();
        if (ratio <= 0.0F) {
            return;
        }

        float fillTop = MIN_Y + (MAX_Y - MIN_Y) * ratio;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(FILL_TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        for (Direction face : Direction.Plane.HORIZONTAL) {
            drawFrontFill(consumer, pose.pose(), pose.normal(), face, fillTop, ratio);
        }
        drawTop(consumer, pose.pose(), pose.normal(), fillTop);
    }

    private static void drawFrontFill(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, Direction facing, float fillTop, float ratio) {
        float vTop = 1.0F - ratio;
        switch (facing) {
            case SOUTH -> {
                vertex(consumer, matrix, normal, MAX, MIN_Y, MAX + OFFSET, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F);
                vertex(consumer, matrix, normal, MIN, MIN_Y, MAX + OFFSET, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F);
                vertex(consumer, matrix, normal, MIN, fillTop, MAX + OFFSET, 1.0F, vTop, 0.0F, 0.0F, 1.0F);
                vertex(consumer, matrix, normal, MAX, fillTop, MAX + OFFSET, 0.0F, vTop, 0.0F, 0.0F, 1.0F);
            }
            case EAST -> {
                vertex(consumer, matrix, normal, MAX + OFFSET, MIN_Y, MIN, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F);
                vertex(consumer, matrix, normal, MAX + OFFSET, MIN_Y, MAX, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F);
                vertex(consumer, matrix, normal, MAX + OFFSET, fillTop, MAX, 1.0F, vTop, 1.0F, 0.0F, 0.0F);
                vertex(consumer, matrix, normal, MAX + OFFSET, fillTop, MIN, 0.0F, vTop, 1.0F, 0.0F, 0.0F);
            }
            case WEST -> {
                vertex(consumer, matrix, normal, MIN - OFFSET, MIN_Y, MAX, 0.0F, 1.0F, -1.0F, 0.0F, 0.0F);
                vertex(consumer, matrix, normal, MIN - OFFSET, MIN_Y, MIN, 1.0F, 1.0F, -1.0F, 0.0F, 0.0F);
                vertex(consumer, matrix, normal, MIN - OFFSET, fillTop, MIN, 1.0F, vTop, -1.0F, 0.0F, 0.0F);
                vertex(consumer, matrix, normal, MIN - OFFSET, fillTop, MAX, 0.0F, vTop, -1.0F, 0.0F, 0.0F);
            }
            default -> {
                vertex(consumer, matrix, normal, MIN, MIN_Y, MIN - OFFSET, 0.0F, 1.0F, 0.0F, 0.0F, -1.0F);
                vertex(consumer, matrix, normal, MAX, MIN_Y, MIN - OFFSET, 1.0F, 1.0F, 0.0F, 0.0F, -1.0F);
                vertex(consumer, matrix, normal, MAX, fillTop, MIN - OFFSET, 1.0F, vTop, 0.0F, 0.0F, -1.0F);
                vertex(consumer, matrix, normal, MIN, fillTop, MIN - OFFSET, 0.0F, vTop, 0.0F, 0.0F, -1.0F);
            }
        }
    }

    private static void drawTop(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float y) {
        vertex(consumer, matrix, normal, MIN, y, MIN, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F);
        vertex(consumer, matrix, normal, MIN, y, MAX, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F);
        vertex(consumer, matrix, normal, MAX, y, MAX, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F);
        vertex(consumer, matrix, normal, MAX, y, MIN, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ) {
        SelariumRenderUtil.vertex(consumer, matrix, normal, x, y, z, u, v,
                LightTexture.FULL_BRIGHT, 225, 245, 255, 210, normalX, normalY, normalZ);
    }
}
