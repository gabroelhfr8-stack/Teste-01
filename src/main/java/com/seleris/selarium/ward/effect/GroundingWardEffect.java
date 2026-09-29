package com.seleris.selarium.ward.effect;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardTargetingService;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

public class GroundingWardEffect implements IWardEffect {
    @Override
    public void tick(WardContext context) {
        if (WardEffectUtils.deactivateIfDisabled(context, SelariumCommonConfig.GROUNDING_WARD_ENABLED.get())) {
            return;
        }

        int affected = 0;
        for (var ally : WardTargetingService.findAlliedPlayersInRange(context.level(), context.pos(), context.sigil(), SelariumCommonConfig.GROUNDING_WARD_RANGE.get())) {
            boolean hasWork = (SelariumCommonConfig.GROUNDING_WARD_REMOVE_LEVITATION.get() && ally.hasEffect(MobEffects.LEVITATION))
                    || (SelariumCommonConfig.GROUNDING_WARD_RESET_FALL_DISTANCE.get() && ally.fallDistance > 0.0F)
                    || (SelariumCommonConfig.GROUNDING_WARD_REDUCE_VERTICAL_KNOCKBACK.get() && ally.getDeltaMovement().y > 0.08D);
            if (!hasWork) {
                continue;
            }

            if (SelariumCommonConfig.GROUNDING_WARD_REMOVE_LEVITATION.get()) {
                ally.removeEffect(MobEffects.LEVITATION);
            }
            if (SelariumCommonConfig.GROUNDING_WARD_RESET_FALL_DISTANCE.get()) {
                ally.fallDistance = 0.0F;
            }
            if (SelariumCommonConfig.GROUNDING_WARD_REDUCE_VERTICAL_KNOCKBACK.get()) {
                Vec3 movement = ally.getDeltaMovement();
                if (movement.y > 0.08D) {
                    ally.setDeltaMovement(movement.x, movement.y * 0.35D, movement.z);
                }
            }
            affected++;
        }
        context.sigil().recordWardDebug(affected, 0, affected > 0 ? "allies grounded" : "no ally grounding needed");
    }
}
