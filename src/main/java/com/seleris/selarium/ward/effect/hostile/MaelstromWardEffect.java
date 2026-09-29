package com.seleris.selarium.ward.effect.hostile;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.ConfiguredWardEffect;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

/** Hostile ward: a drowning whirlpool that drags invaders towards the centre. */
public final class MaelstromWardEffect extends ConfiguredWardEffect {
    public MaelstromWardEffect() {
        super(WardType.MAELSTROM);
    }

    @Override
    protected void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
        int affected = WardEffectUtils.applyToInvaders(context, config.range().get(), config.affectPlayers().get(), config.affectBosses().get(),
                config.maxEntitiesPerCycle().get(), config.manaCost().get(), target -> {
                    target.hurt(context.level().damageSources().drown(), config.strength().get().floatValue());
                    target.setAirSupply(Math.max(-20, target.getAirSupply() - 80));
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, config.effectDurationTicks().get(), config.amplifier().get(), false, true, true));
                    Vec3 toCenter = Vec3.atCenterOf(context.pos()).subtract(target.position()).normalize().scale(0.08D);
                    target.setDeltaMovement(target.getDeltaMovement().add(toCenter.x, -0.03D, toCenter.z));
                    target.hurtMarked = true;
                });
        context.sigil().recordWardDebug(affected, 0, affected > 0 ? "maelstrom pressure applied" : "no invaders");
    }
}
