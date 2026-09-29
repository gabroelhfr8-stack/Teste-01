package com.seleris.selarium.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.seleris.selarium.registry.SelariumParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Options shared by every Selarium glow particle: a packed 0xRRGGBB colour and a size multiplier.
 * Being a proper {@link ParticleOptions} lets the server broadcast coloured particles with the
 * vanilla {@code ServerLevel#sendParticles} path.
 */
public class GlowParticleOptions implements ParticleOptions {
    public static final ParticleOptions.Deserializer<GlowParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public GlowParticleOptions fromCommand(ParticleType<GlowParticleOptions> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            int color = reader.readInt();
            reader.expect(' ');
            float scale = reader.readFloat();
            return new GlowParticleOptions(type, color, scale);
        }

        @Override
        public GlowParticleOptions fromNetwork(ParticleType<GlowParticleOptions> type, FriendlyByteBuf buffer) {
            return new GlowParticleOptions(type, buffer.readInt(), buffer.readFloat());
        }
    };

    private final ParticleType<GlowParticleOptions> type;
    private final int color;
    private final float scale;

    public GlowParticleOptions(ParticleType<GlowParticleOptions> type, int color, float scale) {
        this.type = type;
        this.color = color & 0xFFFFFF;
        this.scale = scale;
    }

    public static GlowParticleOptions wisp(int color, float scale) {
        return new GlowParticleOptions(SelariumParticleTypes.WISP.get(), color, scale);
    }

    public static GlowParticleOptions spark(int color, float scale) {
        return new GlowParticleOptions(SelariumParticleTypes.SPARK.get(), color, scale);
    }

    public static GlowParticleOptions rune(int color, float scale) {
        return new GlowParticleOptions(SelariumParticleTypes.RUNE.get(), color, scale);
    }

    /** Flat expanding ring; {@code radius} is where the ring ends, in blocks. */
    public static GlowParticleOptions ring(int color, float radius) {
        return new GlowParticleOptions(SelariumParticleTypes.RING.get(), color, radius);
    }

    public int color() {
        return color;
    }

    public float scale() {
        return scale;
    }

    public float red() {
        return ((color >> 16) & 0xFF) / 255.0F;
    }

    public float green() {
        return ((color >> 8) & 0xFF) / 255.0F;
    }

    public float blue() {
        return (color & 0xFF) / 255.0F;
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeInt(color);
        buffer.writeFloat(scale);
    }

    @Override
    public String writeToString() {
        return BuiltInRegistries.PARTICLE_TYPE.getKey(type) + " " + color + " " + scale;
    }
}
