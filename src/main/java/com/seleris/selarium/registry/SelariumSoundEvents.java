package com.seleris.selarium.registry;

import com.seleris.selarium.Selarium;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SelariumSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Selarium.MOD_ID);

    public static final RegistryObject<SoundEvent> ARCANE_CRYSTAL_PLACE = register("block.arcane_crystal.place");
    public static final RegistryObject<SoundEvent> ARCANE_CRYSTAL_BREAK = register("block.arcane_crystal.break");
    public static final RegistryObject<SoundEvent> ARCANE_CRYSTAL_SHIMMER = register("block.arcane_crystal.shimmer");

    private SelariumSoundEvents() {
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

    private static RegistryObject<SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
