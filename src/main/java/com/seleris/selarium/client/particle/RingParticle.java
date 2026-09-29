package com.seleris.selarium.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.seleris.selarium.client.vfx.VoxelField;
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

/**
 * A ripple that runs outwards over the ground one ring of blocks at a time: the visible "pulse" of a ward cycle. Every
 * step is a whole block ring ({@link VoxelField#ring}), so the wave follows the block grid like a circle drawn in pixel
 * art, and the two rings behind it fade out as a trail.
 */
public class RingParticle extends TextureSheetParticle {
    private static final int FULL_BRIGHT = 15728880;
    /** Brightness of the ring itself and of the two rings behind it. */
    private static final float[] TRAIL = {1.0F, 0.45F, 0.2F};
    private final float maxRadius;

    protected RingParticle(ClientLevel level, double x, double y, double z, GlowParticleOptions options, SpriteSet sprites) {
        super(level, x, y, z);
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;
        this.maxRadius = Math.min(VoxelField.MAX_RADIUS, Math.max(1.0F, options.scale()));
        this.lifetime = 14 + Math.min(18, (int) (this.maxRadius * 1.6F));
        this.setColor(options.red(), options.green(), options.blue());
        this.alpha = 0.0F;
        this.pickSprite(sprites);
        // the frustum test uses the bounding box, which must span the whole ripple or it vanishes when its centre is off-screen
        double reach = this.maxRadius + 1.5D;
        this.setBoundingBox(new AABB(x - reach, y - 0.1D, z - reach, x + reach, y + 0.1D, z + reach));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTicks) {
        float life = Mth.clamp((this.age + partialTicks) / this.lifetime, 0.0F, 1.0F);
        float ease = 1.0F - (1.0F - life) * (1.0F - life);
        int radius = Math.round(this.maxRadius * ease);
        float fade = 0.85F * Mth.clamp(life / 0.12F, 0.0F, 1.0F) * Mth.clamp((1.0F - life) / 0.7F, 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }
        Vec3 cameraPos = camera.getPosition();
        // the grid of the ripple is the world's block grid, around the block the pulse started in
        float baseX = (float) (Mth.floor(this.x) - cameraPos.x());
        float baseZ = (float) (Mth.floor(this.z) - cameraPos.z());
        float py = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        for (int trail = 0; trail < TRAIL.length && radius - trail >= 0; trail++) {
            float a = fade * TRAIL[trail];
            for (int[] cell : VoxelField.ring(radius - trail)) {
                float x0 = baseX + cell[0];
                float z0 = baseZ + cell[1];
                consumer.vertex(x0, py, z0).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, a).uv2(FULL_BRIGHT).endVertex();
                consumer.vertex(x0, py, z0 + 1.0F).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, a).uv2(FULL_BRIGHT).endVertex();
                consumer.vertex(x0 + 1.0F, py, z0 + 1.0F).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, a).uv2(FULL_BRIGHT).endVertex();
                consumer.vertex(x0 + 1.0F, py, z0).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, a).uv2(FULL_BRIGHT).endVertex();
            }
        }
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
