package com.seleris.selarium.ward;

import java.util.Locale;

public enum WardType {
    NONE("none"),
    AMBIENT_MANA("ambient_mana"),
    WHISPERING("whispering"),
    SPECTRAL("spectral"),
    BULWARK("bulwark"),
    REJUVENATION("rejuvenation"),
    FEATHERWEIGHT("featherweight"),
    GROUNDING("grounding"),
    MAGNETISM("magnetism"),
    BANISHMENT("banishment"),
    ECLIPSE("eclipse"),
    FERTILITY("fertility"),
    CITADEL("citadel"),
    DISRUPTION("disruption"),
    CLOAKING("cloaking"),
    ACCELERATING("accelerating"),
    EFFICIENCY("efficiency"),
    CRUSHING("crushing"),
    INVERSION("inversion"),
    AQUALUNG("aqualung"),
    TRANSMUTATION("transmutation"),
    TANGIBLE("tangible"),
    SANCTUARY("sanctuary"),
    BOUNTY("bounty"),
    IMMORTAL("immortal"),
    DRAIN("drain"),
    SOUL_CHAIN("soul_chain"),
    STASIS("stasis"),
    MAELSTROM("maelstrom"),
    DECAY("decay"),
    DEFLECTION("deflection"),
    SILENCE("silence"),
    PHASING("phasing");

    private final String serializedName;

    WardType(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public String getTranslationKey() {
        return "ward.selarium." + serializedName;
    }

    public static WardType bySerializedName(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        for (WardType type : values()) {
            if (type.serializedName.equals(normalized)) {
                return type;
            }
        }
        return NONE;
    }
}
