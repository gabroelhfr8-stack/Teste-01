package com.seleris.selarium.ward;

import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.dust.DustPurity;
import com.seleris.selarium.dust.DustType;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class WardDefinitions {
    private static final List<WardDefinition> DEFINITIONS = List.of(
            mvp(WardType.SANCTUARY, WardTier.REFINED, 340,
                    req(DustType.ARCANE), req(DustType.FOCUS), req(DustType.AEGIS), req(DustType.BINDING), req(DustType.WARP)),
            mvp(WardType.BOUNTY, WardTier.REFINED, 335,
                    req(DustType.ARCANE), req(DustType.FOCUS), req(DustType.VITAL), req(DustType.CHRONO), req(DustType.BINDING)),
            mvp(WardType.IMMORTAL, WardTier.REFINED, 330,
                    req(DustType.ARCANE), req(DustType.AEGIS), req(DustType.VITAL), req(DustType.CHRONO), req(DustType.BINDING)),
            mvp(WardType.MAELSTROM, WardTier.REFINED, 325,
                    req(DustType.ARCANE), req(DustType.BINDING), req(DustType.DENSITY), req(DustType.VITAL), req(DustType.WARP)),
            mvp(WardType.TANGIBLE, WardTier.REFINED, 320,
                    req(DustType.ARCANE), req(DustType.FOCUS), req(DustType.AEGIS), req(DustType.BINDING)),
            mvp(WardType.DEFLECTION, WardTier.REFINED, 315,
                    req(DustType.ARCANE), req(DustType.AEGIS), req(DustType.WARP), req(DustType.FOCUS)),
            mvp(WardType.TRANSMUTATION, WardTier.REFINED, 310,
                    req(DustType.ARCANE), req(DustType.FOCUS), req(DustType.CHRONO), req(DustType.VITAL)),
            mvp(WardType.SILENCE, WardTier.REFINED, 305,
                    req(DustType.ARCANE), req(DustType.FOCUS), req(DustType.BINDING), req(DustType.ECHO)),
            mvp(WardType.SOUL_CHAIN, WardTier.REFINED, 300,
                    req(DustType.ARCANE), req(DustType.BINDING), req(DustType.VITAL), req(DustType.ECHO)),
            mvp(WardType.DRAIN, WardTier.REFINED, 295,
                    req(DustType.ARCANE), req(DustType.VITAL), req(DustType.BINDING), req(DustType.DENSITY)),
            mvp(WardType.PHASING, WardTier.REFINED, 290,
                    req(DustType.ARCANE), req(DustType.VEIL), req(DustType.WARP), req(DustType.DENSITY)),
            mvp(WardType.INVERSION, WardTier.REFINED, 285,
                    req(DustType.ARCANE), req(DustType.FOCUS), req(DustType.DENSITY), req(DustType.WARP)),
            mvp(WardType.DECAY, WardTier.REFINED, 280,
                    req(DustType.ARCANE), req(DustType.BINDING), req(DustType.VITAL), req(DustType.CHRONO)),
            mvp(WardType.STASIS, WardTier.REFINED, 275,
                    req(DustType.ARCANE), req(DustType.BINDING), req(DustType.CHRONO)),
            mvp(WardType.CITADEL, WardTier.BASIC, 270,
                    req(DustType.ARCANE), req(DustType.AEGIS, 2)),
            mvp(WardType.DISRUPTION, WardTier.BASIC, 265,
                    req(DustType.ARCANE), req(DustType.BINDING), req(DustType.WARP)),
            mvp(WardType.CLOAKING, WardTier.BASIC, 260,
                    req(DustType.ARCANE), req(DustType.FOCUS), req(DustType.VEIL)),
            mvp(WardType.ACCELERATING, WardTier.BASIC, 255,
                    req(DustType.ARCANE), req(DustType.VITAL), req(DustType.CHRONO)),
            mvp(WardType.EFFICIENCY, WardTier.BASIC, 250,
                    req(DustType.ARCANE), req(DustType.FOCUS), req(DustType.CHRONO)),
            mvp(WardType.CRUSHING, WardTier.BASIC, 245,
                    req(DustType.ARCANE), req(DustType.BINDING), req(DustType.DENSITY)),
            mvp(WardType.AQUALUNG, WardTier.BASIC, 240,
                    req(DustType.ARCANE), req(DustType.VITAL), req(DustType.WARP)),
            mvp(WardType.FERTILITY, WardTier.BASIC, 235,
                    req(DustType.ARCANE), req(DustType.BINDING), req(DustType.VITAL)),
            definition(
                    WardType.ECLIPSE,
                    WardTier.BASIC,
                    140,
                    List.of(req(DustType.ARCANE), req(DustType.BINDING), req(DustType.VEIL), req(DustType.FOCUS)),
                    () -> 0,
                    SelariumCommonConfig.ECLIPSE_WARD_MANA_COST_PER_ENTITY::get,
                    SelariumCommonConfig.ECLIPSE_WARD_DURATION_TICKS::get,
                    SelariumCommonConfig.ECLIPSE_WARD_COOLDOWN_TICKS::get,
                    SelariumCommonConfig.ECLIPSE_WARD_TICK_INTERVAL::get,
                    SelariumCommonConfig.ECLIPSE_WARD_RANGE::get,
                    true
            ),
            definition(
                    WardType.MAGNETISM,
                    WardTier.BASIC,
                    130,
                    List.of(req(DustType.ARCANE), req(DustType.FOCUS), req(DustType.DENSITY), req(DustType.BINDING)),
                    () -> 0,
                    SelariumCommonConfig.MAGNETISM_WARD_MANA_COST_PER_CYCLE::get,
                    SelariumCommonConfig.MAGNETISM_WARD_DURATION_TICKS::get,
                    SelariumCommonConfig.MAGNETISM_WARD_COOLDOWN_TICKS::get,
                    SelariumCommonConfig.MAGNETISM_WARD_TICK_INTERVAL::get,
                    SelariumCommonConfig.MAGNETISM_WARD_RANGE::get,
                    true
            ),
            definition(
                    WardType.BANISHMENT,
                    WardTier.BASIC,
                    120,
                    List.of(req(DustType.ARCANE), req(DustType.FOCUS), req(DustType.WARP)),
                    () -> 0,
                    SelariumCommonConfig.BANISHMENT_WARD_MANA_COST_PER_ENTITY::get,
                    SelariumCommonConfig.BANISHMENT_WARD_DURATION_TICKS::get,
                    SelariumCommonConfig.BANISHMENT_WARD_COOLDOWN_TICKS::get,
                    SelariumCommonConfig.BANISHMENT_WARD_TICK_INTERVAL::get,
                    SelariumCommonConfig.BANISHMENT_WARD_RANGE::get,
                    true
            ),
            definition(
                    WardType.GROUNDING,
                    WardTier.BASIC,
                    110,
                    List.of(req(DustType.ARCANE), req(DustType.FOCUS), req(DustType.DENSITY)),
                    () -> 0,
                    SelariumCommonConfig.GROUNDING_WARD_MANA_COST_PER_CYCLE::get,
                    SelariumCommonConfig.GROUNDING_WARD_DURATION_TICKS::get,
                    SelariumCommonConfig.GROUNDING_WARD_COOLDOWN_TICKS::get,
                    SelariumCommonConfig.GROUNDING_WARD_TICK_INTERVAL::get,
                    SelariumCommonConfig.GROUNDING_WARD_RANGE::get,
                    true
            ),
            definition(
                    WardType.FEATHERWEIGHT,
                    WardTier.BASIC,
                    100,
                    List.of(req(DustType.ARCANE), req(DustType.VITAL), req(DustType.DENSITY)),
                    () -> 0,
                    SelariumCommonConfig.FEATHERWEIGHT_WARD_MANA_COST_PER_CYCLE::get,
                    SelariumCommonConfig.FEATHERWEIGHT_WARD_DURATION_TICKS::get,
                    SelariumCommonConfig.FEATHERWEIGHT_WARD_COOLDOWN_TICKS::get,
                    SelariumCommonConfig.FEATHERWEIGHT_WARD_TICK_INTERVAL::get,
                    SelariumCommonConfig.FEATHERWEIGHT_WARD_RANGE::get,
                    true
            ),
            definition(
                    WardType.SPECTRAL,
                    WardTier.BASIC,
                    90,
                    List.of(req(DustType.ARCANE), req(DustType.ECHO), req(DustType.VEIL)),
                    () -> 0,
                    SelariumCommonConfig.SPECTRAL_WARD_MANA_COST_PER_ENTITY::get,
                    SelariumCommonConfig.SPECTRAL_WARD_DURATION_TICKS::get,
                    SelariumCommonConfig.SPECTRAL_WARD_COOLDOWN_TICKS::get,
                    SelariumCommonConfig.SPECTRAL_WARD_TICK_INTERVAL::get,
                    SelariumCommonConfig.SPECTRAL_WARD_RANGE::get,
                    true
            ),
            definition(
                    WardType.WHISPERING,
                    WardTier.BASIC,
                    80,
                    List.of(req(DustType.ARCANE), req(DustType.ECHO), req(DustType.FOCUS)),
                    () -> 0,
                    SelariumCommonConfig.WHISPERING_WARD_MANA_COST_PER_ALERT::get,
                    SelariumCommonConfig.WHISPERING_WARD_DURATION_TICKS::get,
                    SelariumCommonConfig.WHISPERING_WARD_COOLDOWN_TICKS::get,
                    SelariumCommonConfig.WHISPERING_WARD_TICK_INTERVAL::get,
                    SelariumCommonConfig.WHISPERING_WARD_RANGE::get,
                    true
            ),
            definition(
                    WardType.BULWARK,
                    WardTier.BASIC,
                    70,
                    List.of(req(DustType.ARCANE), req(DustType.AEGIS), req(DustType.FOCUS)),
                    () -> 0,
                    SelariumCommonConfig.BULWARK_WARD_MANA_COST_PER_CYCLE::get,
                    SelariumCommonConfig.BULWARK_WARD_DURATION_TICKS::get,
                    SelariumCommonConfig.BULWARK_WARD_COOLDOWN_TICKS::get,
                    SelariumCommonConfig.BULWARK_WARD_TICK_INTERVAL::get,
                    SelariumCommonConfig.BULWARK_WARD_RANGE::get,
                    true
            ),
            definition(
                    WardType.REJUVENATION,
                    WardTier.BASIC,
                    60,
                    List.of(req(DustType.ARCANE), req(DustType.VITAL, 2)),
                    () -> 0,
                    SelariumCommonConfig.REJUVENATION_WARD_MANA_COST_PER_CYCLE::get,
                    SelariumCommonConfig.REJUVENATION_WARD_DURATION_TICKS::get,
                    SelariumCommonConfig.REJUVENATION_WARD_COOLDOWN_TICKS::get,
                    SelariumCommonConfig.REJUVENATION_WARD_TICK_INTERVAL::get,
                    SelariumCommonConfig.REJUVENATION_WARD_RANGE::get,
                    true
            ),
            definition(
                    WardType.AMBIENT_MANA,
                    WardTier.BASIC,
                    50,
                    List.of(req(DustType.ARCANE), req(DustType.FOCUS, 2)),
                    SelariumCommonConfig.AMBIENT_WARD_ACTIVATION_COST::get,
                    () -> 0,
                    SelariumCommonConfig.AMBIENT_WARD_DURATION_TICKS::get,
                    SelariumCommonConfig.AMBIENT_WARD_COOLDOWN_TICKS::get,
                    SelariumCommonConfig.AMBIENT_WARD_TICK_INTERVAL::get,
                    SelariumCommonConfig.AMBIENT_WARD_RANGE::get,
                    true
            )
    );

    private WardDefinitions() {
    }

    public static Optional<WardDefinition> resolve(ArcaneSigilBlockEntity sigil) {
        return DEFINITIONS.stream()
                .filter(definition -> definition.requirements().stream().allMatch(requirement -> requirement.matches(sigil)))
                .max(Comparator.comparingInt(WardDefinition::priority));
    }

    public static Optional<WardDefinition> get(WardType type) {
        return DEFINITIONS.stream().filter(definition -> definition.type() == type).findFirst();
    }

    public static List<WardDefinition> all() {
        return DEFINITIONS;
    }

    private static WardDefinition mvp(WardType type, WardTier tier, int priority, WardRequirement... requirements) {
        SelariumCommonConfig.MvpWardConfig config = SelariumCommonConfig.mvpWard(type);
        return definition(
                type,
                tier,
                priority,
                List.of(requirements),
                () -> 0,
                config.manaCost()::get,
                config.durationTicks()::get,
                config.cooldownTicks()::get,
                config.tickInterval()::get,
                config.range()::get,
                true
        );
    }

    private static WardDefinition definition(WardType type, WardTier tier, int priority, List<WardRequirement> requirements,
                                             java.util.function.IntSupplier activationCostSupplier,
                                             java.util.function.IntSupplier upkeepCostSupplier,
                                             java.util.function.IntSupplier durationTicksSupplier,
                                             java.util.function.IntSupplier cooldownTicksSupplier,
                                             java.util.function.IntSupplier tickIntervalSupplier,
                                             java.util.function.IntSupplier rangeSupplier,
                                             boolean temporary) {
        List<WardRequirement> effectiveRequirements = tier == WardTier.REFINED
                ? requirements.stream().map(requirement -> requirement.type() == DustType.ARCANE
                        ? requirement
                        : new WardRequirement(requirement.type(), DustPurity.REFINED, requirement.count())).toList()
                : requirements;
        return new WardDefinition(type, tier, priority, effectiveRequirements, activationCostSupplier, upkeepCostSupplier, durationTicksSupplier, cooldownTicksSupplier, tickIntervalSupplier, rangeSupplier, temporary);
    }

    private static WardRequirement req(DustType type) {
        return req(type, 1);
    }

    private static WardRequirement req(DustType type, int count) {
        return new WardRequirement(type, DustPurity.BASIC, count);
    }
}
