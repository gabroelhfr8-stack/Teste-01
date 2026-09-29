package com.seleris.selarium.ward;

public enum WardTier {
    BASIC(0),
    REFINED(1);

    private final int level;

    WardTier(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}

