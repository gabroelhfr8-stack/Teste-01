package com.seleris.selarium.ward;

import java.util.List;
import java.util.function.IntSupplier;

public record WardDefinition(
        WardType type,
        WardTier tier,
        int priority,
        List<WardRequirement> requirements,
        IntSupplier activationCostSupplier,
        IntSupplier upkeepCostSupplier,
        IntSupplier durationTicksSupplier,
        IntSupplier cooldownTicksSupplier,
        IntSupplier tickIntervalSupplier,
        IntSupplier rangeSupplier,
        boolean temporary
) {
    public int activationCostValue() {
        return Math.max(0, activationCostSupplier.getAsInt());
    }

    public int upkeepCostValue() {
        return Math.max(0, upkeepCostSupplier.getAsInt());
    }

    public int durationTicks() {
        return Math.max(0, durationTicksSupplier.getAsInt());
    }

    public int cooldownTicks() {
        return Math.max(0, cooldownTicksSupplier.getAsInt());
    }

    public int tickInterval() {
        return Math.max(1, tickIntervalSupplier.getAsInt());
    }

    public int range() {
        return Math.max(1, rangeSupplier.getAsInt());
    }
}
