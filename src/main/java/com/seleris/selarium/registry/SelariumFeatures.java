package com.seleris.selarium.registry;

import com.seleris.selarium.Selarium;
import com.seleris.selarium.worldgen.ArcaneGeodeFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SelariumFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Selarium.MOD_ID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ARCANE_GEODE = FEATURES.register("arcane_geode",
            () -> new ArcaneGeodeFeature(NoneFeatureConfiguration.CODEC));

    private SelariumFeatures() {
    }

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
