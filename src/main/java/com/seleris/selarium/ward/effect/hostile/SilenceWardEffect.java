package com.seleris.selarium.ward.effect.hostile;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.ConfiguredWardEffect;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;

/** Hostile ward: strips beneficial effects from invaders and slows their mining/attacking. */
public final class SilenceWardEffect extends ConfiguredWardEffect {
    public SilenceWardEffect() {
        super(WardType.SILENCE);
    }

    @Override
    protected void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
        WardEffectUtils.applyToInvaders(context, config.range().get(), config.affectPlayers().get(), config.affectBosses().get(),
                config.maxEntitiesPerCycle().get(), config.manaCost().get(), target -> {
                    if (config.optionA().get()) {
                        new ArrayList<>(target.getActiveEffects()).stream()
                                .filter(effect -> effect.getEffect().isBeneficial())
                                .forEach(effect -> target.removeEffect(effect.getEffect()));
                    }
                    if (config.optionB().get()) {
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, config.effectDurationTicks().get(), 0, false, true, true));
                    }
                    target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, config.effectDurationTicks().get(), 0, false, true, true));
                });
    }
}
