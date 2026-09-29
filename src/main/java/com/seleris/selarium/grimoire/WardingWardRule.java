package com.seleris.selarium.grimoire;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.LinkedHashSet;
import java.util.Set;

public class WardingWardRule {
    private boolean useGlobalRules = true;
    private boolean affectHostiles = true;
    private boolean affectPassives;
    private boolean affectNeutral;
    private boolean affectVillagers;
    private boolean affectPets;
    private boolean affectPlayers;
    private boolean affectOwner = true;
    private boolean affectAllies = true;
    private boolean affectBosses;
    private final LinkedHashSet<String> allowPlayers = new LinkedHashSet<>();
    private final LinkedHashSet<String> denyPlayers = new LinkedHashSet<>();
    private final LinkedHashSet<String> allowEntityTypeIds = new LinkedHashSet<>();
    private final LinkedHashSet<String> denyEntityTypeIds = new LinkedHashSet<>();

    public WardingWardRule copy() {
        WardingWardRule copy = new WardingWardRule();
        copy.useGlobalRules = useGlobalRules;
        copy.affectHostiles = affectHostiles;
        copy.affectPassives = affectPassives;
        copy.affectNeutral = affectNeutral;
        copy.affectVillagers = affectVillagers;
        copy.affectPets = affectPets;
        copy.affectPlayers = affectPlayers;
        copy.affectOwner = affectOwner;
        copy.affectAllies = affectAllies;
        copy.affectBosses = affectBosses;
        copy.allowPlayers.addAll(allowPlayers);
        copy.denyPlayers.addAll(denyPlayers);
        copy.allowEntityTypeIds.addAll(allowEntityTypeIds);
        copy.denyEntityTypeIds.addAll(denyEntityTypeIds);
        return copy;
    }

    public void sanitize() {
        trimSet(allowPlayers);
        trimSet(denyPlayers);
        WardingRuleSet.trimEntityTypeSet(allowEntityTypeIds);
        WardingRuleSet.trimEntityTypeSet(denyEntityTypeIds);
        allowPlayers.removeAll(denyPlayers);
        allowEntityTypeIds.removeAll(denyEntityTypeIds);
    }

    public CompoundTag toTag() {
        sanitize();
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("UseGlobalRules", useGlobalRules);
        tag.putBoolean("AffectHostiles", affectHostiles);
        tag.putBoolean("AffectPassives", affectPassives);
        tag.putBoolean("AffectNeutral", affectNeutral);
        tag.putBoolean("AffectVillagers", affectVillagers);
        tag.putBoolean("AffectPets", affectPets);
        tag.putBoolean("AffectPlayers", affectPlayers);
        tag.putBoolean("AffectOwner", affectOwner);
        tag.putBoolean("AffectAllies", affectAllies);
        tag.putBoolean("AffectBosses", affectBosses);
        tag.put("AllowPlayers", WardingRuleSet.writeStringList(allowPlayers));
        tag.put("DenyPlayers", WardingRuleSet.writeStringList(denyPlayers));
        tag.put("AllowEntityTypeIds", WardingRuleSet.writeStringList(allowEntityTypeIds));
        tag.put("DenyEntityTypeIds", WardingRuleSet.writeStringList(denyEntityTypeIds));
        return tag;
    }

    public static WardingWardRule fromTag(CompoundTag tag) {
        WardingWardRule rule = new WardingWardRule();
        if (tag.contains("UseGlobalRules")) rule.useGlobalRules = tag.getBoolean("UseGlobalRules");
        if (tag.contains("AffectHostiles")) rule.affectHostiles = tag.getBoolean("AffectHostiles");
        if (tag.contains("AffectPassives")) rule.affectPassives = tag.getBoolean("AffectPassives");
        if (tag.contains("AffectNeutral")) rule.affectNeutral = tag.getBoolean("AffectNeutral");
        if (tag.contains("AffectVillagers")) rule.affectVillagers = tag.getBoolean("AffectVillagers");
        if (tag.contains("AffectPets")) rule.affectPets = tag.getBoolean("AffectPets");
        if (tag.contains("AffectPlayers")) rule.affectPlayers = tag.getBoolean("AffectPlayers");
        if (tag.contains("AffectOwner")) rule.affectOwner = tag.getBoolean("AffectOwner");
        if (tag.contains("AffectAllies")) rule.affectAllies = tag.getBoolean("AffectAllies");
        if (tag.contains("AffectBosses")) rule.affectBosses = tag.getBoolean("AffectBosses");
        WardingRuleSet.readStringList(tag, "AllowPlayers", rule.allowPlayers);
        WardingRuleSet.readStringList(tag, "DenyPlayers", rule.denyPlayers);
        WardingRuleSet.readStringList(tag, "AllowEntityTypeIds", rule.allowEntityTypeIds);
        WardingRuleSet.readStringList(tag, "DenyEntityTypeIds", rule.denyEntityTypeIds);
        rule.sanitize();
        return rule;
    }

