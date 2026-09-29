package com.seleris.selarium.grimoire;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class WardingGrimoireSavedData extends SavedData {
    private static final String DATA_NAME = "selarium_warding_rules";
    private final Map<UUID, WardingRuleSet> rulesByOwner = new LinkedHashMap<>();

    public static WardingGrimoireSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(WardingGrimoireSavedData::load, WardingGrimoireSavedData::new, DATA_NAME);
    }

    public static WardingGrimoireSavedData load(CompoundTag tag) {
        WardingGrimoireSavedData data = new WardingGrimoireSavedData();
        CompoundTag rules = tag.getCompound("Rules");
        for (String key : rules.getAllKeys()) {
            try {
                UUID owner = UUID.fromString(key);
                data.rulesByOwner.put(owner, WardingRuleSet.fromTag(rules.getCompound(key), owner));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag rules = new CompoundTag();
        rulesByOwner.forEach((owner, ruleSet) -> rules.put(owner.toString(), ruleSet.toTag()));
        tag.put("Rules", rules);
        return tag;
    }

    public WardingRuleSet getRules(UUID owner) {
        if (owner == null) {
            return WardingRuleSet.defaults(null);
        }
        return rulesByOwner.getOrDefault(owner, WardingRuleSet.defaults(owner)).copy();
    }

    public void putRules(WardingRuleSet rules) {
        if (rules.owner() == null) {
            return;
        }
        WardingRuleSet copy = rules.copy();
        copy.sanitize();
        rulesByOwner.put(copy.owner(), copy);
        setDirty();
    }

    public void resetRules(UUID owner) {
        if (owner == null) {
            return;
        }
        rulesByOwner.put(owner, WardingRuleSet.defaults(owner));
        setDirty();
    }
}
