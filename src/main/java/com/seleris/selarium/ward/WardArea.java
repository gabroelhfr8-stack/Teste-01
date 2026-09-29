package com.seleris.selarium.ward;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

/**
 * The spherical volume a ward acts on. Every entity and block query goes through this class so the
 * effect volume always matches the translucent shell drawn on the client.
 */
public final class WardArea {
    private final ServerLevel level;
    private final Vec3 center;
    private final int range;
    private final double rangeSqr;

    private WardArea(ServerLevel level, BlockPos origin, int range) {
        this.level = level;
        this.center = Vec3.atCenterOf(origin);
        this.range = Math.max(1, range);
        this.rangeSqr = (double) this.range * this.range;
    }

    public static WardArea of(ServerLevel level, BlockPos origin, int range) {
        return new WardArea(level, origin, range);
    }

    public static WardArea of(WardContext context, int range) {
        return new WardArea(context.level(), context.pos(), range);
    }

    public Vec3 center() {
        return center;
    }

    public int range() {
        return range;
    }

    /** Axis-aligned box enclosing the sphere (used as the broad-phase query). */
    public AABB bounds() {
        return new AABB(center, center).inflate(range);
    }

    public boolean contains(Vec3 point) {
        return point.distanceToSqr(center) <= rangeSqr;
    }

    public boolean contains(Entity entity) {
        return contains(entity.getBoundingBox().getCenter());
    }

    public boolean contains(BlockPos pos) {
        return contains(Vec3.atCenterOf(pos));
    }

    public <T extends Entity> List<T> entities(Class<T> type, Predicate<? super T> filter) {
        return level.getEntitiesOfClass(type, bounds(), entity -> contains(entity) && filter.test(entity));
    }
}
