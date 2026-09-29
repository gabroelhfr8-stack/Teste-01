package com.seleris.selarium.ward.effect;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardType;

/**
 * Wards that only pay upkeep and are enforced by Forge events ({@code WardEventHandler}) rather than
 * by ticking: Sanctuary, Bounty, Disruption and Soul-Chain.
 */
public final class PassiveWardEffect extends ConfiguredWardEffect {
    public PassiveWardEffect(WardType type) {
        super(type);
    }

    @Override
    protected void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
        // Intentionally empty: behaviour lives in WardEventHandler.
    }
}
