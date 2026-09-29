package com.seleris.selarium.ward;

/** How a ward's area of effect is drawn on the client. Purely cosmetic. */
public enum WardShellStyle {
    /** No translucent shell (the ward is visible some other way, e.g. real blocks). */
    NONE,
    /** Soft flowing energy dome. */
    SOFT,
    /** Hexagonal force-field lattice. */
    HEX,
    /** Scrolling band of runes. */
    RUNES
}
