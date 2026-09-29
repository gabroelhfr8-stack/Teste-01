package com.seleris.selarium.client.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.seleris.selarium.client.render.SelariumRenderUtil;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/** Small geometry helpers shared by the sigil, shell and tank renderers. All colours are 0-255. */
public final class VfxDraw {
    private VfxDraw() {
    }

    public static int red(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    public static int green(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    public static int blue(int rgb) {
        return rgb & 0xFF;
    }

    /** Linear blend of two 0xRRGGBB colours. */
    public static int mix(int a, int b, float t) {
        float clamped = Math.max(0.0F, Math.min(1.0F, t));
        int r = Math.round(red(a) + (red(b) - red(a)) * clamped);
        int g = Math.round(green(a) + (green(b) - green(a)) * clamped);
        int bl = Math.round(blue(a) + (blue(b) - blue(a)) * clamped);
        return (r << 16) | (g << 8) | bl;
    }

    public static int alpha(float value) {
        return Math.max(0, Math.min(255, Math.round(value * 255.0F)));
    }

    /**
     * A horizontal square (facing up) centred on (0.5, y, 0.5) of the current block-space pose,
     * rotated about Y by {@code rotationDegrees} and scaled to half-width {@code half}.
     */
    public static void groundQuad(VertexConsumer consumer, PoseStack poseStack, float half, float y, float rotationDegrees,
                                  int rgb, int alpha, int light) {
        poseStack.pushPose();
        poseStack.translate(0.5F, y, 0.5F);
        if (rotationDegrees != 0.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees));
        }
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        int r = red(rgb), g = green(rgb), b = blue(rgb);
        SelariumRenderUtil.vertex(consumer, matrix, normal, -half, 0.0F, -half, 0.0F, 0.0F, light, r, g, b, alpha, 0.0F, 1.0F, 0.0F);
        SelariumRenderUtil.vertex(consumer, matrix, normal, -half, 0.0F, half, 0.0F, 1.0F, light, r, g, b, alpha, 0.0F, 1.0F, 0.0F);
        SelariumRenderUtil.vertex(consumer, matrix, normal, half, 0.0F, half, 1.0F, 1.0F, light, r, g, b, alpha, 0.0F, 1.0F, 0.0F);
        SelariumRenderUtil.vertex(consumer, matrix, normal, half, 0.0F, -half, 1.0F, 0.0F, light, r, g, b, alpha, 0.0F, 1.0F, 0.0F);
        poseStack.popPose();
    }

