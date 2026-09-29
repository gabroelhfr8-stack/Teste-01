package com.seleris.selarium.mana;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public interface IPlayerMana {
    int getCurrentMana();

    int getMaxMana();

    double getManaExperience();

    long getTotalManaSpent();

    long getTotalManaRegenerated();

    int getUnlockedManaTier();

    int getTicksUntilNextRegen();

    void setCurrentMana(int amount);

    void setMaxMana(int amount);

    void setManaExperience(double amount);

    void setUnlockedManaTier(int tier);

    int addMana(int amount);

    int drainMana(int amount, boolean countsAsSpent);

    boolean consumeMana(int amount);

    void tickRegen(ServerPlayer player);

    void copyFrom(IPlayerMana other);

    CompoundTag serializeNBT();

    void deserializeNBT(CompoundTag tag);
}

