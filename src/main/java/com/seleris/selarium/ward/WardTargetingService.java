package com.seleris.selarium.ward;

import com.seleris.selarium.ward.WardFieldSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;

import java.util.List;
import java.util.Optional;

public final class WardTargetingService {
    private WardTargetingService() {
    }

    public static List<LivingEntity> findInvaders(ServerLevel level, BlockPos pos, WardFieldSource sigil, int range, boolean includeHostileMobs, boolean includePlayers) {
        return findInvaders(level, pos, sigil, range, includeHostileMobs, includePlayers, true);
    }

    public static List<LivingEntity> findInvaders(ServerLevel level, BlockPos pos, WardFieldSource sigil, int range, boolean includeHostileMobs, boolean includePlayers, boolean includeBosses) {
        return WardArea.of(level, pos, range).entities(LivingEntity.class,
                entity -> isInvader(sigil, entity, includeHostileMobs, includePlayers, includeBosses));
    }

    public static Optional<ServerPlayer> findOwnerInRange(ServerLevel level, BlockPos pos, WardFieldSource sigil, int range) {
        if (sigil.getOwner() == null) {
            return Optional.empty();
        }

        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(sigil.getOwner());
        if (owner == null || owner.isSpectator() || owner.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > range * range) {
            return Optional.empty();
        }
        return Optional.of(owner);
    }

    public static List<ServerPlayer> findAlliedPlayersInRange(ServerLevel level, BlockPos pos, WardFieldSource sigil, int range) {
        double rangeSqr = range * range;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        return level.getPlayers(player -> !player.isSpectator()
                && player.distanceToSqr(x, y, z) <= rangeSqr
                && WardAccessService.shouldAffectPositiveWard(sigil, player));
    }

    public static boolean isInvader(WardFieldSource sigil, LivingEntity entity, boolean includeHostileMobs, boolean includePlayers, boolean includeBosses) {
        return WardAccessService.shouldAffectNegativeWard(sigil, entity, includeHostileMobs, includePlayers, includeBosses);
    }

    public static boolean isBoss(LivingEntity entity) {
        return entity instanceof EnderDragon || entity instanceof WitherBoss;
    }
}
