package com.seleris.selarium.ward;

import com.seleris.selarium.ward.WardFieldSource;
import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.grimoire.SoulEssenceCategory;
import com.seleris.selarium.grimoire.SoulEssenceClassifier;
import com.seleris.selarium.grimoire.WardingEntityCategory;
import com.seleris.selarium.grimoire.WardingGrimoireData;
import com.seleris.selarium.grimoire.WardingRuleSet;
import com.seleris.selarium.grimoire.WardingWardRule;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

public final class WardAccessService {
    private static final String TARGETING_PRIORITY = "server_config > owner_protection > ward_deny > ward_allow > ward_category > global_deny > global_allow > global_category > default_safe";

    private WardAccessService() {
    }

    public record TargetDecision(
            WardingEntityCategory category,
            SoulEssenceCategory soul,
            WardType wardType,
            boolean usingCustomWardRule,
            boolean positive,
            String positiveReason,
            boolean negative,
            String negativeReason) {
        public String summary() {
            return "ward=" + wardType.getSerializedName()
                    + " category=" + category
                    + " soul=" + soul.getSerializedName()
                    + " customWardRule=" + usingCustomWardRule
                    + " positive=" + positive
                    + " positiveReason=" + positiveReason
                    + " negative=" + negative
                    + " negativeReason=" + negativeReason
                    + " priority=" + TARGETING_PRIORITY;
        }
    }

    private record RuleDecision(boolean allowed, String reason) {
    }

    public static boolean isOwner(WardFieldSource sigil, Player player) {
        return sigil.getOwner() != null && sigil.getOwner().equals(player.getUUID());
    }

    public static WardingRuleSet rulesFor(WardFieldSource sigil) {
        return WardingGrimoireData.rulesForSigil(sigil);
    }

    public static WardingEntityCategory classify(WardFieldSource sigil, LivingEntity entity) {
        WardingRuleSet rules = rulesFor(sigil);
        if (entity instanceof Player player) {
            if (isOwner(sigil, player)) {
                return WardingEntityCategory.OWNER;
            }
            if (rules.matchesAllowedPlayer(player.getUUID(), player.getGameProfile().getName())) {
                return WardingEntityCategory.ALLY_PLAYER;
            }
            return WardingEntityCategory.ENEMY_PLAYER;
        }
        if (WardTargetingService.isBoss(entity)) {
            return WardingEntityCategory.BOSS;
        }
        if (isTamedPet(entity)) {
            return WardingEntityCategory.PET;
        }
        if (entity instanceof Villager) {
            return WardingEntityCategory.VILLAGER;
        }
        if (entity instanceof Enemy) {
            return WardingEntityCategory.HOSTILE_MOB;
        }
        if (entity instanceof Animal) {
            return WardingEntityCategory.PASSIVE_MOB;
        }
        if (entity instanceof Mob) {
            return WardingEntityCategory.NEUTRAL_MOB;
        }
        return WardingEntityCategory.UNKNOWN;
    }

    public static boolean isAlly(WardFieldSource sigil, LivingEntity entity) {
        return positiveDecision(sigil, entity, sigil.getWardType()).allowed();
    }

    public static boolean shouldAffectPositiveWard(WardFieldSource sigil, LivingEntity entity) {
        return isAlly(sigil, entity);
    }

    public static boolean shouldAffectNegativeWard(WardFieldSource sigil, LivingEntity entity, boolean includeHostileMobs, boolean includePlayers, boolean includeBosses) {
        return negativeDecision(sigil, entity, sigil.getWardType(), includeHostileMobs, includePlayers, includeBosses).allowed();
    }

    public static TargetDecision explainTarget(WardFieldSource sigil, LivingEntity entity, WardType wardType, boolean includeHostileMobs, boolean includePlayers, boolean includeBosses) {
        WardType effectiveType = wardType == null || wardType == WardType.NONE ? sigil.getWardType() : wardType;
        WardingRuleSet rules = rulesFor(sigil);
        WardingWardRule rule = effectiveWardRule(rules, effectiveType);
        RuleDecision positive = positiveDecision(sigil, entity, effectiveType);
        RuleDecision negative = negativeDecision(sigil, entity, effectiveType, includeHostileMobs, includePlayers, includeBosses);
        return new TargetDecision(classify(sigil, entity), SoulEssenceClassifier.classify(entity), effectiveType, rule != null, positive.allowed(), positive.reason(), negative.allowed(), negative.reason());
    }

