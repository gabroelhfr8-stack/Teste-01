package com.seleris.selarium.client.vfx;

import java.util.ArrayList;
import java.util.List;

/**
 * The blocks a ward's field really covers, as a Minecraft circle: a block is inside when its centre is within the
 * ward's range of the sigil block's centre (the test {@code WardArea} uses), so the shell drawn from this class is the
 * exact staircase of blocks the ward acts on. Pure geometry with no Minecraft types, so it can be checked on its own.
 *
 * <p>Cells are relative to the sigil block: cell (0, 0, 0) is the sigil block itself.
 */
public final class VoxelField {
    public static final int MAX_RADIUS = 64;
    private static final VoxelField[] CACHE = new VoxelField[MAX_RADIUS + 1];
    private static final int[][][] RINGS = new int[MAX_RADIUS + 2][][];

    /** Face directions: +X, -X, +Y, -Y, +Z, -Z. */
    static final int[][] NORMALS = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

    final int radius;
    /** Faces of the solid ball's surface: the cell each belongs to, and which side of it. */
    final byte[] cellX;
    final byte[] cellY;
    final byte[] cellZ;
    final byte[] dir;
    final int faces;
    /** Cells of the sigil's own layer that have a neighbour outside the disc: the field's outline on the ground. */
    final byte[] edgeX;
    final byte[] edgeZ;
    final int edgeCount;

    private VoxelField(int radius) {
        this.radius = radius;
        int r2 = radius * radius;
        List<int[]> surface = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (!inside(x, y, z, r2)) {
                        continue;
                    }
                    for (int d = 0; d < 6; d++) {
                        int[] n = NORMALS[d];
                        if (!inside(x + n[0], y + n[1], z + n[2], r2)) {
                            surface.add(new int[]{x, y, z, d});
                        }
                    }
                }
            }
        }
        faces = surface.size();
        cellX = new byte[faces];
        cellY = new byte[faces];
        cellZ = new byte[faces];
        dir = new byte[faces];
        for (int i = 0; i < faces; i++) {
            int[] f = surface.get(i);
            cellX[i] = (byte) f[0];
            cellY[i] = (byte) f[1];
            cellZ[i] = (byte) f[2];
            dir[i] = (byte) f[3];
        }
        List<int[]> edge = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (inside(x, 0, z, r2)
                        && (!inside(x + 1, 0, z, r2) || !inside(x - 1, 0, z, r2) || !inside(x, 0, z + 1, r2) || !inside(x, 0, z - 1, r2))) {
                    edge.add(new int[]{x, z});
                }
            }
        }
        edgeCount = edge.size();
        edgeX = new byte[edgeCount];
        edgeZ = new byte[edgeCount];
        for (int i = 0; i < edgeCount; i++) {
            edgeX[i] = (byte) edge.get(i)[0];
            edgeZ[i] = (byte) edge.get(i)[1];
        }
    }

    public static VoxelField of(int radius) {
        int r = Math.max(1, Math.min(MAX_RADIUS, radius));
        VoxelField field = CACHE[r];
        if (field == null) {
            field = new VoxelField(r);
            CACHE[r] = field;
        }
        return field;
    }

    static boolean inside(int x, int y, int z, int radiusSquared) {
        return x * x + y * y + z * z <= radiusSquared;
    }

    /**
     * The cells (dx, dz) of a one-block-thick ring of the given radius on the ground: the blocks whose centre is
     * between {@code radius - 0.5} and {@code radius + 0.5} from the middle. A pulse that grows ring by ring is a
     * ripple that follows the block grid.
     */
    public static int[][] ring(int radius) {
        int r = Math.max(0, Math.min(MAX_RADIUS + 1, radius));
        int[][] cells = RINGS[r];
        if (cells == null) {
            List<int[]> list = new ArrayList<>();
            double lo = Math.max(0.0D, r - 0.5D);
            double hi = r + 0.5D;
            for (int x = -r - 1; x <= r + 1; x++) {
                for (int z = -r - 1; z <= r + 1; z++) {
                    double d = Math.sqrt(x * x + z * z);
                    if (r == 0 ? (x == 0 && z == 0) : (d > lo && d <= hi)) {
                        list.add(new int[]{x, z});
                    }
                }
            }
            cells = list.toArray(new int[0][]);
            RINGS[r] = cells;
        }
        return cells;
    }

    /** Height of the top of the shell above the sigil block's floor, in blocks: where a light column should end. */
    public float topHeight() {
        return radius + 1.0F;
    }
}
