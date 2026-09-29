package com.seleris.selarium.ward.effect.event;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardAccessService;
import com.seleris.selarium.ward.WardArea;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardFx;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.ConfiguredWardEffect;
import com.seleris.selarium.ward.effect.EntityCooldowns;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

/** Reflects hostile projectiles, either straight back or towards their shooter. */
public final class DeflectionWardEffect extends ConfiguredWardEffect {
    private final EntityCooldowns cooldowns = new EntityCooldowns();

    public DeflectionWardEffect() {
        super(WardType.DEFLECTION);
    }

    @Override
    protected void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
        long now = context.level().getGameTime();
        int affected = 0;
        for (Projectile projectile : WardArea.of(context, config.range().get()).entities(Projectile.class, entity -> entity.isAlive())) {
            if (affected >= WardEffectUtils.entityCap(config.maxEntitiesPerCycle().get())) {
                break;
            }
            if (projectile.getOwner() instanceof LivingEntity owner && WardAccessService.isAlly(context.sigil(), owner)) {
                continue;
            }
            if (!cooldowns.ready(projectile.getUUID(), now)) {
                continue;
            }
            if (!canUseFieldMana(context, config.manaCost().get())) {
                break;
            }

            Vec3 movement = projectile.getDeltaMovement();
            double speed = Math.max(0.45D, movement.length());
            Entity shooter = projectile.getOwner();
            Vec3 direction = config.optionA().get() && shooter != null
                    ? shooter.position().add(0.0D, shooter.getBbHeight() * 0.5D, 0.0D).subtract(projectile.position()).normalize()
                    : safeReverse(movement);
            projectile.setDeltaMovement(direction.scale(speed));
            projectile.hurtMarked = true;
            cooldowns.start(projectile.getUUID(), now + config.cooldownPerEntityTicks().get());
            WardFx.burst(context.level(), projectile.position(), WardType.DEFLECTION, 6, 0.15D);
            affected++;
        }
        context.sigil().recordWardDebug(affected, 0, affected > 0 ? "projectiles deflected" : "no projectile");
        cooldowns.prune(now);
    }

    private static Vec3 safeReverse(Vec3 movement) {
        if (movement.lengthSqr() < 1.0E-4D) {
            return new Vec3(0.0D, 0.0D, 1.0D);
        }
        return movement.reverse().normalize();
    }
}
