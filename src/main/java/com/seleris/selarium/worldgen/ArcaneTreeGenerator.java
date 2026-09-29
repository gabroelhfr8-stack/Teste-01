package com.seleris.selarium.worldgen;

import com.mojang.logging.LogUtils;
import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.registry.SelariumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;

public final class ArcaneTreeGenerator {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Direction[] HORIZONTAL_DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    private ArcaneTreeGenerator() {
    }

    public static boolean tryGenerateAboveGeode(WorldGenLevel level, RandomSource random, BlockPos geodeCenter, int geodeRadius, int verticalRadius) {
        if (!SelariumCommonConfig.ARCANE_TREE_ABOVE_GEODE_ENABLED.get()) {
            debug("Arcane tree skipped: disabled; geodeCenter={}", geodeCenter);
            return false;
        }
        double chance = Math.max(0.0D, Math.min(1.0D, SelariumCommonConfig.ARCANE_TREE_ABOVE_GEODE_CHANCE.get()));
        double roll = random.nextDouble();
        if (chance <= 0.0D || roll >= chance) {
            debug("Arcane tree skipped: chance failed; geodeCenter={} chance={} roll={}", geodeCenter, chance, roll);
            return false;
        }

        BlockPos basePos = findSurfaceForTree(level, random, geodeCenter, geodeRadius);
        if (basePos == null) {
            debug("Arcane tree skipped: no valid surface; geodeCenter={} geodeY={} radius={} biome={}",
                    geodeCenter, geodeCenter.getY(), geodeRadius, biomeName(level, geodeCenter));
            return false;
        }

        int height = Math.max(7, randomHeight(random) + 2);
        if (!generateTree(level, random, basePos, height, SelariumCommonConfig.ARCANE_TREE_GENERATE_INITIAL_PETALS.get(), true)) {
            debug("Arcane tree skipped: template/space failed; geodeCenter={} base={} support={} biome={}",
                    geodeCenter, basePos, level.getBlockState(basePos.below()).getBlock(), biomeName(level, basePos));
            return false;
        }
        if (SelariumCommonConfig.ARCANE_TREE_ROOT_COLUMN_ENABLED.get()) {
            RootColumnResult column = placeRootPath(level, random, basePos.below(), geodeCenter, geodeCenter.getY() + verticalRadius);
            debug("Arcane tree generated: geodeCenter={} base={} surfaceY={} geodeTopY={} verticalDistance={} biome={} rootPlaced={} rootBranches={} rootConnected={} rootBlocked={}",
                    geodeCenter, basePos, basePos.getY(), geodeCenter.getY() + verticalRadius,
                    basePos.getY() - (geodeCenter.getY() + verticalRadius),
                    biomeName(level, basePos), column.placed(), column.branchesPlaced(), column.connected(), column.blocked());
        } else {
            debug("Arcane tree generated without root column: geodeCenter={} base={} biome={}",
                    geodeCenter, basePos, biomeName(level, basePos));
        }
        return true;
    }

    public static boolean growSapling(LevelAccessor level, RandomSource random, BlockPos basePos) {
        int height = Math.max(6, randomHeight(random) + 1);
        return generateTree(level, random, basePos, height, SelariumCommonConfig.ARCANE_TREE_GENERATE_INITIAL_PETALS.get());
    }

    public static boolean generateTree(LevelAccessor level, RandomSource random, BlockPos basePos, int height, boolean placePetals) {
        return generateTree(level, random, basePos, height, placePetals, false);
    }

    private static boolean generateTree(LevelAccessor level, RandomSource random, BlockPos basePos, int height, boolean placePetals, boolean allowAncientVariant) {
        if (!ArcaneTreeTemplate.generate(level, random, basePos, height, allowAncientVariant)) {
            return false;
        }
        if (placePetals && SelariumCommonConfig.ARCANE_PETALS_ENABLED.get()) {
            int attempts = Math.max(0, SelariumCommonConfig.ARCANE_TREE_INITIAL_PETAL_ATTEMPTS.get());
            tryPlacePetalsNear(level, random, basePos, attempts, 4);
        }
        return true;
    }