    private static RuleDecision positiveDecision(WardFieldSource sigil, LivingEntity entity, WardType wardType) {
        WardingRuleSet rules = rulesFor(sigil);
        if (!isValidTargetBase(entity, rules)) {
            return new RuleDecision(false, "default_safe: invalid, spectator, dead, or named entity ignored");
        }
        WardingWardRule wardRule = effectiveWardRule(rules, wardType);
        if (entity instanceof Player player) {
            if (isOwner(sigil, player)) {
                boolean allowed = wardRule == null ? rules.affectOwner() : wardRule.affectOwner();
                return new RuleDecision(allowed, allowed ? "owner protection: owner receives positive Ward" : "ward/global category: owner buffs disabled");
            }
            if (!rules.knowsSoul(SoulEssenceCategory.ANTHROPIC)) {
                return new RuleDecision(false, "soul lock: Anthropic Soul required for player rules");
            }
            if (matchesPlayer(wardRule == null ? Set.of() : wardRule.denyPlayers(), player)) {
                return new RuleDecision(false, "ward deny list");
            }
            if (rules.matchesDeniedPlayer(player.getUUID(), player.getGameProfile().getName())) {
                return new RuleDecision(false, "global deny list");
            }
            if (wardRule != null && wardRule.affectAllies() && matchesPlayer(wardRule.allowPlayers(), player)) {
                return new RuleDecision(true, "ward allow list");
            }
            if (rules.matchesAllowedPlayer(player.getUUID(), player.getGameProfile().getName())) {
                return new RuleDecision(true, "global allow list");
            }
            return new RuleDecision(false, "default_safe: player is not an allowed ally");
        }
        if (entity instanceof TamableAnimal pet && rules.knowsSoul(SoulEssenceCategory.BESTIAL)) {
            boolean affectPets = wardRule == null ? rules.affectPets() : wardRule.affectPets();
            boolean ownedPet = sigil.getOwner() != null && sigil.getOwner().equals(pet.getOwnerUUID());
            return new RuleDecision(affectPets && ownedPet, affectPets && ownedPet ? "ward/global category: owner pet ally" : "default_safe: pet not allowed as ally");
        }
        return new RuleDecision(false, "default_safe: not an ally target");
    }

