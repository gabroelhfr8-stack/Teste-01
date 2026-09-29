package com.seleris.selarium.config;

import com.seleris.selarium.ward.WardType;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.EnumMap;
import java.util.Map;

public final class SelariumCommonConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue INITIAL_MAX_MANA;
    public static final ForgeConfigSpec.IntValue INITIAL_CURRENT_MANA;
    public static final ForgeConfigSpec.IntValue BASE_MANA_REGEN_AMOUNT;
    public static final ForgeConfigSpec.IntValue MANA_REGEN_INTERVAL_TICKS;
    public static final ForgeConfigSpec.BooleanValue MANA_GROWTH_ENABLED;
    public static final ForgeConfigSpec.DoubleValue MANA_EXPERIENCE_PER_REGENERATED_MANA;
    public static final ForgeConfigSpec.DoubleValue MANA_EXPERIENCE_REQUIRED_PER_MAX_MANA;
    public static final ForgeConfigSpec.IntValue MAX_MANA_GAIN_PER_GROWTH_STEP;
    public static final ForgeConfigSpec.IntValue MANA_GROWTH_CREDIT_BATCH_SIZE;
    public static final ForgeConfigSpec.IntValue SOFT_CAP_TIER_0;
    public static final ForgeConfigSpec.IntValue SOFT_CAP_TIER_1;
    public static final ForgeConfigSpec.IntValue SOFT_CAP_TIER_2;
    public static final ForgeConfigSpec.IntValue SOFT_CAP_TIER_3;
    public static final ForgeConfigSpec.IntValue SOFT_CAP_TIER_4;

    public static final ForgeConfigSpec.BooleanValue AMBIENT_WARD_ENABLED;
    public static final ForgeConfigSpec.IntValue AMBIENT_WARD_TICK_INTERVAL;
    public static final ForgeConfigSpec.IntValue AMBIENT_WARD_MANA_GENERATED_PER_CYCLE;
    public static final ForgeConfigSpec.IntValue AMBIENT_WARD_MAX_INTERNAL_BUFFER;
    public static final ForgeConfigSpec.IntValue AMBIENT_WARD_TRANSFER_TO_TANK_PER_CYCLE;
    public static final ForgeConfigSpec.IntValue AMBIENT_WARD_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue AMBIENT_WARD_MAX_MANA_GENERATED_PER_ACTIVATION;
    public static final ForgeConfigSpec.IntValue AMBIENT_WARD_ACTIVATION_COST;
    public static final ForgeConfigSpec.BooleanValue AMBIENT_WARD_CONSUMES_DUST_ON_ACTIVATION;
    public static final ForgeConfigSpec.ConfigValue<String> AMBIENT_WARD_REQUIRED_DUST_FOR_ACTIVATION;
    public static final ForgeConfigSpec.IntValue AMBIENT_WARD_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.BooleanValue AMBIENT_WARD_EXPIRE_WHEN_BUFFER_FULL;
    public static final ForgeConfigSpec.IntValue AMBIENT_WARD_RANGE;

    public static final ForgeConfigSpec.BooleanValue WARDS_CAN_USE_OWNER_MANA;
    public static final ForgeConfigSpec.BooleanValue WARDS_DEACTIVATE_WHEN_UPKEEP_FAILS;
    public static final ForgeConfigSpec.IntValue WARD_GLOBAL_HARD_ENTITY_CAP;
    public static final ForgeConfigSpec.IntValue WARD_GLOBAL_HARD_BLOCK_CAP;
    public static final ForgeConfigSpec.IntValue WHISPERING_WARD_RANGE;
    public static final ForgeConfigSpec.IntValue WHISPERING_WARD_TICK_INTERVAL;
    public static final ForgeConfigSpec.IntValue WHISPERING_WARD_ALERT_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.IntValue WHISPERING_WARD_MANA_COST_PER_ALERT;
    public static final ForgeConfigSpec.BooleanValue WHISPERING_WARD_DETECT_HOSTILE_MOBS;
    public static final ForgeConfigSpec.BooleanValue WHISPERING_WARD_DETECT_PLAYERS;
    public static final ForgeConfigSpec.IntValue WHISPERING_WARD_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue WHISPERING_WARD_COOLDOWN_TICKS;

    public static final ForgeConfigSpec.IntValue SPECTRAL_WARD_RANGE;
    public static final ForgeConfigSpec.IntValue SPECTRAL_WARD_TICK_INTERVAL;
    public static final ForgeConfigSpec.IntValue SPECTRAL_WARD_GLOWING_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue SPECTRAL_WARD_MANA_COST_PER_ENTITY;
    public static final ForgeConfigSpec.IntValue SPECTRAL_WARD_MAX_ENTITIES_PER_CYCLE;
    public static final ForgeConfigSpec.BooleanValue SPECTRAL_WARD_AFFECT_PLAYERS;
    public static final ForgeConfigSpec.IntValue SPECTRAL_WARD_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue SPECTRAL_WARD_COOLDOWN_TICKS;

    public static final ForgeConfigSpec.IntValue BULWARK_WARD_RANGE;
    public static final ForgeConfigSpec.IntValue BULWARK_WARD_TICK_INTERVAL;
    public static final ForgeConfigSpec.IntValue BULWARK_WARD_EFFECT_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue BULWARK_WARD_AMPLIFIER;
    public static final ForgeConfigSpec.IntValue BULWARK_WARD_MANA_COST_PER_CYCLE;
    public static final ForgeConfigSpec.IntValue BULWARK_WARD_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue BULWARK_WARD_COOLDOWN_TICKS;

    public static final ForgeConfigSpec.IntValue REJUVENATION_WARD_RANGE;
    public static final ForgeConfigSpec.IntValue REJUVENATION_WARD_TICK_INTERVAL;
    public static final ForgeConfigSpec.IntValue REJUVENATION_WARD_EFFECT_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue REJUVENATION_WARD_AMPLIFIER;
    public static final ForgeConfigSpec.IntValue REJUVENATION_WARD_MANA_COST_PER_CYCLE;
    public static final ForgeConfigSpec.IntValue REJUVENATION_WARD_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue REJUVENATION_WARD_COOLDOWN_TICKS;

    public static final ForgeConfigSpec.BooleanValue FEATHERWEIGHT_WARD_ENABLED;
    public static final ForgeConfigSpec.IntValue FEATHERWEIGHT_WARD_RANGE;
    public static final ForgeConfigSpec.IntValue FEATHERWEIGHT_WARD_TICK_INTERVAL;
    public static final ForgeConfigSpec.IntValue FEATHERWEIGHT_WARD_EFFECT_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue FEATHERWEIGHT_WARD_MANA_COST_PER_CYCLE;
    public static final ForgeConfigSpec.BooleanValue FEATHERWEIGHT_WARD_RESET_FALL_DISTANCE;
    public static final ForgeConfigSpec.IntValue FEATHERWEIGHT_WARD_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue FEATHERWEIGHT_WARD_COOLDOWN_TICKS;

    public static final ForgeConfigSpec.BooleanValue GROUNDING_WARD_ENABLED;
    public static final ForgeConfigSpec.IntValue GROUNDING_WARD_RANGE;
    public static final ForgeConfigSpec.IntValue GROUNDING_WARD_TICK_INTERVAL;
    public static final ForgeConfigSpec.IntValue GROUNDING_WARD_MANA_COST_PER_CYCLE;
    public static final ForgeConfigSpec.BooleanValue GROUNDING_WARD_REMOVE_LEVITATION;
    public static final ForgeConfigSpec.BooleanValue GROUNDING_WARD_RESET_FALL_DISTANCE;
    public static final ForgeConfigSpec.BooleanValue GROUNDING_WARD_REDUCE_VERTICAL_KNOCKBACK;
    public static final ForgeConfigSpec.IntValue GROUNDING_WARD_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue GROUNDING_WARD_COOLDOWN_TICKS;

    public static final ForgeConfigSpec.BooleanValue MAGNETISM_WARD_ENABLED;
    public static final ForgeConfigSpec.IntValue MAGNETISM_WARD_RANGE;
    public static final ForgeConfigSpec.IntValue MAGNETISM_WARD_TICK_INTERVAL;
    public static final ForgeConfigSpec.IntValue MAGNETISM_WARD_MANA_COST_PER_CYCLE;
    public static final ForgeConfigSpec.DoubleValue MAGNETISM_WARD_PULL_STRENGTH;
    public static final ForgeConfigSpec.IntValue MAGNETISM_WARD_MAX_ITEMS_PER_CYCLE;
    public static final ForgeConfigSpec.DoubleValue MAGNETISM_WARD_STOP_DISTANCE;
    public static final ForgeConfigSpec.IntValue MAGNETISM_WARD_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue MAGNETISM_WARD_COOLDOWN_TICKS;

    public static final ForgeConfigSpec.BooleanValue BANISHMENT_WARD_ENABLED;
    public static final ForgeConfigSpec.IntValue BANISHMENT_WARD_RANGE;
    public static final ForgeConfigSpec.IntValue BANISHMENT_WARD_TICK_INTERVAL;
    public static final ForgeConfigSpec.IntValue BANISHMENT_WARD_MANA_COST_PER_ENTITY;
    public static final ForgeConfigSpec.IntValue BANISHMENT_WARD_TELEPORT_DISTANCE;
    public static final ForgeConfigSpec.IntValue BANISHMENT_WARD_MAX_ENTITIES_PER_CYCLE;
    public static final ForgeConfigSpec.BooleanValue BANISHMENT_WARD_AFFECT_PLAYERS;
    public static final ForgeConfigSpec.BooleanValue BANISHMENT_WARD_AFFECT_BOSSES;
    public static final ForgeConfigSpec.IntValue BANISHMENT_WARD_COOLDOWN_PER_ENTITY_TICKS;
    public static final ForgeConfigSpec.IntValue BANISHMENT_WARD_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue BANISHMENT_WARD_COOLDOWN_TICKS;

    public static final ForgeConfigSpec.BooleanValue ECLIPSE_WARD_ENABLED;
    public static final ForgeConfigSpec.IntValue ECLIPSE_WARD_RANGE;
    public static final ForgeConfigSpec.IntValue ECLIPSE_WARD_TICK_INTERVAL;
    public static final ForgeConfigSpec.IntValue ECLIPSE_WARD_EFFECT_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue ECLIPSE_WARD_MANA_COST_PER_ENTITY;
    public static final ForgeConfigSpec.IntValue ECLIPSE_WARD_MAX_ENTITIES_PER_CYCLE;
    public static final ForgeConfigSpec.BooleanValue ECLIPSE_WARD_AFFECT_PLAYERS;
    public static final ForgeConfigSpec.BooleanValue ECLIPSE_WARD_USE_DARKNESS_INSTEAD_OF_BLINDNESS;
    public static final ForgeConfigSpec.BooleanValue ECLIPSE_WARD_CLEAR_MOB_TARGET;
    public static final ForgeConfigSpec.IntValue ECLIPSE_WARD_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue ECLIPSE_WARD_COOLDOWN_TICKS;

    public static final Map<WardType, MvpWardConfig> MVP_WARD_CONFIGS;

    public static final ForgeConfigSpec.IntValue SIGIL_MAX_COMPONENTS;
    public static final ForgeConfigSpec.IntValue MANA_TANK_CAPACITY;
    public static final ForgeConfigSpec.BooleanValue ARCANE_GRINDER_ENABLED;
    public static final ForgeConfigSpec.IntValue ARCANE_GRINDER_DEFAULT_PROCESSING_TIME;
    public static final ForgeConfigSpec.BooleanValue ARCANE_GRINDER_ALLOW_AUTOMATION;
    public static final ForgeConfigSpec.BooleanValue ARCANE_GRINDER_DROP_INVENTORY_ON_BREAK;
    public static final ForgeConfigSpec.IntValue ARCANE_CRYSTAL_TO_DUST_OUTPUT_COUNT;
    public static final ForgeConfigSpec.BooleanValue ARCANE_GEODES_ENABLED;
    public static final ForgeConfigSpec.IntValue ARCANE_GEODE_CHANCE;
    public static final ForgeConfigSpec.IntValue ARCANE_GEODE_MIN_Y;
    public static final ForgeConfigSpec.IntValue ARCANE_GEODE_MAX_Y;
    public static final ForgeConfigSpec.IntValue ARCANE_GEODE_MIN_SIZE;
    public static final ForgeConfigSpec.IntValue ARCANE_GEODE_MAX_SIZE;
    public static final ForgeConfigSpec.IntValue ARCANE_GEODE_CLUSTER_CHANCE;
    public static final ForgeConfigSpec.IntValue ARCANE_GEODE_BUDDING_CHANCE;
    public static final ForgeConfigSpec.BooleanValue ARCANE_TREE_ABOVE_GEODE_ENABLED;
    public static final ForgeConfigSpec.DoubleValue ARCANE_TREE_ABOVE_GEODE_CHANCE;
    public static final ForgeConfigSpec.BooleanValue ARCANE_TREE_ROOT_COLUMN_ENABLED;
    public static final ForgeConfigSpec.IntValue ARCANE_TREE_ROOT_COLUMN_MAX_LENGTH;
    public static final ForgeConfigSpec.IntValue ARCANE_TREE_MIN_SURFACE_Y;
    public static final ForgeConfigSpec.IntValue ARCANE_TREE_MAX_SURFACE_Y;
    public static final ForgeConfigSpec.BooleanValue ARCANE_TREE_REQUIRE_SKY_LIGHT;
    public static final ForgeConfigSpec.IntValue ARCANE_TREE_MIN_TRUNK_HEIGHT;
    public static final ForgeConfigSpec.IntValue ARCANE_TREE_MAX_TRUNK_HEIGHT;
    public static final ForgeConfigSpec.DoubleValue ARCANE_TREE_TALL_VARIANT_CHANCE;
    public static final ForgeConfigSpec.IntValue ARCANE_TREE_STANDARD_VARIANT_WEIGHT;
    public static final ForgeConfigSpec.IntValue ARCANE_TREE_TALL_VARIANT_WEIGHT;
    public static final ForgeConfigSpec.IntValue ARCANE_TREE_SPREADING_VARIANT_WEIGHT;
    public static final ForgeConfigSpec.IntValue ARCANE_TREE_ANCIENT_VARIANT_WEIGHT;
    public static final ForgeConfigSpec.BooleanValue ARCANE_TREE_WORLDGEN_DEBUG;
    public static final ForgeConfigSpec.BooleanValue ARCANE_TREE_GENERATE_INITIAL_PETALS;
    public static final ForgeConfigSpec.IntValue ARCANE_TREE_INITIAL_PETAL_ATTEMPTS;
    public static final ForgeConfigSpec.BooleanValue ARCANE_PETALS_ENABLED;
    public static final ForgeConfigSpec.DoubleValue ARCANE_PETALS_GENERATION_CHANCE;
    public static final ForgeConfigSpec.IntValue ARCANE_PETALS_MAX_NEARBY;
    public static final ForgeConfigSpec.IntValue ARCANE_PETALS_SEARCH_RADIUS;
    public static final ForgeConfigSpec.IntValue ARCANE_PETALS_DROP_ARCANE_DUST_COUNT;
    public static final ForgeConfigSpec.BooleanValue ARCANE_PETALS_REQUIRE_ARCANE_LEAVES_NEARBY;
    public static final ForgeConfigSpec.BooleanValue ARCANE_CRYSTAL_GROWTH_ENABLED;
    public static final ForgeConfigSpec.IntValue ARCANE_CRYSTAL_GROWTH_CHANCE;
    public static final ForgeConfigSpec.BooleanValue ARCANE_CRYSTAL_RANDOM_TICK_ENABLED;
    public static final ForgeConfigSpec.BooleanValue ARCANE_CRYSTAL_SHIMMER_SOUND_ENABLED;
    public static final ForgeConfigSpec.IntValue ARCANE_CRYSTAL_SHIMMER_SOUND_CHANCE;
    public static final ForgeConfigSpec.BooleanValue SIGIL_ANIMATIONS_ENABLED;
    public static final ForgeConfigSpec.BooleanValue SIGIL_FLOATING_WHEN_ACTIVE;
    public static final ForgeConfigSpec.BooleanValue SIGIL_ACTIVE_BOBBING_ENABLED;
    public static final ForgeConfigSpec.BooleanValue SIGIL_ENABLE_PARTICLES;
    public static final ForgeConfigSpec.IntValue SIGIL_ACTIVE_PARTICLE_INTERVAL;
    public static final ForgeConfigSpec.IntValue SIGIL_ACTIVATION_TRANSITION_TICKS;
    public static final ForgeConfigSpec.IntValue SIGIL_DEACTIVATION_TRANSITION_TICKS;
    public static final ForgeConfigSpec.IntValue SIGIL_ACTIVATION_PARTICLE_BURST;
    public static final ForgeConfigSpec.IntValue SIGIL_DEACTIVATION_PARTICLE_BURST;
    public static final ForgeConfigSpec.BooleanValue SIGIL_COMPONENT_LAYERS_ENABLED;
    public static final ForgeConfigSpec.IntValue SIGIL_MAX_VISIBLE_COMPONENT_LAYERS;
    public static final ForgeConfigSpec.IntValue SIGIL_COMPONENT_LAYER_ALPHA;
    public static final ForgeConfigSpec.IntValue SIGIL_EXTRA_COMPONENT_LAYER_ALPHA;
    public static final ForgeConfigSpec.IntValue SIGIL_WARD_LAYER_ALPHA;
    public static final ForgeConfigSpec.DoubleValue SIGIL_INACTIVE_COMPONENT_ROTATION_SPEED;
    public static final ForgeConfigSpec.DoubleValue SIGIL_COMPONENT_LAYER_ROTATION_SPEED;
    public static final ForgeConfigSpec.DoubleValue SIGIL_WARD_LAYER_ROTATION_SPEED;
    public static final ForgeConfigSpec.DoubleValue SIGIL_ACTIVE_PRIMARY_ROTATION_SPEED;
    public static final ForgeConfigSpec.DoubleValue SIGIL_ACTIVE_SECONDARY_ROTATION_SPEED;
    public static final ForgeConfigSpec.DoubleValue SIGIL_ACTIVE_FLOAT_HEIGHT;
    public static final ForgeConfigSpec.DoubleValue SIGIL_ACTIVE_BOBBING_AMPLITUDE;
    public static final ForgeConfigSpec.DoubleValue SIGIL_ACTIVE_BOBBING_SPEED;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MANA_HUD;
    public static final ForgeConfigSpec.BooleanValue MANA_HUD_SHOW_NUMBERS;
    public static final ForgeConfigSpec.IntValue MANA_HUD_X;
    public static final ForgeConfigSpec.IntValue MANA_HUD_Y;
    public static final ForgeConfigSpec.BooleanValue MANA_HUD_SHOW_GROWTH_FLASH;
    public static final ForgeConfigSpec.BooleanValue DEBUG_COMMANDS_ENABLED;
    public static final ForgeConfigSpec.BooleanValue CODEX_ENABLED;
    public static final ForgeConfigSpec.BooleanValue WARDING_GRIMOIRE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue WARDING_GRIMOIRE_OWNER_LOCKED;
    public static final ForgeConfigSpec.BooleanValue WARDING_GRIMOIRE_PLAYERS_AFFECT_REQUIRES_WARD_CONFIG;
    public static final ForgeConfigSpec.BooleanValue WARDING_GRIMOIRE_BOSSES_AFFECT_REQUIRES_WARD_CONFIG;
    public static final ForgeConfigSpec.BooleanValue WARDING_GRIMOIRE_DEFAULT_AFFECT_HOSTILES;
    public static final ForgeConfigSpec.BooleanValue WARDING_GRIMOIRE_DEFAULT_AFFECT_PASSIVE_MOBS;
    public static final ForgeConfigSpec.BooleanValue WARDING_GRIMOIRE_DEFAULT_AFFECT_PLAYERS;
    public static final ForgeConfigSpec.BooleanValue WARDING_GRIMOIRE_DEFAULT_IGNORE_PETS;
    public static final ForgeConfigSpec.BooleanValue WARDING_GRIMOIRE_DEFAULT_IGNORE_NAMED_ENTITIES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        EnumMap<WardType, MvpWardConfig> mvpWardConfigs = new EnumMap<>(WardType.class);

        builder.push("mana");
        INITIAL_MAX_MANA = builder.comment("Initial maximum mana for new players. It is clamped to the unlocked tier soft cap, so the default tier 0 starts and caps at 500.")
                .defineInRange("initialMaxMana", 500, 1, 1_000_000);
        INITIAL_CURRENT_MANA = builder.comment("Initial current mana for new players. It is clamped between 0 and max mana.")
                .defineInRange("initialCurrentMana", 500, 0, 1_000_000);
        BASE_MANA_REGEN_AMOUNT = builder.comment("Mana restored each regeneration cycle.")
                .defineInRange("baseManaRegenAmount", 1, 0, 10_000);
        MANA_REGEN_INTERVAL_TICKS = builder.comment("Ticks between player mana regeneration cycles. 20 ticks is roughly 1 second.")
                .defineInRange("manaRegenIntervalTicks", 40, 1, 20 * 60 * 60);
        MANA_GROWTH_ENABLED = builder.comment("If true, max mana can grow from a real spend and regeneration cycle.")
                .define("manaGrowthEnabled", true);
        MANA_EXPERIENCE_PER_REGENERATED_MANA = builder.comment("Mana experience earned per eligible regenerated mana after a spend/regeneration cycle.")
                .defineInRange("manaExperiencePerRegeneratedMana", 1.0D, 0.0D, 1_000_000.0D);
        MANA_EXPERIENCE_REQUIRED_PER_MAX_MANA = builder.comment("Mana experience consumed for each max mana growth step.")
                .defineInRange("manaExperienceRequiredPerMaxMana", 25.0D, 0.001D, 1_000_000.0D);
        MAX_MANA_GAIN_PER_GROWTH_STEP = builder.comment("Maximum max mana gained each time enough mana experience is processed.")
                .defineInRange("maxManaGainPerGrowthStep", 1, 1, 10_000);
        MANA_GROWTH_CREDIT_BATCH_SIZE = builder.comment("Eligible regenerated mana must accumulate in batches before awarding growth XP. This keeps tiny spend/regenerate loops from crediting every single tick.")
                .defineInRange("manaGrowthCreditBatchSize", 25, 1, 10_000);
        SOFT_CAP_TIER_0 = builder.comment("Maximum max mana while unlockedManaTier is 0. Default equals initial max mana, so tier 0 has no passive room to grow.")
                .defineInRange("softCapTier0", 500, 1, 1_000_000);
        SOFT_CAP_TIER_1 = builder.comment("Maximum max mana while unlockedManaTier is 1.")
                .defineInRange("softCapTier1", 1000, 1, 1_000_000);
        SOFT_CAP_TIER_2 = builder.comment("Maximum max mana while unlockedManaTier is 2.")
                .defineInRange("softCapTier2", 2500, 1, 1_000_000);
        SOFT_CAP_TIER_3 = builder.comment("Maximum max mana while unlockedManaTier is 3.")
                .defineInRange("softCapTier3", 5000, 1, 1_000_000);
        SOFT_CAP_TIER_4 = builder.comment("Maximum max mana while unlockedManaTier is 4.")
                .defineInRange("softCapTier4", 10000, 1, 1_000_000);
        builder.pop();

        builder.push("wards");
        AMBIENT_WARD_ENABLED = builder.comment("Master switch for the MVP Ambient Mana Ward effect.")
                .define("ambientWardEnabled", true);
        AMBIENT_WARD_TICK_INTERVAL = builder.comment("Ticks between Ambient Mana Ward cycles. The ward only does meaningful work on this interval.")
                .defineInRange("ambientWardTickInterval", 120, 1, 20 * 60 * 60);
        AMBIENT_WARD_MANA_GENERATED_PER_CYCLE = builder.comment("Mana generated into the sigil's internal buffer per Ambient Mana Ward cycle.")
                .defineInRange("ambientWardManaGeneratedPerCycle", 5, 0, 100_000);
        AMBIENT_WARD_MAX_INTERNAL_BUFFER = builder.comment("Maximum mana the sigil can keep internally.")
                .defineInRange("ambientWardMaxInternalBuffer", 1000, 0, 1_000_000);
        AMBIENT_WARD_TRANSFER_TO_TANK_PER_CYCLE = builder.comment("Maximum mana transferred from a sigil to adjacent Mana Tanks each ward cycle.")
                .defineInRange("ambientWardTransferToTankPerCycle", 20, 0, 100_000);
        AMBIENT_WARD_DURATION_TICKS = builder.comment("Duration of a single Ambient Mana Ward activation.")
                .defineInRange("ambientWardDurationTicks", 6000, 1, 20 * 60 * 60);
        AMBIENT_WARD_MAX_MANA_GENERATED_PER_ACTIVATION = builder.comment("Maximum mana generated by one Ambient Mana Ward activation.")
                .defineInRange("ambientWardMaxManaGeneratedPerActivation", 250, 0, 1_000_000);
        AMBIENT_WARD_ACTIVATION_COST = builder.comment("Initial mana cost paid when activating Ambient Mana Ward.")
                .defineInRange("ambientWardActivationCost", 25, 0, 100_000);
        AMBIENT_WARD_CONSUMES_DUST_ON_ACTIVATION = builder.comment("If true, Ambient Mana Ward consumes one configured dust component from the sigil on activation.")
                .define("ambientWardConsumesDustOnActivation", false);
        AMBIENT_WARD_REQUIRED_DUST_FOR_ACTIVATION = builder.comment("Dust type consumed when ambientWardConsumesDustOnActivation is true.")
                .define("ambientWardRequiredDustForActivation", "focus");
        AMBIENT_WARD_COOLDOWN_TICKS = builder.comment("Cooldown after Ambient Mana Ward expires automatically.")
                .defineInRange("ambientWardCooldownTicks", 1800, 0, 20 * 60 * 60);
        AMBIENT_WARD_EXPIRE_WHEN_BUFFER_FULL = builder.comment("If true, Ambient Mana Ward expires when its internal buffer is full and no generated mana can fit.")
                .define("ambientWardExpireWhenBufferFull", true);
        AMBIENT_WARD_RANGE = builder.comment("Resolver/runtime range for Ambient Mana Ward. MVP transfer still only checks adjacent tanks.")
                .defineInRange("ambientWardRange", 1, 1, 64);

        WARDS_CAN_USE_OWNER_MANA = builder.comment("If true, wards can use the online owner's player mana after sigil buffer and adjacent tanks.")
                .define("wardsCanUseOwnerMana", true);
        WARDS_DEACTIVATE_WHEN_UPKEEP_FAILS = builder.comment("If true, an active Ward deactivates when it cannot pay its continuous upkeep. If false, it stays active but inert until mana is available.")
                .define("wardsDeactivateWhenUpkeepFails", false);
        WARD_GLOBAL_HARD_ENTITY_CAP = builder.comment("Global safety cap for entities affected by one Ward field cycle. This is a performance guard, not a balance cap.")
                .defineInRange("wardGlobalHardEntityCap", 128, 1, 1024);
        WARD_GLOBAL_HARD_BLOCK_CAP = builder.comment("Global safety cap for blocks scanned/changed by one Ward field cycle. This is a performance guard, not a balance cap.")
                .defineInRange("wardGlobalHardBlockCap", 4096, 1, 65536);

        WHISPERING_WARD_RANGE = builder.defineInRange("whisperingWardRange", 12, 1, 64);
        WHISPERING_WARD_TICK_INTERVAL = builder.defineInRange("whisperingWardTickInterval", 40, 1, 20 * 60 * 60);
        WHISPERING_WARD_ALERT_COOLDOWN_TICKS = builder.defineInRange("whisperingWardAlertCooldownTicks", 200, 0, 20 * 60 * 60);
        WHISPERING_WARD_MANA_COST_PER_ALERT = builder.defineInRange("whisperingWardManaCostPerAlert", 5, 0, 100_000);
        WHISPERING_WARD_DETECT_HOSTILE_MOBS = builder.define("whisperingWardDetectHostileMobs", true);
        WHISPERING_WARD_DETECT_PLAYERS = builder.define("whisperingWardDetectPlayers", false);
        WHISPERING_WARD_DURATION_TICKS = builder.defineInRange("whisperingWardDurationTicks", 6000, 1, 20 * 60 * 60);
        WHISPERING_WARD_COOLDOWN_TICKS = builder.defineInRange("whisperingWardCooldownTicks", 600, 0, 20 * 60 * 60);

        SPECTRAL_WARD_RANGE = builder.defineInRange("spectralWardRange", 10, 1, 64);
        SPECTRAL_WARD_TICK_INTERVAL = builder.defineInRange("spectralWardTickInterval", 80, 1, 20 * 60 * 60);
        SPECTRAL_WARD_GLOWING_DURATION_TICKS = builder.defineInRange("spectralWardGlowingDurationTicks", 120, 1, 20 * 60 * 60);
        SPECTRAL_WARD_MANA_COST_PER_ENTITY = builder.defineInRange("spectralWardManaCostPerEntity", 6, 0, 100_000);
        SPECTRAL_WARD_MAX_ENTITIES_PER_CYCLE = builder.comment("Maximum invaders the Spectral Ward can affect per cycle. High default makes it behave like a field while still protecting the server.")
                .defineInRange("spectralWardMaxEntitiesPerCycle", 64, 1, 128);
        SPECTRAL_WARD_AFFECT_PLAYERS = builder.define("spectralWardAffectPlayers", false);
        SPECTRAL_WARD_DURATION_TICKS = builder.defineInRange("spectralWardDurationTicks", 6000, 1, 20 * 60 * 60);
        SPECTRAL_WARD_COOLDOWN_TICKS = builder.defineInRange("spectralWardCooldownTicks", 600, 0, 20 * 60 * 60);

        BULWARK_WARD_RANGE = builder.defineInRange("bulwarkWardRange", 8, 1, 64);
        BULWARK_WARD_TICK_INTERVAL = builder.defineInRange("bulwarkWardTickInterval", 80, 1, 20 * 60 * 60);
        BULWARK_WARD_EFFECT_DURATION_TICKS = builder.defineInRange("bulwarkWardEffectDurationTicks", 120, 1, 20 * 60 * 60);
        BULWARK_WARD_AMPLIFIER = builder.defineInRange("bulwarkWardAmplifier", 0, 0, 4);
        BULWARK_WARD_MANA_COST_PER_CYCLE = builder.defineInRange("bulwarkWardManaCostPerCycle", 8, 0, 100_000);
        BULWARK_WARD_DURATION_TICKS = builder.defineInRange("bulwarkWardDurationTicks", 6000, 1, 20 * 60 * 60);
        BULWARK_WARD_COOLDOWN_TICKS = builder.defineInRange("bulwarkWardCooldownTicks", 600, 0, 20 * 60 * 60);

        REJUVENATION_WARD_RANGE = builder.defineInRange("rejuvenationWardRange", 8, 1, 64);
        REJUVENATION_WARD_TICK_INTERVAL = builder.defineInRange("rejuvenationWardTickInterval", 100, 1, 20 * 60 * 60);
        REJUVENATION_WARD_EFFECT_DURATION_TICKS = builder.defineInRange("rejuvenationWardEffectDurationTicks", 120, 1, 20 * 60 * 60);
        REJUVENATION_WARD_AMPLIFIER = builder.defineInRange("rejuvenationWardAmplifier", 0, 0, 4);
        REJUVENATION_WARD_MANA_COST_PER_CYCLE = builder.defineInRange("rejuvenationWardManaCostPerCycle", 10, 0, 100_000);
        REJUVENATION_WARD_DURATION_TICKS = builder.defineInRange("rejuvenationWardDurationTicks", 6000, 1, 20 * 60 * 60);
        REJUVENATION_WARD_COOLDOWN_TICKS = builder.defineInRange("rejuvenationWardCooldownTicks", 600, 0, 20 * 60 * 60);

        FEATHERWEIGHT_WARD_ENABLED = builder.comment("If true, Featherweight Ward can be activated and apply Slow Falling to the sigil owner.")
                .define("featherweightWardEnabled", true);
        FEATHERWEIGHT_WARD_RANGE = builder.defineInRange("featherweightWardRange", 8, 1, 64);
        FEATHERWEIGHT_WARD_TICK_INTERVAL = builder.defineInRange("featherweightWardTickInterval", 40, 1, 20 * 60 * 60);
        FEATHERWEIGHT_WARD_EFFECT_DURATION_TICKS = builder.defineInRange("featherweightWardEffectDurationTicks", 100, 1, 20 * 60 * 60);
        FEATHERWEIGHT_WARD_MANA_COST_PER_CYCLE = builder.defineInRange("featherweightWardManaCostPerCycle", 6, 0, 100_000);
        FEATHERWEIGHT_WARD_RESET_FALL_DISTANCE = builder.comment("If true, Featherweight Ward clears the owner's accumulated fall distance when it applies.")
                .define("featherweightWardResetFallDistance", true);
        FEATHERWEIGHT_WARD_DURATION_TICKS = builder.defineInRange("featherweightWardDurationTicks", 6000, 1, 20 * 60 * 60);
        FEATHERWEIGHT_WARD_COOLDOWN_TICKS = builder.defineInRange("featherweightWardCooldownTicks", 600, 0, 20 * 60 * 60);

        GROUNDING_WARD_ENABLED = builder.comment("If true, Grounding Ward can remove Levitation and stabilize the sigil owner.")
                .define("groundingWardEnabled", true);
        GROUNDING_WARD_RANGE = builder.defineInRange("groundingWardRange", 8, 1, 64);
        GROUNDING_WARD_TICK_INTERVAL = builder.defineInRange("groundingWardTickInterval", 30, 1, 20 * 60 * 60);
        GROUNDING_WARD_MANA_COST_PER_CYCLE = builder.defineInRange("groundingWardManaCostPerCycle", 6, 0, 100_000);
        GROUNDING_WARD_REMOVE_LEVITATION = builder.define("groundingWardRemoveLevitation", true);
        GROUNDING_WARD_RESET_FALL_DISTANCE = builder.define("groundingWardResetFallDistance", true);
        GROUNDING_WARD_REDUCE_VERTICAL_KNOCKBACK = builder.comment("If true, Grounding Ward dampens upward velocity on the owner during its interval. This is intentionally conservative and not a full knockback hook.")
                .define("groundingWardReduceVerticalKnockback", true);
        GROUNDING_WARD_DURATION_TICKS = builder.defineInRange("groundingWardDurationTicks", 6000, 1, 20 * 60 * 60);
        GROUNDING_WARD_COOLDOWN_TICKS = builder.defineInRange("groundingWardCooldownTicks", 600, 0, 20 * 60 * 60);

        MAGNETISM_WARD_ENABLED = builder.comment("If true, Magnetism Ward can pull nearby dropped items toward the sigil center.")
                .define("magnetismWardEnabled", true);
        MAGNETISM_WARD_RANGE = builder.defineInRange("magnetismWardRange", 10, 1, 32);
        MAGNETISM_WARD_TICK_INTERVAL = builder.defineInRange("magnetismWardTickInterval", 20, 1, 20 * 60 * 60);
        MAGNETISM_WARD_MANA_COST_PER_CYCLE = builder.defineInRange("magnetismWardManaCostPerCycle", 5, 0, 100_000);
        MAGNETISM_WARD_PULL_STRENGTH = builder.defineInRange("magnetismWardPullStrength", 0.08D, 0.0D, 2.0D);
        MAGNETISM_WARD_MAX_ITEMS_PER_CYCLE = builder.defineInRange("magnetismWardMaxItemsPerCycle", 64, 1, 256);
        MAGNETISM_WARD_STOP_DISTANCE = builder.defineInRange("magnetismWardStopDistance", 0.75D, 0.05D, 8.0D);
        MAGNETISM_WARD_DURATION_TICKS = builder.defineInRange("magnetismWardDurationTicks", 6000, 1, 20 * 60 * 60);
        MAGNETISM_WARD_COOLDOWN_TICKS = builder.defineInRange("magnetismWardCooldownTicks", 600, 0, 20 * 60 * 60);

        BANISHMENT_WARD_ENABLED = builder.comment("If true, Banishment Ward can move invaders to a nearby safe position outside its protected area.")
                .define("banishmentWardEnabled", true);
        BANISHMENT_WARD_RANGE = builder.defineInRange("banishmentWardRange", 9, 1, 32);
        BANISHMENT_WARD_TICK_INTERVAL = builder.defineInRange("banishmentWardTickInterval", 60, 1, 20 * 60 * 60);
        BANISHMENT_WARD_MANA_COST_PER_ENTITY = builder.comment("Continuous upkeep paid by Banishment Ward. The legacy name mentions entity cost, but the field pays it once per interval.")
                .defineInRange("banishmentWardManaCostPerEntity", 18, 0, 100_000);
        BANISHMENT_WARD_TELEPORT_DISTANCE = builder.defineInRange("banishmentWardTeleportDistance", 12, 2, 64);
        BANISHMENT_WARD_MAX_ENTITIES_PER_CYCLE = builder.defineInRange("banishmentWardMaxEntitiesPerCycle", 32, 1, 128);
        BANISHMENT_WARD_AFFECT_PLAYERS = builder.define("banishmentWardAffectPlayers", false);
        BANISHMENT_WARD_AFFECT_BOSSES = builder.define("banishmentWardAffectBosses", false);
        BANISHMENT_WARD_COOLDOWN_PER_ENTITY_TICKS = builder.defineInRange("banishmentWardCooldownPerEntityTicks", 200, 0, 20 * 60 * 60);
        BANISHMENT_WARD_DURATION_TICKS = builder.defineInRange("banishmentWardDurationTicks", 6000, 1, 20 * 60 * 60);
        BANISHMENT_WARD_COOLDOWN_TICKS = builder.defineInRange("banishmentWardCooldownTicks", 900, 0, 20 * 60 * 60);

        ECLIPSE_WARD_ENABLED = builder.comment("If true, Eclipse Ward can obscure invaders with Blindness or Darkness.")
                .define("eclipseWardEnabled", true);
        ECLIPSE_WARD_RANGE = builder.defineInRange("eclipseWardRange", 8, 1, 32);
        ECLIPSE_WARD_TICK_INTERVAL = builder.defineInRange("eclipseWardTickInterval", 60, 1, 20 * 60 * 60);
        ECLIPSE_WARD_EFFECT_DURATION_TICKS = builder.defineInRange("eclipseWardEffectDurationTicks", 120, 1, 20 * 60 * 60);
        ECLIPSE_WARD_MANA_COST_PER_ENTITY = builder.comment("Continuous upkeep paid by Eclipse Ward. The legacy name mentions entity cost, but the field pays it once per interval.")
                .defineInRange("eclipseWardManaCostPerEntity", 12, 0, 100_000);
        ECLIPSE_WARD_MAX_ENTITIES_PER_CYCLE = builder.defineInRange("eclipseWardMaxEntitiesPerCycle", 64, 1, 128);
        ECLIPSE_WARD_AFFECT_PLAYERS = builder.define("eclipseWardAffectPlayers", false);
        ECLIPSE_WARD_USE_DARKNESS_INSTEAD_OF_BLINDNESS = builder.define("eclipseWardUseDarknessInsteadOfBlindness", false);
        ECLIPSE_WARD_CLEAR_MOB_TARGET = builder.comment("If true, Eclipse Ward clears hostile mob targets after applying its effect.")
                .define("eclipseWardClearMobTarget", true);
        ECLIPSE_WARD_DURATION_TICKS = builder.defineInRange("eclipseWardDurationTicks", 6000, 1, 20 * 60 * 60);
        ECLIPSE_WARD_COOLDOWN_TICKS = builder.defineInRange("eclipseWardCooldownTicks", 700, 0, 20 * 60 * 60);

        mvpWardConfigs.put(WardType.FERTILITY, defineMvpWard(builder, "fertility", 10, 100, 6000, 600, 12, 0, 64, 0, 600, 0, 0.0D, 0.0D, false, false, true, false));
        mvpWardConfigs.put(WardType.CITADEL, defineMvpWard(builder, "citadel", 10, 40, 3600, 900, 35, 0, 0, 1400, 0, 4, 0.0D, 0.0D, false, false, true, false));
        mvpWardConfigs.put(WardType.DISRUPTION, defineMvpWard(builder, "disruption", 14, 40, 6000, 700, 20, 0, 64, 0, 60, 0, 0.0D, 0.0D, false, false, true, true));
        mvpWardConfigs.put(WardType.CLOAKING, defineMvpWard(builder, "cloaking", 10, 60, 6000, 600, 8, 160, 0, 0, 0, 0, 0.0D, 0.0D, false, false, true, false));
        mvpWardConfigs.put(WardType.ACCELERATING, defineMvpWard(builder, "accelerating", 8, 100, 6000, 600, 14, 0, 64, 1024, 0, 0, 0.0D, 0.0D, false, false, true, true));
        mvpWardConfigs.put(WardType.EFFICIENCY, defineMvpWard(builder, "efficiency", 8, 80, 6000, 600, 12, 0, 0, 128, 0, 60, 0.0D, 0.0D, false, false, true, false));
        mvpWardConfigs.put(WardType.CRUSHING, defineMvpWard(builder, "crushing", 9, 40, 6000, 700, 18, 120, 64, 0, 0, 4, 1.0D, 0.0D, false, false, true, false));
        mvpWardConfigs.put(WardType.INVERSION, defineMvpWard(builder, "inversion", 9, 80, 6000, 800, 24, 0, 32, 0, 80, 0, 1.35D, 0.0D, false, false, true, false));
        mvpWardConfigs.put(WardType.AQUALUNG, defineMvpWard(builder, "aqualung", 12, 80, 6000, 600, 8, 180, 0, 0, 0, 0, 0.0D, 0.0D, false, false, true, true));
        mvpWardConfigs.put(WardType.TRANSMUTATION, defineMvpWard(builder, "transmutation", 8, 100, 6000, 700, 16, 0, 64, 512, 0, 0, 0.0D, 0.0D, false, false, true, true));
        mvpWardConfigs.put(WardType.TANGIBLE, defineMvpWard(builder, "tangible", 5, 40, 2400, 900, 50, 0, 0, 1200, 0, 5, 0.0D, 0.0D, false, false, true, false));
        mvpWardConfigs.put(WardType.SANCTUARY, defineMvpWard(builder, "sanctuary", 20, 60, 6000, 900, 35, 0, 64, 0, 0, 0, 0.0D, 0.0D, false, false, true, false));
        mvpWardConfigs.put(WardType.BOUNTY, defineMvpWard(builder, "bounty", 12, 60, 6000, 800, 28, 0, 3, 0, 0, 0, 0.0D, 0.5D, false, false, true, false));
        mvpWardConfigs.put(WardType.IMMORTAL, defineMvpWard(builder, "immortal", 8, 20, 6000, 1200, 80, 0, 64, 0, 20, 0, 0.0D, 0.0D, false, false, true, true));
        mvpWardConfigs.put(WardType.DRAIN, defineMvpWard(builder, "drain", 9, 40, 6000, 800, 25, 0, 64, 0, 0, 0, 5.0D, 0.65D, false, false, true, true));
        mvpWardConfigs.put(WardType.SOUL_CHAIN, defineMvpWard(builder, "soulChain", 9, 40, 6000, 900, 25, 0, 32, 0, 30, 0, 0.35D, 0.0D, false, false, true, false));
        mvpWardConfigs.put(WardType.STASIS, defineMvpWard(builder, "stasis", 9, 40, 6000, 800, 35, 120, 64, 0, 0, 8, 0.08D, 0.0D, false, false, true, true));
        mvpWardConfigs.put(WardType.MAELSTROM, defineMvpWard(builder, "maelstrom", 9, 40, 6000, 800, 26, 120, 64, 0, 0, 2, 3.5D, 0.0D, false, false, true, false));
        mvpWardConfigs.put(WardType.DECAY, defineMvpWard(builder, "decay", 9, 60, 6000, 800, 24, 160, 64, 0, 0, 1, 0.0D, 0.0D, false, false, true, false));
        mvpWardConfigs.put(WardType.DEFLECTION, defineMvpWard(builder, "deflection", 10, 20, 6000, 700, 25, 0, 64, 0, 20, 0, 1.0D, 0.0D, false, false, true, true));
        mvpWardConfigs.put(WardType.SILENCE, defineMvpWard(builder, "silence", 8, 80, 6000, 800, 20, 120, 64, 0, 0, 0, 0.0D, 0.0D, false, false, true, true));
        mvpWardConfigs.put(WardType.PHASING, defineMvpWard(builder, "phasing", 8, 40, 6000, 900, 18, 80, 0, 256, 0, 0, 0.0D, 0.0D, false, false, true, false));
        builder.pop();

        builder.push("sigils");
        SIGIL_MAX_COMPONENTS = builder.comment("Maximum stored components on a sigil, including the base Arcane component placed at creation.")
                .defineInRange("sigilMaxComponents", 8, 1, 64);
        SIGIL_ANIMATIONS_ENABLED = builder.comment("Client-side visual toggle for animated sigil overlays. This does not change sigil logic.")
                .define("sigilAnimationsEnabled", true);
        SIGIL_FLOATING_WHEN_ACTIVE = builder.comment("If true, active sigils render slightly above the floor.")
                .define("sigilFloatingWhenActive", true);
        SIGIL_ACTIVE_BOBBING_ENABLED = builder.comment("If true, active sigils use a subtle vertical bobbing animation.")
                .define("sigilActiveBobbingEnabled", true);
        SIGIL_ENABLE_PARTICLES = builder.comment("If true, sigils emit subtle client-side particles when components are added, activated, and while active.")
                .define("sigilEnableParticles", true);
        SIGIL_ACTIVE_PARTICLE_INTERVAL = builder.comment("Client ticks between subtle ambient particles from active sigils.")
                .defineInRange("sigilActiveParticleInterval", 18, 1, 20 * 60);
        SIGIL_ACTIVATION_TRANSITION_TICKS = builder.comment("Client-side visual ticks for sigils to ease from inactive to active.")
                .defineInRange("sigilActivationTransitionTicks", 24, 1, 20 * 60);
        SIGIL_DEACTIVATION_TRANSITION_TICKS = builder.comment("Client-side visual ticks for sigils to ease from active back to inactive.")
                .defineInRange("sigilDeactivationTransitionTicks", 28, 1, 20 * 60);
        SIGIL_ACTIVATION_PARTICLE_BURST = builder.comment("Number of subtle particles emitted when a sigil visually activates.")
                .defineInRange("sigilActivationParticleBurst", 10, 0, 100);
        SIGIL_DEACTIVATION_PARTICLE_BURST = builder.comment("Number of subtle particles emitted when a sigil visually deactivates.")
                .defineInRange("sigilDeactivationParticleBurst", 4, 0, 100);
        SIGIL_COMPONENT_LAYERS_ENABLED = builder.comment("If true, visible dust components add subtle composited sigil layers instead of being replaced by the ward overlay.")
                .define("sigilComponentLayersEnabled", true);
        SIGIL_MAX_VISIBLE_COMPONENT_LAYERS = builder.comment("Maximum distinct dust component layers rendered on a sigil. Repeated dusts intensify one layer instead of drawing duplicates.")
                .defineInRange("sigilMaxVisibleComponentLayers", 4, 0, 10);
        SIGIL_COMPONENT_LAYER_ALPHA = builder.comment("Base alpha for primary component sigil layers.")
                .defineInRange("sigilComponentLayerAlpha", 108, 0, 255);
        SIGIL_EXTRA_COMPONENT_LAYER_ALPHA = builder.comment("Base alpha for component layers beyond the first three visible layers.")
                .defineInRange("sigilExtraComponentLayerAlpha", 62, 0, 255);
        SIGIL_WARD_LAYER_ALPHA = builder.comment("Base alpha for the final ward signature overlay.")
                .defineInRange("sigilWardLayerAlpha", 122, 0, 255);
        SIGIL_INACTIVE_COMPONENT_ROTATION_SPEED = builder.comment("Degrees per client tick for inactive component or resolved ward overlays.")
                .defineInRange("sigilInactiveComponentRotationSpeed", 0.25D, -20.0D, 20.0D);
        SIGIL_COMPONENT_LAYER_ROTATION_SPEED = builder.comment("Degrees per client tick for composited dust component layers while the sigil is inactive.")
                .defineInRange("sigilComponentLayerRotationSpeed", 0.18D, -20.0D, 20.0D);
        SIGIL_WARD_LAYER_ROTATION_SPEED = builder.comment("Degrees per client tick for the final ward signature layer while the sigil is inactive.")
                .defineInRange("sigilWardLayerRotationSpeed", 0.28D, -20.0D, 20.0D);
        SIGIL_ACTIVE_PRIMARY_ROTATION_SPEED = builder.comment("Degrees per client tick for the main overlay of active sigils.")
                .defineInRange("sigilActivePrimaryRotationSpeed", 1.2D, -40.0D, 40.0D);
        SIGIL_ACTIVE_SECONDARY_ROTATION_SPEED = builder.comment("Degrees per client tick for the active energy overlay. Negative values rotate counter to the main overlay.")
                .defineInRange("sigilActiveSecondaryRotationSpeed", -0.8D, -40.0D, 40.0D);
        SIGIL_ACTIVE_FLOAT_HEIGHT = builder.comment("Height in blocks added to active sigil rendering when floating is enabled.")
                .defineInRange("sigilActiveFloatHeight", 0.055D, 0.0D, 0.5D);
        SIGIL_ACTIVE_BOBBING_AMPLITUDE = builder.comment("Maximum vertical bobbing amplitude in blocks for active sigils.")
                .defineInRange("sigilActiveBobbingAmplitude", 0.018D, 0.0D, 0.25D);
        SIGIL_ACTIVE_BOBBING_SPEED = builder.comment("Radians per client tick for active sigil bobbing.")
                .defineInRange("sigilActiveBobbingSpeed", 0.16D, 0.0D, 2.0D);
        builder.pop();

        builder.push("mana_tank");
        MANA_TANK_CAPACITY = builder.comment("Maximum mana stored by the simple MVP Mana Tank. Tank contents are not preserved when the block is broken yet.")
                .defineInRange("manaTankCapacity", 10000, 1, 1_000_000);
        builder.pop();

        builder.push("arcane_grinder");
        ARCANE_GRINDER_ENABLED = builder.comment("Master switch for the Arcane Grinder machine.")
                .define("arcaneGrinderEnabled", true);
        ARCANE_GRINDER_DEFAULT_PROCESSING_TIME = builder.comment("Default processing time for Arcane Grinding recipes that do not specify processingTime.")
                .defineInRange("arcaneGrinderDefaultProcessingTime", 120, 1, 20 * 60 * 60);
        ARCANE_GRINDER_ALLOW_AUTOMATION = builder.comment("If true, hoppers can insert into input/reagent slots and extract from the output slot.")
                .define("arcaneGrinderAllowAutomation", true);
        ARCANE_GRINDER_DROP_INVENTORY_ON_BREAK = builder.comment("If true, the Arcane Grinder drops its internal inventory when broken.")
                .define("arcaneGrinderDropInventoryOnBreak", true);
        ARCANE_CRYSTAL_TO_DUST_OUTPUT_COUNT = builder.comment("Output count for Arcane Crystal to Arcane Dust grinding recipes that opt into this config.")
                .defineInRange("arcaneCrystalToDustOutputCount", 2, 1, 64);
        builder.pop();

        builder.push("worldgen");
        ARCANE_GEODES_ENABLED = builder.comment("If true, Arcane Crystal Geodes can generate in new Overworld chunks.")
                .define("arcaneGeodesEnabled", true);
        ARCANE_GEODE_CHANCE = builder.comment("One Arcane Geode generation attempt is made per chunk by the placed feature. This is the 1-in-N chance for that attempt to actually generate.")
                .defineInRange("arcaneGeodeChance", 96, 1, 10_000);
        ARCANE_GEODE_MIN_Y = builder.comment("Minimum Y level for Arcane Geode generation. The placed feature has a broad hardcoded Y range, and the runtime feature applies this config gate.")
                .defineInRange("arcaneGeodeMinY", -48, -64, 320);
        ARCANE_GEODE_MAX_Y = builder.comment("Maximum Y level for Arcane Geode generation.")
                .defineInRange("arcaneGeodeMaxY", 32, -64, 320);
        ARCANE_GEODE_MIN_SIZE = builder.comment("Minimum horizontal radius of an Arcane Geode.")
                .defineInRange("arcaneGeodeMinSize", 4, 3, 16);
        ARCANE_GEODE_MAX_SIZE = builder.comment("Maximum horizontal radius of an Arcane Geode.")
                .defineInRange("arcaneGeodeMaxSize", 7, 3, 24);
        ARCANE_GEODE_CLUSTER_CHANCE = builder.comment("Percent chance for eligible inner air positions to try placing Arcane Crystal Clusters.")
                .defineInRange("arcaneGeodeClusterChance", 16, 0, 100);
        ARCANE_GEODE_BUDDING_CHANCE = builder.comment("Percent chance for inner crystal lining blocks to become Budding Arcane Crystal instead of Arcane Crystal Block.")
                .defineInRange("arcaneGeodeBuddingChance", 8, 0, 100);
        ARCANE_TREE_ABOVE_GEODE_ENABLED = builder.comment("If true, successful Arcane Geodes try to grow an Arcane Tree on the surface above them.")
                .define("arcaneTreeAboveGeodeEnabled", true);
        ARCANE_TREE_ABOVE_GEODE_CHANCE = builder.comment("Chance for a generated Arcane Geode to create its surface Arcane Tree marker. Alpha default is reliable for testing.")
                .defineInRange("arcaneTreeAboveGeodeChance", 1.0D, 0.0D, 1.0D);
        ARCANE_TREE_ROOT_COLUMN_ENABLED = builder.comment("If true, Arcane Trees above geodes create a vertical Arcane Geode Stone root column toward the geode.")
                .define("arcaneTreeRootColumnEnabled", true);
        ARCANE_TREE_ROOT_COLUMN_MAX_LENGTH = builder.comment("Maximum length of the Arcane Geode Stone root column from tree to geode.")
                .defineInRange("arcaneTreeRootColumnMaxLength", 256, 0, 384);
        ARCANE_TREE_MIN_SURFACE_Y = builder.comment("Minimum surface Y where Arcane Trees may generate above geodes.")
                .defineInRange("arcaneTreeMinSurfaceY", 50, -64, 320);
        ARCANE_TREE_MAX_SURFACE_Y = builder.comment("Maximum surface Y where Arcane Trees may generate above geodes.")
                .defineInRange("arcaneTreeMaxSurfaceY", 160, -64, 320);
        ARCANE_TREE_REQUIRE_SKY_LIGHT = builder.comment("If true, geode marker trees require sky access at the trunk base. Alpha default is false because the surface finder already avoids caves.")
                .define("arcaneTreeRequireSkyLight", false);
        ARCANE_TREE_MIN_TRUNK_HEIGHT = builder.comment("Minimum Arcane Tree trunk height.")
                .defineInRange("arcaneTreeMinTrunkHeight", 4, 2, 16);
        ARCANE_TREE_MAX_TRUNK_HEIGHT = builder.comment("Maximum Arcane Tree trunk height.")
                .defineInRange("arcaneTreeMaxTrunkHeight", 6, 2, 24);
        ARCANE_TREE_TALL_VARIANT_CHANCE = builder.comment("Chance for an Arcane Tree to grow a slightly taller, branchier trunk variant.")
                .defineInRange("arcaneTreeTallVariantChance", 0.22D, 0.0D, 1.0D);
        ARCANE_TREE_STANDARD_VARIANT_WEIGHT = builder.comment("Selection weight for the standard Arcane Tree silhouette.")
                .defineInRange("arcaneTreeStandardVariantWeight", 8, 0, 1000);
        ARCANE_TREE_TALL_VARIANT_WEIGHT = builder.comment("Selection weight for taller Arcane Trees.")
                .defineInRange("arcaneTreeTallVariantWeight", 4, 0, 1000);
        ARCANE_TREE_SPREADING_VARIANT_WEIGHT = builder.comment("Selection weight for wider Arcane Trees with more lateral canopy.")
                .defineInRange("arcaneTreeSpreadingVariantWeight", 4, 0, 1000);
        ARCANE_TREE_ANCIENT_VARIANT_WEIGHT = builder.comment("Selection weight for rare landmark Arcane Trees. Saplings do not use this variant.")
                .defineInRange("arcaneTreeAncientVariantWeight", 1, 0, 1000);
        ARCANE_TREE_WORLDGEN_DEBUG = builder.comment("If true, logs Arcane Tree worldgen attempts above geodes and their failure reasons.")
                .define("arcaneTreeWorldgenDebug", false);
        ARCANE_TREE_GENERATE_INITIAL_PETALS = builder.comment("If true, generated Arcane Trees scatter a few Arcane Petals around the base.")
                .define("arcaneTreeGenerateInitialPetals", true);
        ARCANE_TREE_INITIAL_PETAL_ATTEMPTS = builder.comment("Placement attempts for Arcane Petals when an Arcane Tree generates or grows.")
                .defineInRange("arcaneTreeInitialPetalAttempts", 10, 0, 64);
        ARCANE_PETALS_ENABLED = builder.comment("If true, Arcane Leaves can slowly create Arcane Petals nearby.")
                .define("arcanePetalsEnabled", true);
        ARCANE_PETALS_GENERATION_CHANCE = builder.comment("Chance per Arcane Leaves random tick to attempt one nearby Arcane Petals placement.")
                .defineInRange("arcanePetalsGenerationChance", 0.18D, 0.0D, 1.0D);
        ARCANE_PETALS_MAX_NEARBY = builder.comment("Maximum Arcane Petals allowed near a leaf before new petal growth is skipped.")
                .defineInRange("arcanePetalsMaxNearby", 8, 0, 64);
        ARCANE_PETALS_SEARCH_RADIUS = builder.comment("Radius used by Arcane Leaves when looking for ground to place petals.")
                .defineInRange("arcanePetalsSearchRadius", 4, 1, 8);
        ARCANE_PETALS_DROP_ARCANE_DUST_COUNT = builder.comment("Arcane Dust dropped when Arcane Petals are broken.")
                .defineInRange("arcanePetalsDropArcaneDustCount", 1, 1, 16);
        ARCANE_PETALS_REQUIRE_ARCANE_LEAVES_NEARBY = builder.comment("If true, Arcane Petals survive only while Arcane Leaves are nearby. Default false keeps player-placed petals decorative.")
                .define("arcanePetalsRequireArcaneLeavesNearby", false);
        ARCANE_CRYSTAL_GROWTH_ENABLED = builder.comment("If true, Budding Arcane Crystal can grow adjacent buds and clusters.")
                .define("arcaneCrystalGrowthEnabled", true);
        ARCANE_CRYSTAL_GROWTH_CHANCE = builder.comment("One-in-N chance per random tick for Budding Arcane Crystal to attempt growth. Vanilla-like default is 5.")
                .defineInRange("arcaneCrystalGrowthChance", 5, 1, 10_000);
        ARCANE_CRYSTAL_RANDOM_TICK_ENABLED = builder.comment("If false, random tick growth for Budding Arcane Crystal is fully disabled.")
                .define("arcaneCrystalRandomTickEnabled", true);
        ARCANE_CRYSTAL_SHIMMER_SOUND_ENABLED = builder.comment("If true, successful arcane crystal growth can play the shimmer sound.")
                .define("arcaneCrystalShimmerSoundEnabled", true);
        ARCANE_CRYSTAL_SHIMMER_SOUND_CHANCE = builder.comment("Percent chance to play the shimmer sound on a successful growth event.")
                .defineInRange("arcaneCrystalShimmerSoundChance", 70, 0, 100);
        builder.pop();

        builder.push("hud");
        ENABLE_MANA_HUD = builder.comment("Client-side toggle for the simple mana HUD overlay.")
                .define("enableManaHud", true);
        MANA_HUD_SHOW_NUMBERS = builder.define("manaHudShowNumbers", true);
        MANA_HUD_X = builder.defineInRange("manaHudX", 10, 0, 10000);
        MANA_HUD_Y = builder.defineInRange("manaHudY", 10, 0, 10000);
        MANA_HUD_SHOW_GROWTH_FLASH = builder.define("manaHudShowGrowthFlash", true);
        builder.pop();

        builder.push("debug");
        DEBUG_COMMANDS_ENABLED = builder.comment("If false, the /selarium debug command tree is unavailable. Alpha builds keep this off by default; enable it locally when testing wards.")
                .define("debugCommandsEnabled", false);
        builder.pop();

        builder.push("codex");
        CODEX_ENABLED = builder.comment("If true, the Selarium Codex guide item can open its informational screen.")
                .define("codexEnabled", true);
        builder.pop();

        builder.push("warding_grimoire");
        WARDING_GRIMOIRE_ENABLED = builder.comment("If true, the Warding Grimoire can open and edit player ward targeting rules.")
                .define("wardingGrimoireEnabled", true);
        WARDING_GRIMOIRE_OWNER_LOCKED = builder.comment("If true, a Warding Grimoire binds to the first player who uses it and only that owner can edit it.")
                .define("wardingGrimoireOwnerLocked", true);
        WARDING_GRIMOIRE_PLAYERS_AFFECT_REQUIRES_WARD_CONFIG = builder.comment("If true, offensive Wards can affect players only when both the Grimoire and that Ward's own affectPlayers config allow it.")
                .define("wardingGrimoirePlayersAffectRequiresWardConfig", true);
        WARDING_GRIMOIRE_BOSSES_AFFECT_REQUIRES_WARD_CONFIG = builder.comment("If true, Wards can affect bosses only when both the Grimoire and that Ward's own affectBosses config allow it.")
                .define("wardingGrimoireBossesAffectRequiresWardConfig", true);
        WARDING_GRIMOIRE_DEFAULT_AFFECT_HOSTILES = builder.comment("Default Grimoire rule for hostile mob targeting.")
                .define("wardingGrimoireDefaultAffectHostiles", true);
        WARDING_GRIMOIRE_DEFAULT_AFFECT_PASSIVE_MOBS = builder.comment("Default Grimoire rule for passive animal targeting.")
                .define("wardingGrimoireDefaultAffectPassiveMobs", false);
        WARDING_GRIMOIRE_DEFAULT_AFFECT_PLAYERS = builder.comment("Default Grimoire rule for player targeting. Ward-specific configs still override this by default.")
                .define("wardingGrimoireDefaultAffectPlayers", false);
        WARDING_GRIMOIRE_DEFAULT_IGNORE_PETS = builder.comment("Default Grimoire rule for ignoring tamed pets as harmful Ward targets.")
                .define("wardingGrimoireDefaultIgnorePets", true);
        WARDING_GRIMOIRE_DEFAULT_IGNORE_NAMED_ENTITIES = builder.comment("Default Grimoire rule for ignoring named entities as harmful Ward targets.")
                .define("wardingGrimoireDefaultIgnoreNamedEntities", false);
        builder.pop();

        MVP_WARD_CONFIGS = Map.copyOf(mvpWardConfigs);
        SPEC = builder.build();
    }

    private SelariumCommonConfig() {
    }

    public static int getSoftCapForTier(int tier) {
        return switch (Math.max(0, Math.min(4, tier))) {
            case 1 -> SOFT_CAP_TIER_1.get();
            case 2 -> SOFT_CAP_TIER_2.get();
            case 3 -> SOFT_CAP_TIER_3.get();
            case 4 -> SOFT_CAP_TIER_4.get();
            default -> SOFT_CAP_TIER_0.get();
        };
    }

    public static MvpWardConfig mvpWard(WardType type) {
        MvpWardConfig config = MVP_WARD_CONFIGS.get(type);
        if (config == null) {
            throw new IllegalArgumentException("No MVP ward config for " + type);
        }
        return config;
    }

    private static MvpWardConfig defineMvpWard(ForgeConfigSpec.Builder builder, String prefix, int range, int tickInterval, int durationTicks, int cooldownTicks, int manaCost, int effectDurationTicks, int maxEntitiesPerCycle, int maxBlocksPerCycle, int cooldownPerEntityTicks, int amplifier, double strength, double chance, boolean affectPlayers, boolean affectBosses, boolean optionA, boolean optionB) {
        return new MvpWardConfig(
                builder.define(prefix + "WardEnabled", true),
                builder.defineInRange(prefix + "WardRange", range, 1, 64),
                builder.defineInRange(prefix + "WardTickInterval", tickInterval, 1, 20 * 60 * 60),
                builder.defineInRange(prefix + "WardDurationTicks", durationTicks, 1, 20 * 60 * 60),
                builder.defineInRange(prefix + "WardCooldownTicks", cooldownTicks, 0, 20 * 60 * 60),
                builder.defineInRange(prefix + "WardManaCost", manaCost, 0, 100_000),
                builder.defineInRange(prefix + "WardEffectDurationTicks", effectDurationTicks, 0, 20 * 60 * 60),
                builder.defineInRange(prefix + "WardMaxEntitiesPerCycle", maxEntitiesPerCycle, 0, 256),
                builder.defineInRange(prefix + "WardMaxBlocksPerCycle", maxBlocksPerCycle, 0, 2048),
                builder.defineInRange(prefix + "WardCooldownPerEntityTicks", cooldownPerEntityTicks, 0, 20 * 60 * 60),
                builder.defineInRange(prefix + "WardAmplifier", amplifier, 0, 16),
                builder.defineInRange(prefix + "WardStrength", strength, 0.0D, 100.0D),
                builder.defineInRange(prefix + "WardChance", chance, 0.0D, 1.0D),
                builder.define(prefix + "WardAffectPlayers", affectPlayers),
                builder.define(prefix + "WardAffectBosses", affectBosses),
                builder.define(prefix + "WardOptionA", optionA),
                builder.define(prefix + "WardOptionB", optionB));
    }

    public record MvpWardConfig(
            ForgeConfigSpec.BooleanValue enabled,
            ForgeConfigSpec.IntValue range,
            ForgeConfigSpec.IntValue tickInterval,
            ForgeConfigSpec.IntValue durationTicks,
            ForgeConfigSpec.IntValue cooldownTicks,
            ForgeConfigSpec.IntValue manaCost,
            ForgeConfigSpec.IntValue effectDurationTicks,
            ForgeConfigSpec.IntValue maxEntitiesPerCycle,
            ForgeConfigSpec.IntValue maxBlocksPerCycle,
            ForgeConfigSpec.IntValue cooldownPerEntityTicks,
            ForgeConfigSpec.IntValue amplifier,
            ForgeConfigSpec.DoubleValue strength,
            ForgeConfigSpec.DoubleValue chance,
            ForgeConfigSpec.BooleanValue affectPlayers,
            ForgeConfigSpec.BooleanValue affectBosses,
            ForgeConfigSpec.BooleanValue optionA,
            ForgeConfigSpec.BooleanValue optionB) {
    }
}
