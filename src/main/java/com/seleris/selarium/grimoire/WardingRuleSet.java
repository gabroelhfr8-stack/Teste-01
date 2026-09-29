package com.seleris.selarium.grimoire;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WardingRuleSet {
    public static final int MAX_LIST_ENTRIES = 64;
    public static final int MAX_IDENTIFIER_LENGTH = 96;

    private UUID owner;
    private boolean affectHostileMobs;
    private boolean affectPassiveMobs;
    private boolean affectNeutralMobs;
    private boolean affectVillagers;
    private boolean affectPets;
    private boolean affectPlayers;
    private boolean affectOwner;
    private boolean affectOwnerWithNegativeWards;
    private boolean affectBosses;
    private boolean ignoreNamedEntities;
    private boolean ignoreTamedPets;
    private final LinkedHashSet<String> allowPlayers = new LinkedHashSet<>();
    private final LinkedHashSet<String> denyPlayers = new LinkedHashSet<>();
    private final LinkedHashSet<String> allowEntityTypeIds = new LinkedHashSet<>();
    private final LinkedHashSet<String> denyEntityTypeIds = new LinkedHashSet<>();
    private final EnumSet<SoulEssenceCategory> unlockedSoulCategories = EnumSet.noneOf(SoulEssenceCategory.class);
    private final LinkedHashSet<String> knownEntityTypes = new LinkedHashSet<>();
    private final EnumSet<WardType> unlockedWardTypes = EnumSet.noneOf(WardType.class);
    private final EnumMap<WardType, WardingWardRule> wardRules = new EnumMap<>(WardType.class);

    public static WardingRuleSet defaults(UUID owner) {
        WardingRuleSet rules = new WardingRuleSet();
        rules.owner = owner;
        rules.affectHostileMobs = SelariumCommonConfig.WARDING_GRIMOIRE_DEFAULT_AFFECT_HOSTILES.get();
        rules.affectPassiveMobs = SelariumCommonConfig.WARDING_GRIMOIRE_DEFAULT_AFFECT_PASSIVE_MOBS.get();
        rules.affectNeutralMobs = false;
        rules.affectVillagers = false;
        rules.affectPets = false;
        rules.affectPlayers = SelariumCommonConfig.WARDING_GRIMOIRE_DEFAULT_AFFECT_PLAYERS.get();
        rules.affectOwner = true;
        rules.affectOwnerWithNegativeWards = false;
        rules.affectBosses = false;
        rules.ignoreNamedEntities = SelariumCommonConfig.WARDING_GRIMOIRE_DEFAULT_IGNORE_NAMED_ENTITIES.get();
        rules.ignoreTamedPets = SelariumCommonConfig.WARDING_GRIMOIRE_DEFAULT_IGNORE_PETS.get();
        return rules;
    }

    public WardingRuleSet copy() {
        WardingRuleSet copy = new WardingRuleSet();
        copy.owner = owner;
        copy.affectHostileMobs = affectHostileMobs;
        copy.affectPassiveMobs = affectPassiveMobs;
        copy.affectNeutralMobs = affectNeutralMobs;
        copy.affectVillagers = affectVillagers;
        copy.affectPets = affectPets;
        copy.affectPlayers = affectPlayers;
        copy.affectOwner = affectOwner;
        copy.affectOwnerWithNegativeWards = affectOwnerWithNegativeWards;
        copy.affectBosses = affectBosses;
        copy.ignoreNamedEntities = ignoreNamedEntities;
        copy.ignoreTamedPets = ignoreTamedPets;
        copy.allowPlayers.addAll(allowPlayers);
        copy.denyPlayers.addAll(denyPlayers);
        copy.allowEntityTypeIds.addAll(allowEntityTypeIds);
        copy.denyEntityTypeIds.addAll(denyEntityTypeIds);
        copy.unlockedSoulCategories.addAll(unlockedSoulCategories);
        copy.knownEntityTypes.addAll(knownEntityTypes);
        copy.unlockedWardTypes.addAll(unlockedWardTypes);
        wardRules.forEach((type, rule) -> copy.wardRules.put(type, rule.copy()));
        return copy;
    }

    public void sanitize() {
        trimSet(allowPlayers);
        trimSet(denyPlayers);
        trimEntityTypeSet(allowEntityTypeIds);
        trimEntityTypeSet(denyEntityTypeIds);
        trimEntityTypeSet(knownEntityTypes);
        allowPlayers.removeAll(denyPlayers);
        allowEntityTypeIds.removeAll(denyEntityTypeIds);
        unlockedSoulCategories.remove(SoulEssenceCategory.UNKNOWN);
        unlockedWardTypes.remove(WardType.NONE);
        wardRules.entrySet().removeIf(entry -> entry.getKey() == WardType.NONE);
        wardRules.values().forEach(WardingWardRule::sanitize);
    }

    public CompoundTag toTag() {
        sanitize();
        CompoundTag tag = new CompoundTag();
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
        tag.putBoolean("AffectHostileMobs", affectHostileMobs);
        tag.putBoolean("AffectPassiveMobs", affectPassiveMobs);
        tag.putBoolean("AffectNeutralMobs", affectNeutralMobs);
        tag.putBoolean("AffectVillagers", affectVillagers);
        tag.putBoolean("AffectPets", affectPets);
        tag.putBoolean("AffectPlayers", affectPlayers);
        tag.putBoolean("AffectOwner", affectOwner);
        tag.putBoolean("AffectOwnerWithNegativeWards", affectOwnerWithNegativeWards);
        tag.putBoolean("AffectBosses", affectBosses);
        tag.putBoolean("IgnoreNamedEntities", ignoreNamedEntities);
        tag.putBoolean("IgnoreTamedPets", ignoreTamedPets);
        tag.put("AllowPlayers", writeStringList(allowPlayers));
        tag.put("DenyPlayers", writeStringList(denyPlayers));
        tag.put("AllowEntityTypeIds", writeStringList(allowEntityTypeIds));
        tag.put("DenyEntityTypeIds", writeStringList(denyEntityTypeIds));
        tag.put("UnlockedSoulCategories", writeSoulCategories(unlockedSoulCategories));
        tag.put("KnownEntityTypes", writeStringList(knownEntityTypes));
        tag.put("UnlockedWardTypes", writeWardTypes(unlockedWardTypes));
        CompoundTag wardRulesTag = new CompoundTag();
        wardRules.forEach((type, rule) -> wardRulesTag.put(type.getSerializedName(), rule.toTag()));
        tag.put("WardRules", wardRulesTag);
        return tag;
    }

    public static WardingRuleSet fromTag(CompoundTag tag, UUID fallbackOwner) {
        WardingRuleSet rules = defaults(fallbackOwner);
        if (tag.contains("Owner")) {
            rules.owner = tag.getUUID("Owner");
        }
        if (tag.contains("AffectHostileMobs")) {
            rules.affectHostileMobs = tag.getBoolean("AffectHostileMobs");
        }
        if (tag.contains("AffectPassiveMobs")) {
            rules.affectPassiveMobs = tag.getBoolean("AffectPassiveMobs");
        }
        if (tag.contains("AffectNeutralMobs")) {
            rules.affectNeutralMobs = tag.getBoolean("AffectNeutralMobs");
        }
        if (tag.contains("AffectVillagers")) {
            rules.affectVillagers = tag.getBoolean("AffectVillagers");
        }
        if (tag.contains("AffectPets")) {
            rules.affectPets = tag.getBoolean("AffectPets");
        }
        if (tag.contains("AffectPlayers")) {
            rules.affectPlayers = tag.getBoolean("AffectPlayers");
        }
        if (tag.contains("AffectOwner")) {
            rules.affectOwner = tag.getBoolean("AffectOwner");
        }
        if (tag.contains("AffectOwnerWithNegativeWards")) {
            rules.affectOwnerWithNegativeWards = tag.getBoolean("AffectOwnerWithNegativeWards");
        }
        if (tag.contains("AffectBosses")) {
            rules.affectBosses = tag.getBoolean("AffectBosses");
        }
        if (tag.contains("IgnoreNamedEntities")) {
            rules.ignoreNamedEntities = tag.getBoolean("IgnoreNamedEntities");
        }
        if (tag.contains("IgnoreTamedPets")) {
            rules.ignoreTamedPets = tag.getBoolean("IgnoreTamedPets");
        }
        readStringList(tag, "AllowPlayers", rules.allowPlayers);
        readStringList(tag, "DenyPlayers", rules.denyPlayers);
        readStringList(tag, "AllowEntityTypeIds", rules.allowEntityTypeIds);
        readStringList(tag, "DenyEntityTypeIds", rules.denyEntityTypeIds);
        readSoulCategories(tag, "UnlockedSoulCategories", rules.unlockedSoulCategories);
        readStringList(tag, "KnownEntityTypes", rules.knownEntityTypes);
        readWardTypes(tag, "UnlockedWardTypes", rules.unlockedWardTypes);
        if (tag.contains("WardRules")) {
            CompoundTag wardRulesTag = tag.getCompound("WardRules");
            for (String key : wardRulesTag.getAllKeys()) {
                WardType type = WardType.bySerializedName(key);
                if (type != WardType.NONE) {
                    rules.wardRules.put(type, WardingWardRule.fromTag(wardRulesTag.getCompound(key)));
                }
            }
        }
        rules.sanitize();
        return rules;
    }

    public void encode(FriendlyByteBuf buffer) {
        sanitize();
        buffer.writeBoolean(owner != null);
        if (owner != null) {
            buffer.writeUUID(owner);
        }
        buffer.writeBoolean(affectHostileMobs);
        buffer.writeBoolean(affectPassiveMobs);
        buffer.writeBoolean(affectNeutralMobs);
        buffer.writeBoolean(affectVillagers);
        buffer.writeBoolean(affectPets);
        buffer.writeBoolean(affectPlayers);
        buffer.writeBoolean(affectOwner);
        buffer.writeBoolean(affectOwnerWithNegativeWards);
        buffer.writeBoolean(affectBosses);
        buffer.writeBoolean(ignoreNamedEntities);
        buffer.writeBoolean(ignoreTamedPets);
        writeStringSet(buffer, allowPlayers);
        writeStringSet(buffer, denyPlayers);
        writeStringSet(buffer, allowEntityTypeIds);
        writeStringSet(buffer, denyEntityTypeIds);
        writeSoulCategorySet(buffer, unlockedSoulCategories);
        writeStringSet(buffer, knownEntityTypes);
        writeWardTypeSet(buffer, unlockedWardTypes);
        buffer.writeVarInt(Math.min(wardRules.size(), MAX_LIST_ENTRIES));
        int written = 0;
        for (Map.Entry<WardType, WardingWardRule> entry : wardRules.entrySet()) {
            if (written >= MAX_LIST_ENTRIES) {
                break;
            }
            if (entry.getKey() != WardType.NONE) {
                buffer.writeUtf(entry.getKey().getSerializedName(), MAX_IDENTIFIER_LENGTH);
                entry.getValue().encode(buffer);
                written++;
            }
        }
    }

    public static WardingRuleSet decode(FriendlyByteBuf buffer) {
        WardingRuleSet rules = defaults(null);
        rules.owner = buffer.readBoolean() ? buffer.readUUID() : null;
        rules.affectHostileMobs = buffer.readBoolean();
        rules.affectPassiveMobs = buffer.readBoolean();
        rules.affectNeutralMobs = buffer.readBoolean();
        rules.affectVillagers = buffer.readBoolean();
        rules.affectPets = buffer.readBoolean();
        rules.affectPlayers = buffer.readBoolean();
        rules.affectOwner = buffer.readBoolean();
        rules.affectOwnerWithNegativeWards = buffer.readBoolean();
        rules.affectBosses = buffer.readBoolean();
        rules.ignoreNamedEntities = buffer.readBoolean();
        rules.ignoreTamedPets = buffer.readBoolean();
        readStringSet(buffer, rules.allowPlayers);
        readStringSet(buffer, rules.denyPlayers);
        readStringSet(buffer, rules.allowEntityTypeIds);
        readStringSet(buffer, rules.denyEntityTypeIds);
        readSoulCategorySet(buffer, rules.unlockedSoulCategories);
        readStringSet(buffer, rules.knownEntityTypes);
        readWardTypeSet(buffer, rules.unlockedWardTypes);
        int wardRuleCount = Math.min(buffer.readVarInt(), MAX_LIST_ENTRIES);
        for (int index = 0; index < wardRuleCount; index++) {
            WardType type = WardType.bySerializedName(buffer.readUtf(MAX_IDENTIFIER_LENGTH));
            WardingWardRule rule = WardingWardRule.decode(buffer);
            if (type != WardType.NONE) {
                rules.wardRules.put(type, rule);
            }
        }
        rules.sanitize();
        return rules;
    }

    public void addAllowedPlayer(String value) {
        String sanitized = sanitizeIdentifier(value);
        if (!sanitized.isBlank()) {
            allowPlayers.add(sanitized);
            denyPlayers.remove(sanitized);
            trimSet(allowPlayers);
        }
    }

    public void addDeniedPlayer(String value) {
        String sanitized = sanitizeIdentifier(value);
        if (!sanitized.isBlank()) {
            denyPlayers.add(sanitized);
            allowPlayers.remove(sanitized);
            trimSet(denyPlayers);
        }
    }

    public void clearPlayerLists() {
        allowPlayers.clear();
        denyPlayers.clear();
    }

    public void removePlayer(String value) {
        String sanitized = sanitizeIdentifier(value);
        if (!sanitized.isBlank()) {
            allowPlayers.remove(sanitized);
            denyPlayers.remove(sanitized);
        }
    }

    public void addAllowedEntityType(String value) {
        String sanitized = sanitizeIdentifier(value);
        if (isValidResourceId(sanitized)) {
            allowEntityTypeIds.add(sanitized);
            denyEntityTypeIds.remove(sanitized);
            trimEntityTypeSet(allowEntityTypeIds);
        }
    }

    public void addDeniedEntityType(String value) {
        String sanitized = sanitizeIdentifier(value);
        if (isValidResourceId(sanitized)) {
            denyEntityTypeIds.add(sanitized);
            allowEntityTypeIds.remove(sanitized);
            trimEntityTypeSet(denyEntityTypeIds);
        }
    }

    public void removeEntityType(String value) {
        String sanitized = sanitizeIdentifier(value);
        if (!sanitized.isBlank()) {
            allowEntityTypeIds.remove(sanitized);
            denyEntityTypeIds.remove(sanitized);
        }
    }

    public void clearEntityTypeLists() {
        allowEntityTypeIds.clear();
        denyEntityTypeIds.clear();
    }

    public boolean unlockSoul(SoulEssenceCategory category) {
        if (category == null || category == SoulEssenceCategory.UNKNOWN) {
            return false;
        }
        return unlockedSoulCategories.add(category);
    }

    public boolean knowsSoul(SoulEssenceCategory category) {
        return category != null && category != SoulEssenceCategory.UNKNOWN && unlockedSoulCategories.contains(category);
    }

    public boolean addKnownEntityType(String value) {
        String sanitized = sanitizeIdentifier(value);
        if (!isValidResourceId(sanitized)) {
            return false;
        }
        boolean added = knownEntityTypes.add(sanitized);
        trimEntityTypeSet(knownEntityTypes);
        return added;
    }

    public boolean unlockWard(WardType type) {
        if (type == null || type == WardType.NONE) {
            return false;
        }
        return unlockedWardTypes.add(type);
    }

    public boolean isWardUnlocked(WardType type) {
        return type != null && type != WardType.NONE && unlockedWardTypes.contains(type);
    }

    public WardingWardRule wardRule(WardType type) {
        if (type == null || type == WardType.NONE) {
            return new WardingWardRule();
        }
        return wardRules.computeIfAbsent(type, ignored -> new WardingWardRule());
    }

    public WardingWardRule wardRuleOrNull(WardType type) {
        return type == null || type == WardType.NONE ? null : wardRules.get(type);
    }

    public boolean matchesAllowedPlayer(UUID uuid, String name) {
        return matchesPlayer(allowPlayers, uuid, name);
    }

    public boolean matchesDeniedPlayer(UUID uuid, String name) {
        return matchesPlayer(denyPlayers, uuid, name);
    }

    public static String sanitizeIdentifier(String value) {
        if (value == null) {
            return "";
        }
        String sanitized = value.trim().toLowerCase(Locale.ROOT);
        if (sanitized.length() > MAX_IDENTIFIER_LENGTH) {
            sanitized = sanitized.substring(0, MAX_IDENTIFIER_LENGTH);
        }
        return sanitized;
    }

    public static boolean isValidResourceId(String value) {
        return value != null && value.contains(":") && ResourceLocation.tryParse(value) != null;
    }

    private static boolean matchesPlayer(Set<String> entries, UUID uuid, String name) {
        String uuidText = uuid == null ? "" : uuid.toString().toLowerCase(Locale.ROOT);
        String nameText = sanitizeIdentifier(name);
        return (!uuidText.isBlank() && entries.contains(uuidText)) || (!nameText.isBlank() && entries.contains(nameText));
    }

    public static ListTag writeStringList(Set<String> values) {
        ListTag list = new ListTag();
        int written = 0;
        for (String value : values) {
            if (written >= MAX_LIST_ENTRIES) {
                break;
            }
            String sanitized = sanitizeIdentifier(value);
            if (!sanitized.isBlank()) {
                list.add(StringTag.valueOf(sanitized));
                written++;
            }
        }
        return list;
    }

    public static void readStringList(CompoundTag tag, String key, Set<String> target) {
        target.clear();
        if (!tag.contains(key)) {
            return;
        }
        ListTag list = tag.getList(key, 8);
        for (int index = 0; index < list.size() && target.size() < MAX_LIST_ENTRIES; index++) {
            String value = sanitizeIdentifier(list.getString(index));
            if (!value.isBlank()) {
                target.add(value);
            }
        }
    }

    public static void writeStringSet(FriendlyByteBuf buffer, Set<String> values) {
        buffer.writeVarInt(Math.min(values.size(), MAX_LIST_ENTRIES));
        int written = 0;
        for (String value : values) {
            if (written >= MAX_LIST_ENTRIES) {
                break;
            }
            buffer.writeUtf(sanitizeIdentifier(value), MAX_IDENTIFIER_LENGTH);
            written++;
        }
    }

    public static void readStringSet(FriendlyByteBuf buffer, Set<String> target) {
        target.clear();
        int count = Math.min(buffer.readVarInt(), MAX_LIST_ENTRIES);
        for (int index = 0; index < count; index++) {
            String value = sanitizeIdentifier(buffer.readUtf(MAX_IDENTIFIER_LENGTH));
            if (!value.isBlank()) {
                target.add(value);
            }
        }
    }

    public static void trimSet(LinkedHashSet<String> target) {
        LinkedHashSet<String> trimmed = new LinkedHashSet<>();
        for (String value : target) {
            String sanitized = sanitizeIdentifier(value);
            if (!sanitized.isBlank()) {
                trimmed.add(sanitized);
            }
            if (trimmed.size() >= MAX_LIST_ENTRIES) {
                break;
            }
        }
        target.clear();
        target.addAll(trimmed);
    }

    public static void trimEntityTypeSet(LinkedHashSet<String> target) {
        LinkedHashSet<String> trimmed = new LinkedHashSet<>();
        for (String value : target) {
            String sanitized = sanitizeIdentifier(value);
            if (isValidResourceId(sanitized)) {
                trimmed.add(sanitized);
            }
            if (trimmed.size() >= MAX_LIST_ENTRIES) {
                break;
            }
        }
        target.clear();
        target.addAll(trimmed);
    }

    private static ListTag writeSoulCategories(Set<SoulEssenceCategory> values) {
        ListTag list = new ListTag();
        for (SoulEssenceCategory category : values) {
            if (category != SoulEssenceCategory.UNKNOWN) {
                list.add(StringTag.valueOf(category.getSerializedName()));
            }
        }
        return list;
    }

    private static void readSoulCategories(CompoundTag tag, String key, Set<SoulEssenceCategory> target) {
        target.clear();
        if (!tag.contains(key)) {
            return;
        }
        ListTag list = tag.getList(key, 8);
        for (int index = 0; index < list.size(); index++) {
            SoulEssenceCategory category = SoulEssenceCategory.bySerializedName(list.getString(index));
            if (category != SoulEssenceCategory.UNKNOWN) {
                target.add(category);
            }
        }
    }

    private static ListTag writeWardTypes(Set<WardType> values) {
        ListTag list = new ListTag();
        for (WardType type : values) {
            if (type != WardType.NONE) {
                list.add(StringTag.valueOf(type.getSerializedName()));
            }
        }
        return list;
    }

    private static void readWardTypes(CompoundTag tag, String key, Set<WardType> target) {
        target.clear();
        if (!tag.contains(key)) {
            return;
        }
        ListTag list = tag.getList(key, 8);
        for (int index = 0; index < list.size(); index++) {
            WardType type = WardType.bySerializedName(list.getString(index));
            if (type != WardType.NONE) {
                target.add(type);
            }
        }
    }

    private static void writeSoulCategorySet(FriendlyByteBuf buffer, Set<SoulEssenceCategory> values) {
        buffer.writeVarInt(values.size());
        for (SoulEssenceCategory category : values) {
            buffer.writeUtf(category.getSerializedName(), MAX_IDENTIFIER_LENGTH);
        }
    }

    private static void readSoulCategorySet(FriendlyByteBuf buffer, Set<SoulEssenceCategory> target) {
        target.clear();
        int count = Math.min(buffer.readVarInt(), MAX_LIST_ENTRIES);
        for (int index = 0; index < count; index++) {
            SoulEssenceCategory category = SoulEssenceCategory.bySerializedName(buffer.readUtf(MAX_IDENTIFIER_LENGTH));
            if (category != SoulEssenceCategory.UNKNOWN) {
                target.add(category);
            }
        }
    }

    private static void writeWardTypeSet(FriendlyByteBuf buffer, Set<WardType> values) {
        buffer.writeVarInt(values.size());
        for (WardType type : values) {
            buffer.writeUtf(type.getSerializedName(), MAX_IDENTIFIER_LENGTH);
        }
    }

    private static void readWardTypeSet(FriendlyByteBuf buffer, Set<WardType> target) {
        target.clear();
        int count = Math.min(buffer.readVarInt(), MAX_LIST_ENTRIES);
        for (int index = 0; index < count; index++) {
            WardType type = WardType.bySerializedName(buffer.readUtf(MAX_IDENTIFIER_LENGTH));
            if (type != WardType.NONE) {
                target.add(type);
            }
        }
    }

    public UUID owner() {
        return owner;
    }

    public void owner(UUID owner) {
        this.owner = owner;
    }

    public boolean affectHostileMobs() {
        return affectHostileMobs;
    }

    public void affectHostileMobs(boolean affectHostileMobs) {
        this.affectHostileMobs = affectHostileMobs;
    }

    public boolean affectPassiveMobs() {
        return affectPassiveMobs;
    }

    public void affectPassiveMobs(boolean affectPassiveMobs) {
        this.affectPassiveMobs = affectPassiveMobs;
    }

    public boolean affectNeutralMobs() {
        return affectNeutralMobs;
    }

    public void affectNeutralMobs(boolean affectNeutralMobs) {
        this.affectNeutralMobs = affectNeutralMobs;
    }

    public boolean affectVillagers() {
        return affectVillagers;
    }

    public void affectVillagers(boolean affectVillagers) {
        this.affectVillagers = affectVillagers;
    }

    public boolean affectPets() {
        return affectPets;
    }

    public void affectPets(boolean affectPets) {
        this.affectPets = affectPets;
    }

    public boolean affectPlayers() {
        return affectPlayers;
    }

    public void affectPlayers(boolean affectPlayers) {
        this.affectPlayers = affectPlayers;
    }

    public boolean affectOwner() {
        return affectOwner;
    }

    public void affectOwner(boolean affectOwner) {
        this.affectOwner = affectOwner;
    }

    public boolean affectOwnerWithNegativeWards() {
        return affectOwnerWithNegativeWards;
    }

    public void affectOwnerWithNegativeWards(boolean affectOwnerWithNegativeWards) {
        this.affectOwnerWithNegativeWards = affectOwnerWithNegativeWards;
    }

    public boolean affectBosses() {
        return affectBosses;
    }

    public void affectBosses(boolean affectBosses) {
        this.affectBosses = affectBosses;
    }

    public boolean ignoreNamedEntities() {
        return ignoreNamedEntities;
    }

    public void ignoreNamedEntities(boolean ignoreNamedEntities) {
        this.ignoreNamedEntities = ignoreNamedEntities;
    }

    public boolean ignoreTamedPets() {
        return ignoreTamedPets;
    }

    public void ignoreTamedPets(boolean ignoreTamedPets) {
        this.ignoreTamedPets = ignoreTamedPets;
    }

    public Set<String> allowPlayers() {
        return allowPlayers;
    }

    public Set<String> denyPlayers() {
        return denyPlayers;
    }

    public Set<String> allowEntityTypeIds() {
        return allowEntityTypeIds;
    }

    public Set<String> denyEntityTypeIds() {
        return denyEntityTypeIds;
    }

    public Set<SoulEssenceCategory> unlockedSoulCategories() {
        return unlockedSoulCategories;
    }

    public Set<String> knownEntityTypes() {
        return knownEntityTypes;
    }

    public Set<WardType> unlockedWardTypes() {
        return unlockedWardTypes;
    }

    public Map<WardType, WardingWardRule> wardRules() {
        return wardRules;
    }
}
