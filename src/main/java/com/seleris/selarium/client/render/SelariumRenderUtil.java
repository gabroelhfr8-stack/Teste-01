package com.seleris.selarium.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class SelariumRenderUtil {
    private SelariumRenderUtil() {
    }

    public static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                              float x, float y, float z, float u, float v,
                              int light, int red, int green, int blue, int alpha,
                              float normalX, float normalY, float normalZ) {
        consumer.vertex(matrix, x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, normalX, normalY, normalZ)
                .endVertex();
    }
}
