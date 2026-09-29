package com.seleris.selarium.ward.effect.detection;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.effect.IWardEffect;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Detection ward: reveals invaders with the Glowing effect. */
public final class SpectralWardEffect implements IWardEffect {
    @Override
    public void tick(WardContext context) {
        WardEffectUtils.applyToInvaders(
                context,
                SelariumCommonConfig.SPECTRAL_WARD_RANGE.get(),
                SelariumCommonConfig.SPECTRAL_WARD_AFFECT_PLAYERS.get(),
                true,
                SelariumCommonConfig.SPECTRAL_WARD_MAX_ENTITIES_PER_CYCLE.get(),
                SelariumCommonConfig.SPECTRAL_WARD_MANA_COST_PER_ENTITY.get(),
                target -> target.addEffect(new MobEffectInstance(MobEffects.GLOWING, SelariumCommonConfig.SPECTRAL_WARD_GLOWING_DURATION_TICKS.get(), 0, false, false, true)));
    }
}
