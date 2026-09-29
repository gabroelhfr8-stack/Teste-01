package com.seleris.selarium.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleType;

public class GlowParticleType extends ParticleType<GlowParticleOptions> {
    private final Codec<GlowParticleOptions> codec = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("color").forGetter(GlowParticleOptions::color),
            Codec.FLOAT.fieldOf("scale").forGetter(GlowParticleOptions::scale)
    ).apply(instance, (color, scale) -> new GlowParticleOptions(this, color, scale)));

    public GlowParticleType(boolean overrideLimiter) {
        super(overrideLimiter, GlowParticleOptions.DESERIALIZER);
    }

    @Override
    public Codec<GlowParticleOptions> codec() {
        return codec;
    }
}
