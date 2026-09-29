package com.seleris.selarium.client.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.seleris.selarium.Selarium;
import com.seleris.selarium.client.render.SelariumRenderUtil;
import com.seleris.selarium.config.SelariumClientConfig;
import com.seleris.selarium.ward.WardShellStyle;
import com.seleris.selarium.ward.WardStyles;
import com.seleris.selarium.ward.WardType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Draws the visible area of a ward as a cut-crystal dome ({@link ShellGeometry}): flat facets that catch the light
 * at different strengths, glowing edges, a runic outline on the ground and, for the {@code RUNES} style, a band of
 * runes hugging the equator. Nothing here is round: the dome is a polyhedron, and its outline is a polygon.
 */
public final class WardShellRenderer {
    private static final ResourceLocation HEX = texture("vfx/shell_hex.png");
    private static final ResourceLocation SOFT = texture("vfx/shell_soft.png");
    private static final ResourceLocation RUNES = texture("vfx/shell_runes.png");
    private static final ResourceLocation FACET = texture("vfx/shell_facet.png");
    private static final ResourceLocation RING_EDGE = texture("vfx/ring_edge.png");
    private static final ResourceLocation GLINT = texture("particle/spark_2.png");

    /** Blocks between two rune ticks on the ground outline, and per repeat of the rune band. */
    private static final float TICK_SPACING = 3.0F;
    private static final float BAND_TILE = 7.0F;

