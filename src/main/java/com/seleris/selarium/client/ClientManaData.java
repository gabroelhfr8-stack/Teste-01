package com.seleris.selarium.client;

public final class ClientManaData {
    private static int currentMana;
    private static int maxMana = 1;
    private static double manaExperience;
    private static int unlockedTier;
    private static int flashTicks;
    private static int flashDirection;
    private static boolean initialized;

    private ClientManaData() {
    }

    public static void update(int current, int max, double experience, int tier) {
        if (initialized && current != currentMana) {
            flashTicks = 30;
            flashDirection = Integer.compare(current, currentMana);
        }

        currentMana = Math.max(0, current);
        maxMana = Math.max(1, max);
        manaExperience = Math.max(0.0D, experience);
        unlockedTier = Math.max(0, tier);
        initialized = true;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static int getCurrentMana() {
        return currentMana;
    }

    public static int getMaxMana() {
        return maxMana;
    }

    public static double getManaExperience() {
        return manaExperience;
    }

    public static int getUnlockedTier() {
        return unlockedTier;
    }

    public static int getFlashTicks() {
        return flashTicks;
    }

    public static int getFlashDirection() {
        return flashDirection;
    }

    public static void tickHud() {
        if (flashTicks > 0) {
            flashTicks--;
        }
    }
}

