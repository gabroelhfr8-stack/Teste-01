package com.seleris.selarium.ward.effect.event;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardAccessService;
import com.seleris.selarium.ward.WardArea;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardFx;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.ConfiguredWardEffect;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

/**
 * Keeps the Immortal field "warm": counts protectable allies and pays the maintenance cost.
 * The actual death prevention lives in {@code WardEventHandler}.
 */
public final class ImmortalWardEffect extends ConfiguredWardEffect {
    public ImmortalWardEffect() {
        super(WardType.IMMORTAL);
    }

    @Override
    protected void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
        List<LivingEntity> protectedEntities = WardArea.of(context, config.range().get()).entities(LivingEntity.class,
                entity -> entity.isAlive() && !entity.isSpectator() && WardAccessService.shouldAffectPositiveWard(context.sigil(), entity));
        if (protectedEntities.isEmpty()) {
            context.sigil().recordWardDebug(0, 0, "no entities in immortal field");
            return;
        }
        if (!canUseFieldMana(context, config.manaCost().get())) {
            context.sigil().recordWardDebug(protectedEntities.size(), 0, "no mana for immortal maintenance");
            return;
        }
        protectedEntities.forEach(entity -> WardFx.touch(context.level(), entity, WardType.IMMORTAL, 2));
        context.sigil().recordWardDebug(protectedEntities.size(), 0, "immortal field maintained");
    }
}
