package com.seleris.selarium.block;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.ActiveWardIndex;
import com.seleris.selarium.ward.ProjectionDisplayCache;
import com.seleris.selarium.ward.WardAccessService;
import com.seleris.selarium.ward.WardType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumSet;

public class TemporaryWardBlock extends Block {
    public TemporaryWardBlock(Properties properties) {
        super(properties);
    }

    /** Neighbouring wall blocks merge into one continuous surface instead of drawing every inner face. */
    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, direction);
    }

    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level instanceof Level gameLevel && gameLevel.isClientSide
                && context instanceof EntityCollisionContext entityContext
                && entityContext.getEntity() instanceof Player player
                && ProjectionDisplayCache.canPass(gameLevel, pos, player.getUUID())) {
            return Shapes.empty();
        }
        if (level instanceof ServerLevel serverLevel && context instanceof EntityCollisionContext entityContext
                && entityContext.getEntity() instanceof LivingEntity living) {
            int range = Math.max(SelariumCommonConfig.mvpWard(WardType.CITADEL).range().get(),
                    SelariumCommonConfig.mvpWard(WardType.TANGIBLE).range().get()) + 2;
            for (ActiveWardIndex.WardInstance ward : ActiveWardIndex.find(serverLevel, Vec3.atCenterOf(pos),
                    EnumSet.of(WardType.CITADEL, WardType.TANGIBLE), range)) {
                if (ward.sigil().ownsTemporaryWardBlock(pos) && WardAccessService.isAlly(ward.sigil(), living)) {
                    return Shapes.empty();
                }
            }
        }
        return super.getCollisionShape(state, level, pos, context);
    }
}