    public void encode(FriendlyByteBuf buffer) {
        sanitize();
        buffer.writeBoolean(useGlobalRules);
        buffer.writeBoolean(affectHostiles);
        buffer.writeBoolean(affectPassives);
        buffer.writeBoolean(affectNeutral);
        buffer.writeBoolean(affectVillagers);
        buffer.writeBoolean(affectPets);
        buffer.writeBoolean(affectPlayers);
        buffer.writeBoolean(affectOwner);
        buffer.writeBoolean(affectAllies);
        buffer.writeBoolean(affectBosses);
        WardingRuleSet.writeStringSet(buffer, allowPlayers);
        WardingRuleSet.writeStringSet(buffer, denyPlayers);
        WardingRuleSet.writeStringSet(buffer, allowEntityTypeIds);
        WardingRuleSet.writeStringSet(buffer, denyEntityTypeIds);
    }

    public static WardingWardRule decode(FriendlyByteBuf buffer) {
        WardingWardRule rule = new WardingWardRule();
        rule.useGlobalRules = buffer.readBoolean();
        rule.affectHostiles = buffer.readBoolean();
        rule.affectPassives = buffer.readBoolean();
        rule.affectNeutral = buffer.readBoolean();
        rule.affectVillagers = buffer.readBoolean();
        rule.affectPets = buffer.readBoolean();
        rule.affectPlayers = buffer.readBoolean();
        rule.affectOwner = buffer.readBoolean();
        rule.affectAllies = buffer.readBoolean();
        rule.affectBosses = buffer.readBoolean();
        WardingRuleSet.readStringSet(buffer, rule.allowPlayers);
        WardingRuleSet.readStringSet(buffer, rule.denyPlayers);
        WardingRuleSet.readStringSet(buffer, rule.allowEntityTypeIds);
        WardingRuleSet.readStringSet(buffer, rule.denyEntityTypeIds);
        rule.sanitize();
        return rule;
    }

    public void addAllowedPlayer(String value) {
        String sanitized = WardingRuleSet.sanitizeIdentifier(value);
        if (!sanitized.isBlank()) {
            allowPlayers.add(sanitized);
            denyPlayers.remove(sanitized);
            trimSet(allowPlayers);
        }
    }

    public void addDeniedPlayer(String value) {
        String sanitized = WardingRuleSet.sanitizeIdentifier(value);
        if (!sanitized.isBlank()) {
            denyPlayers.add(sanitized);
            allowPlayers.remove(sanitized);
            trimSet(denyPlayers);
        }
    }

    public void removePlayer(String value) {
        String sanitized = WardingRuleSet.sanitizeIdentifier(value);
        if (!sanitized.isBlank()) {
            allowPlayers.remove(sanitized);
            denyPlayers.remove(sanitized);
        }
    }

    public void addAllowedEntityType(String value) {
        String sanitized = WardingRuleSet.sanitizeIdentifier(value);
        if (WardingRuleSet.isValidResourceId(sanitized)) {
            allowEntityTypeIds.add(sanitized);
            denyEntityTypeIds.remove(sanitized);
            WardingRuleSet.trimEntityTypeSet(allowEntityTypeIds);
        }
    }

    public void addDeniedEntityType(String value) {
        String sanitized = WardingRuleSet.sanitizeIdentifier(value);
        if (WardingRuleSet.isValidResourceId(sanitized)) {
            denyEntityTypeIds.add(sanitized);
            allowEntityTypeIds.remove(sanitized);
            WardingRuleSet.trimEntityTypeSet(denyEntityTypeIds);
        }
    }

    public void removeEntityType(String value) {
        String sanitized = WardingRuleSet.sanitizeIdentifier(value);
        if (!sanitized.isBlank()) {
            allowEntityTypeIds.remove(sanitized);
            denyEntityTypeIds.remove(sanitized);
        }
    }

    private static void trimSet(LinkedHashSet<String> target) {
        WardingRuleSet.trimSet(target);
    }

    public boolean useGlobalRules() { return useGlobalRules; }
    public void useGlobalRules(boolean useGlobalRules) { this.useGlobalRules = useGlobalRules; }
    public boolean affectHostiles() { return affectHostiles; }
    public void affectHostiles(boolean affectHostiles) { this.affectHostiles = affectHostiles; }
    public boolean affectPassives() { return affectPassives; }
    public void affectPassives(boolean affectPassives) { this.affectPassives = affectPassives; }
    public boolean affectNeutral() { return affectNeutral; }
    public void affectNeutral(boolean affectNeutral) { this.affectNeutral = affectNeutral; }
    public boolean affectVillagers() { return affectVillagers; }
    public void affectVillagers(boolean affectVillagers) { this.affectVillagers = affectVillagers; }
    public boolean affectPets() { return affectPets; }
    public void affectPets(boolean affectPets) { this.affectPets = affectPets; }
    public boolean affectPlayers() { return affectPlayers; }
    public void affectPlayers(boolean affectPlayers) { this.affectPlayers = affectPlayers; }
    public boolean affectOwner() { return affectOwner; }
    public void affectOwner(boolean affectOwner) { this.affectOwner = affectOwner; }
    public boolean affectAllies() { return affectAllies; }
    public void affectAllies(boolean affectAllies) { this.affectAllies = affectAllies; }
    public boolean affectBosses() { return affectBosses; }
    public void affectBosses(boolean affectBosses) { this.affectBosses = affectBosses; }
    public Set<String> allowPlayers() { return allowPlayers; }
    public Set<String> denyPlayers() { return denyPlayers; }
    public Set<String> allowEntityTypeIds() { return allowEntityTypeIds; }
    public Set<String> denyEntityTypeIds() { return denyEntityTypeIds; }
}
