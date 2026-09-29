package com.seleris.selarium.ward.effect;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardManaService;
import com.seleris.selarium.ward.WardType;

/**
 * Base class for wards tuned through {@link SelariumCommonConfig.MvpWardConfig}. It handles the
 * master "enabled" switch so subclasses only implement {@link #apply}.
 */
public abstract class ConfiguredWardEffect implements IWardEffect {
    private final WardType type;

    protected ConfiguredWardEffect(WardType type) {
        this.type = type;
    }

    public final WardType type() {
        return type;
    }

    @Override
    public final void tick(WardContext context) {
        SelariumCommonConfig.MvpWardConfig config = SelariumCommonConfig.mvpWard(type);
        if (WardEffectUtils.deactivateIfDisabled(context, config.enabled().get())) {
            return;
        }
        apply(context, config);
    }

    protected abstract void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config);

    /** Pays a one-off mana cost from the field's sources; a non-positive cost is always affordable. */
    protected static boolean canUseFieldMana(WardContext context, int cost) {
        return cost <= 0 || WardManaService.consume(context.level(), context.pos(), context.sigil(), cost);
    }
}
