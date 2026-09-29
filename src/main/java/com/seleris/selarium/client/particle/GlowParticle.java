package com.seleris.selarium.client.particle;

import com.seleris.selarium.particle.GlowParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;

/** Glowing sprite particle: wisps drift upwards, sparks pop and decelerate, runes float and turn. */
public class GlowParticle extends TextureSheetParticle {
    public enum Kind {
        WISP, SPARK, RUNE
    }

    private static final int FULL_BRIGHT = 15728880;

    private final SpriteSet sprites;
    private final Kind kind;
    private final float baseSize;
    private final float swayPhase;
    private final float spin;

    protected GlowParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
                           GlowParticleOptions options, SpriteSet sprites, Kind kind) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.kind = kind;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.swayPhase = this.random.nextFloat() * 6.2831855F;
        this.setColor(options.red(), options.green(), options.blue());
        float scale = Math.max(0.05F, options.scale());
        switch (kind) {
            case SPARK -> {
                this.lifetime = 10 + this.random.nextInt(9);
                this.baseSize = 0.10F * scale;
                this.friction = 0.86F;
                this.spin = 0.0F;
            }
            case RUNE -> {
                this.lifetime = 36 + this.random.nextInt(22);
                this.baseSize = 0.11F * scale;
                this.friction = 0.97F;
                this.spin = (this.random.nextFloat() - 0.5F) * 0.08F;
                this.roll = this.random.nextFloat() * 0.6F - 0.3F;
                this.oRoll = this.roll;
            }
            default -> {
                this.lifetime = 22 + this.random.nextInt(18);
                this.baseSize = 0.13F * scale;
                this.friction = 0.95F;
                this.spin = 0.0F;
            }
        }
        this.quadSize = this.baseSize;
        this.alpha = 0.0F;
        if (kind == Kind.RUNE) {
            this.pickSprite(sprites);
        } else {
            this.setSpriteFromAge(sprites);
        }
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        float life = this.age / (float) this.lifetime;
        if (kind == Kind.WISP) {
            this.yd += 0.0007D;
            this.xd += Mth.sin(this.age * 0.22F + swayPhase) * 0.0011D;
            this.zd += Mth.cos(this.age * 0.19F + swayPhase) * 0.0011D;
        } else if (kind == Kind.RUNE) {
            this.yd += 0.0004D;
            this.roll += spin;
        }
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;

        float fadeIn = Mth.clamp(life / 0.18F, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((1.0F - life) / 0.45F, 0.0F, 1.0F);
        this.alpha = 0.95F * fadeIn * fadeOut;
        this.quadSize = baseSize * (kind == Kind.SPARK ? 1.0F - 0.55F * life : 0.65F + 0.55F * Mth.sin(life * (float) Math.PI));
        if (kind != Kind.RUNE) {
            this.setSpriteFromAge(sprites);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return SelariumParticleRenderTypes.ADDITIVE;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return FULL_BRIGHT;
    }

    public static final class Provider implements ParticleProvider<GlowParticleOptions> {
        private final SpriteSet sprites;
        private final Kind kind;

        public Provider(SpriteSet sprites, Kind kind) {
            this.sprites = sprites;
            this.kind = kind;
        }

        @Override
        public Particle createParticle(GlowParticleOptions options, ClientLevel level, double x, double y, double z,
                                       double xd, double yd, double zd) {
            return new GlowParticle(level, x, y, z, xd, yd, zd, options, sprites, kind);
        }
    }
}
