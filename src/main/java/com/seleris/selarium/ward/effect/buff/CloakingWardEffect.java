package com.seleris.selarium.ward.effect.buff;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardArea;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.ConfiguredWardEffect;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;

/** Buff ward: turns allies invisible and makes nearby mobs lose track of them. */
public final class CloakingWardEffect extends ConfiguredWardEffect {
    public CloakingWardEffect() {
        super(WardType.CLOAKING);
    }

    @Override
    protected void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
        WardArea area = WardArea.of(context, config.range().get());
        WardEffectUtils.applyOwnerEffect(context, config.range().get(), config.manaCost().get(), MobEffects.INVISIBILITY,
                config.effectDurationTicks().get(), 0, owner -> {
                    int cleared = 0;
                    if (config.optionA().get()) {
                        for (Mob mob : area.entities(Mob.class, mob -> mob.getTarget() == owner)) {
                            mob.setTarget(null);
                            cleared++;
                        }
                    }
                    context.sigil().recordWardDebug(cleared, 0, "owner cloaked");
                });
    }
}
