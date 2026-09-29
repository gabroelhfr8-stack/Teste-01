package com.seleris.selarium.ward.effect;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import net.minecraft.world.effect.MobEffects;

public class BulwarkWardEffect implements IWardEffect {
    @Override
    public void tick(WardContext context) {
        WardEffectUtils.applyOwnerEffect(
                context,
                SelariumCommonConfig.BULWARK_WARD_RANGE.get(),
                SelariumCommonConfig.BULWARK_WARD_MANA_COST_PER_CYCLE.get(),
                MobEffects.DAMAGE_RESISTANCE,
                SelariumCommonConfig.BULWARK_WARD_EFFECT_DURATION_TICKS.get(),
                SelariumCommonConfig.BULWARK_WARD_AMPLIFIER.get());
    }
}
