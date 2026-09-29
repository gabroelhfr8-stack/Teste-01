package com.seleris.selarium.ward;

/** Gameplay role of a ward; drives colour, documentation grouping and default field visuals. */
public enum WardCategory {
    SOURCE("source"),
    DETECTION("detection"),
    BUFF("buff"),
    UTILITY("utility"),
    HOSTILE("hostile"),
    STRUCTURE("structure"),
    EVENT("event");

    private final String serializedName;

    WardCategory(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public String getTranslationKey() {
        return "ward_category.selarium." + serializedName;
    }
}
