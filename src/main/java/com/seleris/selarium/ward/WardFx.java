package com.seleris.selarium.ward;

import com.seleris.selarium.particle.GlowParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Server-side helpers that broadcast ward visuals. Everything is a plain vanilla particle packet
 * carrying a {@link GlowParticleOptions}, so clients without any special handling render it.
 */
public final class WardFx {
    private WardFx() {
    }

    /** A ring that sweeps outwards along the ground from the field's centre. */
    public static void pulse(ServerLevel level, net.minecraft.core.BlockPos pos, WardType type, int range) {
        int color = WardStyles.secondary(type);
        level.sendParticles(GlowParticleOptions.ring(color, range), pos.getX() + 0.5D, pos.getY() + 0.06D, pos.getZ() + 0.5D,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    /** A small cloud of wisps around an entity the ward just affected. */
    public static void touch(ServerLevel level, Entity target, WardType type) {
        touch(level, target, type, 4);
    }

    public static void touch(ServerLevel level, Entity target, WardType type, int count) {
        float width = Math.max(0.3F, target.getBbWidth());
        level.sendParticles(GlowParticleOptions.wisp(WardStyles.primary(type), 0.7F),
                target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                count, width * 0.35D, target.getBbHeight() * 0.3D, width * 0.35D, 0.015D);
    }

    /** A bright burst of sparks, e.g. when a projectile is deflected or a death is prevented. */
    public static void burst(ServerLevel level, Vec3 at, WardType type, int count, double spread) {
        level.sendParticles(GlowParticleOptions.spark(WardStyles.secondary(type), 0.9F), at.x, at.y, at.z,
                count, spread, spread, spread, 0.06D);
        level.sendParticles(GlowParticleOptions.wisp(WardStyles.primary(type), 1.1F), at.x, at.y, at.z,
                Math.max(1, count / 2), spread * 0.5D, spread * 0.5D, spread * 0.5D, 0.03D);
    }

    /** A dotted line of sparks between two points (drain, soul-chain, magnetism...). */
    public static void trail(ServerLevel level, Vec3 from, Vec3 to, WardType type, int steps) {
        int count = Math.max(2, steps);
        GlowParticleOptions options = GlowParticleOptions.spark(WardStyles.primary(type), 0.6F);
        for (int i = 0; i <= count; i++) {
            double t = i / (double) count;
            level.sendParticles(options, from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t, from.z + (to.z - from.z) * t,
                    1, 0.02D, 0.02D, 0.02D, 0.0D);
        }
    }
}
