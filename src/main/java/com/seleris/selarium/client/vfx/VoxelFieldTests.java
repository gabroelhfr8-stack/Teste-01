package com.seleris.selarium.client.vfx;

import com.seleris.selarium.Selarium;
import com.seleris.selarium.ward.WardArea;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * The field shell is drawn from {@link VoxelField}; these checks keep it identical to the blocks the ward acts on.
 * (Pure geometry plus {@link WardArea}, so it runs on the headless server.)
 */
@GameTestHolder(Selarium.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VoxelFieldTests {
    private static final String EMPTY = "empty";

    private VoxelFieldTests() {
    }

    @GameTest(template = EMPTY)
    public static void shellCoversExactlyTheBlocksTheWardActsOn(GameTestHelper helper) {
        BlockPos origin = helper.absolutePos(new BlockPos(3, 1, 3));
        for (int radius : new int[]{1, 2, 5, 8, 12}) {
            VoxelField field = VoxelField.of(radius);
            WardArea area = WardArea.of(helper.getLevel(), origin, radius);
            // every face of the shell separates a block inside the ward's area from one outside it
            for (int i = 0; i < field.faces; i++) {
                int x = field.cellX[i], y = field.cellY[i], z = field.cellZ[i];
                int[] n = VoxelField.NORMALS[field.dir[i]];
                helper.assertTrue(area.contains(origin.offset(x, y, z)), "shell face inside a block the ward does not cover at r=" + radius);
                helper.assertTrue(!area.contains(origin.offset(x + n[0], y + n[1], z + n[2])),
                        "shell face between two covered blocks at r=" + radius);
            }
            // and every covered block on the boundary shows up: counting faces from the area gives the same total
            int expected = 0;
            for (int x = -radius - 1; x <= radius + 1; x++) {
                for (int y = -radius - 1; y <= radius + 1; y++) {
                    for (int z = -radius - 1; z <= radius + 1; z++) {
                        boolean here = area.contains(origin.offset(x, y, z));
                        if (here) {
                            for (int[] n : VoxelField.NORMALS) {
                                if (!area.contains(origin.offset(x + n[0], y + n[1], z + n[2]))) {
                                    expected++;
                                }
                            }
                        }
                    }
                }
            }
            helper.assertTrue(expected == field.faces, "shell has " + field.faces + " faces but the area has " + expected + " at r=" + radius);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void groundOutlineAndRingsAreBlockCircles(GameTestHelper helper) {
        for (int radius : new int[]{3, 6, 9}) {
            VoxelField field = VoxelField.of(radius);
            helper.assertTrue(field.edgeCount >= 4 * radius, "outline too short at r=" + radius + ": " + field.edgeCount);
            for (int i = 0; i < field.edgeCount; i++) {
                double d = Math.hypot(field.edgeX[i], field.edgeZ[i]);
                helper.assertTrue(d <= radius + 1.0E-9D && d >= radius - 1.5D, "outline cell too far from the circle: " + d);
            }
            int[][] ring = VoxelField.ring(radius);
            helper.assertTrue(ring.length >= 4 * radius - 4, "pulse ring too small at r=" + radius);
            helper.assertTrue(VoxelField.of(radius).topHeight() == radius + 1.0F, "top height");
        }
        helper.assertTrue(VoxelField.ring(0).length == 1, "ring 0 is the sigil block");
        helper.succeed();
    }
}
