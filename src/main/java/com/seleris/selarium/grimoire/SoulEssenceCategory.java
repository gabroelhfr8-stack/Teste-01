package com.seleris.selarium.grimoire;

import java.util.Locale;

public enum SoulEssenceCategory {
    MONSTROUS("monstrous"),
    BESTIAL("bestial"),
    ANTHROPIC("anthropic"),
    UNKNOWN("unknown");

    private final String serializedName;

    SoulEssenceCategory(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public String getTranslationKey() {
        return "soul.selarium." + serializedName;
    }

    public static SoulEssenceCategory bySerializedName(String name) {
        String normalized = name == null ? "" : name.toLowerCase(Locale.ROOT);
        for (SoulEssenceCategory category : values()) {
            if (category.serializedName.equals(normalized)) {
                return category;
            }
        }
        return UNKNOWN;
    }
}