    /*
     * Targeting priority:
     * 1. server Ward config, 2. owner protection, 3. Ward-specific deny,
     * 4. Ward-specific allow, 5. Ward-specific category toggle,
     * 6. global deny, 7. global allow, 8. global category toggle,
     * 9. safe default.
     */
    private static RuleDecision negativeDecision(WardFieldSource sigil, LivingEntity entity, WardType wardType, boolean includeHostileMobs, boolean includePlayers, boolean includeBosses) {
        WardingRuleSet rules = rulesFor(sigil);
        if (!isValidTargetBase(entity, rules)) {
            return new RuleDecision(false, "default_safe: invalid, spectator, dead, or named entity ignored");
        }
        boolean playerTarget = entity instanceof Player;
        boolean bossTarget = WardTargetingService.isBoss(entity);
        if (playerTarget && SelariumCommonConfig.WARDING_GRIMOIRE_PLAYERS_AFFECT_REQUIRES_WARD_CONFIG.get() && !includePlayers) {
            return new RuleDecision(false, "server config: this Ward cannot affect players");
        }
        if (bossTarget && SelariumCommonConfig.WARDING_GRIMOIRE_BOSSES_AFFECT_REQUIRES_WARD_CONFIG.get() && !includeBosses) {
            return new RuleDecision(false, "server config: this Ward cannot affect bosses");
        }

        WardingWardRule wardRule = effectiveWardRule(rules, wardType);
        if (entity instanceof Player player && isOwner(sigil, player)) {
            if (!rules.affectOwnerWithNegativeWards()) {
                return new RuleDecision(false, "owner protection: owner harm disabled");
            }
            if (wardRule != null && !wardRule.affectOwner()) {
                return new RuleDecision(false, "ward category: owner harm disabled for this Ward");
            }
            return new RuleDecision(true, "owner protection explicitly overridden");
        }

        if (isDeniedEntityType(wardRule, entity)) {
            return new RuleDecision(false, "ward deny entity type");
        }
        if (entity instanceof Player player && matchesPlayer(wardRule == null ? Set.of() : wardRule.denyPlayers(), player)) {
            return new RuleDecision(false, "ward deny player");
        }
        if (entity instanceof Player player && wardRule != null && matchesPlayer(wardRule.allowPlayers(), player)) {
            return canSpecificRuleAffect(rules, sigil, entity, includeHostileMobs, includePlayers, includeBosses)
                    ? new RuleDecision(true, "ward allow player")
                    : new RuleDecision(false, "ward allow blocked by soul/config");
        }
        if (isAllowedEntityType(wardRule, entity)) {
            return canSpecificRuleAffect(rules, sigil, entity, includeHostileMobs, includePlayers, includeBosses)
                    ? new RuleDecision(true, "ward allow entity type")
                    : new RuleDecision(false, "ward allow entity type blocked by soul/config");
        }
        if (wardRule != null) {
            return categoryDecision("ward category", rules, wardRule, sigil, entity, includeHostileMobs, includePlayers, includeBosses);
        }

        if (isDeniedEntityType(rules, entity)) {
            return new RuleDecision(false, "global deny entity type");
        }
        if (entity instanceof Player player && rules.matchesDeniedPlayer(player.getUUID(), player.getGameProfile().getName())) {
            return new RuleDecision(false, "global deny player");
        }
        if (entity instanceof Player player && rules.matchesAllowedPlayer(player.getUUID(), player.getGameProfile().getName())) {
            return canSpecificRuleAffect(rules, sigil, entity, includeHostileMobs, includePlayers, includeBosses)
                    ? new RuleDecision(true, "global allow player")
                    : new RuleDecision(false, "global allow blocked by soul/config");
        }
        if (isAllowedEntityType(rules, entity)) {
            return canSpecificRuleAffect(rules, sigil, entity, includeHostileMobs, includePlayers, includeBosses)
                    ? new RuleDecision(true, "global allow entity type")
                    : new RuleDecision(false, "global allow entity type blocked by soul/config");
        }
        return categoryDecision("global category", rules, null, sigil, entity, includeHostileMobs, includePlayers, includeBosses);
    }

    private static RuleDecision categoryDecision(String source, WardingRuleSet rules, WardingWardRule wardRule, WardFieldSource sigil, LivingEntity entity, boolean includeHostileMobs, boolean includePlayers, boolean includeBosses) {
        if (WardTargetingService.isBoss(entity)) {
            boolean configured = wardRule == null ? rules.affectBosses() : wardRule.affectBosses();
            boolean allowed = rules.knowsSoul(SoulEssenceCategory.MONSTROUS) && configured && includeBosses;
            return new RuleDecision(allowed, allowed ? source + ": boss allowed" : source + ": boss blocked by soul, config, or toggle");
        }
        if (entity instanceof Player) {
            boolean configured = wardRule == null ? rules.affectPlayers() : wardRule.affectPlayers();
            boolean allowed = rules.knowsSoul(SoulEssenceCategory.ANTHROPIC) && configured && includePlayers;
            return new RuleDecision(allowed, allowed ? source + ": player allowed" : source + ": player blocked by soul, config, or toggle");
        }
        if (isTamedPet(entity)) {
            boolean affectPets = wardRule == null ? rules.affectPets() : wardRule.affectPets();
            boolean allowed = rules.knowsSoul(SoulEssenceCategory.BESTIAL) && !rules.ignoreTamedPets() && affectPets && !isAlly(sigil, entity);
            return new RuleDecision(allowed, allowed ? source + ": pet allowed" : source + ": pet blocked by soul, ignore-pets, ally, or toggle");
        }
        if (entity instanceof Villager) {
            boolean configured = wardRule == null ? rules.affectVillagers() : wardRule.affectVillagers();
            boolean allowed = rules.knowsSoul(SoulEssenceCategory.ANTHROPIC) && configured;
            return new RuleDecision(allowed, allowed ? source + ": villager allowed" : source + ": villager blocked by soul or toggle");
        }
        if (entity instanceof Enemy) {
            boolean configured = wardRule == null ? rules.affectHostileMobs() : wardRule.affectHostiles();
            boolean allowed = includeHostileMobs && configured;
            return new RuleDecision(allowed, allowed ? source + ": hostile allowed" : source + ": hostile blocked by Ward config or toggle");
        }
        if (entity instanceof Animal) {
            boolean configured = wardRule == null ? rules.affectPassiveMobs() : wardRule.affectPassives();
            boolean allowed = rules.knowsSoul(SoulEssenceCategory.BESTIAL) && configured;
            return new RuleDecision(allowed, allowed ? source + ": passive allowed" : source + ": passive blocked by soul or toggle");
        }
        if (entity instanceof Mob) {
            boolean configured = wardRule == null ? rules.affectNeutralMobs() : wardRule.affectNeutral();
            boolean allowed = rules.knowsSoul(SoulEssenceCategory.BESTIAL) && configured;
            return new RuleDecision(allowed, allowed ? source + ": neutral mob allowed" : source + ": neutral mob blocked by soul or toggle");
        }
        return new RuleDecision(false, "default_safe: unknown entity category");
    }

