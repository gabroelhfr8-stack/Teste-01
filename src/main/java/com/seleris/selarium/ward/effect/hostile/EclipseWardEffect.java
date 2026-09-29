package com.seleris.selarium.ward.effect.hostile;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.effect.IWardEffect;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;

/** Hostile ward: blinds (or darkens) invaders and makes mobs forget their target. */
public final class EclipseWardEffect implements IWardEffect {
    @Override
    public void tick(WardContext context) {
        if (WardEffectUtils.deactivateIfDisabled(context, SelariumCommonConfig.ECLIPSE_WARD_ENABLED.get())) {
            return;
        }

        MobEffect effect = SelariumCommonConfig.ECLIPSE_WARD_USE_DARKNESS_INSTEAD_OF_BLINDNESS.get() ? MobEffects.DARKNESS : MobEffects.BLINDNESS;
        WardEffectUtils.applyToInvaders(
                context,
                SelariumCommonConfig.ECLIPSE_WARD_RANGE.get(),
                SelariumCommonConfig.ECLIPSE_WARD_AFFECT_PLAYERS.get(),
                false,
                SelariumCommonConfig.ECLIPSE_WARD_MAX_ENTITIES_PER_CYCLE.get(),
                SelariumCommonConfig.ECLIPSE_WARD_MANA_COST_PER_ENTITY.get(),
                target -> {
                    target.addEffect(new MobEffectInstance(effect, SelariumCommonConfig.ECLIPSE_WARD_EFFECT_DURATION_TICKS.get(), 0, false, false, true));
                    if (SelariumCommonConfig.ECLIPSE_WARD_CLEAR_MOB_TARGET.get() && target instanceof Mob mob) {
                        mob.setTarget(null);
                    }
                });
    }
}
