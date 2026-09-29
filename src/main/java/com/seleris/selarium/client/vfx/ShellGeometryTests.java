package com.seleris.selarium.client.vfx;

import com.seleris.selarium.Selarium;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.HashMap;
import java.util.Map;

/** Checks the pure geometry of the ward dome (no client classes are touched, so it runs on the headless server). */
@GameTestHolder(Selarium.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ShellGeometryTests {
    private static final String EMPTY = "empty";

    private ShellGeometryTests() {
    }

    @GameTest(template = EMPTY)
    public static void fieldShellIsAClosedFacetedSolid(GameTestHelper helper) {
        for (float radius : new float[]{1.0F, 4.0F, 9.0F, 20.0F}) {
            for (long seed : new long[]{0L, 7L, 123456789L}) {
                ShellGeometry shell = ShellGeometry.of(seed, radius);
                helper.assertTrue(shell.vertices.length == 42 && shell.faces.length == ShellGeometry.FACES,
                        "unexpected mesh size " + shell);
                Map<Long, Integer> uses = new HashMap<>();
                for (int[] face : shell.faces) {
                    for (int k = 0; k < 3; k++) {
                        int a = face[k], b = face[(k + 1) % 3];
                        uses.merge(((long) Math.min(a, b) << 32) | Math.max(a, b), 1, Integer::sum);
                    }
                }
                helper.assertTrue(uses.size() == 120 && uses.values().stream().allMatch(count -> count == 2),
                        "every edge of the shell must belong to exactly two faces: " + shell);
                float farthest = 0.0F;
                float nearest = Float.MAX_VALUE;
                for (float[] corner : shell.vertices) {
                    float distance = (float) Math.sqrt(corner[0] * corner[0] + corner[1] * corner[1] + corner[2] * corner[2]);
                    farthest = Math.max(farthest, distance);
                    nearest = Math.min(nearest, distance);
                }
                helper.assertTrue(farthest <= radius + 1.0E-3F, "the shell must stay inside the ward's sphere: " + farthest + " > " + radius);
                helper.assertTrue(nearest < radius * 0.97F, "the shell must not be a sphere: every corner is on the radius " + radius);
                for (int f = 0; f < shell.faces.length; f++) {
                    float[] n = shell.normals[f];
                    float[] c = shell.centers[f];
                    helper.assertTrue(n[0] * c[0] + n[1] * c[1] + n[2] * c[2] > 0.0F, "face " + f + " points inwards");
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void groundOutlineIsAnIrregularPolygon(GameTestHelper helper) {
        for (float radius : new float[]{4.0F, 9.0F, 20.0F}) {
            ShellGeometry shell = ShellGeometry.of(11L, radius);
            float[][] outline = shell.groundOutline();
            helper.assertTrue(outline.length >= 8 && outline.length <= 40, "ground outline has " + outline.length + " corners");
            float longest = 0.0F;
            float shortest = Float.MAX_VALUE;
            double previous = -Math.PI - 1.0D;
            for (float[] point : outline) {
                double angle = Math.atan2(point[1], point[0]);
                helper.assertTrue(angle >= previous, "outline corners must run around the axis");
                previous = angle;
                float distance = (float) Math.hypot(point[0], point[1]);
                longest = Math.max(longest, distance);
                shortest = Math.min(shortest, distance);
            }
            helper.assertTrue(longest / shortest > 1.02F, "ground outline is a circle at radius " + radius);
            ShellGeometry.Band band = shell.band();
            helper.assertTrue(band.angles().length >= 10, "the rune band needs at least ten segments");
        }
        helper.succeed();
    }
}