    private static WardingWardRule effectiveWardRule(WardFieldSource sigil, WardingRuleSet rules) {
        return effectiveWardRule(rules, sigil.getWardType());
    }

    private static WardingWardRule effectiveWardRule(WardingRuleSet rules, WardType type) {
        WardingWardRule rule = rules.wardRuleOrNull(type);
        if (type == null || type == WardType.NONE || !rules.isWardUnlocked(type) || rule == null || rule.useGlobalRules()) {
            return null;
        }
        return rule;
    }

    private static boolean canSpecificRuleAffect(WardingRuleSet rules, WardFieldSource sigil, LivingEntity entity, boolean includeHostileMobs, boolean includePlayers, boolean includeBosses) {
        if (WardTargetingService.isBoss(entity)) {
            return rules.knowsSoul(SoulEssenceCategory.MONSTROUS)
                    && (!SelariumCommonConfig.WARDING_GRIMOIRE_BOSSES_AFFECT_REQUIRES_WARD_CONFIG.get() || includeBosses);
        }
        if (entity instanceof Player player) {
            return !isOwner(sigil, player)
                    && rules.knowsSoul(SoulEssenceCategory.ANTHROPIC)
                    && (!SelariumCommonConfig.WARDING_GRIMOIRE_PLAYERS_AFFECT_REQUIRES_WARD_CONFIG.get() || includePlayers);
        }
        if (entity instanceof Villager) {
            return rules.knowsSoul(SoulEssenceCategory.ANTHROPIC);
        }
        if (entity instanceof Enemy) {
            return includeHostileMobs;
        }
        if (entity instanceof Animal || isTamedPet(entity) || entity instanceof Mob) {
            return rules.knowsSoul(SoulEssenceCategory.BESTIAL);
        }
        return false;
    }

    private static boolean isValidTargetBase(LivingEntity entity, WardingRuleSet rules) {
        if (!entity.isAlive() || entity.isSpectator()) {
            return false;
        }
        return !rules.ignoreNamedEntities() || !entity.hasCustomName();
    }

    private static boolean isTamedPet(LivingEntity entity) {
        return entity instanceof TamableAnimal pet && pet.isTame();
    }

    private static boolean isDeniedEntityType(WardingRuleSet rules, LivingEntity entity) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key != null && rules.denyEntityTypeIds().contains(key.toString());
    }

    private static boolean isAllowedEntityType(WardingRuleSet rules, LivingEntity entity) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key != null && rules.allowEntityTypeIds().contains(key.toString());
    }

    private static boolean isDeniedEntityType(WardingWardRule rule, LivingEntity entity) {
        if (rule == null) {
            return false;
        }
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key != null && rule.denyEntityTypeIds().contains(key.toString());
    }

    private static boolean isAllowedEntityType(WardingWardRule rule, LivingEntity entity) {
        if (rule == null) {
            return false;
        }
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key != null && rule.allowEntityTypeIds().contains(key.toString());
    }

    private static boolean matchesPlayer(Set<String> entries, Player player) {
        String uuid = player.getUUID().toString().toLowerCase(java.util.Locale.ROOT);
        String name = WardingRuleSet.sanitizeIdentifier(player.getGameProfile().getName());
        return entries.contains(uuid) || entries.contains(name);
    }
}
