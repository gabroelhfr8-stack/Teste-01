package com.seleris.selarium.block;

import com.seleris.selarium.blockentity.InscriptionBenchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import com.seleris.selarium.config.SelariumClientConfig;
import com.seleris.selarium.particle.GlowParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public final class InscriptionBenchBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Shapes.or(
            box(0, 9, 0, 16, 14, 16), box(3, 14, 3, 13, 16, 11),
            box(2, 13, 13, 14, 16, 16),
            box(1, 0, 1, 5, 9, 5), box(11, 0, 1, 15, 9, 5),
            box(1, 0, 11, 5, 9, 15), box(11, 0, 11, 15, 9, 15));

    public InscriptionBenchBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    @Override public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    @Override public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    /** An occasional violet glint above the open book. */
    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(14) != 0 || !SelariumClientConfig.vfxEnabled() || !SelariumClientConfig.AMBIENT_PARTICLES.get()) {
            return;
        }
        level.addParticle(GlowParticleOptions.spark(0xC9A8FF, 0.6F), pos.getX() + 0.35D + random.nextDouble() * 0.3D,
                pos.getY() + 0.95D, pos.getZ() + 0.35D + random.nextDouble() * 0.3D, 0.0D, 0.01D, 0.0D);
    }

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                           InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof InscriptionBenchBlockEntity bench) {
            NetworkHooks.openScreen(serverPlayer, bench, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!level.isClientSide && !state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof InscriptionBenchBlockEntity bench) {
            Containers.dropContents(level, pos, bench);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InscriptionBenchBlockEntity(pos, state);
    }
}
