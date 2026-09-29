package com.seleris.selarium.registry;

import com.seleris.selarium.Selarium;
import com.seleris.selarium.particle.GlowParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SelariumParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Selarium.MOD_ID);

    /** Soft glowing orb that drifts upwards. */
    public static final RegistryObject<GlowParticleType> WISP = PARTICLE_TYPES.register("wisp", () -> new GlowParticleType(false));
    /** Four-point star glint. */
    public static final RegistryObject<GlowParticleType> SPARK = PARTICLE_TYPES.register("spark", () -> new GlowParticleType(false));
    /** A floating rune glyph. */
    public static final RegistryObject<GlowParticleType> RUNE = PARTICLE_TYPES.register("rune", () -> new GlowParticleType(false));
    /** Flat ring that expands across the ground; used for ward pulses. */
    public static final RegistryObject<GlowParticleType> RING = PARTICLE_TYPES.register("ring", () -> new GlowParticleType(true));

    private SelariumParticleTypes() {
    }

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
