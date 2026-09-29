package com.seleris.selarium.block;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.registry.SelariumBlocks;
import com.seleris.selarium.registry.SelariumItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class ArcanePetalsBlock extends BushBlock {
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public ArcanePetalsBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT)
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(SelariumBlocks.ARCANE_GEODE_STONE.get())
                || state.is(SelariumBlocks.ARCANE_CRYSTAL_BLOCK.get())
                || state.isFaceSturdy(level, pos, Direction.UP);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!super.canSurvive(state, level, pos)) {
            return false;
        }
        if (!SelariumCommonConfig.ARCANE_PETALS_REQUIRE_ARCANE_LEAVES_NEARBY.get()) {
            return true;
        }
        int radius = Math.max(1, SelariumCommonConfig.ARCANE_PETALS_SEARCH_RADIUS.get());
        return hasNearbyArcaneLeaves(level, pos, radius);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (!state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        int count = Math.max(1, SelariumCommonConfig.ARCANE_PETALS_DROP_ARCANE_DUST_COUNT.get());
        return List.of(new ItemStack(SelariumItems.ARCANE_DUST.get(), count));
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return true;
    }

    public static boolean hasNearbyArcaneLeaves(LevelReader level, BlockPos pos, int radius) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = 0; dy <= Math.min(6, radius + 2); dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    mutable.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    if (level.getBlockState(mutable).is(SelariumBlocks.ARCANE_LEAVES.get())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
