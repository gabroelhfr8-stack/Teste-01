package com.seleris.selarium.ward.effect.buff;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.effect.IWardEffect;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.world.effect.MobEffects;

/** Buff ward: Resistance for allies inside the field. */
public final class BulwarkWardEffect implements IWardEffect {
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
