package com.seleris.selarium.ward;

import com.seleris.selarium.ward.effect.WardEffects;

public final class WardManager {
    private WardManager() {
    }

    public static void tick(WardContext context) {
        context.sigil().tickWardTimers();

        if (!context.sigil().isActive()) {
            if (context.sigil() instanceof WardProjection projection) ActiveWardIndex.removeProjection(projection.id());
            else ActiveWardIndex.remove(context.level(), context.pos());
            return;
        }

        WardDefinition definition = WardDefinitions.get(context.sigil().getWardType()).orElse(null);
        if (definition == null) {
            context.sigil().deactivateWard(0);
            return;
        }

        if (definition.temporary() && context.sigil().getWardDurationRemainingTicks() <= 0) {
            context.sigil().deactivateWard(definition.cooldownTicks());
            return;
        }

        ActiveWardIndex.update(context.level(), context.pos(), context.sigil());

        if (context.sigil() instanceof WardProjection
                && context.sigil().getLastUpkeepTick() == context.level().getGameTime()) {
            runCycle(context, definition);
            return;
        }

        int interval = Math.max(1, definition.tickInterval());
        boolean firstUpkeep = context.sigil().getLastUpkeepTick() < 0;
        if (!firstUpkeep && Math.floorMod(context.level().getGameTime() + context.pos().asLong(), interval) != 0) {
            return;
        }

        if (!WardUpkeepService.pay(context, definition)) {
            return;
        }

        runCycle(context, definition);
    }

    /** One paid ward cycle: run the effect, then broadcast a visual pulse (at most about once a second). */
    private static void runCycle(WardContext context, WardDefinition definition) {
        WardType type = context.sigil().getWardType();
        WardEffects.get(type).tick(context);

        int interval = Math.max(1, definition.tickInterval());
        int cyclesPerPulse = Math.max(1, 20 / interval);
        long cycle = context.level().getGameTime() / interval;
        if (context.sigil().isActive() && cycle % cyclesPerPulse == 0 && WardStyles.of(type).shell() != WardShellStyle.NONE) {
            WardFx.pulse(context.level(), context.pos(), type, definition.range());
        }
    }
}
