package com.seleris.selarium.ward.effect.utility;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.ConfiguredWardEffect;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Utility ward: lets allies pass through Phasing Blocks and grants light movement perks. */
public final class PhasingWardEffect extends ConfiguredWardEffect {
    public PhasingWardEffect() {
        super(WardType.PHASING);
    }

    @Override
    protected void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
        int duration = Math.max(20, config.effectDurationTicks().get());
        WardEffectUtils.applyOwnerEffect(context, config.range().get(), config.manaCost().get(), MobEffects.MOVEMENT_SPEED, duration, 0, owner -> {
            owner.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration, 0, true, false, true));
            owner.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, duration, 0, true, false, true));
            if (context.level().getGameTime() % 40 == 0) {
                owner.displayClientMessage(Component.translatable("message.selarium.phasing.available"), true);
            }
            context.sigil().recordWardDebug(1, 0, "phasing support active");
        });
    }
}
