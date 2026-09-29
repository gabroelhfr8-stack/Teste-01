package com.seleris.selarium.worldgen;

import com.mojang.serialization.Codec;
import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.registry.SelariumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ArcaneGeodeFeature extends Feature<NoneFeatureConfiguration> {
    public ArcaneGeodeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!SelariumCommonConfig.ARCANE_GEODES_ENABLED.get()) {
            return false;
        }

        RandomSource random = context.random();
        int chance = Math.max(1, SelariumCommonConfig.ARCANE_GEODE_CHANCE.get());
        if (random.nextInt(chance) != 0) {
            return false;
        }

        BlockPos origin = context.origin();
        int minY = Math.min(SelariumCommonConfig.ARCANE_GEODE_MIN_Y.get(), SelariumCommonConfig.ARCANE_GEODE_MAX_Y.get());
        int maxY = Math.max(SelariumCommonConfig.ARCANE_GEODE_MIN_Y.get(), SelariumCommonConfig.ARCANE_GEODE_MAX_Y.get());
        if (origin.getY() < minY || origin.getY() > maxY) {
            return false;
        }

        WorldGenLevel level = context.level();
        int minSize = Math.max(3, Math.min(SelariumCommonConfig.ARCANE_GEODE_MIN_SIZE.get(), SelariumCommonConfig.ARCANE_GEODE_MAX_SIZE.get()));
        int maxSize = Math.max(minSize, SelariumCommonConfig.ARCANE_GEODE_MAX_SIZE.get());
        int radius = minSize + random.nextInt(maxSize - minSize + 1);
        int verticalRadius = Math.max(2, Math.round(radius * 0.65F));
        long seed = random.nextLong();

        int placed = generateBody(level, random, origin, radius, verticalRadius, seed);
        if (placed <= 0) {
            return false;
        }

        placeClusters(level, random, origin, radius, verticalRadius, seed);
        // the lumpy top can sit a little under the nominal one, so let the tree's roots reach a little further down
        ArcaneTreeGenerator.tryGenerateAboveGeode(level, random, origin, radius, Math.max(1, Math.round(verticalRadius * 0.85F)));
        return true;
    }

    /**
     * How far a block is from the geode's centre, squared and normalised so 1.0 is the surface. The shape is an
     * ellipsoid with its eight corners cut off like a gem, pushed in and out by smooth noise: a lumpy, faceted
     * crystal cavity rather than a perfect ball.
     */
    public static double shape(int dx, int dy, int dz, int radius, int verticalRadius, long seed) {
        double nx = dx / (double) radius;
        double ny = dy / (double) verticalRadius;
        double nz = dz / (double) radius;
        double ellipsoid = Math.sqrt(nx * nx + ny * ny + nz * nz);
        double chamfer = (Math.abs(nx) + Math.abs(ny) + Math.abs(nz)) * 0.78D;
        double lump = 1.0D + 0.32D * (valueNoise(dx / 3.0D, dy / 3.0D, dz / 3.0D, seed) - 0.5D);
        double metric = Math.max(ellipsoid, chamfer) / (lump * 1.08D);     // the chamfers take volume, so grow a little to make up
        return metric * metric;
    }

    /** Smooth value noise in [0, 1): a random value on every lattice corner, blended with a smoothstep. */
    private static double valueNoise(double x, double y, double z, long seed) {
        int x0 = (int) Math.floor(x), y0 = (int) Math.floor(y), z0 = (int) Math.floor(z);
        double fx = smooth(x - x0), fy = smooth(y - y0), fz = smooth(z - z0);
        double c00 = lerp(corner(x0, y0, z0, seed), corner(x0 + 1, y0, z0, seed), fx);
        double c10 = lerp(corner(x0, y0 + 1, z0, seed), corner(x0 + 1, y0 + 1, z0, seed), fx);
        double c01 = lerp(corner(x0, y0, z0 + 1, seed), corner(x0 + 1, y0, z0 + 1, seed), fx);
        double c11 = lerp(corner(x0, y0 + 1, z0 + 1, seed), corner(x0 + 1, y0 + 1, z0 + 1, seed), fx);
        return lerp(lerp(c00, c10, fy), lerp(c01, c11, fy), fz);
    }

    private static double corner(int x, int y, int z, long seed) {
        long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (y * 0xC2B2AE3D27D4EB4FL) ^ (z * 0x165667B19E3779F9L);
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return (h >>> 11) / (double) (1L << 53);
    }

    private static double smooth(double t) {
        return t * t * (3.0D - 2.0D * t);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /** Lumps and the growth in {@link #shape} can push the surface out by a quarter, so the scan reaches a bit further. */
    private static int reach(int radius) {
        return (int) Math.ceil(radius * 1.35D);
    }

    private static int generateBody(WorldGenLevel level, RandomSource random, BlockPos origin, int radius, int verticalRadius, long seed) {
        int placed = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int reachX = reach(radius);
        int reachY = reach(verticalRadius);

        for (int dx = -reachX; dx <= reachX; dx++) {
            for (int dy = -reachY; dy <= reachY; dy++) {
                for (int dz = -reachX; dz <= reachX; dz++) {
                    double distance = shape(dx, dy, dz, radius, verticalRadius, seed);
                    if (distance > 1.0D) {
                        continue;
                    }

                    mutable.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    BlockState current = level.getBlockState(mutable);
                    if (!canReplace(current)) {
                        continue;
                    }

                    if (distance > 0.76D) {
                        set(level, mutable, SelariumBlocks.ARCANE_GEODE_STONE.get().defaultBlockState());
                        placed++;
                    } else if (distance > 0.48D) {
                        set(level, mutable, chooseCrystalLining(random));
                        placed++;
                    } else {
                        set(level, mutable, Blocks.CAVE_AIR.defaultBlockState());
                        placed++;
                    }
                }
            }
        }
        return placed;
    }

    private static BlockState chooseCrystalLining(RandomSource random) {
        int buddingChance = Math.max(0, Math.min(100, SelariumCommonConfig.ARCANE_GEODE_BUDDING_CHANCE.get()));
        if (buddingChance > 0 && random.nextInt(100) < buddingChance) {
            return SelariumBlocks.BUDDING_ARCANE_CRYSTAL.get().defaultBlockState();
        }
        return SelariumBlocks.ARCANE_CRYSTAL_BLOCK.get().defaultBlockState();
    }

    private static void placeClusters(WorldGenLevel level, RandomSource random, BlockPos origin, int radius, int verticalRadius, long seed) {
        int clusterChance = Math.max(0, Math.min(100, SelariumCommonConfig.ARCANE_GEODE_CLUSTER_CHANCE.get()));
        if (clusterChance <= 0) {
            return;
        }

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int reachX = reach(radius);
        int reachY = reach(verticalRadius);
        int maxClusters = Math.max(4, radius * 3);
        int placed = 0;

        for (int dx = -reachX + 1; dx < reachX && placed < maxClusters; dx++) {
            for (int dy = -reachY + 1; dy < reachY && placed < maxClusters; dy++) {
                for (int dz = -reachX + 1; dz < reachX && placed < maxClusters; dz++) {
                    double distance = shape(dx, dy, dz, radius, verticalRadius, seed);
                    if (distance <= 0.24D || distance >= 0.52D || random.nextInt(100) >= clusterChance) {
                        continue;
                    }

                    mutable.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (!level.getBlockState(mutable).isAir()) {
                        continue;
                    }

                    if (tryPlaceCluster(level, mutable, random)) {
                        placed++;
                    }
                }
            }
        }
    }

    private static boolean tryPlaceCluster(WorldGenLevel level, BlockPos pos, RandomSource random) {
        Direction[] directions = Direction.values();
        Direction first = directions[random.nextInt(directions.length)];
        for (int i = 0; i < directions.length; i++) {
            Direction facing = directions[(first.ordinal() + i) % directions.length];
            BlockPos supportPos = pos.relative(facing.getOpposite());
            if (!isCrystalSupport(level.getBlockState(supportPos))) {
                continue;
            }

            BlockState cluster = chooseClusterStage(random).defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, facing)
                    .setValue(AmethystClusterBlock.WATERLOGGED, false);
            if (cluster.canSurvive(level, pos)) {
                set(level, pos, cluster);
                return true;
            }
        }
        return false;
    }

    private static Block chooseClusterStage(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 35) {
            return SelariumBlocks.SMALL_ARCANE_CRYSTAL_BUD.get();
        }
        if (roll < 60) {
            return SelariumBlocks.MEDIUM_ARCANE_CRYSTAL_BUD.get();
        }
        if (roll < 82) {
            return SelariumBlocks.LARGE_ARCANE_CRYSTAL_BUD.get();
        }
        return SelariumBlocks.ARCANE_CRYSTAL_CLUSTER.get();
    }

    private static boolean canReplace(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private static boolean isCrystalSupport(BlockState state) {
        return state.is(SelariumBlocks.ARCANE_CRYSTAL_BLOCK.get()) || state.is(SelariumBlocks.BUDDING_ARCANE_CRYSTAL.get());
    }

    private static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 2);
    }
}
