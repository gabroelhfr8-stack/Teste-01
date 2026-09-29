package com.seleris.selarium.client.particle;

import com.seleris.selarium.Selarium;
import com.seleris.selarium.registry.SelariumParticleTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Selarium.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ParticleRegistration {
    private ParticleRegistration() {
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(SelariumParticleTypes.WISP.get(), sprites -> new GlowParticle.Provider(sprites, GlowParticle.Kind.WISP));
        event.registerSpriteSet(SelariumParticleTypes.SPARK.get(), sprites -> new GlowParticle.Provider(sprites, GlowParticle.Kind.SPARK));
        event.registerSpriteSet(SelariumParticleTypes.RUNE.get(), sprites -> new GlowParticle.Provider(sprites, GlowParticle.Kind.RUNE));
        event.registerSpriteSet(SelariumParticleTypes.RING.get(), RingParticle.Provider::new);
    }
}
