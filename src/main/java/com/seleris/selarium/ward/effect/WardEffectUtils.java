package com.seleris.selarium.ward.effect;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardFx;
import com.seleris.selarium.ward.WardManaService;
import com.seleris.selarium.ward.WardTargetingService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Consumer;

public final class WardEffectUtils {
    private WardEffectUtils() {
    }

    public static boolean deactivateIfDisabled(WardContext context, boolean enabled) {
        if (enabled) {
            return false;
        }

        context.sigil().deactivateWard(0);
        return true;
    }

    public static void applyOwnerEffect(WardContext context, int range, int manaCost, MobEffect effect, int durationTicks, int amplifier) {
        applyOwnerEffect(context, range, manaCost, effect, durationTicks, amplifier, owner -> {
        });
    }

    public static void applyOwnerEffect(WardContext context, int range, int manaCost, MobEffect effect, int durationTicks, int amplifier, Consumer<ServerPlayer> afterApply) {
        for (ServerPlayer ally : WardTargetingService.findAlliedPlayersInRange(context.level(), context.pos(), context.sigil(), range)) {
            if (hasFieldUpkeep(context) || WardManaService.consume(context.level(), context.pos(), context.sigil(), manaCost)) {
                ally.addEffect(new MobEffectInstance(effect, durationTicks, amplifier, true, false, true));
                afterApply.accept(ally);
                WardFx.touch(context.level(), ally, context.sigil().getWardType());
            }
        }
    }

    public static int applyToInvaders(WardContext context, int range, boolean includePlayers, boolean includeBosses, int maxTargets, int manaCostPerTarget, Consumer<LivingEntity> action) {
        int cap = entityCap(maxTargets);
        if (cap <= 0) {
            return 0;
        }

        var targets = WardTargetingService.findInvaders(context.level(), context.pos(), context.sigil(), range, true, includePlayers, includeBosses);
        int affected = 0;
        for (LivingEntity target : targets) {
            if (affected >= cap || (!hasFieldUpkeep(context) && !WardManaService.consume(context.level(), context.pos(), context.sigil(), manaCostPerTarget))) {
                return affected;
            }

            action.accept(target);
            WardFx.touch(context.level(), target, context.sigil().getWardType());
            affected++;
        }
        return affected;
    }

    public static boolean hasFieldUpkeep(WardContext context) {
        return context.sigil().hasRecentPaidUpkeep(SelariumCommonConfig.WARD_GLOBAL_HARD_ENTITY_CAP.get());
    }

    public static int entityCap(int perWardCap) {
        int globalCap = Math.max(1, SelariumCommonConfig.WARD_GLOBAL_HARD_ENTITY_CAP.get());
        int requestedCap = perWardCap <= 0 ? globalCap : perWardCap;
        return Math.max(0, Math.min(globalCap, requestedCap));
    }

    public static int blockCap(int perWardCap) {
        int globalCap = Math.max(1, SelariumCommonConfig.WARD_GLOBAL_HARD_BLOCK_CAP.get());
        int requestedCap = perWardCap <= 0 ? globalCap : perWardCap;
        return Math.max(0, Math.min(globalCap, requestedCap));
    }
}
