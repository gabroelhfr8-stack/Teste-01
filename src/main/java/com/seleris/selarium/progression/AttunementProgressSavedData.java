package com.seleris.selarium.progression;

import com.seleris.selarium.ward.WardTier;
import com.seleris.selarium.ward.WardType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Permanent discoveries are independent from the editable Grimoire rules. */
public final class AttunementProgressSavedData extends SavedData {
    private static final String DATA_NAME = "selarium_attunement_progress";
    private final Map<UUID, Discoveries> discoveries = new HashMap<>();

    public static AttunementProgressSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                AttunementProgressSavedData::load, AttunementProgressSavedData::new, DATA_NAME);
    }

    public static AttunementProgressSavedData load(CompoundTag tag) {
        AttunementProgressSavedData data = new AttunementProgressSavedData();
        CompoundTag players = tag.getCompound("Players");
        for (String key : players.getAllKeys()) {
            try {
                UUID owner = UUID.fromString(key);
                CompoundTag player = players.getCompound(key);
                Discoveries entry = new Discoveries();
                read(player.getList("Activated", Tag.TAG_STRING), entry.activated);
                read(player.getList("Refined", Tag.TAG_STRING), entry.refined);
                entry.refined.retainAll(entry.activated);
                data.discoveries.put(owner, entry);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag players = new CompoundTag();
        discoveries.forEach((owner, entry) -> {
            CompoundTag player = new CompoundTag();
            player.put("Activated", write(entry.activated));
            player.put("Refined", write(entry.refined));
            players.put(owner.toString(), player);
        });
        tag.put("Players", players);
        return tag;
    }

    public boolean record(UUID owner, WardType type, WardTier tier) {
        if (owner == null || type == null || type == WardType.NONE) {
            return false;
        }
        Discoveries entry = discoveries.computeIfAbsent(owner, ignored -> new Discoveries());
        boolean changed = entry.activated.add(type);
        if (tier == WardTier.REFINED) {
            changed |= entry.refined.add(type);
        }
        if (changed) setDirty();
        return changed;
    }

    public boolean knows(UUID owner, WardType type) {
        Discoveries entry = discoveries.get(owner);
        return entry != null && entry.activated.contains(type);
    }

    public int distinctCount(UUID owner) {
        Discoveries entry = discoveries.get(owner);
        return entry == null ? 0 : entry.activated.size();
    }

    public int refinedCount(UUID owner) {
        Discoveries entry = discoveries.get(owner);
        return entry == null ? 0 : entry.refined.size();
    }

    public Set<WardType> activated(UUID owner) {
        Discoveries entry = discoveries.get(owner);
        return entry == null ? Set.of() : Set.copyOf(entry.activated);
    }

    private static ListTag write(Set<WardType> values) {
        ListTag list = new ListTag();
        values.forEach(type -> list.add(StringTag.valueOf(type.getSerializedName())));
        return list;
    }

    private static void read(ListTag list, Set<WardType> target) {
        for (int i = 0; i < list.size(); i++) {
            WardType type = WardType.bySerializedName(list.getString(i));
            if (type != WardType.NONE) target.add(type);
        }
    }

    private static final class Discoveries {
        private final EnumSet<WardType> activated = EnumSet.noneOf(WardType.class);
        private final EnumSet<WardType> refined = EnumSet.noneOf(WardType.class);
    }
}
