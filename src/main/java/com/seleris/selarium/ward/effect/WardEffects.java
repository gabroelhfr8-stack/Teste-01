package com.seleris.selarium.ward.effect;

import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.buff.AqualungWardEffect;
import com.seleris.selarium.ward.effect.buff.BulwarkWardEffect;
import com.seleris.selarium.ward.effect.buff.CloakingWardEffect;
import com.seleris.selarium.ward.effect.buff.FeatherweightWardEffect;
import com.seleris.selarium.ward.effect.buff.GroundingWardEffect;
import com.seleris.selarium.ward.effect.buff.RejuvenationWardEffect;
import com.seleris.selarium.ward.effect.detection.SpectralWardEffect;
import com.seleris.selarium.ward.effect.detection.WhisperingWardEffect;
import com.seleris.selarium.ward.effect.event.DeflectionWardEffect;
import com.seleris.selarium.ward.effect.event.ImmortalWardEffect;
import com.seleris.selarium.ward.effect.hostile.BanishmentWardEffect;
import com.seleris.selarium.ward.effect.hostile.CrushingWardEffect;
import com.seleris.selarium.ward.effect.hostile.DecayWardEffect;
import com.seleris.selarium.ward.effect.hostile.DrainWardEffect;
import com.seleris.selarium.ward.effect.hostile.EclipseWardEffect;
import com.seleris.selarium.ward.effect.hostile.InversionWardEffect;
import com.seleris.selarium.ward.effect.hostile.MaelstromWardEffect;
import com.seleris.selarium.ward.effect.hostile.SilenceWardEffect;
import com.seleris.selarium.ward.effect.hostile.StasisWardEffect;
import com.seleris.selarium.ward.effect.source.AmbientManaWardEffect;
import com.seleris.selarium.ward.effect.structure.TemporaryWallWardEffect;
import com.seleris.selarium.ward.effect.utility.AcceleratingWardEffect;
import com.seleris.selarium.ward.effect.utility.EfficiencyWardEffect;
import com.seleris.selarium.ward.effect.utility.FertilityWardEffect;
import com.seleris.selarium.ward.effect.utility.MagnetismWardEffect;
import com.seleris.selarium.ward.effect.utility.PhasingWardEffect;
import com.seleris.selarium.ward.effect.utility.TransmutationWardEffect;

import java.util.EnumMap;
import java.util.Map;

/**
 * Registry mapping every {@link WardType} to the object that runs its periodic behaviour.
 * Effects are stateless singletons apart from small per-entity cooldown tables.
 */
public final class WardEffects {
    private static final IWardEffect NONE = context -> {
    };
    private static final Map<WardType, IWardEffect> EFFECTS = new EnumMap<>(WardType.class);

    static {
        register(WardType.AMBIENT_MANA, new AmbientManaWardEffect());

        register(WardType.WHISPERING, new WhisperingWardEffect());
        register(WardType.SPECTRAL, new SpectralWardEffect());

        register(WardType.BULWARK, new BulwarkWardEffect());
        register(WardType.REJUVENATION, new RejuvenationWardEffect());
        register(WardType.FEATHERWEIGHT, new FeatherweightWardEffect());
        register(WardType.GROUNDING, new GroundingWardEffect());
        register(WardType.CLOAKING, new CloakingWardEffect());
        register(WardType.AQUALUNG, new AqualungWardEffect());

        register(WardType.MAGNETISM, new MagnetismWardEffect());
        register(WardType.FERTILITY, new FertilityWardEffect());
        register(WardType.ACCELERATING, new AcceleratingWardEffect());
        register(WardType.EFFICIENCY, new EfficiencyWardEffect());
        register(WardType.TRANSMUTATION, new TransmutationWardEffect());
        register(WardType.PHASING, new PhasingWardEffect());

        register(WardType.BANISHMENT, new BanishmentWardEffect());
        register(WardType.ECLIPSE, new EclipseWardEffect());
        register(WardType.CRUSHING, new CrushingWardEffect());
        register(WardType.INVERSION, new InversionWardEffect());
        register(WardType.DRAIN, new DrainWardEffect());
        register(WardType.STASIS, new StasisWardEffect());
        register(WardType.MAELSTROM, new MaelstromWardEffect());
        register(WardType.DECAY, new DecayWardEffect());
        register(WardType.SILENCE, new SilenceWardEffect());

        register(WardType.CITADEL, TemporaryWallWardEffect.citadel());
        register(WardType.TANGIBLE, TemporaryWallWardEffect.tangible());

        register(WardType.IMMORTAL, new ImmortalWardEffect());
        register(WardType.DEFLECTION, new DeflectionWardEffect());
        // Event-driven wards: only upkeep is ticked, behaviour lives in WardEventHandler.
        register(WardType.SANCTUARY, new PassiveWardEffect(WardType.SANCTUARY));
        register(WardType.BOUNTY, new PassiveWardEffect(WardType.BOUNTY));
        register(WardType.DISRUPTION, new PassiveWardEffect(WardType.DISRUPTION));
        register(WardType.SOUL_CHAIN, new PassiveWardEffect(WardType.SOUL_CHAIN));
    }

    private WardEffects() {
    }

    private static void register(WardType type, IWardEffect effect) {
        IWardEffect previous = EFFECTS.put(type, effect);
        if (previous != null) {
            throw new IllegalStateException("Duplicate ward effect for " + type);
        }
    }

    /** The effect for a ward; {@link WardType#NONE} (or an unregistered type) does nothing. */
    public static IWardEffect get(WardType type) {
        return EFFECTS.getOrDefault(type, NONE);
    }

    public static boolean isRegistered(WardType type) {
        return EFFECTS.containsKey(type);
    }
}