    public static int countNearbyPetals(LevelReader level, BlockPos center, int radius) {
        int count = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -6; dy <= 2; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    mutable.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (level.getBlockState(mutable).is(SelariumBlocks.ARCANE_PETALS.get())) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public static boolean tryPlacePetalsNear(LevelAccessor level, RandomSource random, BlockPos anchor, int attempts, int radius) {
        boolean placed = false;
        for (int i = 0; i < attempts; i++) {
            int dx = random.nextInt(radius * 2 + 1) - radius;
            int dz = random.nextInt(radius * 2 + 1) - radius;
            BlockPos candidate = findPetalPosition(level, anchor.offset(dx, 1, dz));
            if (candidate != null) {
                BlockState petals = SelariumBlocks.ARCANE_PETALS.get().defaultBlockState();
                if (level.getBlockState(candidate).isAir() && petals.canSurvive(level, candidate)) {
                    level.setBlock(candidate, petals, 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static int randomHeight(RandomSource random) {
        int min = SelariumCommonConfig.ARCANE_TREE_MIN_TRUNK_HEIGHT.get();
        int max = SelariumCommonConfig.ARCANE_TREE_MAX_TRUNK_HEIGHT.get();
        int low = Math.max(2, Math.min(min, max));
        int high = Math.max(low, Math.max(min, max));
        int height = low + random.nextInt(high - low + 1);
        double tallChance = Math.max(0.0D, Math.min(1.0D, SelariumCommonConfig.ARCANE_TREE_TALL_VARIANT_CHANCE.get()));
        if (tallChance > 0.0D && random.nextDouble() < tallChance) {
            height += 1 + random.nextInt(3);
        }
        return Math.min(24, height);
    }

    private static RootColumnResult placeRootPath(LevelAccessor level, RandomSource random, BlockPos start, BlockPos geodeCenter, int geodeTopY) {
        int maxLength = Math.max(0, SelariumCommonConfig.ARCANE_TREE_ROOT_COLUMN_MAX_LENGTH.get());
        if (maxLength <= 0) {
            return new RootColumnResult(0, 0, false, false);
        }
        int minY = Math.max(geodeTopY, start.getY() - maxLength + 1);
        BlockState root = SelariumBlocks.ARCANE_GEODE_STONE.get().defaultBlockState();
        int placed = 0;
        int branchesPlaced = 0;
        boolean blocked = false;
        int x = start.getX();
        int z = start.getZ();
        int targetX = geodeCenter.getX();
        int targetZ = geodeCenter.getZ();

        for (int y = start.getY(); y >= minY; y--) {
            int remaining = y - geodeTopY;
            if (remaining <= 6) {
                x += Integer.compare(targetX, x);
                z += Integer.compare(targetZ, z);
            } else {
                int depth = start.getY() - y;
                if (depth % 3 == 1 && random.nextBoolean()) {
                    if (Math.abs(targetX - x) >= Math.abs(targetZ - z) && x != targetX) {
                        x += Integer.compare(targetX, x);
                    } else if (z != targetZ) {
                        z += Integer.compare(targetZ, z);
                    }
                } else if (depth % 5 == 0 && random.nextInt(3) == 0) {
                    Direction drift = HORIZONTAL_DIRECTIONS[random.nextInt(HORIZONTAL_DIRECTIONS.length)];
                    int nextX = x + drift.getStepX();
                    int nextZ = z + drift.getStepZ();
                    if (Math.abs(nextX - targetX) <= 8 && Math.abs(nextZ - targetZ) <= 8) {
                        x = nextX;
                        z = nextZ;
                    }
                }
            }

            BlockPos mainPos = new BlockPos(x, y, z);
            if (placeRootBlock(level, mainPos, root)) {
                placed++;
            } else {
                blocked = true;
            }
            if (random.nextInt(5) == 0 && placeRootBlock(level, mainPos.relative(HORIZONTAL_DIRECTIONS[random.nextInt(HORIZONTAL_DIRECTIONS.length)]), root)) {
                placed++;
            }
            if ((start.getY() - y) % 7 == 3 && random.nextInt(4) != 0) {
                branchesPlaced += placeRootBranch(level, random, mainPos, root);
            }
        }

        BlockPos end = new BlockPos(x, minY, z);
        if (minY == geodeTopY && (x != targetX || z != targetZ)) {
            int connector = placeRootConnector(level, end, new BlockPos(targetX, minY, targetZ), root);
            placed += connector;
            x = targetX;
            z = targetZ;
        }
        boolean connected = minY == geodeTopY && x == targetX && z == targetZ && !blocked;
        if (start.getY() - geodeTopY + 1 > maxLength) {
            debug("Arcane root path shorter than target: start={} geodeTopY={} maxLength={} needed={}",
                    start, geodeTopY, maxLength, start.getY() - geodeTopY + 1);
        }
        return new RootColumnResult(placed, branchesPlaced, connected, blocked);
    }

    private static boolean placeRootBlock(LevelAccessor level, BlockPos pos, BlockState root) {
        if (!canReplaceRoot(level.getBlockState(pos))) {
            return false;
        }
        level.setBlock(pos, root, 2);
        return true;
    }

    private static int placeRootBranch(LevelAccessor level, RandomSource random, BlockPos start, BlockState root) {
        Direction direction = HORIZONTAL_DIRECTIONS[random.nextInt(HORIZONTAL_DIRECTIONS.length)];
        int length = 1 + random.nextInt(3);
        int placed = 0;
        for (int step = 1; step <= length; step++) {
            BlockPos branchPos = start.relative(direction, step);
            if (step == length && random.nextBoolean()) {
                branchPos = branchPos.below();
            }
            if (!placeRootBlock(level, branchPos, root)) {
                break;
            }
            placed++;
        }
        return placed;
    }

    private static int placeRootConnector(LevelAccessor level, BlockPos from, BlockPos to, BlockState root) {
        int placed = 0;
        int x = from.getX();
        int z = from.getZ();
        while (x != to.getX() || z != to.getZ()) {
            if (Math.abs(to.getX() - x) >= Math.abs(to.getZ() - z) && x != to.getX()) {
                x += Integer.compare(to.getX(), x);
            } else if (z != to.getZ()) {
                z += Integer.compare(to.getZ(), z);
            }
            if (placeRootBlock(level, new BlockPos(x, from.getY(), z), root)) {
                placed++;
            }
        }
        return placed;
    }

    private static BlockPos findSurfaceForTree(WorldGenLevel level, RandomSource random, BlockPos geodeCenter, int geodeRadius) {
        int searchRadius = Math.min(8, Math.max(3, geodeRadius));
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int radius = 0; radius <= searchRadius; radius++) {
            int first = radius == 0 ? 0 : random.nextInt(Math.max(1, radius * 8));
            int checks = radius == 0 ? 1 : radius * 8;
            for (int i = 0; i < checks; i++) {
                int index = (first + i) % checks;
                int[] offset = ringOffset(radius, index);
                int x = geodeCenter.getX() + offset[0];
                int z = geodeCenter.getZ() + offset[1];
                int height = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                BlockPos candidate = normalizeSurface(level, mutable.set(x, height, z).immutable());
                if (candidate != null && isSurfaceInConfiguredRange(candidate)
                        && (!SelariumCommonConfig.ARCANE_TREE_REQUIRE_SKY_LIGHT.get() || level.canSeeSky(candidate))) {
                    debug("Arcane tree surface found: geodeCenter={} surface={} support={} biome={} searchRadius={}",
                            geodeCenter, candidate, level.getBlockState(candidate.below()).getBlock(), biomeName(level, candidate), radius);
                    return candidate;
                }
            }
        }
        return null;
    }

    private static BlockPos normalizeSurface(WorldGenLevel level, BlockPos heightmapBase) {
        int minY = Math.max(level.getMinBuildHeight() + 1, heightmapBase.getY() - 16);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int y = heightmapBase.getY(); y >= minY; y--) {
            mutable.set(heightmapBase.getX(), y, heightmapBase.getZ());
            BlockState baseState = level.getBlockState(mutable);
            BlockState support = level.getBlockState(mutable.below());
            if (canReplaceTreeBlock(level, mutable) && baseState.getFluidState().isEmpty()
                    && support.getFluidState().isEmpty() && isValidTreeSupport(support)) {
                return mutable.immutable();
            }
        }
        return null;
    }

    private static int[] ringOffset(int radius, int index) {
        if (radius == 0) {
            return new int[]{0, 0};
        }
        int side = Math.max(1, radius * 2);
        int perimeter = side * 4;
        int normalized = Math.floorMod(index, perimeter);
        if (normalized < side) {
            return new int[]{-radius + normalized, -radius};
        }
        normalized -= side;
        if (normalized < side) {
            return new int[]{radius, -radius + normalized};
        }
        normalized -= side;
        if (normalized < side) {
            return new int[]{radius - normalized, radius};
        }
        normalized -= side;
        return new int[]{-radius, radius - normalized};
    }

    private static boolean isSurfaceInConfiguredRange(BlockPos basePos) {
        int minY = SelariumCommonConfig.ARCANE_TREE_MIN_SURFACE_Y.get();
        int maxY = SelariumCommonConfig.ARCANE_TREE_MAX_SURFACE_Y.get();
        return basePos.getY() >= Math.min(minY, maxY) && basePos.getY() <= Math.max(minY, maxY);
    }

    private static BlockPos findPetalPosition(LevelReader level, BlockPos columnStart) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int y = 0; y <= 8; y++) {
            mutable.set(columnStart.getX(), columnStart.getY() - y, columnStart.getZ());
            if (!level.getBlockState(mutable).isAir()) {
                BlockPos above = mutable.above();
                if (level.getBlockState(above).isAir()) {
                    return above.immutable();
                }
                return null;
            }
        }
        return null;
    }

    static BlockState log(Direction.Axis axis) {
        return SelariumBlocks.ARCANE_LOG.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, axis);
    }

    static BlockState leaves(int distance) {
        return SelariumBlocks.ARCANE_LEAVES.get().defaultBlockState()
                .setValue(LeavesBlock.DISTANCE, Math.max(1, Math.min(7, distance)))
                .setValue(LeavesBlock.PERSISTENT, false);
    }

    static boolean canReplaceTreeBlock(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir()
                || state.is(SelariumBlocks.ARCANE_SAPLING.get())
                || state.is(SelariumBlocks.ARCANE_LEAVES.get())
                || (!state.hasBlockEntity() && state.getFluidState().isEmpty() && state.getCollisionShape(level, pos).isEmpty());
    }

    static boolean isValidTreeSupport(BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(SelariumBlocks.ARCANE_GEODE_STONE.get())
                || state.is(SelariumBlocks.ARCANE_CRYSTAL_BLOCK.get());
    }

    private static boolean canReplaceRoot(BlockState state) {
        if (state.hasBlockEntity() || !state.getFluidState().isEmpty()) {
            return false;
        }
        return state.isAir()
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.DIRT)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(SelariumBlocks.ARCANE_GEODE_STONE.get());
    }

    private static String biomeName(WorldGenLevel level, BlockPos pos) {
        return level.getBiome(pos).unwrapKey()
                .map(key -> key.location().toString())
                .orElse("unknown");
    }

    private static void debug(String message, Object... args) {
        if (SelariumCommonConfig.ARCANE_TREE_WORLDGEN_DEBUG.get()) {
            LOGGER.info("[Selarium Arcane Tree] " + message, args);
        }
    }

    private record RootColumnResult(int placed, int branchesPlaced, boolean connected, boolean blocked) {
    }
}
