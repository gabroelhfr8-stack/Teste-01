package com.seleris.selarium.ward;

import com.seleris.selarium.config.SelariumCommonConfig;

public final class WardUpkeepService {
    private WardUpkeepService() {
    }

    public static boolean pay(WardContext context, WardDefinition definition) {
        int cost = definition.upkeepCostValue();
        boolean paid = cost <= 0 || WardManaService.consume(context.level(), context.pos(), context.sigil(), cost);
        context.sigil().recordWardUpkeep(cost, paid, paid ? "upkeep paid" : "not enough mana for upkeep");
        if (!paid) {
            context.sigil().recordWardDebug(0, 0, "field inert: no upkeep mana");
            if (definition.type() == WardType.CITADEL || definition.type() == WardType.TANGIBLE) {
                context.sigil().cleanupTemporaryWardBlocks();
            }
            if (SelariumCommonConfig.WARDS_DEACTIVATE_WHEN_UPKEEP_FAILS.get()) {
                context.sigil().deactivateWard(definition.cooldownTicks());
            }
        }
        return paid;
    }
}
