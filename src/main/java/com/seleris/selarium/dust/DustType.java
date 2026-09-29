package com.seleris.selarium.dust;

import java.util.Locale;
import java.util.Optional;

public enum DustType {
    ARCANE("arcane"),
    AEGIS("aegis"),
    VITAL("vital"),
    FOCUS("focus"),
    BINDING("binding"),
    ECHO("echo"),
    DENSITY("density"),
    WARP("warp"),
    VEIL("veil"),
    CHRONO("chrono");

    private final String serializedName;

    DustType(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static Optional<DustType> fromSerializedName(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        for (DustType type : values()) {
            if (type.serializedName.equals(normalized)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}