    /** A camera-facing square centred at (x, y, z) of the current pose. */
    public static void billboard(VertexConsumer consumer, PoseStack poseStack, Quaternionf cameraRotation,
                                 float x, float y, float z, float half, float rollDegrees,
                                 int rgb, int alpha, int light) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(cameraRotation);
        if (rollDegrees != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(rollDegrees));
        }
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        int r = red(rgb), g = green(rgb), b = blue(rgb);
        SelariumRenderUtil.vertex(consumer, matrix, normal, -half, -half, 0.0F, 0.0F, 1.0F, light, r, g, b, alpha, 0.0F, 0.0F, 1.0F);
        SelariumRenderUtil.vertex(consumer, matrix, normal, half, -half, 0.0F, 1.0F, 1.0F, light, r, g, b, alpha, 0.0F, 0.0F, 1.0F);
        SelariumRenderUtil.vertex(consumer, matrix, normal, half, half, 0.0F, 1.0F, 0.0F, light, r, g, b, alpha, 0.0F, 0.0F, 1.0F);
        SelariumRenderUtil.vertex(consumer, matrix, normal, -half, half, 0.0F, 0.0F, 0.0F, light, r, g, b, alpha, 0.0F, 0.0F, 1.0F);
        poseStack.popPose();
    }

    /** A vertical square standing on the XY plane at the current pose origin (used for light beams). */
    public static void verticalQuad(VertexConsumer consumer, PoseStack.Pose pose, float halfWidth, float height,
                                    int rgb, int alpha, int light) {
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        int r = red(rgb), g = green(rgb), b = blue(rgb);
        SelariumRenderUtil.vertex(consumer, matrix, normal, -halfWidth, 0.0F, 0.0F, 0.0F, 1.0F, light, r, g, b, alpha, 0.0F, 0.0F, 1.0F);
        SelariumRenderUtil.vertex(consumer, matrix, normal, halfWidth, 0.0F, 0.0F, 1.0F, 1.0F, light, r, g, b, alpha, 0.0F, 0.0F, 1.0F);
        SelariumRenderUtil.vertex(consumer, matrix, normal, halfWidth, height, 0.0F, 1.0F, 0.0F, light, r, g, b, alpha, 0.0F, 0.0F, 1.0F);
        SelariumRenderUtil.vertex(consumer, matrix, normal, -halfWidth, height, 0.0F, 0.0F, 0.0F, light, r, g, b, alpha, 0.0F, 0.0F, 1.0F);
    }

    /** A triangle whose three corners have their own alpha (0-255), so a flat facet can carry a soft gradient. */
    public static void shadedTriangle(VertexConsumer consumer, PoseStack.Pose pose,
                                      float ax, float ay, float az, float au, float av, int aa,
                                      float bx, float by, float bz, float bu, float bv, int ba,
                                      float cx, float cy, float cz, float cu, float cv, int ca,
                                      int rgb, int light) {
        Matrix4f matrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        float ux = bx - ax, uy = by - ay, uz = bz - az;
        float vx = cx - ax, vy = cy - ay, vz = cz - az;
        float nx = uy * vz - uz * vy, ny = uz * vx - ux * vz, nz = ux * vy - uy * vx;
        float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length > 1.0E-6F) {
            nx /= length;
            ny /= length;
            nz /= length;
        }
        int r = red(rgb), g = green(rgb), b = blue(rgb);
        SelariumRenderUtil.vertex(consumer, matrix, normalMatrix, ax, ay, az, au, av, light, r, g, b, aa, nx, ny, nz);
        SelariumRenderUtil.vertex(consumer, matrix, normalMatrix, bx, by, bz, bu, bv, light, r, g, b, ba, nx, ny, nz);
        SelariumRenderUtil.vertex(consumer, matrix, normalMatrix, cx, cy, cz, cu, cv, light, r, g, b, ca, nx, ny, nz);
        SelariumRenderUtil.vertex(consumer, matrix, normalMatrix, cx, cy, cz, cu, cv, light, r, g, b, ca, nx, ny, nz);
    }

    /** One triangle, emitted as a degenerate quad so it works with the QUADS render types. */
    public static void triangle(VertexConsumer consumer, PoseStack.Pose pose,
                                float ax, float ay, float az, float au, float av,
                                float bx, float by, float bz, float bu, float bv,
                                float cx, float cy, float cz, float cu, float cv,
                                int rgb, int alpha, int light) {
        Matrix4f matrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        float ux = bx - ax, uy = by - ay, uz = bz - az;
        float vx = cx - ax, vy = cy - ay, vz = cz - az;
        float nx = uy * vz - uz * vy, ny = uz * vx - ux * vz, nz = ux * vy - uy * vx;
        float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length > 1.0E-6F) {
            nx /= length;
            ny /= length;
            nz /= length;
        }
        int r = red(rgb), g = green(rgb), b = blue(rgb);
        SelariumRenderUtil.vertex(consumer, matrix, normalMatrix, ax, ay, az, au, av, light, r, g, b, alpha, nx, ny, nz);
        SelariumRenderUtil.vertex(consumer, matrix, normalMatrix, bx, by, bz, bu, bv, light, r, g, b, alpha, nx, ny, nz);
        SelariumRenderUtil.vertex(consumer, matrix, normalMatrix, cx, cy, cz, cu, cv, light, r, g, b, alpha, nx, ny, nz);
        SelariumRenderUtil.vertex(consumer, matrix, normalMatrix, cx, cy, cz, cu, cv, light, r, g, b, alpha, nx, ny, nz);
    }
}