    private WardShellRenderer() {
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID, "textures/" + path);
    }

    /**
     * @param poseStack pose at the block origin of the field centre (block-space)
     * @param strength  0..1 visibility (activation / fade in-out)
     * @param viewer    camera position relative to the field centre
     * @param seed      identifies this field: it decides how the crystal's corners are cut
     */
    public static void draw(PoseStack poseStack, MultiBufferSource buffers, WardType type, float radius, float strength,
                            float time, Vec3 viewer, long seed) {
        SelariumClientConfig.VfxQuality quality = SelariumClientConfig.VFX_QUALITY.get();
        if (quality == SelariumClientConfig.VfxQuality.OFF || strength <= 0.01F) {
            return;
        }
        WardStyles.Style style = WardStyles.of(type);
        float opacity = SelariumClientConfig.SHELL_OPACITY.get().floatValue() * strength;
        ShellGeometry shell = ShellGeometry.of(seed, radius);

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        drawGroundOutline(poseStack, buffers, shell, style, opacity, time);

        SelariumClientConfig.ShellMode mode = SelariumClientConfig.WARD_SHELLS.get();
        double distance = viewer.length();
        boolean skipDome = mode == SelariumClientConfig.ShellMode.OFF || !quality.atLeast(SelariumClientConfig.VfxQuality.MEDIUM)
                || style.shell() == WardShellStyle.NONE
                || (mode == SelariumClientConfig.ShellMode.NEAR && distance > radius + 14.0D);
        if (!skipDome) {
            boolean onlyFront = distance > shell.outerRadius;
            switch (style.shell()) {
                case HEX -> {
                    fill(poseStack, buffers.getBuffer(SelariumRenderTypes.additiveSmooth(HEX)), shell, 5.0F + radius * 0.25F, time,
                            style, 0.50F * opacity, viewer, onlyFront);
                    edges(poseStack, buffers.getBuffer(SelariumRenderTypes.additiveSmooth(FACET)), shell, style, 0.62F * opacity, viewer, onlyFront, time);
                }
                case RUNES -> {
                    fill(poseStack, buffers.getBuffer(SelariumRenderTypes.additiveSmooth(SOFT)), shell, 10.0F + radius * 0.5F, time,
                            style, 0.26F * opacity, viewer, onlyFront);
                    edges(poseStack, buffers.getBuffer(SelariumRenderTypes.additiveSmooth(FACET)), shell, style, 0.40F * opacity, viewer, onlyFront, time);
                    band(poseStack, buffers.getBuffer(SelariumRenderTypes.additiveSmooth(RUNES)), shell, style.secondary(),
                            0.9F * opacity, viewer, time * 0.004F);
                }
                default -> {
                    fill(poseStack, buffers.getBuffer(SelariumRenderTypes.additiveSmooth(SOFT)), shell, 10.0F + radius * 0.5F, time,
                            style, 0.40F * opacity, viewer, onlyFront);
                    edges(poseStack, buffers.getBuffer(SelariumRenderTypes.additiveSmooth(FACET)), shell, style, 0.46F * opacity, viewer, onlyFront, time);
                }
            }
            if (quality.atLeast(SelariumClientConfig.VfxQuality.HIGH)) {
                glints(poseStack, buffers.getBuffer(SelariumRenderTypes.additive(GLINT)), shell, style.secondary(), opacity, viewer, time);
            }
        }
        poseStack.popPose();
    }

    // ------------------------------------------------------------------------------------------------ facets
    /** The flat, softly textured faces of the dome; brighter where they turn edge-on to the viewer. */
    private static void fill(PoseStack poseStack, VertexConsumer consumer, ShellGeometry shell, float tile, float time,
                             WardStyles.Style style, float baseAlpha, Vec3 viewer, boolean onlyFront) {
        PoseStack.Pose pose = poseStack.last();
        float scrollU = time * 0.0035F;
        float scrollV = time * 0.0021F;
        for (int f = 0; f < ShellGeometry.FACES; f++) {
            float[] center = shell.centers[f];
            float[] normal = shell.normals[f];
            float vx = (float) viewer.x - center[0];
            float vy = (float) viewer.y - center[1];
            float vz = (float) viewer.z - center[2];
            float dist = Mth.sqrt(vx * vx + vy * vy + vz * vz);
            float facing = dist > 1.0E-4F ? (normal[0] * vx + normal[1] * vy + normal[2] * vz) / dist : 1.0F;
            if (onlyFront && facing <= 0.0F) {
                continue;
            }
            float edge = 1.0F - Math.abs(facing);
            float near = 1.0F - Mth.clamp((dist - 8.0F) / 26.0F, 0.0F, 0.9F);
            // a slow sweep of light travels over the crystal
            float sweep = 0.80F + 0.20F * Mth.sin(time * 0.045F + center[1] * 0.30F + center[0] * 0.17F);
            float alpha = baseAlpha * shell.shade[f] * sweep * (0.22F + 1.25F * edge * edge) * (0.35F + 0.65F * near);
            int a = VfxDraw.alpha(alpha);
            if (a <= 1) {
                continue;
            }
            int rgb = VfxDraw.mix(style.primary(), style.secondary(), shell.tint[f] * 0.45F);
            int[] tri = shell.faces[f];
            float[] p0 = shell.vertices[tri[0]];
            float[] p1 = shell.vertices[tri[1]];
            float[] p2 = shell.vertices[tri[2]];
            int axis = shell.axis[f];
            VfxDraw.triangle(consumer, pose,
                    p0[0], p0[1], p0[2], mapU(axis, p0, tile, scrollU), mapV(axis, p0, tile, scrollV),
                    p1[0], p1[1], p1[2], mapU(axis, p1, tile, scrollU), mapV(axis, p1, tile, scrollV),
                    p2[0], p2[1], p2[2], mapU(axis, p2, tile, scrollU), mapV(axis, p2, tile, scrollV),
                    rgb, a, LightTexture.FULL_BRIGHT);
        }
    }

    /** Box-mapped texture coordinates: faces that lean the same way share one continuous pattern. */
    private static float mapU(int axis, float[] p, float tile, float scroll) {
        return (axis == 0 ? p[2] : p[0]) / tile + scroll;
    }

    private static float mapV(int axis, float[] p, float tile, float scroll) {
        return (axis == 1 ? p[2] : p[1]) / tile + scroll;
    }

    /** The bright rim, inset line and corner glints of every facet: what makes the dome read as cut crystal. */
    private static void edges(PoseStack poseStack, VertexConsumer consumer, ShellGeometry shell, WardStyles.Style style,
                              float baseAlpha, Vec3 viewer, boolean onlyFront, float time) {
        PoseStack.Pose pose = poseStack.last();
        int rgb = VfxDraw.mix(style.secondary(), 0xFFFFFF, 0.25F);
        for (int f = 0; f < ShellGeometry.FACES; f++) {
            float[] center = shell.centers[f];
            float[] normal = shell.normals[f];
            float vx = (float) viewer.x - center[0];
            float vy = (float) viewer.y - center[1];
            float vz = (float) viewer.z - center[2];
            float dist = Mth.sqrt(vx * vx + vy * vy + vz * vz);
            float facing = dist > 1.0E-4F ? (normal[0] * vx + normal[1] * vy + normal[2] * vz) / dist : 1.0F;
            if (onlyFront && facing <= 0.0F) {
                continue;
            }
            float edge = 1.0F - Math.abs(facing);
            float near = 1.0F - Mth.clamp((dist - 10.0F) / 40.0F, 0.0F, 0.85F);
            float twinkle = 0.72F + 0.28F * Mth.sin(time * 0.07F + f * 0.93F);
            int a = VfxDraw.alpha(baseAlpha * (0.50F + 0.95F * edge * edge) * (0.40F + 0.60F * near) * twinkle * (0.85F + 0.15F * shell.shade[f]));
            if (a <= 1) {
                continue;
            }
            int[] tri = shell.faces[f];
            float[] p0 = shell.vertices[tri[0]];
            float[] p1 = shell.vertices[tri[1]];
            float[] p2 = shell.vertices[tri[2]];
            VfxDraw.triangle(consumer, pose,
                    p0[0], p0[1], p0[2], 0.0F, 0.0F,
                    p1[0], p1[1], p1[2], 1.0F, 0.0F,
                    p2[0], p2[1], p2[2], 0.5F, 1.0F,
                    rgb, a, LightTexture.FULL_BRIGHT);
        }
    }

    /** A handful of corners twinkle like cut gems. */
    private static void glints(PoseStack poseStack, VertexConsumer consumer, ShellGeometry shell, int rgb, float opacity,
                               Vec3 viewer, float time) {
        Quaternionf camera = Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation();
        for (int i = 0; i < shell.vertices.length; i += 3) {
            float[] p = shell.vertices[i];
            float dx = (float) viewer.x - p[0], dy = (float) viewer.y - p[1], dz = (float) viewer.z - p[2];
            float dist = Mth.sqrt(dx * dx + dy * dy + dz * dz);
            float wave = Mth.sin(time * 0.06F + i * 1.9F);
            float flash = wave > 0.0F ? wave * wave * wave : 0.0F;
            if (flash < 0.02F) {
                continue;
            }
            float half = Mth.clamp(0.16F + 0.018F * dist, 0.2F, 1.1F) * (0.6F + 0.6F * flash);
            VfxDraw.billboard(consumer, poseStack, camera, p[0], p[1], p[2], half, 0.0F, rgb,
                    VfxDraw.alpha(0.85F * flash * Math.min(1.0F, opacity * 1.4F)), LightTexture.FULL_BRIGHT);
        }
    }

    // --------------------------------------------------------------------------------------------- outline and band
    /** The runic line where the field meets the ground: a polygon that follows the dome's own outline there. */
    private static void drawGroundOutline(PoseStack poseStack, MultiBufferSource buffers, ShellGeometry shell, WardStyles.Style style,
                                          float opacity, float time) {
        float[][] outline = shell.groundOutline();
        int count = outline.length;
        if (count < 3) {
            return;
        }
        float pulse = 0.78F + 0.22F * Mth.sin(time * 0.08F);
        int alpha = VfxDraw.alpha(0.68F * pulse * Math.min(1.0F, opacity));
        if (alpha <= 1) {
            return;
        }
        float width = Mth.clamp(shell.radius * 0.10F, 0.6F, 1.6F);
        int rgb = style.primary();
        int r = VfxDraw.red(rgb), g = VfxDraw.green(rgb), b = VfxDraw.blue(rgb);
        VertexConsumer consumer = buffers.getBuffer(SelariumRenderTypes.additiveSmooth(RING_EDGE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        float perimeter = 0.0F;
        for (int i = 0; i < count; i++) {
            float[] p = outline[i], q = outline[(i + 1) % count];
            perimeter += Mth.sqrt((q[0] - p[0]) * (q[0] - p[0]) + (q[1] - p[1]) * (q[1] - p[1]));
        }
        float ticks = Math.max(4.0F, Math.round(perimeter / TICK_SPACING));     // whole ticks, so the seam is invisible
        float scroll = time * 0.012F;
        float run = 0.0F;
        float y = ShellGeometry.GROUND_Y;
        for (int i = 0; i < count; i++) {
            float[] p = outline[i], q = outline[(i + 1) % count];
            float length = Mth.sqrt((q[0] - p[0]) * (q[0] - p[0]) + (q[1] - p[1]) * (q[1] - p[1]));
            float u0 = run / perimeter * ticks + scroll;
            float u1 = (run + length) / perimeter * ticks + scroll;
            run += length;
            float kp = inwardScale(p, width), kq = inwardScale(q, width);
            SelariumRenderUtil.vertex(consumer, matrix, normal, p[0], y, p[1], u0, 0.0F, LightTexture.FULL_BRIGHT, r, g, b, alpha, 0.0F, 1.0F, 0.0F);
            SelariumRenderUtil.vertex(consumer, matrix, normal, q[0], y, q[1], u1, 0.0F, LightTexture.FULL_BRIGHT, r, g, b, alpha, 0.0F, 1.0F, 0.0F);
            SelariumRenderUtil.vertex(consumer, matrix, normal, q[0] * kq, y, q[1] * kq, u1, 1.0F, LightTexture.FULL_BRIGHT, r, g, b, alpha, 0.0F, 1.0F, 0.0F);
            SelariumRenderUtil.vertex(consumer, matrix, normal, p[0] * kp, y, p[1] * kp, u0, 1.0F, LightTexture.FULL_BRIGHT, r, g, b, alpha, 0.0F, 1.0F, 0.0F);
        }
    }

    /** Scale that pulls an outline point {@code width} blocks towards the axis. */
    private static float inwardScale(float[] p, float width) {
        float length = Mth.sqrt(p[0] * p[0] + p[1] * p[1]);
        return length > 1.0E-4F ? Math.max(0.55F, 1.0F - width / length) : 1.0F;
    }

    /** The scrolling band of runes around the equator, following the facets. */
    private static void band(PoseStack poseStack, VertexConsumer consumer, ShellGeometry shell, int rgb, float baseAlpha,
                             Vec3 viewer, float scroll) {
        ShellGeometry.Band band = shell.band();
        int count = band.angles().length;
        if (count < 3) {
            return;
        }
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        int r = VfxDraw.red(rgb), g = VfxDraw.green(rgb), b = VfxDraw.blue(rgb);
        float h = band.halfHeight();
        float[] topX = new float[count], topZ = new float[count], botX = new float[count], botZ = new float[count];
        for (int i = 0; i < count; i++) {
            float cos = (float) Math.cos(band.angles()[i]);
            float sin = (float) Math.sin(band.angles()[i]);
            topX[i] = cos * band.topRadius()[i];
            topZ[i] = sin * band.topRadius()[i];
            botX[i] = cos * band.bottomRadius()[i];
            botZ[i] = sin * band.bottomRadius()[i];
        }
        float perimeter = 0.0F;
        for (int i = 0; i < count; i++) {
            int j = (i + 1) % count;
            perimeter += Mth.sqrt((topX[j] - topX[i]) * (topX[j] - topX[i]) + (topZ[j] - topZ[i]) * (topZ[j] - topZ[i]));
        }
        float tiles = Math.max(2.0F, Math.round(perimeter / BAND_TILE));
        float run = 0.0F;
        for (int i = 0; i < count; i++) {
            int j = (i + 1) % count;
            float length = Mth.sqrt((topX[j] - topX[i]) * (topX[j] - topX[i]) + (topZ[j] - topZ[i]) * (topZ[j] - topZ[i]));
            float u0 = run / perimeter * tiles + scroll;
            float u1 = (run + length) / perimeter * tiles + scroll;
            run += length;
            float mx = (topX[i] + topX[j]) * 0.5F, mz = (topZ[i] + topZ[j]) * 0.5F;
            float dx = (float) viewer.x - mx, dz = (float) viewer.z - mz;
            float dist = Mth.sqrt(dx * dx + dz * dz);
            int a = VfxDraw.alpha(baseAlpha * (0.35F + 0.65F * (1.0F - Mth.clamp((dist - 6.0F) / 30.0F, 0.0F, 0.9F))));
            float nl = Math.max(1.0E-4F, Mth.sqrt(mx * mx + mz * mz));
            float nx = mx / nl, nz = mz / nl;
            SelariumRenderUtil.vertex(consumer, matrix, normal, botX[i], -h, botZ[i], u0, 1.0F, LightTexture.FULL_BRIGHT, r, g, b, a, nx, 0.0F, nz);
            SelariumRenderUtil.vertex(consumer, matrix, normal, botX[j], -h, botZ[j], u1, 1.0F, LightTexture.FULL_BRIGHT, r, g, b, a, nx, 0.0F, nz);
            SelariumRenderUtil.vertex(consumer, matrix, normal, topX[j], h, topZ[j], u1, 0.0F, LightTexture.FULL_BRIGHT, r, g, b, a, nx, 0.0F, nz);
            SelariumRenderUtil.vertex(consumer, matrix, normal, topX[i], h, topZ[i], u0, 0.0F, LightTexture.FULL_BRIGHT, r, g, b, a, nx, 0.0F, nz);
        }
    }
}
