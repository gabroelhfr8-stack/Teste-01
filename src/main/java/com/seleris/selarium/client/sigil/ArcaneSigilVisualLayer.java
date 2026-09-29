package com.seleris.selarium.client.sigil;

import com.seleris.selarium.Selarium;
import com.seleris.selarium.dust.DustType;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public enum ArcaneSigilVisualLayer {
    INCOMPLETE(null, "arcane_sigil_incomplete", -10),
    AMBIENT_MANA(null, "arcane_sigil_ambient_mana", -9),
    WHISPERING(null, "arcane_sigil_whispering", -8),
    SPECTRAL(null, "arcane_sigil_spectral", -7),
    BULWARK(null, "arcane_sigil_bulwark", -6),
    REJUVENATION(null, "arcane_sigil_rejuvenation", -5),
    FEATHERWEIGHT(null, "arcane_sigil_featherweight", -4),
    GROUNDING(null, "arcane_sigil_grounding", -3),
    MAGNETISM(null, "arcane_sigil_magnetism", -2),
    BANISHMENT(null, "arcane_sigil_banishment", -1),
    ECLIPSE(null, "arcane_sigil_eclipse", 0),
    FERTILITY(null, "arcane_sigil_fertility", 1),
    CITADEL(null, "arcane_sigil_citadel", 2),
    DISRUPTION(null, "arcane_sigil_disruption", 3),
    CLOAKING(null, "arcane_sigil_cloaking", 4),
    ACCELERATING(null, "arcane_sigil_accelerating", 5),
    EFFICIENCY(null, "arcane_sigil_efficiency", 6),
    CRUSHING(null, "arcane_sigil_crushing", 7),
    INVERSION(null, "arcane_sigil_inversion", 8),
    AQUALUNG(null, "arcane_sigil_aqualung", 9),
    TRANSMUTATION(null, "arcane_sigil_transmutation", 10),
    TANGIBLE(null, "arcane_sigil_tangible", 11),
    SANCTUARY(null, "arcane_sigil_sanctuary", 12),
    BOUNTY(null, "arcane_sigil_bounty", 13),
    IMMORTAL(null, "arcane_sigil_immortal", 14),
    DRAIN(null, "arcane_sigil_drain", 15),
    SOUL_CHAIN(null, "arcane_sigil_soul_chain", 16),
    STASIS(null, "arcane_sigil_stasis", 17),
    MAELSTROM(null, "arcane_sigil_maelstrom", 18),
    DECAY(null, "arcane_sigil_decay", 19),
    DEFLECTION(null, "arcane_sigil_deflection", 20),
    SILENCE(null, "arcane_sigil_silence", 21),
    PHASING(null, "arcane_sigil_phasing", 22),
    FOCUS(DustType.FOCUS, "arcane_sigil_component_focus", 10),
    AEGIS(DustType.AEGIS, "arcane_sigil_component_aegis", 11),
    VITAL(DustType.VITAL, "arcane_sigil_component_vital", 12),
    ECHO(DustType.ECHO, "arcane_sigil_component_echo", 13),
    BINDING(DustType.BINDING, "arcane_sigil_component_binding", 14),
    DENSITY(DustType.DENSITY, "arcane_sigil_component_density", 15),
    WARP(DustType.WARP, "arcane_sigil_component_warp", 16),
    VEIL(DustType.VEIL, "arcane_sigil_component_veil", 17),
    CHRONO(DustType.CHRONO, "arcane_sigil_component_chrono", 18),
    ARCANE(DustType.ARCANE, "arcane_sigil_component_arcane", 19);

    private final DustType dustType;
    private final ResourceLocation texture;
    private final int visualPriority;

    ArcaneSigilVisualLayer(DustType dustType, String textureName, int visualPriority) {
        this.dustType = dustType;
        this.texture = ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID, "textures/block/" + textureName + ".png");
        this.visualPriority = visualPriority;
    }

    public DustType dustType() {
        return dustType;
    }

    public ResourceLocation texture() {
        return texture;
    }

    public int visualPriority() {
        return visualPriority;
    }

    public boolean isComponentLayer() {
        return dustType != null;
    }

    public static Optional<ArcaneSigilVisualLayer> forDustType(DustType dustType) {
        return Optional.ofNullable(switch (dustType) {
            case FOCUS -> FOCUS;
            case AEGIS -> AEGIS;
            case VITAL -> VITAL;
            case ECHO -> ECHO;
            case BINDING -> BINDING;
            case DENSITY -> DENSITY;
            case WARP -> WARP;
            case VEIL -> VEIL;
            case CHRONO -> CHRONO;
            case ARCANE -> ARCANE;
        });
    }
}
