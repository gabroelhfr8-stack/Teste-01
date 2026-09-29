package com.seleris.selarium.client.vfx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The cut-crystal dome that shows a ward's field: an 80-face geodesic polyhedron whose corners are pulled in by
 * a different amount for every ward, so no field is a sphere and no two are alike. Every corner stays inside the
 * true spherical volume the ward acts on, so what the dome encloses is always affected.
 *
 * <p>Pure geometry with no Minecraft types, so it can be checked on its own. All coordinates are relative to the
 * centre of the field.
 */
final class ShellGeometry {
    static final int FACES = 80;
    /** The ground plane relative to the field's centre: the sigil sits half a block below it, with the chalk on top. */
    static final float GROUND_Y = 0.035F - 0.5F;
    /** How far a corner may be pulled in from the true radius (0.11 = up to 11 %). */
    private static final float JITTER = 0.11F;
    private static final int CACHE_SIZE = 48;

    private static final Map<Key, ShellGeometry> CACHE = new LinkedHashMap<>(16, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Key, ShellGeometry> eldest) {
            return size() > CACHE_SIZE;
        }
    };

    private record Key(long seed, int radiusBits) {
    }

    final float radius;
    final float[][] vertices;
    final int[][] faces;
    final float[][] normals;
    final float[][] centers;
    /** Per-face brightness, 0.72 to 1. */
    final float[] shade;
    /** Per-face lean from the primary to the secondary colour, 0 to 1. */
    final float[] tint;
    /** Dominant axis of each face's normal (0 = x, 1 = y, 2 = z), for box-mapped textures. */
    final int[] axis;
    /** The viewer is certainly inside when closer to the centre than this (smallest distance to a face plane). */
    final float innerRadius;
    /** The viewer is certainly outside when farther than this (largest corner distance). */
    final float outerRadius;

    private final int[][] edges;

    static ShellGeometry of(long seed, float radius) {
        Key key = new Key(seed, Float.floatToIntBits(radius));
        ShellGeometry cached = CACHE.get(key);
        if (cached == null) {
            cached = new ShellGeometry(seed, radius);
            CACHE.put(key, cached);
        }
        return cached;
    }

    private ShellGeometry(long seed, float radius) {
        this.radius = radius;
        float[][] unit = icosphere();
        this.vertices = new float[unit.length][3];
        float outer = 0.0F;
        for (int i = 0; i < unit.length; i++) {
            float factor = 1.0F - JITTER * unit(seed, i);
            for (int k = 0; k < 3; k++) {
                vertices[i][k] = unit[i][k] * radius * factor;
            }
            outer = Math.max(outer, radius * factor);
        }
        this.outerRadius = outer;
        this.faces = FACE_INDEX;
        this.normals = new float[faces.length][];
        this.centers = new float[faces.length][];
        this.shade = new float[faces.length];
        this.tint = new float[faces.length];
        this.axis = new int[faces.length];
        float inner = Float.MAX_VALUE;
        for (int f = 0; f < faces.length; f++) {
            float[] a = vertices[faces[f][0]];
            float[] b = vertices[faces[f][1]];
            float[] c = vertices[faces[f][2]];
            float[] center = {(a[0] + b[0] + c[0]) / 3.0F, (a[1] + b[1] + c[1]) / 3.0F, (a[2] + b[2] + c[2]) / 3.0F};
            float ux = b[0] - a[0], uy = b[1] - a[1], uz = b[2] - a[2];
            float vx = c[0] - a[0], vy = c[1] - a[1], vz = c[2] - a[2];
            float nx = uy * vz - uz * vy, ny = uz * vx - ux * vz, nz = ux * vy - uy * vx;
            float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            nx /= length;
            ny /= length;
            nz /= length;
            if (nx * center[0] + ny * center[1] + nz * center[2] < 0.0F) {
                nx = -nx;
                ny = -ny;
                nz = -nz;
            }
            centers[f] = center;
            normals[f] = new float[]{nx, ny, nz};
            shade[f] = 0.72F + 0.28F * unit(seed, 1000 + f);
            tint[f] = unit(seed, 2000 + f);
            float ax = Math.abs(nx), ay = Math.abs(ny), az = Math.abs(nz);
            axis[f] = ay >= ax && ay >= az ? 1 : (ax >= az ? 0 : 2);
            inner = Math.min(inner, nx * a[0] + ny * a[1] + nz * a[2]);
        }
        this.innerRadius = inner;
        this.edges = uniqueEdges(faces);
    }

    /**
     * The outline of the dome where a horizontal plane at height {@code y} cuts it: (x, z) pairs sorted by angle
     * around the axis. The dome is star-shaped around its centre, so the result is a simple polygon.
     */
    float[][] outlineAt(float y) {
        // The equatorial corners sit exactly at y = 0: nudge the plane so they count as below it.
        float plane = y + 1.0E-4F;
        List<float[]> points = new ArrayList<>();
        for (int[] edge : edges) {
            float[] a = vertices[edge[0]];
            float[] b = vertices[edge[1]];
            if ((a[1] - plane) * (b[1] - plane) < 0.0F) {
                float t = (plane - a[1]) / (b[1] - a[1]);
                points.add(new float[]{a[0] + (b[0] - a[0]) * t, a[2] + (b[2] - a[2]) * t});
            }
        }
        points.sort((p, q) -> Double.compare(Math.atan2(p[1], p[0]), Math.atan2(q[1], q[0])));
        return points.toArray(new float[0][]);
    }

    /**
     * A strip that hugs the dome around its equator, {@code 2 * halfHeight} tall. Its corners are the corners of the
     * dome's outline at both heights, so the strip follows the facets instead of cutting across them.
     *
     * @param angles       angle around the axis of each sample, ascending
     * @param topRadius    distance from the axis at {@code +halfHeight}
     * @param bottomRadius distance from the axis at {@code -halfHeight}
     */
    record Band(float halfHeight, double[] angles, float[] topRadius, float[] bottomRadius) {
    }

    private Band band;
    private float[][] ground;

    /** The dome's outline on the ground plane (see {@link #GROUND_Y}); empty when the dome does not reach it. */
    float[][] groundOutline() {
        if (ground == null) {
            ground = outlineAt(GROUND_Y);
        }
        return ground;
    }

    Band band() {
        if (band == null) {
            float h = Math.max(0.35F, radius * 0.08F);
            float[][] top = outlineAt(h);
            float[][] bottom = outlineAt(-h);
            List<Double> angles = new ArrayList<>();
            for (float[][] outline : new float[][][]{top, bottom}) {
                for (float[] p : outline) {
                    angles.add(Math.atan2(p[1], p[0]));
                }
            }
            angles.sort(Double::compare);
            List<Double> distinct = new ArrayList<>();
            for (double a : angles) {
                if (distinct.isEmpty() || a - distinct.get(distinct.size() - 1) > 1.0E-4D) {
                    distinct.add(a);
                }
            }
            double[] samples = new double[distinct.size()];
            float[] topRadius = new float[samples.length];
            float[] bottomRadius = new float[samples.length];
            for (int i = 0; i < samples.length; i++) {
                samples[i] = distinct.get(i);
                topRadius[i] = radiusAt(top, samples[i]);
                bottomRadius[i] = radiusAt(bottom, samples[i]);
            }
            band = new Band(h, samples, topRadius, bottomRadius);
        }
        return band;
    }

    /** Distance from the axis to {@code outline} along the horizontal direction {@code angle} (radians). */
    static float radiusAt(float[][] outline, double angle) {
        double dx = Math.cos(angle), dz = Math.sin(angle);
        float best = 0.0F;
        for (int i = 0; i < outline.length; i++) {
            float[] a = outline[i];
            float[] b = outline[(i + 1) % outline.length];
            double ex = b[0] - a[0], ez = b[1] - a[1];
            double denom = dx * ez - dz * ex;
            if (Math.abs(denom) < 1.0E-9D) {
                continue;
            }
            double t = (a[0] * ez - a[1] * ex) / denom;
            double u = (a[0] * dz - a[1] * dx) / denom;
            if (t > 0.0D && u >= -1.0E-6D && u <= 1.0D + 1.0E-6D) {
                best = Math.max(best, (float) t);
            }
        }
        return best;
    }

    // ------------------------------------------------------------------------------------- mesh construction
    private static final float[][] UNIT_VERTICES;
    private static final int[][] FACE_INDEX;

    static {
        List<float[]> verts = new ArrayList<>();
        verts.add(new float[]{0.0F, 1.0F, 0.0F});
        double ring = 2.0D / Math.sqrt(5.0D);
        double height = 1.0D / Math.sqrt(5.0D);
        for (int i = 0; i < 5; i++) {
            double a = Math.toRadians(72.0D * i);
            verts.add(new float[]{(float) (ring * Math.cos(a)), (float) height, (float) (ring * Math.sin(a))});
        }
        for (int i = 0; i < 5; i++) {
            double a = Math.toRadians(72.0D * i + 36.0D);
            verts.add(new float[]{(float) (ring * Math.cos(a)), (float) -height, (float) (ring * Math.sin(a))});
        }
        verts.add(new float[]{0.0F, -1.0F, 0.0F});
        List<int[]> tris = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int u = 1 + i, un = 1 + (i + 1) % 5, l = 6 + i, ln = 6 + (i + 1) % 5;
            tris.add(new int[]{0, u, un});
            tris.add(new int[]{u, l, un});
            tris.add(new int[]{l, ln, un});
            tris.add(new int[]{11, ln, l});
        }
        Map<Long, Integer> midpoints = new HashMap<>();
        List<int[]> refined = new ArrayList<>();
        for (int[] t : tris) {
            int ab = midpoint(verts, midpoints, t[0], t[1]);
            int bc = midpoint(verts, midpoints, t[1], t[2]);
            int ca = midpoint(verts, midpoints, t[2], t[0]);
            refined.add(new int[]{t[0], ab, ca});
            refined.add(new int[]{t[1], bc, ab});
            refined.add(new int[]{t[2], ca, bc});
            refined.add(new int[]{ab, bc, ca});
        }
        UNIT_VERTICES = verts.toArray(new float[0][]);
        FACE_INDEX = refined.toArray(new int[0][]);
    }

    private static float[][] icosphere() {
        return UNIT_VERTICES;
    }

    private static int midpoint(List<float[]> verts, Map<Long, Integer> cache, int a, int b) {
        long key = ((long) Math.min(a, b) << 32) | Math.max(a, b);
        Integer existing = cache.get(key);
        if (existing != null) {
            return existing;
        }
        float[] p = verts.get(a);
        float[] q = verts.get(b);
        float x = p[0] + q[0], y = p[1] + q[1], z = p[2] + q[2];
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        verts.add(new float[]{x / length, y / length, z / length});
        cache.put(key, verts.size() - 1);
        return verts.size() - 1;
    }

    private static int[][] uniqueEdges(int[][] faces) {
        Map<Long, int[]> unique = new LinkedHashMap<>();
        for (int[] face : faces) {
            for (int k = 0; k < 3; k++) {
                int a = face[k], b = face[(k + 1) % 3];
                unique.putIfAbsent(((long) Math.min(a, b) << 32) | Math.max(a, b), new int[]{Math.min(a, b), Math.max(a, b)});
            }
        }
        return unique.values().toArray(new int[0][]);
    }

    /** Deterministic pseudo-random value in [0, 1) for a ward seed and a slot. */
    private static float unit(long seed, int slot) {
        long z = seed * 0x9E3779B97F4A7C15L + slot * 0xD1B54A32D192ED03L + 0x632BE59BD9B4E019L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z ^= z >>> 31;
        return (z >>> 40) / (float) (1 << 24);
    }

    @Override
    public String toString() {
        return "ShellGeometry[r=" + radius + ", faces=" + faces.length + ", vertices=" + vertices.length + ", inner="
                + innerRadius + ", outer=" + outerRadius + ", equator corners=" + outlineAt(0.0F).length + "]";
    }
}
