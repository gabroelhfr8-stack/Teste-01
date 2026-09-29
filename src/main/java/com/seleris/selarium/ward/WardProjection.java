package com.seleris.selarium.ward;

import com.seleris.selarium.registry.SelariumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Saved server projection; mutable fields are dirtied by the owning SavedData. */
public final class WardProjection implements WardFieldSource {
    private final UUID id;
    private final UUID owner;
    private final WardType type;
    private final boolean mobile;
    private ResourceKey<Level> dimension;
    private BlockPos pos;
    private int remainingTicks;
    private int internalMana;
    private int generatedMana;
    private int alertCooldown;
    private long lastUpkeepTick = -1;
    private boolean lastUpkeepPaid;
    private boolean active = true;
    private boolean cooldownRecorded;
    private final Set<BlockPos> temporaryBlocks = new HashSet<>();
    private transient ServerLevel level;
    private transient Runnable dirty = () -> {};

    public WardProjection(UUID id, UUID owner, WardType type, boolean mobile,
                          ServerLevel level, BlockPos pos, int remainingTicks) {
        this.id = id;
        this.owner = owner;
        this.type = type;
        this.mobile = mobile;
        this.dimension = level.dimension();
        this.level = level;
        this.pos = pos.immutable();
        this.remainingTicks = Math.max(1, remainingTicks);
    }

    public UUID id() { return id; }
    public boolean mobile() { return mobile; }
    public ResourceKey<Level> dimension() { return dimension; }
    public BlockPos pos() { return pos; }
    public boolean cooldownRecorded() { return cooldownRecorded; }
    public void markCooldownRecorded() { cooldownRecorded = true; dirty.run(); }
    public void bind(ServerLevel level, Runnable dirty) { this.level = level; this.dirty = dirty; }
    public void move(ServerLevel level, BlockPos pos) {
        if (this.dimension != level.dimension() || !this.pos.equals(pos)) {
            this.dimension = level.dimension();
            this.pos = pos.immutable();
            this.level = level;
            dirty.run();
        }
    }

