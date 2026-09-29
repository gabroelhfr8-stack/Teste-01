package com.seleris.selarium.ward.effect.hostile;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardArea;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardFx;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.ConfiguredWardEffect;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.Projectile;

/** Hostile ward: time nearly stops for invaders and (optionally) projectiles. */
public final class StasisWardEffect extends ConfiguredWardEffect {
    public StasisWardEffect() {
        super(WardType.STASIS);
    }

    @Override
    protected void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
        int affected = WardEffectUtils.applyToInvaders(context, config.range().get(), config.affectPlayers().get(), config.affectBosses().get(),
                config.maxEntitiesPerCycle().get(), config.manaCost().get(), target -> {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, config.effectDurationTicks().get(), config.amplifier().get(), false, false, true));
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, config.effectDurationTicks().get(), 0, false, false, true));
                    target.setDeltaMovement(target.getDeltaMovement().scale(config.strength().get()));
                    target.hurtMarked = true;
                });

        if (!config.optionA().get()) {
            context.sigil().recordWardDebug(affected, 0, affected > 0 ? "entities stilled" : "no invaders");
            return;
        }
        int projectiles = 0;
        for (Projectile projectile : WardArea.of(context, config.range().get()).entities(Projectile.class, entity -> entity.isAlive())) {
            if (projectiles >= WardEffectUtils.entityCap(config.maxEntitiesPerCycle().get())) {
                break;
            }
            if (!canUseFieldMana(context, config.manaCost().get())) {
                break;
            }
            projectile.setDeltaMovement(projectile.getDeltaMovement().scale(config.strength().get()));
            projectile.hurtMarked = true;
            WardFx.touch(context.level(), projectile, WardType.STASIS, 2);
            projectiles++;
        }
        context.sigil().recordWardDebug(affected + projectiles, 0, affected + projectiles > 0 ? "stasis applied" : "no targets");
    }
}
