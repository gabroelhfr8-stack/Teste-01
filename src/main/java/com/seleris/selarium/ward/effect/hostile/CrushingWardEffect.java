package com.seleris.selarium.ward.effect.hostile;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.ConfiguredWardEffect;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

/** Hostile ward: heavy pressure that slows, weakens and damages invaders. */
public final class CrushingWardEffect extends ConfiguredWardEffect {
    public CrushingWardEffect() {
        super(WardType.CRUSHING);
    }

    @Override
    protected void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
        int affected = WardEffectUtils.applyToInvaders(context, config.range().get(), config.affectPlayers().get(), config.affectBosses().get(),
                config.maxEntitiesPerCycle().get(), config.manaCost().get(), target -> {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, config.effectDurationTicks().get(), config.amplifier().get(), false, true, true));
                    if (config.optionA().get()) {
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, config.effectDurationTicks().get(), 0, false, true, true));
                    }
                    Vec3 motion = target.getDeltaMovement();
                    double horizontalScale = Math.max(0.05D, Math.min(0.45D, config.strength().get() <= 0.0D ? 0.2D : 1.0D / (config.strength().get() + 1.0D)));
                    double vertical = motion.y > 0.0D ? motion.y * 0.15D : motion.y;
                    target.setDeltaMovement(motion.x * horizontalScale, vertical, motion.z * horizontalScale);
                    target.hurtMarked = true;
                    if (config.strength().get() > 0.0D) {
                        target.hurt(context.level().damageSources().magic(), Math.max(0.5F, config.strength().get().floatValue() * 0.5F));
                    }
                });
        context.sigil().recordWardDebug(affected, 0, affected > 0 ? "crushing pressure applied" : "no invaders");
    }
}
