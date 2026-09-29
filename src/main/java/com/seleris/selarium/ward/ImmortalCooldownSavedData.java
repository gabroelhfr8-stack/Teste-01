package com.seleris.selarium.ward;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Per-target death prevention cooldown survives server restarts. */
public final class ImmortalCooldownSavedData extends SavedData {
    private static final String DATA_NAME = "selarium_immortal_cooldowns";
    private final Map<UUID, Long> endings = new HashMap<>();

    public static ImmortalCooldownSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                ImmortalCooldownSavedData::load, ImmortalCooldownSavedData::new, DATA_NAME);
    }

    public static ImmortalCooldownSavedData load(CompoundTag tag) {
        ImmortalCooldownSavedData data = new ImmortalCooldownSavedData();
        CompoundTag entries = tag.getCompound("Targets");
        for (String key : entries.getAllKeys()) {
            try { data.endings.put(UUID.fromString(key), entries.getLong(key)); }
            catch (IllegalArgumentException ignored) { }
        }
        return data;
    }

    @Override public CompoundTag save(CompoundTag tag) {
        CompoundTag entries = new CompoundTag();
        endings.forEach((target, end) -> entries.putLong(target.toString(), end));
        tag.put("Targets", entries);
        return tag;
    }

    public boolean canProtect(UUID target, long now) {
        return endings.getOrDefault(target, 0L) <= now;
    }

    public void protectedUntil(UUID target, long end) {
        endings.put(target, end);
        setDirty();
    }
}
