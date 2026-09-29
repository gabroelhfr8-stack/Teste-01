package com.seleris.selarium.util;

/**
 * Selarium's shape rule is that nothing is drawn or scattered on a true circle. These helpers turn round paths
 * into polygons so orbits, particle rings and bursts share the angular look of the textures.
 */
public final class Facets {
    /** Sides of the polygon that stands in for a circle (an octagon, like the chalk ring texture). */
    public static final int SIDES = 8;

    /** Unit vectors towards the 6 faces, 12 edges and 8 corners of a cube: the directions of a faceted burst. */
    public static final double[][] CUBE_DIRECTIONS = cubeDirections();

    private Facets() {
    }

    /**
     * Distance from the centre of a regular polygon (circumradius 1, a flat side facing +x) to its outline along
     * {@code angle} in radians, so that {@code (cos(angle), sin(angle)) * polygonRadius(angle, n)} walks the outline.
     */
    public static double polygonRadius(double angle, int sides) {
        double step = 2.0D * Math.PI / sides;
        double local = ((angle + step / 2.0D) % step + step) % step;
        return Math.cos(Math.PI / sides) / Math.cos(local - step / 2.0D);
    }

    public static float polygonRadius(float angle, int sides) {
        return (float) polygonRadius((double) angle, sides);
    }

    /** {@link #polygonRadius(double, int)} for the default octagon. */
    public static double octagonRadius(double angle) {
        return polygonRadius(angle, SIDES);
    }

    private static double[][] cubeDirections() {
        double[][] result = new double[26][];
        int count = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    double length = Math.sqrt(x * x + y * y + z * z);
                    result[count++] = new double[]{x / length, y / length, z / length};
                }
            }
        }
        return result;
    }
}
