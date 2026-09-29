package com.seleris.selarium.client.sigil;

import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.dust.DustDefinition;
import com.seleris.selarium.dust.DustType;
import com.seleris.selarium.ward.WardType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public final class SigilVisualResolver {
    private SigilVisualResolver() {
    }

    public static SigilVisualPlan resolve(ArcaneSigilBlockEntity sigil) {
        return resolve(sigil, sigil.getWardType());
    }

    public static SigilVisualPlan resolve(ArcaneSigilBlockEntity sigil, WardType displayedWard) {
        Map<DustType, Integer> componentCounts = collectVisualComponentCounts(sigil);
        Optional<ArcaneSigilVisualLayer> wardLayer = Optional.ofNullable(resolveWardLayer(displayedWard));
        if (wardLayer.isEmpty() && !componentCounts.isEmpty()) {
            wardLayer = Optional.of(ArcaneSigilVisualLayer.INCOMPLETE);
        }

        if (!SelariumCommonConfig.SIGIL_COMPONENT_LAYERS_ENABLED.get() || componentCounts.isEmpty()) {
            return new SigilVisualPlan(java.util.List.of(), wardLayer, componentCounts.size());
        }

        int maxLayers = Math.max(0, SelariumCommonConfig.SIGIL_MAX_VISIBLE_COMPONENT_LAYERS.get());
        ArrayList<SigilVisualPlan.ComponentLayer> layers = new ArrayList<>();
        componentCounts.entrySet().stream()
                .map(entry -> ArcaneSigilVisualLayer.forDustType(entry.getKey())
                        .map(layer -> new ComponentCandidate(layer, entry.getValue())))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparingInt(candidate -> candidate.layer().visualPriority()))
                .limit(maxLayers)
                .forEach(candidate -> layers.add(new SigilVisualPlan.ComponentLayer(candidate.layer(), candidate.count(), layers.size() >= 3)));

        int hiddenLayers = Math.max(0, componentCounts.size() - layers.size());
        return new SigilVisualPlan(java.util.List.copyOf(layers), wardLayer, hiddenLayers);
    }

    private static Map<DustType, Integer> collectVisualComponentCounts(ArcaneSigilBlockEntity sigil) {
        Map<DustType, Integer> counts = new EnumMap<>(DustType.class);
        for (Map.Entry<DustDefinition, Integer> entry : sigil.getComponents().entrySet()) {
            int count = Math.max(0, entry.getValue());
            if (count > 0) {
                counts.merge(entry.getKey().type(), count, Integer::sum);
            }
        }

        int arcaneCount = counts.getOrDefault(DustType.ARCANE, 0);
        if (arcaneCount <= 1) {
            counts.remove(DustType.ARCANE);
        } else {
            counts.put(DustType.ARCANE, arcaneCount - 1);
        }
        counts.entrySet().removeIf(entry -> entry.getValue() <= 0);
        return counts;
    }

    private static ArcaneSigilVisualLayer resolveWardLayer(WardType wardType) {
        return switch (wardType) {
            case AMBIENT_MANA -> ArcaneSigilVisualLayer.AMBIENT_MANA;
            case WHISPERING -> ArcaneSigilVisualLayer.WHISPERING;
            case SPECTRAL -> ArcaneSigilVisualLayer.SPECTRAL;
            case BULWARK -> ArcaneSigilVisualLayer.BULWARK;
            case REJUVENATION -> ArcaneSigilVisualLayer.REJUVENATION;
            case FEATHERWEIGHT -> ArcaneSigilVisualLayer.FEATHERWEIGHT;
            case GROUNDING -> ArcaneSigilVisualLayer.GROUNDING;
            case MAGNETISM -> ArcaneSigilVisualLayer.MAGNETISM;
            case BANISHMENT -> ArcaneSigilVisualLayer.BANISHMENT;
            case ECLIPSE -> ArcaneSigilVisualLayer.ECLIPSE;
            case FERTILITY -> ArcaneSigilVisualLayer.FERTILITY;
            case CITADEL -> ArcaneSigilVisualLayer.CITADEL;
            case DISRUPTION -> ArcaneSigilVisualLayer.DISRUPTION;
            case CLOAKING -> ArcaneSigilVisualLayer.CLOAKING;
            case ACCELERATING -> ArcaneSigilVisualLayer.ACCELERATING;
            case EFFICIENCY -> ArcaneSigilVisualLayer.EFFICIENCY;
            case CRUSHING -> ArcaneSigilVisualLayer.CRUSHING;
            case INVERSION -> ArcaneSigilVisualLayer.INVERSION;
            case AQUALUNG -> ArcaneSigilVisualLayer.AQUALUNG;
            case TRANSMUTATION -> ArcaneSigilVisualLayer.TRANSMUTATION;
            case TANGIBLE -> ArcaneSigilVisualLayer.TANGIBLE;
            case SANCTUARY -> ArcaneSigilVisualLayer.SANCTUARY;
            case BOUNTY -> ArcaneSigilVisualLayer.BOUNTY;
            case IMMORTAL -> ArcaneSigilVisualLayer.IMMORTAL;
            case DRAIN -> ArcaneSigilVisualLayer.DRAIN;
            case SOUL_CHAIN -> ArcaneSigilVisualLayer.SOUL_CHAIN;
            case STASIS -> ArcaneSigilVisualLayer.STASIS;
            case MAELSTROM -> ArcaneSigilVisualLayer.MAELSTROM;
            case DECAY -> ArcaneSigilVisualLayer.DECAY;
            case DEFLECTION -> ArcaneSigilVisualLayer.DEFLECTION;
            case SILENCE -> ArcaneSigilVisualLayer.SILENCE;
            case PHASING -> ArcaneSigilVisualLayer.PHASING;
            default -> null;
        };
    }

    private record ComponentCandidate(ArcaneSigilVisualLayer layer, int count) {
    }
}
