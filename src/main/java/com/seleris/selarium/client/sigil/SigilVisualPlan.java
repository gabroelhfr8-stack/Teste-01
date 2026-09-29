package com.seleris.selarium.client.sigil;

import java.util.List;
import java.util.Optional;

public record SigilVisualPlan(
        List<ComponentLayer> componentLayers,
        Optional<ArcaneSigilVisualLayer> wardLayer,
        int hiddenComponentLayerCount) {
    public static final SigilVisualPlan EMPTY = new SigilVisualPlan(List.of(), Optional.empty(), 0);

    public boolean hasComponentLayers() {
        return !componentLayers.isEmpty();
    }

    public boolean hasWardLayer() {
        return wardLayer.isPresent();
    }

    public record ComponentLayer(ArcaneSigilVisualLayer layer, int componentCount, boolean extraLayer) {
    }
}
