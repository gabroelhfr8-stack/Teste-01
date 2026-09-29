package com.seleris.selarium.ward.effect;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MagnetismWardEffect implements IWardEffect {
    @Override
    public void tick(WardContext context) {
        if (WardEffectUtils.deactivateIfDisabled(context, SelariumCommonConfig.MAGNETISM_WARD_ENABLED.get())) {
            return;
        }

        int range = SelariumCommonConfig.MAGNETISM_WARD_RANGE.get();
        double stopDistanceSqr = SelariumCommonConfig.MAGNETISM_WARD_STOP_DISTANCE.get() * SelariumCommonConfig.MAGNETISM_WARD_STOP_DISTANCE.get();
        Vec3 center = Vec3.atCenterOf(context.pos()).add(0.0D, -0.25D, 0.0D);
        var items = context.level().getEntitiesOfClass(ItemEntity.class, new AABB(context.pos()).inflate(range), item ->
                item.isAlive() && !item.getItem().isEmpty() && item.distanceToSqr(center) > stopDistanceSqr);

        if (items.isEmpty()) {
            context.sigil().recordWardDebug(0, 0, "no dropped items");
            return;
        }

        double strength = SelariumCommonConfig.MAGNETISM_WARD_PULL_STRENGTH.get();
        int affected = 0;
        int cap = WardEffectUtils.entityCap(SelariumCommonConfig.MAGNETISM_WARD_MAX_ITEMS_PER_CYCLE.get());
        for (ItemEntity item : items) {
            if (affected >= cap) {
                context.sigil().recordWardDebug(affected, 0, "item cap reached");
                return;
            }

            Vec3 toCenter = center.subtract(item.position());
            if (toCenter.lengthSqr() <= stopDistanceSqr) {
                continue;
            }

            Vec3 pull = toCenter.normalize().scale(strength);
            Vec3 current = item.getDeltaMovement();
            item.setDeltaMovement(current.scale(0.72D).add(pull));
            item.hasImpulse = true;
            affected++;
        }
        context.sigil().recordWardDebug(affected, 0, affected > 0 ? "items pulled" : "items already centered");
    }
}
