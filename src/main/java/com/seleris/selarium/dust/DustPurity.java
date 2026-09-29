package com.seleris.selarium.dust;

import java.util.Locale;
import java.util.Optional;

public enum DustPurity {
    BASIC("basic", 0),
    REFINED("refined", 1);

    private final String serializedName;
    private final int level;

    DustPurity(String serializedName, int level) {
        this.serializedName = serializedName;
        this.level = level;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public int getLevel() {
        return level;
    }

    public boolean isAtLeast(DustPurity other) {
        return level >= other.level;
    }

    public static Optional<DustPurity> fromSerializedName(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        for (DustPurity purity : values()) {
            if (purity.serializedName.equals(normalized)) {
                return Optional.of(purity);
            }
        }
        return Optional.empty();
    }
}

