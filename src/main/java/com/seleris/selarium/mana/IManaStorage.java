package com.seleris.selarium.mana;

public interface IManaStorage {
    int receiveMana(int amount, boolean simulate);

    int extractMana(int amount, boolean simulate);

    int getStoredMana();

    int getManaCapacity();
}

