package com.seleris.selarium.ward;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.effect.AmbientManaWardEffect;
import com.seleris.selarium.ward.effect.BanishmentWardEffect;
import com.seleris.selarium.ward.effect.BulwarkWardEffect;
import com.seleris.selarium.ward.effect.EclipseWardEffect;
import com.seleris.selarium.ward.effect.FeatherweightWardEffect;
import com.seleris.selarium.ward.effect.GroundingWardEffect;
import com.seleris.selarium.ward.effect.IWardEffect;
import com.seleris.selarium.ward.effect.MagnetismWardEffect;
import com.seleris.selarium.ward.effect.MvpWardEffects;
import com.seleris.selarium.ward.effect.RejuvenationWardEffect;
import com.seleris.selarium.ward.effect.SpectralWardEffect;
import com.seleris.selarium.ward.effect.WhisperingWardEffect;

public final class WardManager {
    private static final IWardEffect AMBIENT_MANA = new AmbientManaWardEffect();
    private static final IWardEffect WHISPERING = new WhisperingWardEffect();
    private static final IWardEffect SPECTRAL = new SpectralWardEffect();
    private static final IWardEffect BULWARK = new BulwarkWardEffect();
    private static final IWardEffect REJUVENATION = new RejuvenationWardEffect();
    private static final IWardEffect FEATHERWEIGHT = new FeatherweightWardEffect();
    private static final IWardEffect GROUNDING = new GroundingWardEffect();
    private static final IWardEffect MAGNETISM = new MagnetismWardEffect();
    private static final IWardEffect BANISHMENT = new BanishmentWardEffect();
    private static final IWardEffect ECLIPSE = new EclipseWardEffect();
    private static final IWardEffect FERTILITY = new MvpWardEffects.Fertility();
    private static final IWardEffect CITADEL = new MvpWardEffects.Citadel();
    private static final IWardEffect DISRUPTION = new MvpWardEffects.Disruption();
    private static final IWardEffect CLOAKING = new MvpWardEffects.Cloaking();
    private static final IWardEffect ACCELERATING = new MvpWardEffects.Accelerating();
    private static final IWardEffect EFFICIENCY = new MvpWardEffects.Efficiency();
    private static final IWardEffect CRUSHING = new MvpWardEffects.Crushing();
    private static final IWardEffect INVERSION = new MvpWardEffects.Inversion();
    private static final IWardEffect AQUALUNG = new MvpWardEffects.Aqualung();
    private static final IWardEffect TRANSMUTATION = new MvpWardEffects.Transmutation();
    private static final IWardEffect TANGIBLE = new MvpWardEffects.Tangible();
    private static final IWardEffect SANCTUARY = new MvpWardEffects.Sanctuary();
    private static final IWardEffect BOUNTY = new MvpWardEffects.Bounty();
    private static final IWardEffect IMMORTAL = new MvpWardEffects.Immortal();
    private static final IWardEffect DRAIN = new MvpWardEffects.Drain();
    private static final IWardEffect SOUL_CHAIN = new MvpWardEffects.SoulChain();
    private static final IWardEffect STASIS = new MvpWardEffects.Stasis();
    private static final IWardEffect MAELSTROM = new MvpWardEffects.Maelstrom();
    private static final IWardEffect DECAY = new MvpWardEffects.Decay();
    private static final IWardEffect DEFLECTION = new MvpWardEffects.Deflection();
    private static final IWardEffect SILENCE = new MvpWardEffects.Silence();
    private static final IWardEffect PHASING = new MvpWardEffects.Phasing();

    private WardManager() {
    }

    public static void tick(WardContext context) {
        context.sigil().tickWardTimers();

        if (!context.sigil().isActive()) {
            if (context.sigil() instanceof WardProjection projection) ActiveWardIndex.removeProjection(projection.id());
            else ActiveWardIndex.remove(context.level(), context.pos());
            return;
        }

        WardDefinition definition = WardDefinitions.get(context.sigil().getWardType()).orElse(null);
        if (definition == null) {
            context.sigil().deactivateWard(0);
            return;
        }

        if (definition.temporary() && context.sigil().getWardDurationRemainingTicks() <= 0) {
            context.sigil().deactivateWard(definition.cooldownTicks());
            return;
        }

        ActiveWardIndex.update(context.level(), context.pos(), context.sigil());

        if (context.sigil() instanceof WardProjection
                && context.sigil().getLastUpkeepTick() == context.level().getGameTime()) {
            effectFor(context.sigil().getWardType()).tick(context);
            return;
        }

        int interval = Math.max(1, definition.tickInterval());
        boolean firstUpkeep = context.sigil().getLastUpkeepTick() < 0;
        if (!firstUpkeep && Math.floorMod(context.level().getGameTime() + context.pos().asLong(), interval) != 0) {
            return;
        }

        if (!WardUpkeepService.pay(context, definition)) {
            return;
        }

        effectFor(context.sigil().getWardType()).tick(context);
    }

    private static IWardEffect effectFor(WardType type) {
        return switch (type) {
            case AMBIENT_MANA -> AMBIENT_MANA;
            case WHISPERING -> WHISPERING;
            case SPECTRAL -> SPECTRAL;
            case BULWARK -> BULWARK;
            case REJUVENATION -> REJUVENATION;
            case FEATHERWEIGHT -> FEATHERWEIGHT;
            case GROUNDING -> GROUNDING;
            case MAGNETISM -> MAGNETISM;
            case BANISHMENT -> BANISHMENT;
            case ECLIPSE -> ECLIPSE;
            case FERTILITY -> FERTILITY;
            case CITADEL -> CITADEL;
            case DISRUPTION -> DISRUPTION;
            case CLOAKING -> CLOAKING;
            case ACCELERATING -> ACCELERATING;
            case EFFICIENCY -> EFFICIENCY;
            case CRUSHING -> CRUSHING;
            case INVERSION -> INVERSION;
            case AQUALUNG -> AQUALUNG;
            case TRANSMUTATION -> TRANSMUTATION;
            case TANGIBLE -> TANGIBLE;
            case SANCTUARY -> SANCTUARY;
            case BOUNTY -> BOUNTY;
            case IMMORTAL -> IMMORTAL;
            case DRAIN -> DRAIN;
            case SOUL_CHAIN -> SOUL_CHAIN;
            case STASIS -> STASIS;
            case MAELSTROM -> MAELSTROM;
            case DECAY -> DECAY;
            case DEFLECTION -> DEFLECTION;
            case SILENCE -> SILENCE;
            case PHASING -> PHASING;
            default -> context -> {
            };
        };
    }
}
