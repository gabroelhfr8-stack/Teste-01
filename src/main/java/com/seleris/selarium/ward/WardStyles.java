package com.seleris.selarium.ward;

import java.util.EnumMap;
import java.util.Map;

/**
 * Category, colours and shell style of every ward. Shared by the server (particle colours)
 * and the client (sigil glyphs, shells, scroll seals) so both always agree.
 */
public final class WardStyles {
    public record Style(WardCategory category, int primary, int secondary, WardShellStyle shell) {
    }

    private static final Map<WardType, Style> STYLES = new EnumMap<>(WardType.class);
    private static final Style NEUTRAL = new Style(WardCategory.UTILITY, 0xC9C2EE, 0xEDE9FF, WardShellStyle.NONE);

    static {
        put(WardType.AMBIENT_MANA, WardCategory.SOURCE, 0x5BC8FF, 0xB5F0FF, WardShellStyle.SOFT);
        put(WardType.WHISPERING, WardCategory.DETECTION, 0xC7A6FF, 0xEBDDFF, WardShellStyle.RUNES);
        put(WardType.SPECTRAL, WardCategory.DETECTION, 0xB58CFF, 0xE3D3FF, WardShellStyle.HEX);

        put(WardType.BULWARK, WardCategory.BUFF, 0x4FC3F7, 0xC4ECFF, WardShellStyle.HEX);
        put(WardType.REJUVENATION, WardCategory.BUFF, 0x69F0A6, 0xD0FFE6, WardShellStyle.SOFT);
        put(WardType.FEATHERWEIGHT, WardCategory.BUFF, 0xBFE9FF, 0xF2FBFF, WardShellStyle.SOFT);
        put(WardType.GROUNDING, WardCategory.BUFF, 0xC9A66B, 0xF2DDB0, WardShellStyle.SOFT);
        put(WardType.CLOAKING, WardCategory.BUFF, 0x8E7CFF, 0xD6CEFF, WardShellStyle.SOFT);
        put(WardType.AQUALUNG, WardCategory.BUFF, 0x3FB6FF, 0xB8E4FF, WardShellStyle.SOFT);

        put(WardType.MAGNETISM, WardCategory.UTILITY, 0xF2C14E, 0xFFE9A8, WardShellStyle.SOFT);
        put(WardType.FERTILITY, WardCategory.UTILITY, 0x8BE38B, 0xD5FBD5, WardShellStyle.SOFT);
        put(WardType.ACCELERATING, WardCategory.UTILITY, 0xE8E36B, 0xFBF9B8, WardShellStyle.SOFT);
        put(WardType.EFFICIENCY, WardCategory.UTILITY, 0xFFB347, 0xFFE2B8, WardShellStyle.SOFT);
        put(WardType.TRANSMUTATION, WardCategory.UTILITY, 0xE8A8FF, 0xF6DCFF, WardShellStyle.RUNES);
        put(WardType.PHASING, WardCategory.UTILITY, 0x9FE8FF, 0xDDF7FF, WardShellStyle.SOFT);

        put(WardType.BANISHMENT, WardCategory.HOSTILE, 0xF2456E, 0xFFA3B8, WardShellStyle.HEX);
        put(WardType.ECLIPSE, WardCategory.HOSTILE, 0x7A5CFF, 0xC9BDFF, WardShellStyle.SOFT);
        put(WardType.CRUSHING, WardCategory.HOSTILE, 0xE0603A, 0xFFB79E, WardShellStyle.HEX);
        put(WardType.INVERSION, WardCategory.HOSTILE, 0xFF5CC8, 0xFFB8EA, WardShellStyle.SOFT);
        put(WardType.DRAIN, WardCategory.HOSTILE, 0xD1305A, 0xFF9DB4, WardShellStyle.SOFT);
        put(WardType.STASIS, WardCategory.HOSTILE, 0x9BE7FF, 0xE3F8FF, WardShellStyle.HEX);
        put(WardType.MAELSTROM, WardCategory.HOSTILE, 0x3D7BFF, 0xA9C6FF, WardShellStyle.SOFT);
        put(WardType.DECAY, WardCategory.HOSTILE, 0x7BD34B, 0xCBF2B0, WardShellStyle.SOFT);
        put(WardType.SILENCE, WardCategory.HOSTILE, 0xB0B4C8, 0xE5E7F0, WardShellStyle.RUNES);

        put(WardType.CITADEL, WardCategory.STRUCTURE, 0x9D8CFF, 0xDCD5FF, WardShellStyle.NONE);
        put(WardType.TANGIBLE, WardCategory.STRUCTURE, 0xB79CFF, 0xE6DBFF, WardShellStyle.NONE);
        put(WardType.SANCTUARY, WardCategory.STRUCTURE, 0xFFE38A, 0xFFF6D0, WardShellStyle.SOFT);

        put(WardType.BOUNTY, WardCategory.EVENT, 0xFFC94D, 0xFFEBB0, WardShellStyle.SOFT);
        put(WardType.IMMORTAL, WardCategory.EVENT, 0xFFD6F5, 0xFFF0FB, WardShellStyle.SOFT);
        put(WardType.SOUL_CHAIN, WardCategory.EVENT, 0x6FD6C8, 0xC4F5EF, WardShellStyle.HEX);
        put(WardType.DEFLECTION, WardCategory.EVENT, 0x66B2FF, 0xC2E0FF, WardShellStyle.HEX);
        put(WardType.DISRUPTION, WardCategory.EVENT, 0xFF9A3D, 0xFFD9A8, WardShellStyle.HEX);
    }

    private WardStyles() {
    }

    private static void put(WardType type, WardCategory category, int primary, int secondary, WardShellStyle shell) {
        STYLES.put(type, new Style(category, primary, secondary, shell));
    }

    /** True when the ward has an explicit style (every real ward should; NONE does not). */
    public static boolean has(WardType type) {
        return STYLES.containsKey(type);
    }

    public static Style of(WardType type) {
        return STYLES.getOrDefault(type, NEUTRAL);
    }

    public static WardCategory category(WardType type) {
        return of(type).category();
    }

    public static int primary(WardType type) {
        return of(type).primary();
    }

    public static int secondary(WardType type) {
        return of(type).secondary();
    }
}
