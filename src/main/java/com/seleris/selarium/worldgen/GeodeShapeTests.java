package com.seleris.selarium.worldgen;

import com.seleris.selarium.Selarium;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Geodes are lumpy faceted crystals, never perfect ellipsoids, and about as large as the ellipsoid they replace. */
@GameTestHolder(Selarium.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GeodeShapeTests {
    private GeodeShapeTests() {
    }

    @GameTest(template = "empty")
    public static void geodesAreNotPerfectEllipsoids(GameTestHelper helper) {
        int radius = 6;
        int vertical = 4;
        for (long seed : new long[]{1L, 99L, 123456789L}) {
            int inside = 0;
            int ellipsoid = 0;
            for (int dx = -9; dx <= 9; dx++) {
                for (int dy = -7; dy <= 7; dy++) {
                    for (int dz = -9; dz <= 9; dz++) {
                        if (ArcaneGeodeFeature.shape(dx, dy, dz, radius, vertical, seed) <= 1.0D) {
                            inside++;
                        }
                        if (dx * dx / 36.0D + dy * dy / 16.0D + dz * dz / 36.0D <= 1.0D) {
                            ellipsoid++;
                        }
                    }
                }
            }
            helper.assertTrue(inside != ellipsoid, "geode " + seed + " is a perfect ellipsoid");
            helper.assertTrue(inside > ellipsoid * 0.65D && inside < ellipsoid * 1.25D,
                    "geode " + seed + " changed size too much: " + inside + " blocks against " + ellipsoid);
        }
        helper.succeed();
    }
}
