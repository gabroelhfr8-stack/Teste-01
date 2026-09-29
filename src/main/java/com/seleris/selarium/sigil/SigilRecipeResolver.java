package com.seleris.selarium.sigil;

import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.ward.WardDefinitions;
import com.seleris.selarium.ward.WardType;

public final class SigilRecipeResolver {
    private SigilRecipeResolver() {
    }

    public static WardType resolve(ArcaneSigilBlockEntity sigil) {
        return WardDefinitions.resolve(sigil).map(definition -> definition.type()).orElse(WardType.NONE);
    }
}
