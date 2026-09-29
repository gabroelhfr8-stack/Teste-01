package com.seleris.selarium.block;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.registry.SelariumBlocks;
import com.seleris.selarium.registry.SelariumSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class BuddingArcaneCrystalBlock extends Block {
    private static final Direction[] DIRECTIONS = Direction.values();

    public BuddingArcaneCrystalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!SelariumCommonConfig.ARCANE_CRYSTAL_RANDOM_TICK_ENABLED.get()
                || !SelariumCommonConfig.ARCANE_CRYSTAL_GROWTH_ENABLED.get()) {
            return;
        }

        int growthChance = Math.max(1, SelariumCommonConfig.ARCANE_CRYSTAL_GROWTH_CHANCE.get());
        if (random.nextInt(growthChance) != 0) {
            return;
        }

        Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos targetPos = pos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);
        BlockState nextState = getNextGrowthState(targetState, direction);
        if (nextState == null || !nextState.canSurvive(level, targetPos)) {
            return;
        }

        level.setBlock(targetPos, nextState, Block.UPDATE_ALL);
        playShimmer(level, targetPos, random);
    }

    private static BlockState getNextGrowthState(BlockState targetState, Direction facing) {
        boolean waterlogged = targetState.getFluidState().getType() == Fluids.WATER;
        if (targetState.isAir() || waterlogged) {
            return SelariumBlocks.SMALL_ARCANE_CRYSTAL_BUD.get().defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, facing)
                    .setValue(AmethystClusterBlock.WATERLOGGED, waterlogged);
        }

        if (targetState.getBlock() instanceof ArcaneCrystalClusterBlock cluster
                && cluster.canAdvance()
                && targetState.getValue(AmethystClusterBlock.FACING) == facing) {
            return cluster.nextStageBlock().defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, facing)
                    .setValue(AmethystClusterBlock.WATERLOGGED, targetState.getValue(AmethystClusterBlock.WATERLOGGED));
        }

        return null;
    }

    private static void playShimmer(ServerLevel level, BlockPos pos, RandomSource random) {
        if (!SelariumCommonConfig.ARCANE_CRYSTAL_SHIMMER_SOUND_ENABLED.get()) {
            return;
        }

        int soundChance = Math.max(0, Math.min(100, SelariumCommonConfig.ARCANE_CRYSTAL_SHIMMER_SOUND_CHANCE.get()));
        if (soundChance <= 0 || random.nextInt(100) >= soundChance) {
            return;
        }

        level.playSound(null, pos, SelariumSoundEvents.ARCANE_CRYSTAL_SHIMMER.get(), SoundSource.BLOCKS,
                2.0F, 0.9F + random.nextFloat() * 0.2F);
    }
}
