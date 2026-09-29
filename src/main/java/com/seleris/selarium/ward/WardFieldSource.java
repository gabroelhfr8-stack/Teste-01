package com.seleris.selarium.ward;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.UUID;
import java.util.Set;

/** Server-owned state shared by placed sigils and temporary scroll projections. */
public interface WardFieldSource {
    Level getLevel();
    UUID getOwner();
    WardType getWardType();
    default int getRange() { return WardDefinitions.get(getWardType()).map(WardDefinition::range).orElse(1); }
    boolean isActive();
    int getWardDurationRemainingTicks();
    int getWardCooldownRemainingTicks();
    int getInternalManaBuffer();
    int extractInternalMana(int amount);
    int addInternalMana(int amount, int maxBuffer);
    void tickWardTimers();
    void deactivateWard(int cooldownTicks);
    long getLastUpkeepTick();
    void recordWardUpkeep(int cost, boolean paid, String debug);
    boolean hasRecentPaidUpkeep(int graceTicks);
    void recordWardDebug(int entitiesAffected, int blocksAffected, String debug);
    int getManaGeneratedThisActivation();
    void recordManaGeneratedThisActivation(int amount);
    int getAlertCooldownRemainingTicks();
    void setAlertCooldownRemainingTicks(int ticks);
    int getTemporaryWardBlockCount();
    boolean trackTemporaryWardBlock(BlockPos pos);
    boolean ownsTemporaryWardBlock(BlockPos pos);
    Set<BlockPos> temporaryWardBlocks();
    void cleanupTemporaryWardBlocks();
}