    @Override public Level getLevel() { return level; }
    @Override public UUID getOwner() { return owner; }
    @Override public WardType getWardType() { return type; }
    @Override public boolean isActive() { return active; }
    @Override public int getWardDurationRemainingTicks() { return remainingTicks; }
    @Override public int getWardCooldownRemainingTicks() { return 0; }
    @Override public int getInternalManaBuffer() { return internalMana; }
    @Override public int extractInternalMana(int amount) {
        int taken = Math.min(Math.max(0, amount), internalMana);
        internalMana -= taken;
        if (taken > 0) dirty.run();
        return taken;
    }
    @Override public int addInternalMana(int amount, int maxBuffer) {
        int accepted = Math.min(Math.max(0, amount), Math.max(0, maxBuffer - internalMana));
        internalMana += accepted;
        if (accepted > 0) dirty.run();
        return accepted;
    }
    @Override public void tickWardTimers() {
        if (alertCooldown > 0) alertCooldown--;
        if (active && remainingTicks > 0) remainingTicks--;
        if (level != null && level.getGameTime() % 20 == 0) dirty.run();
    }
    @Override public void deactivateWard(int cooldownTicks) {
        if (!active) return;
        active = false;
        remainingTicks = 0;
        cleanupTemporaryWardBlocks();
        dirty.run();
    }
    @Override public long getLastUpkeepTick() { return lastUpkeepTick; }
    @Override public void recordWardUpkeep(int cost, boolean paid, String debug) {
        lastUpkeepTick = level == null ? -1 : level.getGameTime();
        lastUpkeepPaid = paid;
        dirty.run();
    }
    @Override public boolean hasRecentPaidUpkeep(int graceTicks) {
        return lastUpkeepPaid && level != null && lastUpkeepTick >= 0
                && level.getGameTime() - lastUpkeepTick <= Math.max(1, graceTicks);
    }
    @Override public void recordWardDebug(int entitiesAffected, int blocksAffected, String debug) { }
    @Override public int getManaGeneratedThisActivation() { return generatedMana; }
    @Override public void recordManaGeneratedThisActivation(int amount) {
        if (amount > 0) { generatedMana += amount; dirty.run(); }
    }
    @Override public int getAlertCooldownRemainingTicks() { return alertCooldown; }
    @Override public void setAlertCooldownRemainingTicks(int ticks) { alertCooldown = Math.max(0, ticks); dirty.run(); }
    @Override public int getTemporaryWardBlockCount() { return temporaryBlocks.size(); }
    @Override public boolean trackTemporaryWardBlock(BlockPos pos) {
        boolean added = temporaryBlocks.add(pos.immutable());
        if (added) dirty.run();
        return added;
    }
    @Override public boolean ownsTemporaryWardBlock(BlockPos pos) { return temporaryBlocks.contains(pos); }
    @Override public Set<BlockPos> temporaryWardBlocks() { return Set.copyOf(temporaryBlocks); }
    @Override public void cleanupTemporaryWardBlocks() {
        if (level == null) return;
        temporaryBlocks.removeIf(blockPos -> {
            if (!level.isLoaded(blockPos)) return false;
            BlockState state = level.getBlockState(blockPos);
            if (state.is(SelariumBlocks.TEMPORARY_CITADEL_WALL.get())
                    || state.is(SelariumBlocks.TANGIBLE_BARRIER_BLOCK.get())) {
                level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            return true;
        });
        dirty.run();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putUUID("Owner", owner);
        tag.putString("Type", type.getSerializedName());
        tag.putBoolean("Mobile", mobile);
        tag.putString("Dimension", dimension.location().toString());
        tag.putInt("X", pos.getX()); tag.putInt("Y", pos.getY()); tag.putInt("Z", pos.getZ());
        tag.putInt("Remaining", remainingTicks);
        tag.putInt("InternalMana", internalMana);
        tag.putInt("GeneratedMana", generatedMana);
        tag.putInt("AlertCooldown", alertCooldown);
        tag.putLong("LastUpkeepTick", lastUpkeepTick);
        tag.putBoolean("LastUpkeepPaid", lastUpkeepPaid);
        tag.putBoolean("Active", active);
        tag.putBoolean("CooldownRecorded", cooldownRecorded);
        ListTag blocks = new ListTag();
        for (BlockPos block : temporaryBlocks) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("X", block.getX()); posTag.putInt("Y", block.getY()); posTag.putInt("Z", block.getZ());
            blocks.add(posTag);
        }
        tag.put("TemporaryBlocks", blocks);
        return tag;
    }

    public static WardProjection load(CompoundTag tag, ServerLevel level) {
        WardType type = WardType.bySerializedName(tag.getString("Type"));
        if (type == WardType.NONE || !tag.hasUUID("Id") || !tag.hasUUID("Owner")) return null;
        WardProjection projection = new WardProjection(tag.getUUID("Id"), tag.getUUID("Owner"), type,
                tag.getBoolean("Mobile"), level,
                new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z")),
                Math.max(1, tag.getInt("Remaining")));
        ResourceLocation dimension = ResourceLocation.tryParse(tag.getString("Dimension"));
        if (dimension != null) projection.dimension = ResourceKey.create(Registries.DIMENSION, dimension);
        projection.remainingTicks = Math.max(0, tag.getInt("Remaining"));
        projection.internalMana = Math.max(0, tag.getInt("InternalMana"));
        projection.generatedMana = Math.max(0, tag.getInt("GeneratedMana"));
        projection.alertCooldown = Math.max(0, tag.getInt("AlertCooldown"));
        projection.lastUpkeepTick = tag.getLong("LastUpkeepTick");
        projection.lastUpkeepPaid = tag.getBoolean("LastUpkeepPaid");
        projection.active = tag.getBoolean("Active");
        projection.cooldownRecorded = tag.getBoolean("CooldownRecorded");
        ListTag blocks = tag.getList("TemporaryBlocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < blocks.size(); i++) {
            CompoundTag block = blocks.getCompound(i);
            projection.temporaryBlocks.add(new BlockPos(block.getInt("X"), block.getInt("Y"), block.getInt("Z")));
        }
        return projection;
    }
}
