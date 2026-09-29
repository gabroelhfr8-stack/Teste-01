package com.seleris.selarium.dust;

import net.minecraft.network.chat.Component;

public record DustDefinition(DustType type, DustPurity purity) {
    public String getSerializedName() {
        return purity.getSerializedName() + "_" + type.getSerializedName();
    }

    public Component getDisplayName() {
        return Component.translatable("dust.selarium." + getSerializedName());
    }

    public boolean satisfies(DustType requiredType, DustPurity minimumPurity) {
        return type == requiredType && purity.isAtLeast(minimumPurity);
    }
}

