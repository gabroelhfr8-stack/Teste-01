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
 * Draws the area of a ward the Minecraft way: as the blocks it covers. The field is a voxel sphere (see
 * {@link VoxelField}), so its shell is a staircase of translucent block faces with a pixel frame, and its outline on the
 * ground is a ring of glowing floor tiles. What the player sees is exactly the set of blocks the ward acts on.
 */
public final class WardShellRenderer {
    private static final ResourceLocation SOFT = texture("vfx/shell_soft.png");
    private static final ResourceLocation HEX = texture("vfx/shell_hex.png");
    private static final ResourceLocation RUNES = texture("vfx/shell_runes.png");
    private static final ResourceLocation RING_TILE = texture("vfx/ring_tile.png");
    private static final ResourceLocation GLINT = texture("particle/spark_2.png");

    /** Height of the outline tiles above the sigil block's floor. */
    private static final float GROUND_Y = 0.03F;
    /** Faces farther than this from the camera are not drawn. */
    private static final float MAX_DISTANCE = 96.0F;
    /** Glints are a distant sparkle: none is drawn this close to the camera. */
    private static final float GLINT_MIN_DISTANCE = 6.0F;
    private static final int INERT_COLOR = 0x7A8494;

    /** Corners of each face direction (+X, -X, +Y, -Y, +Z, -Z) relative to the cell's minimum corner, counter-clockwise from outside. */
    private static final float[][][] CORNERS = {
            {{1, 0, 0}, {1, 1, 0}, {1, 1, 1}, {1, 0, 1}},
            {{0, 0, 0}, {0, 0, 1}, {0, 1, 1}, {0, 1, 0}},
            {{0, 1, 0}, {0, 1, 1}, {1, 1, 1}, {1, 1, 0}},
            {{0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, 0, 1}},
            {{0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}},
            {{0, 0, 0}, {0, 1, 0}, {1, 1, 0}, {1, 0, 0}},
    };
    private static final float[][] UVS = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};

    private WardShellRenderer() {
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID, "textures/" + path);
    }

    /**
     * @param poseStack pose at the origin (minimum corner) of the sigil block, in block space
     * @param strength  0..1 visibility (activation / fade in-out)
     * @param viewer    camera position relative to the centre of the sigil block
     * @param seed      identifies this field: it varies the turn and tint of the shell's tiles
     * @param inert     the ward cannot pay its upkeep: draw it dimmed and grey
     */
    public static void draw(PoseStack poseStack, MultiBufferSource buffers, WardType type, float radius, float strength,
                            float time, Vec3 viewer, long seed, boolean inert) {
        SelariumClientConfig.VfxQuality quality = SelariumClientConfig.VFX_QUALITY.get();
        if (quality == SelariumClientConfig.VfxQuality.OFF || strength <= 0.01F) {
            return;
        }
        WardStyles.Style style = WardStyles.of(type);
        float opacity = SelariumClientConfig.SHELL_OPACITY.get().floatValue() * strength * (inert ? 0.4F : 1.0F);
        int primary = inert ? VfxDraw.mix(style.primary(), INERT_COLOR, 0.8F) : style.primary();
        int secondary = inert ? VfxDraw.mix(style.secondary(), INERT_COLOR, 0.8F) : style.secondary();
        VoxelField field = VoxelField.of(Math.round(radius));

        groundTiles(poseStack, buffers.getBuffer(SelariumRenderTypes.additive(RING_TILE)), field, primary, opacity, time);

        SelariumClientConfig.ShellMode mode = SelariumClientConfig.WARD_SHELLS.get();
        double distance = viewer.length();
        boolean skipShell = mode == SelariumClientConfig.ShellMode.OFF || !quality.atLeast(SelariumClientConfig.VfxQuality.MEDIUM)
                || style.shell() == WardShellStyle.NONE
                || (mode == SelariumClientConfig.ShellMode.NEAR && distance > field.radius + 14.0D);
        if (skipShell) {
            return;
        }
        ResourceLocation tile = switch (style.shell()) {
            case HEX -> HEX;
            case RUNES -> RUNES;
            default -> SOFT;
        };
        float base = switch (style.shell()) {
            case HEX -> 0.50F;
            case RUNES -> 0.55F;
            default -> 0.42F;
        };
        shell(poseStack, buffers.getBuffer(SelariumRenderTypes.additive(tile)), field, style.shell() != WardShellStyle.RUNES,
                primary, secondary, base * opacity, viewer, time, seed);
        if (!inert && quality.atLeast(SelariumClientConfig.VfxQuality.HIGH)) {
            glints(poseStack, buffers.getBuffer(SelariumRenderTypes.additive(GLINT)), field, secondary, opacity, viewer, time, seed);
        }
    }

    // --------------------------------------------------------------------------------------------- the shell
    /** The block faces of the sphere's surface: brighter where they turn edge-on to the viewer, with a slow upward sweep. */
    private static void shell(PoseStack poseStack, VertexConsumer consumer, VoxelField field, boolean turnTiles, int primary,
                              int secondary, float baseAlpha, Vec3 viewer, float time, long seed) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        double distance = viewer.length();
        boolean outside = distance > field.radius + 1.0D;
        boolean inside = distance < field.radius - 1.0D;
        float vx = (float) viewer.x + 0.5F, vy = (float) viewer.y + 0.5F, vz = (float) viewer.z + 0.5F;
        float sweep = time * 0.09F;
        float maxSq = MAX_DISTANCE * MAX_DISTANCE;
        for (int i = 0; i < field.faces; i++) {
            int d = field.dir[i];
            int x = field.cellX[i], y = field.cellY[i], z = field.cellZ[i];
            int[] n = VoxelField.NORMALS[d];
            float cx = x + 0.5F + n[0] * 0.5F, cy = y + 0.5F + n[1] * 0.5F, cz = z + 0.5F + n[2] * 0.5F;
            float dx = vx - cx, dy = vy - cy, dz = vz - cz;
            float distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > maxSq) {
                continue;
            }
            float dist = Mth.sqrt(distSq);
            float facing = (n[0] * dx + n[1] * dy + n[2] * dz) / Math.max(dist, 1.0E-4F);
            if ((outside && facing <= 0.0F) || (inside && facing >= 0.0F)) {
                continue;
            }
            float edge = 1.0F - Math.abs(facing);
            float near = 1.0F - Mth.clamp((dist - 10.0F) / 50.0F, 0.0F, 0.85F);
            float wave = 0.70F + 0.30F * Mth.sin(sweep - cy * 0.55F);
            // faces right next to the camera melt away instead of filling the screen
            float veil = Mth.clamp((dist - 0.8F) / 2.2F, 0.0F, 1.0F);
            int alpha = VfxDraw.alpha(baseAlpha * (0.22F + 1.15F * edge * edge) * wave * (0.40F + 0.60F * near) * veil);
            if (alpha <= 2) {
                continue;
            }
            int h = hash(x, y, z, d, seed);
            int rgb = VfxDraw.mix(primary, secondary, ((h >>> 4) & 15) / 15.0F * 0.5F);
            int r = VfxDraw.red(rgb), g = VfxDraw.green(rgb), b = VfxDraw.blue(rgb);
            int turn = turnTiles ? h & 3 : 0;
            float[][] corners = CORNERS[d];
            for (int k = 0; k < 4; k++) {
                float[] c = corners[k];
                float[] uv = UVS[(k + turn) & 3];
                SelariumRenderUtil.vertex(consumer, matrix, normalMatrix, x + c[0], y + c[1], z + c[2], uv[0], uv[1],
                        LightTexture.FULL_BRIGHT, r, g, b, alpha, n[0], n[1], n[2]);
            }
        }
    }

    /** A few block faces glint like cut gems. */
    private static void glints(PoseStack poseStack, VertexConsumer consumer, VoxelField field, int rgb, float opacity,
                               Vec3 viewer, float time, long seed) {
        Quaternionf camera = Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation();
        double distance = viewer.length();
        boolean outside = distance > field.radius + 1.0D;
        float vx = (float) viewer.x + 0.5F, vy = (float) viewer.y + 0.5F, vz = (float) viewer.z + 0.5F;
        int step = Math.max(1, field.faces / 14);
        for (int i = (int) (Math.abs(seed) % step); i < field.faces; i += step) {
            int d = field.dir[i];
            int[] n = VoxelField.NORMALS[d];
            float cx = field.cellX[i] + 0.5F + n[0] * 0.5F, cy = field.cellY[i] + 0.5F + n[1] * 0.5F, cz = field.cellZ[i] + 0.5F + n[2] * 0.5F;
            float dx = vx - cx, dy = vy - cy, dz = vz - cz;
            float dist = Mth.sqrt(dx * dx + dy * dy + dz * dz);
            float facing = (n[0] * dx + n[1] * dy + n[2] * dz) / Math.max(dist, 1.0E-4F);
            if ((outside && facing <= 0.0F) || (!outside && facing >= 0.0F) || dist > MAX_DISTANCE || dist < GLINT_MIN_DISTANCE) {
                continue;
            }
            float wave = Mth.sin(time * 0.06F + i * 1.9F);
            float flash = wave > 0.0F ? wave * wave * wave : 0.0F;
            if (flash < 0.02F) {
                continue;
            }
            float half = Mth.clamp(0.10F + 0.010F * dist, 0.12F, 0.32F) * (0.6F + 0.6F * flash);
            VfxDraw.billboard(consumer, poseStack, camera, cx, cy, cz, half, 0.0F, rgb,
                    VfxDraw.alpha(0.9F * flash * Math.min(1.0F, opacity * 1.4F)), LightTexture.FULL_BRIGHT);
        }
    }

    // -------------------------------------------------------------------------------------- the ground outline
    /** The outline of the field on the ground: the blocks at its edge glow like floor tiles, with a light running round them. */
    private static void groundTiles(PoseStack poseStack, VertexConsumer consumer, VoxelField field, int rgb, float opacity, float time) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        int r = VfxDraw.red(rgb), g = VfxDraw.green(rgb), b = VfxDraw.blue(rgb);
        float pulse = 0.72F + 0.28F * Mth.sin(time * 0.08F);
        float base = 0.75F * pulse * Math.min(1.0F, opacity);
        for (int i = 0; i < field.edgeCount; i++) {
            int x = field.edgeX[i], z = field.edgeZ[i];
            float run = 0.72F + 0.28F * Mth.sin(time * 0.12F - (float) Math.atan2(z, x) * 2.0F);
            int alpha = VfxDraw.alpha(base * run);
            if (alpha <= 2) {
                continue;
            }
            SelariumRenderUtil.vertex(consumer, matrix, normalMatrix, x, GROUND_Y, z, 0.0F, 0.0F, LightTexture.FULL_BRIGHT, r, g, b, alpha, 0.0F, 1.0F, 0.0F);
            SelariumRenderUtil.vertex(consumer, matrix, normalMatrix, x, GROUND_Y, z + 1.0F, 0.0F, 1.0F, LightTexture.FULL_BRIGHT, r, g, b, alpha, 0.0F, 1.0F, 0.0F);
            SelariumRenderUtil.vertex(consumer, matrix, normalMatrix, x + 1.0F, GROUND_Y, z + 1.0F, 1.0F, 1.0F, LightTexture.FULL_BRIGHT, r, g, b, alpha, 0.0F, 1.0F, 0.0F);
            SelariumRenderUtil.vertex(consumer, matrix, normalMatrix, x + 1.0F, GROUND_Y, z, 1.0F, 0.0F, LightTexture.FULL_BRIGHT, r, g, b, alpha, 0.0F, 1.0F, 0.0F);
        }
    }

    private static int hash(int x, int y, int z, int d, long seed) {
        long h = seed * 0x9E3779B97F4A7C15L + x * 0xC2B2AE3D27D4EB4FL + y * 0x165667B19E3779F9L + z * 0xD1B54A32D192ED03L + d * 0x632BE59BD9B4E019L;
        h ^= h >>> 29;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 32;
        return (int) h;
    }
}
