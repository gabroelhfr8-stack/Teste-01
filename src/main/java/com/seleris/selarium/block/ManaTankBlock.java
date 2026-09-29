package com.seleris.selarium.block;

import com.seleris.selarium.blockentity.ManaTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import com.seleris.selarium.config.SelariumClientConfig;
import com.seleris.selarium.particle.GlowParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ManaTankBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Shapes.or(box(1, 0, 1, 15, 4, 15),
            box(3, 4, 3, 13, 13, 13), box(1, 3, 1, 3, 14, 3),
            box(13, 3, 1, 15, 14, 3), box(1, 3, 13, 3, 14, 15),
            box(13, 3, 13, 15, 14, 15), box(2, 12, 2, 14, 16, 14));

    public ManaTankBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaTankBlockEntity(pos, state);
    }

    /** Motes of mana drifting up through the liquid; denser the fuller the tank. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!SelariumClientConfig.vfxEnabled() || !SelariumClientConfig.AMBIENT_PARTICLES.get()
                || !(level.getBlockEntity(pos) instanceof ManaTankBlockEntity tank)) {
            return;
        }
        float ratio = tank.getVisualFillRatio();
        if (ratio <= 0.0F || random.nextFloat() > 0.35F + 0.4F * ratio) {
            return;
        }
        double top = 4.2D / 16.0D + (12.8D - 4.2D) / 16.0D * ratio;
        level.addParticle(GlowParticleOptions.wisp(0x7EE6F2, 0.6F), pos.getX() + 0.3D + random.nextDouble() * 0.4D,
                pos.getY() + 0.28D + random.nextDouble() * Math.max(0.05D, top - 0.28D), pos.getZ() + 0.3D + random.nextDouble() * 0.4D,
                0.0D, 0.012D, 0.0D);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ManaTankBlockEntity manaTank) {
            player.displayClientMessage(Component.translatable("message.selarium.tank.info", manaTank.getStoredMana(), manaTank.getManaCapacity()), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
