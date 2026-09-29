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

        int placed = generateBody(level, random, origin, radius, verticalRadius);
        if (placed <= 0) {
            return false;
        }

        placeClusters(level, random, origin, radius, verticalRadius);
        ArcaneTreeGenerator.tryGenerateAboveGeode(level, random, origin, radius, verticalRadius);
        return true;
    }

    private static int generateBody(WorldGenLevel level, RandomSource random, BlockPos origin, int radius, int verticalRadius) {
        int placed = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        double radiusSq = radius * radius;
        double verticalSq = verticalRadius * verticalRadius;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -verticalRadius; dy <= verticalRadius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double distance = dx * dx / radiusSq + dy * dy / verticalSq + dz * dz / radiusSq;
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

    private static void placeClusters(WorldGenLevel level, RandomSource random, BlockPos origin, int radius, int verticalRadius) {
        int clusterChance = Math.max(0, Math.min(100, SelariumCommonConfig.ARCANE_GEODE_CLUSTER_CHANCE.get()));
        if (clusterChance <= 0) {
            return;
        }

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        double radiusSq = radius * radius;
        double verticalSq = verticalRadius * verticalRadius;
        int maxClusters = Math.max(4, radius * 3);
        int placed = 0;

        for (int dx = -radius + 1; dx < radius && placed < maxClusters; dx++) {
            for (int dy = -verticalRadius + 1; dy < verticalRadius && placed < maxClusters; dy++) {
                for (int dz = -radius + 1; dz < radius && placed < maxClusters; dz++) {
                    double distance = dx * dx / radiusSq + dy * dy / verticalSq + dz * dz / radiusSq;
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
