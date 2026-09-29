package com.seleris.selarium.ward;

import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.dust.DustPurity;
import com.seleris.selarium.dust.DustType;

public record WardRequirement(DustType type, DustPurity minimumPurity, int count) {
    public boolean matches(ArcaneSigilBlockEntity sigil) {
        return sigil.getComponentCount(type, minimumPurity) >= count;
    }

    public String describe() {
        return count + "x " + minimumPurity.getSerializedName() + "+" + type.getSerializedName();
    }
}

