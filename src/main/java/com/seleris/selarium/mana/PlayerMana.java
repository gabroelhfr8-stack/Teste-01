package com.seleris.selarium.mana;

import com.seleris.selarium.config.SelariumCommonConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class PlayerMana implements IPlayerMana {
    private static final int MIN_MANA_TIER = 0;
    private static final int MAX_MANA_TIER = 4;

    private int currentMana;
    private int maxMana;
    private double manaExperience;
    private long totalManaSpent;
    private long totalManaRegenerated;
    private int unlockedManaTier;
    private int ticksUntilNextRegen;
    private int recentManaSpent;
    private int pendingGrowthRegeneratedMana;

    public PlayerMana() {
        this.maxMana = clamp(SelariumCommonConfig.INITIAL_MAX_MANA.get(), 1, getCurrentSoftCap());
        this.currentMana = clamp(SelariumCommonConfig.INITIAL_CURRENT_MANA.get(), 0, this.maxMana);
        this.ticksUntilNextRegen = Math.max(1, SelariumCommonConfig.MANA_REGEN_INTERVAL_TICKS.get());
    }

    @Override
    public int getCurrentMana() {
        return currentMana;
    }

    @Override
    public int getMaxMana() {
        return maxMana;
    }

    @Override
    public double getManaExperience() {
        return manaExperience;
    }

    @Override
    public long getTotalManaSpent() {
        return totalManaSpent;
    }

    @Override
    public long getTotalManaRegenerated() {
        return totalManaRegenerated;
    }

    @Override
    public int getUnlockedManaTier() {
        return unlockedManaTier;
    }

    @Override
    public int getTicksUntilNextRegen() {
        return ticksUntilNextRegen;
    }

    @Override
    public void setCurrentMana(int amount) {
        currentMana = clamp(amount, 0, maxMana);
    }

    @Override
    public void setMaxMana(int amount) {
        maxMana = clamp(amount, 1, getCurrentSoftCap());
        sanitizeState();
    }

    @Override
    public void setManaExperience(double amount) {
        manaExperience = Math.max(0.0D, amount);
        processGrowth();
    }

    @Override
    public void setUnlockedManaTier(int tier) {
        unlockedManaTier = clamp(tier, MIN_MANA_TIER, MAX_MANA_TIER);
        processGrowth();
    }

    @Override
    public int addMana(int amount) {
        if (amount <= 0 || currentMana >= maxMana) {
            return 0;
        }

        int accepted = Math.min(amount, maxMana - currentMana);
        currentMana += accepted;
        return accepted;
    }

    @Override
    public int drainMana(int amount, boolean countsAsSpent) {
        if (amount <= 0 || currentMana <= 0) {
            return 0;
        }

        int drained = Math.min(amount, currentMana);
        currentMana -= drained;
        if (countsAsSpent) {
            registerManaSpent(drained);
        }
        return drained;
    }

    @Override
    public boolean consumeMana(int amount) {
        if (amount <= 0) {
            return true;
        }

        if (currentMana < amount) {
            return false;
        }

        currentMana -= amount;
        registerManaSpent(amount);
        return true;
    }

    @Override
    public void tickRegen(ServerPlayer player) {
        ticksUntilNextRegen--;
        if (ticksUntilNextRegen > 0) {
            return;
        }

        ticksUntilNextRegen = Math.max(1, SelariumCommonConfig.MANA_REGEN_INTERVAL_TICKS.get());
        regenerateConfiguredAmount();
    }

    @Override
    public void copyFrom(IPlayerMana other) {
        currentMana = other.getCurrentMana();
        maxMana = other.getMaxMana();
        manaExperience = other.getManaExperience();
        totalManaSpent = other.getTotalManaSpent();
        totalManaRegenerated = other.getTotalManaRegenerated();
        unlockedManaTier = clamp(other.getUnlockedManaTier(), MIN_MANA_TIER, MAX_MANA_TIER);
        ticksUntilNextRegen = Math.max(1, other.getTicksUntilNextRegen());
        if (other instanceof PlayerMana playerMana) {
            recentManaSpent = playerMana.recentManaSpent;
            pendingGrowthRegeneratedMana = playerMana.pendingGrowthRegeneratedMana;
        }
        sanitizeState();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("CurrentMana", currentMana);
        tag.putInt("MaxMana", maxMana);
        tag.putDouble("ManaExperience", manaExperience);
        tag.putLong("TotalManaSpent", totalManaSpent);
        tag.putLong("TotalManaRegenerated", totalManaRegenerated);
        tag.putInt("UnlockedManaTier", unlockedManaTier);
        tag.putInt("TicksUntilNextRegen", ticksUntilNextRegen);
        tag.putInt("RecentManaSpent", recentManaSpent);
        tag.putInt("PendingGrowthRegeneratedMana", pendingGrowthRegeneratedMana);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (!tag.contains("MaxMana")) {
            sanitizeState();
            return;
        }

        unlockedManaTier = clamp(tag.getInt("UnlockedManaTier"), MIN_MANA_TIER, MAX_MANA_TIER);
        maxMana = clamp(tag.getInt("MaxMana"), 1, getCurrentSoftCap());
        currentMana = clamp(tag.getInt("CurrentMana"), 0, maxMana);
        manaExperience = Math.max(0.0D, tag.getDouble("ManaExperience"));
        totalManaSpent = Math.max(0L, tag.getLong("TotalManaSpent"));
        totalManaRegenerated = Math.max(0L, tag.getLong("TotalManaRegenerated"));
        ticksUntilNextRegen = Math.max(1, tag.getInt("TicksUntilNextRegen"));
        recentManaSpent = Math.max(0, tag.getInt("RecentManaSpent"));
        pendingGrowthRegeneratedMana = Math.max(0, tag.getInt("PendingGrowthRegeneratedMana"));
        sanitizeState();
        processGrowth();
    }

    private void regenerateConfiguredAmount() {
        int amount = Math.max(0, SelariumCommonConfig.BASE_MANA_REGEN_AMOUNT.get());
        if (amount <= 0 || currentMana >= maxMana) {
            return;
        }

        int regenerated = Math.min(amount, maxMana - currentMana);
        currentMana += regenerated;
        totalManaRegenerated += regenerated;
        applyGrowthFromRegeneration(regenerated);
    }

    private void applyGrowthFromRegeneration(int regenerated) {
        if (regenerated <= 0) {
            return;
        }

        int eligibleRegenerated = Math.min(regenerated, recentManaSpent);
        if (eligibleRegenerated <= 0) {
            return;
        }

        recentManaSpent -= eligibleRegenerated;
        if (!SelariumCommonConfig.MANA_GROWTH_ENABLED.get()) {
            pendingGrowthRegeneratedMana = 0;
            return;
        }

        pendingGrowthRegeneratedMana += eligibleRegenerated;
        int batchSize = Math.max(1, SelariumCommonConfig.MANA_GROWTH_CREDIT_BATCH_SIZE.get());
        int creditedMana = pendingGrowthRegeneratedMana / batchSize * batchSize;
        if (creditedMana <= 0) {
            return;
        }

        pendingGrowthRegeneratedMana -= creditedMana;
        manaExperience += creditedMana * Math.max(0.0D, SelariumCommonConfig.MANA_EXPERIENCE_PER_REGENERATED_MANA.get());
        processGrowth();
    }

    private void processGrowth() {
        sanitizeState();
        int softCap = getCurrentSoftCap();
        double requiredExperience = Math.max(0.001D, SelariumCommonConfig.MANA_EXPERIENCE_REQUIRED_PER_MAX_MANA.get());
        int gainStep = Math.max(1, SelariumCommonConfig.MAX_MANA_GAIN_PER_GROWTH_STEP.get());

        while (maxMana < softCap && manaExperience >= requiredExperience) {
            int gain = Math.min(gainStep, softCap - maxMana);
            maxMana += gain;
            manaExperience -= requiredExperience;
        }

        currentMana = clamp(currentMana, 0, maxMana);
    }

    private void registerManaSpent(int amount) {
        if (amount <= 0) {
            return;
        }

        totalManaSpent += amount;
        recentManaSpent = Math.min(Math.max(0, maxMana), recentManaSpent + amount);
    }

    private void sanitizeState() {
        unlockedManaTier = clamp(unlockedManaTier, MIN_MANA_TIER, MAX_MANA_TIER);
        maxMana = clamp(maxMana, 1, getCurrentSoftCap());
        currentMana = clamp(currentMana, 0, maxMana);
        manaExperience = Math.max(0.0D, manaExperience);
        ticksUntilNextRegen = Math.max(1, ticksUntilNextRegen);
        recentManaSpent = clamp(recentManaSpent, 0, maxMana);
        pendingGrowthRegeneratedMana = Math.max(0, pendingGrowthRegeneratedMana);
    }

    private int getCurrentSoftCap() {
        return Math.max(1, SelariumCommonConfig.getSoftCapForTier(unlockedManaTier));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
