package com.seleris.selarium.ward.effect;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import net.minecraft.world.effect.MobEffects;

public class FeatherweightWardEffect implements IWardEffect {
    @Override
    public void tick(WardContext context) {
        if (WardEffectUtils.deactivateIfDisabled(context, SelariumCommonConfig.FEATHERWEIGHT_WARD_ENABLED.get())) {
            return;
        }

        WardEffectUtils.applyOwnerEffect(
                context,
                SelariumCommonConfig.FEATHERWEIGHT_WARD_RANGE.get(),
                SelariumCommonConfig.FEATHERWEIGHT_WARD_MANA_COST_PER_CYCLE.get(),
                MobEffects.SLOW_FALLING,
                SelariumCommonConfig.FEATHERWEIGHT_WARD_EFFECT_DURATION_TICKS.get(),
                0,
                owner -> {
                    if (SelariumCommonConfig.FEATHERWEIGHT_WARD_RESET_FALL_DISTANCE.get()) {
                        owner.fallDistance = 0.0F;
                    }
                });
    }
}
