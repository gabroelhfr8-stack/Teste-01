package com.seleris.selarium.ward.effect.buff;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.ConfiguredWardEffect;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Buff ward: Water Breathing (optionally Dolphin's Grace / Conduit Power) for allies. */
public final class AqualungWardEffect extends ConfiguredWardEffect {
    public AqualungWardEffect() {
        super(WardType.AQUALUNG);
    }

    @Override
    protected void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
        WardEffectUtils.applyOwnerEffect(context, config.range().get(), config.manaCost().get(), MobEffects.WATER_BREATHING,
                config.effectDurationTicks().get(), 0, owner -> {
                    if (config.optionA().get()) {
                        owner.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, config.effectDurationTicks().get(), 0, true, false, true));
                    }
                    if (config.optionB().get()) {
                        owner.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, config.effectDurationTicks().get(), 0, true, false, true));
                    }
                    owner.setAirSupply(owner.getMaxAirSupply());
                    context.sigil().recordWardDebug(1, 0, "owner breathing restored");
                });
    }
}
