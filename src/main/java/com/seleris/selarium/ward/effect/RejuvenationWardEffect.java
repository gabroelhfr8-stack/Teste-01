package com.seleris.selarium.ward.effect;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import net.minecraft.world.effect.MobEffects;

public class RejuvenationWardEffect implements IWardEffect {
    @Override
    public void tick(WardContext context) {
        WardEffectUtils.applyOwnerEffect(
                context,
                SelariumCommonConfig.REJUVENATION_WARD_RANGE.get(),
                SelariumCommonConfig.REJUVENATION_WARD_MANA_COST_PER_CYCLE.get(),
                MobEffects.REGENERATION,
                SelariumCommonConfig.REJUVENATION_WARD_EFFECT_DURATION_TICKS.get(),
                SelariumCommonConfig.REJUVENATION_WARD_AMPLIFIER.get());
    }
}
