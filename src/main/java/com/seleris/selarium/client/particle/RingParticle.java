package com.seleris.selarium.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.seleris.selarium.particle.GlowParticleOptions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** A flat ring lying on the ground that sweeps outwards and fades: the visible "pulse" of a ward cycle. */
public class RingParticle extends TextureSheetParticle {
    private static final int FULL_BRIGHT = 15728880;
    private final float maxRadius;

    protected RingParticle(ClientLevel level, double x, double y, double z, GlowParticleOptions options, SpriteSet sprites) {
        super(level, x, y, z);
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;
        this.maxRadius = Math.max(1.0F, options.scale());
        this.lifetime = 14 + Math.min(18, (int) (this.maxRadius * 1.6F));
        this.setColor(options.red(), options.green(), options.blue());
        this.alpha = 0.0F;
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        float life = this.age / (float) this.lifetime;
        float ease = 1.0F - (1.0F - life) * (1.0F - life);
        this.quadSize = Math.max(0.15F, this.maxRadius * ease);
        // the frustum test uses the bounding box, which must span the whole ring or it vanishes when its centre is off-screen
        this.setBoundingBox(new AABB(this.x - this.quadSize, this.y - 0.1D, this.z - this.quadSize,
                this.x + this.quadSize, this.y + 0.1D, this.z + this.quadSize));
        this.alpha = 0.85F * Mth.clamp(life / 0.12F, 0.0F, 1.0F) * Mth.clamp((1.0F - life) / 0.7F, 0.0F, 1.0F);
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.getPosition();
        float px = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float py = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float pz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());
        float half = this.quadSize;
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        consumer.vertex(px - half, py, pz - half).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(FULL_BRIGHT).endVertex();
        consumer.vertex(px - half, py, pz + half).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(FULL_BRIGHT).endVertex();
        consumer.vertex(px + half, py, pz + half).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(FULL_BRIGHT).endVertex();
        consumer.vertex(px + half, py, pz - half).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(FULL_BRIGHT).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return SelariumParticleRenderTypes.ADDITIVE;
    }

    public static final class Provider implements ParticleProvider<GlowParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(GlowParticleOptions options, ClientLevel level, double x, double y, double z,
                                       double xd, double yd, double zd) {
            return new RingParticle(level, x, y, z, options, sprites);
        }
    }
}
