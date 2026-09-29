package com.seleris.selarium.client.sigil;

import com.seleris.selarium.dust.DustType;

/** Tint colour (0xRRGGBB) of each dust type, used for the component marks on a sigil. */
public final class DustPalette {
    private DustPalette() {
    }

    public static int color(DustType type) {
        return switch (type) {
            case ARCANE -> 0x9A6CF5;
            case AEGIS -> 0xC3C3D6;
            case VITAL -> 0x7FE79A;
            case FOCUS -> 0xFF9CCF;
            case BINDING -> 0xFFC55C;
            case ECHO -> 0x6ADCF0;
            case DENSITY -> 0x8C84FF;
            case WARP -> 0x5EE8C8;
            case VEIL -> 0xA07FE0;
            case CHRONO -> 0xFDF1C8;
        };
    }

    /** Drawing priority: lower first, so the most identity-defining dusts are never the ones dropped. */
    public static int priority(DustType type) {
        return switch (type) {
            case FOCUS -> 0;
            case AEGIS -> 1;
            case VITAL -> 2;
            case ECHO -> 3;
            case BINDING -> 4;
            case DENSITY -> 5;
            case WARP -> 6;
            case VEIL -> 7;
            case CHRONO -> 8;
            case ARCANE -> 9;
        };
    }
}
