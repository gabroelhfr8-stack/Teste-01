package com.seleris.selarium.ward.effect.hostile;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardFx;
import com.seleris.selarium.ward.WardTargetingService;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.EntityCooldowns;
import com.seleris.selarium.ward.effect.IWardEffect;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/** Hostile ward: teleports invaders to a safe spot just outside the field. */
public final class BanishmentWardEffect implements IWardEffect {
    private final EntityCooldowns cooldowns = new EntityCooldowns();

    @Override
    public void tick(WardContext context) {
        if (WardEffectUtils.deactivateIfDisabled(context, SelariumCommonConfig.BANISHMENT_WARD_ENABLED.get())) {
            return;
        }

        long gameTime = context.level().getGameTime();
        var targets = WardTargetingService.findInvaders(
                context.level(),
                context.pos(),
                context.sigil(),
                SelariumCommonConfig.BANISHMENT_WARD_RANGE.get(),
                true,
                SelariumCommonConfig.BANISHMENT_WARD_AFFECT_PLAYERS.get(),
                SelariumCommonConfig.BANISHMENT_WARD_AFFECT_BOSSES.get());

        int affected = 0;
        int cap = WardEffectUtils.entityCap(SelariumCommonConfig.BANISHMENT_WARD_MAX_ENTITIES_PER_CYCLE.get());
        for (LivingEntity target : targets) {
            if (affected >= cap) {
                context.sigil().recordWardDebug(affected, 0, "banishment entity cap reached");
                cooldowns.prune(gameTime);
                return;
            }
            if (!cooldowns.ready(target.getUUID(), gameTime)) {
                continue;
            }

            Optional<Vec3> safeDestination = findSafeDestination(context, target);
            if (safeDestination.isEmpty()) {
                continue;
            }

            WardFx.burst(context.level(), target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D), WardType.BANISHMENT, 8, 0.4D);
            teleport(target, safeDestination.get(), context);
            WardFx.burst(context.level(), target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D), WardType.BANISHMENT, 8, 0.4D);
            cooldowns.start(target.getUUID(), gameTime + SelariumCommonConfig.BANISHMENT_WARD_COOLDOWN_PER_ENTITY_TICKS.get());
            affected++;
        }
        context.sigil().recordWardDebug(affected, 0, affected > 0 ? "invaders banished" : "no valid banishment target");
        cooldowns.prune(gameTime);
    }

    private Optional<Vec3> findSafeDestination(WardContext context, LivingEntity target) {
        Vec3 center = Vec3.atCenterOf(context.pos());
        Vec3 away = target.position().subtract(center);
        if (away.horizontalDistanceSqr() < 0.01D) {
            float angle = context.level().random.nextFloat() * ((float) Math.PI * 2.0F);
            away = new Vec3(Mth.cos(angle), 0.0D, Mth.sin(angle));
        }

        away = new Vec3(away.x, 0.0D, away.z).normalize();
        int distance = Math.max(SelariumCommonConfig.BANISHMENT_WARD_RANGE.get() + 2, SelariumCommonConfig.BANISHMENT_WARD_TELEPORT_DISTANCE.get());
        double baseX = center.x + away.x * distance;
        double baseZ = center.z + away.z * distance;
        int baseY = Mth.floor(target.getY());

        for (int radiusOffset = 0; radiusOffset <= 3; radiusOffset++) {
            for (int yOffset = -2; yOffset <= 3; yOffset++) {
                BlockPos pos = BlockPos.containing(baseX + away.x * radiusOffset, baseY + yOffset, baseZ + away.z * radiusOffset);
                if (isSafeStandingPosition(context, pos)) {
                    return Optional.of(Vec3.atBottomCenterOf(pos));
                }
            }
        }
        return Optional.empty();
    }

    private boolean isSafeStandingPosition(WardContext context, BlockPos pos) {
        if (!context.level().getWorldBorder().isWithinBounds(pos) || pos.getY() <= context.level().getMinBuildHeight() || pos.getY() >= context.level().getMaxBuildHeight() - 2) {
            return false;
        }

        BlockState feet = context.level().getBlockState(pos);
        BlockState head = context.level().getBlockState(pos.above());
        BlockState below = context.level().getBlockState(pos.below());
        return feet.isAir()
                && head.isAir()
                && feet.getFluidState().isEmpty()
                && head.getFluidState().isEmpty()
                && below.getFluidState().isEmpty()
                && !below.getCollisionShape(context.level(), pos.below()).isEmpty();
    }

    private static void teleport(LivingEntity target, Vec3 destination, WardContext context) {
        target.stopRiding();
        target.setDeltaMovement(Vec3.ZERO);
        if (target instanceof ServerPlayer player) {
            player.teleportTo(context.level(), destination.x, destination.y, destination.z, player.getYRot(), player.getXRot());
        } else {
            target.teleportTo(destination.x, destination.y, destination.z);
        }
    }
}
