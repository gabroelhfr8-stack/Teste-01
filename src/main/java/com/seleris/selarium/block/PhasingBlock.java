package com.seleris.selarium.block;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.ward.ActiveWardIndex;
import com.seleris.selarium.ward.ProjectionDisplayCache;
import com.seleris.selarium.ward.WardType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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

public class PhasingBlock extends Block {
    public PhasingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level instanceof ServerLevel serverLevel
                && context instanceof EntityCollisionContext entityContext
                && entityContext.getEntity() instanceof Player player
                && canOwnerPhaseThrough(serverLevel, pos, player)) {
            return Shapes.empty();
        }
        if (level instanceof Level gameLevel
                && context instanceof EntityCollisionContext entityContext
                && entityContext.getEntity() instanceof Player player
                && (ProjectionDisplayCache.canPhase(gameLevel, pos, player.getUUID())
                || canOwnerPhaseThroughSyncedSigil(gameLevel, pos, player))) {
            return Shapes.empty();
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    private boolean canOwnerPhaseThrough(ServerLevel level, BlockPos pos, Player player) {
        SelariumCommonConfig.MvpWardConfig config = SelariumCommonConfig.mvpWard(WardType.PHASING);
        if (!config.enabled().get()) {
            return false;
        }
        for (ActiveWardIndex.WardInstance ward : ActiveWardIndex.find(level, Vec3.atCenterOf(pos), WardType.PHASING, config.range().get())) {
            if (player.getUUID().equals(ward.sigil().getOwner())) {
                return true;
            }
        }
        return false;
    }

    private boolean canOwnerPhaseThroughSyncedSigil(Level level, BlockPos pos, Player player) {
        SelariumCommonConfig.MvpWardConfig config = SelariumCommonConfig.mvpWard(WardType.PHASING);
        if (!config.enabled().get()) {
            return false;
        }
        int range = config.range().get();
        double rangeSqr = range * range;
        for (BlockPos candidate : BlockPos.betweenClosed(pos.offset(-range, -range, -range), pos.offset(range, range, range))) {
            if (candidate.distToCenterSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > rangeSqr
                    || !(level.getBlockEntity(candidate) instanceof ArcaneSigilBlockEntity sigil)
                    || !sigil.isActive()
                    || sigil.getWardType() != WardType.PHASING
                    || !player.getUUID().equals(sigil.getOwner())) {
                continue;
            }
            return true;
        }
        return false;
    }
}
