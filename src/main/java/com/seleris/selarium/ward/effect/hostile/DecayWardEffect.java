package com.seleris.selarium.ward.effect.hostile;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.ConfiguredWardEffect;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Hostile ward: Wither (and optionally Weakness) on invaders. */
public final class DecayWardEffect extends ConfiguredWardEffect {
    public DecayWardEffect() {
        super(WardType.DECAY);
    }

    @Override
    protected void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
        int affected = WardEffectUtils.applyToInvaders(context, config.range().get(), config.affectPlayers().get(), config.affectBosses().get(),
                config.maxEntitiesPerCycle().get(), config.manaCost().get(), target -> {
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, config.effectDurationTicks().get(), config.amplifier().get(), false, false, true));
                    if (config.optionA().get()) {
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, config.effectDurationTicks().get(), 0, false, false, true));
                    }
                });
        context.sigil().recordWardDebug(affected, 0, affected > 0 ? "decay applied" : "no invaders");
    }
}
