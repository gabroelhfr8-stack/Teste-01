package com.seleris.selarium.ward.effect.hostile;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardFx;
import com.seleris.selarium.ward.WardTargetingService;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.ConfiguredWardEffect;
import com.seleris.selarium.ward.effect.EntityCooldowns;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Hostile ward: launches invaders upwards, on a per-entity cooldown. */
public final class InversionWardEffect extends ConfiguredWardEffect {
    private final EntityCooldowns cooldowns = new EntityCooldowns();

    public InversionWardEffect() {
        super(WardType.INVERSION);
    }

    @Override
    protected void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
        long now = context.level().getGameTime();
        int affected = 0;
        for (LivingEntity target : WardTargetingService.findInvaders(context.level(), context.pos(), context.sigil(), config.range().get(),
                true, config.affectPlayers().get(), config.affectBosses().get())) {
            if (affected >= WardEffectUtils.entityCap(config.maxEntitiesPerCycle().get())) {
                break;
            }
            if (!cooldowns.ready(target.getUUID(), now) || !canUseFieldMana(context, config.manaCost().get())) {
                continue;
            }
            Vec3 motion = target.getDeltaMovement();
            target.setDeltaMovement(motion.x * 0.25D, Math.max(motion.y + config.strength().get(), config.strength().get()), motion.z * 0.25D);
            target.hurtMarked = true;
            cooldowns.start(target.getUUID(), now + config.cooldownPerEntityTicks().get());
            WardFx.burst(context.level(), target.position(), WardType.INVERSION, 6, 0.3D);
            affected++;
        }
        context.sigil().recordWardDebug(affected, 0, affected > 0 ? "targets inverted upward" : "no invaders or cooldown");
        cooldowns.prune(now);
    }
}
