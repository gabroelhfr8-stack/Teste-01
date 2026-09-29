package com.seleris.selarium.ward;

import com.seleris.selarium.network.ProjectionSyncPacket;
import com.seleris.selarium.network.SelariumNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.phys.Vec3;

/** Persists all scroll fields and their cooldowns in the overworld save. */
public final class WardProjectionSavedData extends SavedData {
    private static final String DATA_NAME = "selarium_scroll_fields";
    private final Map<UUID, WardProjection> projections = new HashMap<>();
    private final Map<UUID, Map<WardType, Long>> cooldowns = new HashMap<>();

    public static WardProjectionSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(tag -> load(tag, overworld),
                WardProjectionSavedData::new, DATA_NAME);
    }

    public static WardProjectionSavedData load(CompoundTag tag, ServerLevel level) {
        WardProjectionSavedData data = new WardProjectionSavedData();
        ListTag fields = tag.getList("Fields", Tag.TAG_COMPOUND);
        for (int i = 0; i < fields.size(); i++) {
            WardProjection projection = WardProjection.load(fields.getCompound(i), level);
            if (projection != null) {
                projection.bind(level.getServer().getLevel(projection.dimension()), data::setDirty);
                data.projections.put(projection.id(), projection);
            }
        }
        CompoundTag cooldownTag = tag.getCompound("Cooldowns");
        for (String ownerKey : cooldownTag.getAllKeys()) {
            try {
                UUID owner = UUID.fromString(ownerKey);
                Map<WardType, Long> byType = new HashMap<>();
                CompoundTag values = cooldownTag.getCompound(ownerKey);
                for (String typeKey : values.getAllKeys()) {
                    WardType type = WardType.bySerializedName(typeKey);
                    if (type != WardType.NONE) byType.put(type, values.getLong(typeKey));
                }
                data.cooldowns.put(owner, byType);
            } catch (IllegalArgumentException ignored) { }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag fields = new ListTag();
        projections.values().forEach(projection -> fields.add(projection.save()));
        tag.put("Fields", fields);
        CompoundTag cooldownTag = new CompoundTag();
        cooldowns.forEach((owner, values) -> {
            CompoundTag byType = new CompoundTag();
            values.forEach((type, end) -> byType.putLong(type.getSerializedName(), end));
            cooldownTag.put(owner.toString(), byType);
        });
        tag.put("Cooldowns", cooldownTag);
        return tag;
    }

    public CastResult cast(ServerPlayer player, WardDefinition definition, BlockPos target, boolean mobile) {
        int mobileCount = 0;
        int fixedCount = 0;
        for (WardProjection existing : projections.values()) {
            if (!existing.isActive() || !player.getUUID().equals(existing.getOwner())) continue;
            if (existing.getWardType() == definition.type()) return CastResult.fail("message.selarium.scroll.duplicate");
            if (existing.mobile()) mobileCount++; else fixedCount++;
        }
        if ((mobile && mobileCount >= 2) || (!mobile && fixedCount >= 3)) {
            return CastResult.fail("message.selarium.scroll.limit");
        }
        ServerLevel level = player.serverLevel();
        if (cooldownRemaining(player.getUUID(), definition.type(), level) > 0) {
            return CastResult.fail("message.selarium.ward.cooldown");
        }
        if (!level.isLoaded(target)) return CastResult.fail("message.selarium.scroll.unloaded");
        WardProjection projection = new WardProjection(UUID.randomUUID(), player.getUUID(), definition.type(),
                mobile, level, target, Math.max(1, definition.durationTicks() / 2));
        projection.bind(level, this::setDirty);
        int firstCost = definition.activationCostValue() + definition.upkeepCostValue();
        if (!WardManaService.consume(level, target, projection, firstCost)) {
            return CastResult.fail("message.selarium.ward.not_enough_mana");
        }
        projection.recordWardUpkeep(definition.upkeepCostValue(), true, "first scroll charge paid");
        projections.put(projection.id(), projection);
        ActiveWardIndex.updateProjection(projection);
        setDirty();
        return CastResult.ok();
    }

    public int cooldownRemaining(UUID owner, WardType type, ServerLevel level) {
        long end = cooldowns.getOrDefault(owner, Map.of()).getOrDefault(type, 0L);
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, end - level.getServer().overworld().getGameTime()));
    }

    public void stopMobile(UUID owner, ServerLevel level) {
        for (WardProjection projection : new ArrayList<>(projections.values())) {
            if (projection.mobile() && owner.equals(projection.getOwner()) && projection.isActive()) {
                finish(projection, level);
            }
        }
    }

    public void tick(ServerLevel overworld) {
        long now = overworld.getGameTime();
        for (WardProjection projection : new ArrayList<>(projections.values())) {
            ServerLevel level = overworld.getServer().getLevel(projection.dimension());
            if (projection.mobile()) {
                ServerPlayer owner = overworld.getServer().getPlayerList().getPlayer(projection.getOwner());
                if (owner == null) {
                    ActiveWardIndex.removeProjection(projection.id());
                    continue; // Duration is paused while the creator is disconnected.
                }
                level = owner.serverLevel();
                projection.move(level, owner.blockPosition());
            }
            if (level == null) continue;
            projection.bind(level, this::setDirty);
            if (!projection.isActive()) {
                finish(projection, overworld);
                continue;
            }
            WardDefinition definition = WardDefinitions.get(projection.getWardType()).orElse(null);
            if (definition == null) {
                finish(projection, overworld);
                continue;
            }
            if (level.isLoaded(projection.pos())) {
                WardManager.tick(new WardContext(level, projection.pos(), level.getBlockState(projection.pos()), projection));
                if (now % 20 == 0) {
                    level.sendParticles(ParticleTypes.ENCHANT, projection.pos().getX() + 0.5,
                            projection.pos().getY() + 0.5, projection.pos().getZ() + 0.5,
                            8, 0.6, 0.4, 0.6, 0.02);
                }
            } else {
                projection.tickWardTimers(); // Fixed fields still expire in unloaded chunks.
                if (projection.getWardDurationRemainingTicks() <= 0) projection.deactivateWard(definition.cooldownTicks());
            }
            if (!projection.isActive()) finish(projection, overworld);
            else ActiveWardIndex.updateProjection(projection);
        }
        if (now % 1200 == 0) {
            cooldowns.values().forEach(values -> values.values().removeIf(end -> end <= now));
            setDirty();
        }
        if (now % 20 == 0) sendDisplaySnapshots(overworld);
    }

    private void sendDisplaySnapshots(ServerLevel overworld) {
        for (ServerPlayer player : overworld.getServer().getPlayerList().getPlayers()) {
            ServerLevel level = player.serverLevel();
            List<ProjectionSyncPacket.View> views = projections.values().stream()
                    .filter(projection -> projection.isActive() && projection.dimension() == level.dimension()
                            && projection.pos().distToCenterSqr(player.getX(), player.getY(), player.getZ()) <= 160 * 160
                            && level.isLoaded(projection.pos()))
                    .limit(256)
                    .map(projection -> new ProjectionSyncPacket.View(projection.getOwner(), projection.getWardType(),
                            projection.pos(), projection.getRange(), projection.getWardDurationRemainingTicks(),
                            projection.mobile()))
                    .toList();
            Set<BlockPos> passable = new HashSet<>();
            for (ActiveWardIndex.WardInstance ward : ActiveWardIndex.findAll(level, player.position(), 160)) {
                if ((ward.type() == WardType.CITADEL || ward.type() == WardType.TANGIBLE)
                        && WardAccessService.isAlly(ward.sigil(), player)) {
                    for (BlockPos block : ward.sigil().temporaryWardBlocks()) {
                        if (passable.size() >= 4096) break;
                        if (block.distToCenterSqr(player.getX(), player.getY(), player.getZ()) <= 64 * 64) {
                            passable.add(block);
                        }
                    }
                }
            }
            SelariumNetwork.sendProjectionSync(player, new ProjectionSyncPacket(player.getUUID(),
                    level.dimension().location(), views, Set.copyOf(passable)));
        }
    }

    private void finish(WardProjection projection, ServerLevel overworld) {
        if (projection.isActive()) projection.deactivateWard(0);
        if (!projection.cooldownRecorded()) {
            int duration = WardDefinitions.get(projection.getWardType()).map(WardDefinition::cooldownTicks).orElse(0);
            cooldowns.computeIfAbsent(projection.getOwner(), ignored -> new HashMap<>())
                    .put(projection.getWardType(), overworld.getServer().overworld().getGameTime() + duration);
            projection.markCooldownRecorded();
        }
        projection.cleanupTemporaryWardBlocks();
        ActiveWardIndex.removeProjection(projection.id());
        if (projection.getTemporaryWardBlockCount() == 0) projections.remove(projection.id());
        setDirty();
    }

    public record CastResult(boolean success, String messageKey) {
        public static CastResult ok() { return new CastResult(true, ""); }
        public static CastResult fail(String key) { return new CastResult(false, key); }
    }
}
