package com.seleris.selarium.client.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.seleris.selarium.Selarium;
import com.seleris.selarium.client.render.SelariumRenderUtil;
import com.seleris.selarium.config.SelariumClientConfig;
import com.seleris.selarium.ward.WardShellStyle;
import com.seleris.selarium.ward.WardStyles;
import com.seleris.selarium.ward.WardType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Draws the visible area of a ward: a ring on the ground plus (depending on style and settings)
 * a translucent shell whose opacity peaks at the silhouette and near the viewer.
 */
public final class WardShellRenderer {
    private static final ResourceLocation HEX = texture("vfx/shell_hex.png");
    private static final ResourceLocation SOFT = texture("vfx/shell_soft.png");
    private static final ResourceLocation RUNES = texture("vfx/shell_runes.png");
    private static final ResourceLocation WAVE = texture("vfx/wave.png");

    private static float[][] cachedDirs;
    private static int cachedLon = -1;
    private static int cachedLat = -1;

    private WardShellRenderer() {
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID, "textures/" + path);
    }

    /**
     * @param poseStack pose at the block origin of the field centre (block-space)
     * @param strength  0..1 visibility (activation / fade in-out)
     * @param viewer    camera position relative to the field centre
     */
    public static void draw(PoseStack poseStack, MultiBufferSource buffers, WardType type, float radius, float strength,
                            float time, Vec3 viewer) {
        SelariumClientConfig.VfxQuality quality = SelariumClientConfig.VFX_QUALITY.get();
        if (quality == SelariumClientConfig.VfxQuality.OFF || strength <= 0.01F) {
            return;
        }
        WardStyles.Style style = WardStyles.of(type);
        float opacity = SelariumClientConfig.SHELL_OPACITY.get().floatValue() * strength;

        drawGroundRing(poseStack, buffers, radius, style.primary(), opacity, time);

        SelariumClientConfig.ShellMode mode = SelariumClientConfig.WARD_SHELLS.get();
        if (mode == SelariumClientConfig.ShellMode.OFF || !quality.atLeast(SelariumClientConfig.VfxQuality.MEDIUM)
                || style.shell() == WardShellStyle.NONE) {
            return;
        }
        double distance = viewer.length();
        if (mode == SelariumClientConfig.ShellMode.NEAR && distance > radius + 14.0D) {
            return;
        }

        boolean high = quality.atLeast(SelariumClientConfig.VfxQuality.HIGH);
        int lon = high ? 36 : 24;
        int lat = high ? 18 : 12;
        boolean inside = distance < radius;
        float scroll = time * 0.0035F;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        switch (style.shell()) {
            case HEX -> sphere(poseStack, buffers.getBuffer(SelariumRenderTypes.glow(HEX)), radius, lon, lat, 6.0F, 3.0F,
                    scroll, scroll * 0.6F, style.primary(), 0.55F * opacity, viewer, inside);
            case RUNES -> {
                sphere(poseStack, buffers.getBuffer(SelariumRenderTypes.glow(SOFT)), radius, lon, lat, 3.0F, 2.0F,
                        scroll * 0.5F, scroll * 0.3F, style.primary(), 0.30F * opacity, viewer, inside);
                band(poseStack, buffers.getBuffer(SelariumRenderTypes.additive(RUNES)), radius, 0.16F, high ? 48 : 32,
                        time * 0.004F, style.secondary(), 0.85F * opacity, viewer);
            }
            default -> sphere(poseStack, buffers.getBuffer(SelariumRenderTypes.glow(SOFT)), radius, lon, lat, 3.0F, 2.0F,
                    scroll * 0.6F, scroll * 0.4F, style.primary(), 0.42F * opacity, viewer, inside);
        }
        poseStack.popPose();
    }

    /** A thin ring at the field's radius, lying at the sigil's floor level. */
    private static void drawGroundRing(PoseStack poseStack, MultiBufferSource buffers, float radius, int color, float opacity, float time) {
        float pulse = 0.75F + 0.25F * Mth.sin(time * 0.08F);
        VertexConsumer consumer = buffers.getBuffer(SelariumRenderTypes.additive(WAVE));
        // The ring sits at 0.9 of the quad's half-width in the texture.
        float half = radius / 0.9F;
        VfxDraw.groundQuad(consumer, poseStack, half, 0.035F, time * 0.15F, color, VfxDraw.alpha(0.55F * pulse * Math.min(1.0F, opacity)),
                LightTexture.FULL_BRIGHT);
    }

    private static float[][] directions(int lon, int lat) {
        if (cachedDirs != null && cachedLon == lon && cachedLat == lat) {
            return cachedDirs;
        }
        float[][] dirs = new float[(lat + 1) * (lon + 1)][];
        for (int i = 0; i <= lat; i++) {
            float theta = (float) Math.PI * i / lat;
            for (int j = 0; j <= lon; j++) {
                float phi = (float) (2.0D * Math.PI) * j / lon;
                dirs[i * (lon + 1) + j] = new float[]{
                        Mth.sin(theta) * Mth.cos(phi), Mth.cos(theta), Mth.sin(theta) * Mth.sin(phi)};
            }
        }
        cachedDirs = dirs;
        cachedLon = lon;
        cachedLat = lat;
        return dirs;
    }

    private static void sphere(PoseStack poseStack, VertexConsumer consumer, float radius, int lon, int lat,
                               float uTiles, float vTiles, float uScroll, float vScroll,
                               int rgb, float baseAlpha, Vec3 viewer, boolean inside) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        float[][] dirs = directions(lon, lat);
        int stride = lon + 1;
        int r = VfxDraw.red(rgb), g = VfxDraw.green(rgb), b = VfxDraw.blue(rgb);
        float[] alpha = new float[4];
        float[] facing = new float[4];
        for (int i = 0; i < lat; i++) {
            for (int j = 0; j < lon; j++) {
                int[] corners = {i * stride + j, (i + 1) * stride + j, (i + 1) * stride + j + 1, i * stride + j + 1};
                boolean anyFront = inside;
                for (int k = 0; k < 4; k++) {
                    float[] d = dirs[corners[k]];
                    float vx = (float) viewer.x - d[0] * radius;
                    float vy = (float) viewer.y - d[1] * radius;
                    float vz = (float) viewer.z - d[2] * radius;
                    float dist = Mth.sqrt(vx * vx + vy * vy + vz * vz);
                    float cos = dist > 1.0E-4F ? (d[0] * vx + d[1] * vy + d[2] * vz) / dist : 1.0F;
                    facing[k] = cos;
                    float edge = 1.0F - Math.abs(cos);
                    float fresnel = edge * edge;
                    float near = 1.0F - Mth.clamp((dist - 8.0F) / 26.0F, 0.0F, 0.9F);
                    alpha[k] = baseAlpha * (0.20F + 1.25F * fresnel) * (0.35F + 0.65F * near);
                    if (cos > 0.0F) {
                        anyFront = true;
                    }
                }
                if (!anyFront) {
                    continue;
                }
                for (int k = 0; k < 4; k++) {
                    float[] d = dirs[corners[k]];
                    int ci = corners[k];
                    int row = ci / stride;
                    int col = ci % stride;
                    float u = col / (float) lon * uTiles + uScroll;
                    float v = row / (float) lat * vTiles + vScroll;
                    SelariumRenderUtil.vertex(consumer, matrix, normal, d[0] * radius, d[1] * radius, d[2] * radius, u, v,
                            LightTexture.FULL_BRIGHT, r, g, b, VfxDraw.alpha(alpha[k]), d[0], d[1], d[2]);
                }
            }
        }
    }

    /** A short cylinder wrapped around the equator (a scrolling band of runes). */
    private static void band(PoseStack poseStack, VertexConsumer consumer, float radius, float heightFraction, int segments,
                             float scroll, int rgb, float baseAlpha, Vec3 viewer) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        float h = Math.max(0.35F, radius * heightFraction * 0.5F);
        int r = VfxDraw.red(rgb), g = VfxDraw.green(rgb), b = VfxDraw.blue(rgb);
        float tiles = Math.max(4.0F, radius * 0.9F);
        for (int i = 0; i < segments; i++) {
            float a0 = (float) (2.0D * Math.PI) * i / segments;
            float a1 = (float) (2.0D * Math.PI) * (i + 1) / segments;
            float x0 = Mth.cos(a0), z0 = Mth.sin(a0), x1 = Mth.cos(a1), z1 = Mth.sin(a1);
            float mx = (x0 + x1) * 0.5F * radius, mz = (z0 + z1) * 0.5F * radius;
            float dist = (float) Math.sqrt((viewer.x - mx) * (viewer.x - mx) + (viewer.z - mz) * (viewer.z - mz));
            float alpha = baseAlpha * (0.35F + 0.65F * (1.0F - Mth.clamp((dist - 6.0F) / 30.0F, 0.0F, 0.9F)));
            int a = VfxDraw.alpha(alpha);
            float u0 = i / (float) segments * tiles + scroll;
            float u1 = (i + 1) / (float) segments * tiles + scroll;
            SelariumRenderUtil.vertex(consumer, matrix, normal, x0 * radius, -h, z0 * radius, u0, 1.0F, LightTexture.FULL_BRIGHT, r, g, b, a, x0, 0.0F, z0);
            SelariumRenderUtil.vertex(consumer, matrix, normal, x1 * radius, -h, z1 * radius, u1, 1.0F, LightTexture.FULL_BRIGHT, r, g, b, a, x1, 0.0F, z1);
            SelariumRenderUtil.vertex(consumer, matrix, normal, x1 * radius, h, z1 * radius, u1, 0.0F, LightTexture.FULL_BRIGHT, r, g, b, a, x1, 0.0F, z1);
            SelariumRenderUtil.vertex(consumer, matrix, normal, x0 * radius, h, z0 * radius, u0, 0.0F, LightTexture.FULL_BRIGHT, r, g, b, a, x0, 0.0F, z0);
        }
    }
}
