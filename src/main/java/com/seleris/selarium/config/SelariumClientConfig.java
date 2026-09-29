package com.seleris.selarium.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Client-only, purely cosmetic settings. Nothing here influences gameplay or the server.
 */
public final class SelariumClientConfig {
    public static final ForgeConfigSpec SPEC;

    /** Global level of detail for Selarium visual effects. */
    public enum VfxQuality {
        OFF, LOW, MEDIUM, HIGH;

        public boolean atLeast(VfxQuality other) {
            return ordinal() >= other.ordinal();
        }
    }

    /** When the translucent ward field shells are drawn. */
    public enum ShellMode {
        OFF, NEAR, ALWAYS
    }

    public static final ForgeConfigSpec.EnumValue<VfxQuality> VFX_QUALITY;
    public static final ForgeConfigSpec.EnumValue<ShellMode> WARD_SHELLS;
    public static final ForgeConfigSpec.DoubleValue SHELL_OPACITY;
    public static final ForgeConfigSpec.BooleanValue SIGIL_ANIMATIONS;
    public static final ForgeConfigSpec.BooleanValue SIGIL_FLOATING_RUNES;
    public static final ForgeConfigSpec.BooleanValue SIGIL_LIGHT_BEAM;
    public static final ForgeConfigSpec.BooleanValue AMBIENT_PARTICLES;

    public static final ForgeConfigSpec.BooleanValue ENABLE_MANA_HUD;
    public static final ForgeConfigSpec.BooleanValue MANA_HUD_SHOW_NUMBERS;
    public static final ForgeConfigSpec.BooleanValue MANA_HUD_SHOW_GROWTH_FLASH;
    public static final ForgeConfigSpec.IntValue MANA_HUD_X;
    public static final ForgeConfigSpec.IntValue MANA_HUD_Y;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("vfx");
        VFX_QUALITY = builder.comment("Overall detail of Selarium visual effects: OFF disables them, HIGH draws everything.")
                .defineEnum("vfxQuality", VfxQuality.HIGH);
        WARD_SHELLS = builder.comment("When to draw the translucent shell that shows a ward's area: OFF, NEAR (only close to the sigil) or ALWAYS.")
                .defineEnum("wardShells", ShellMode.NEAR);
        SHELL_OPACITY = builder.comment("Opacity multiplier for ward shells (0 = invisible, 1 = default, 2 = strong).")
                .defineInRange("shellOpacity", 1.0D, 0.0D, 2.0D);
        SIGIL_ANIMATIONS = builder.comment("Animate sigils (rotating rings, pulsing glyph). Disable for a static sigil.")
                .define("sigilAnimations", true);
        SIGIL_FLOATING_RUNES = builder.comment("Draw runes orbiting active sigils and the floating focus crystal.")
                .define("sigilFloatingRunes", true);
        SIGIL_LIGHT_BEAM = builder.comment("Draw a faint column of light above active sigils.")
                .define("sigilLightBeam", true);
        AMBIENT_PARTICLES = builder.comment("Spawn ambient particles from sigils, tanks and grinders.")
                .define("ambientParticles", true);
        builder.pop();

        builder.push("hud");
        ENABLE_MANA_HUD = builder.comment("Show the mana gauge on screen.")
                .define("enableManaHud", true);
        MANA_HUD_SHOW_NUMBERS = builder.define("manaHudShowNumbers", true);
        MANA_HUD_SHOW_GROWTH_FLASH = builder.define("manaHudShowGrowthFlash", true);
        MANA_HUD_X = builder.defineInRange("manaHudX", 10, 0, 10000);
        MANA_HUD_Y = builder.defineInRange("manaHudY", 10, 0, 10000);
        builder.pop();

        SPEC = builder.build();
    }

    private SelariumClientConfig() {
    }

    public static boolean vfxEnabled() {
        return VFX_QUALITY.get() != VfxQuality.OFF;
    }
}
