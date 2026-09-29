package com.seleris.selarium.ward;

import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ActiveWardIndex {
    private static final Map<ResourceKey<Level>, Map<BlockPos, WardType>> ACTIVE_SIGILS = new HashMap<>();
    private static final Map<ResourceKey<Level>, Map<UUID, WardProjection>> ACTIVE_PROJECTIONS = new HashMap<>();

    private ActiveWardIndex() {
    }

    /** Forget every indexed field (server shutdown). */
    public static void clear() {
        ACTIVE_SIGILS.clear();
        ACTIVE_PROJECTIONS.clear();
    }

    public static void update(ServerLevel level, BlockPos pos, WardFieldSource sigil) {
        if (sigil instanceof WardProjection projection) {
            updateProjection(projection);
            return;
        }
        if (!sigil.isActive() || sigil.getWardType() == WardType.NONE) {
            remove(level, pos);
            return;
        }
        ACTIVE_SIGILS.computeIfAbsent(level.dimension(), ignored -> new HashMap<>()).put(pos.immutable(), sigil.getWardType());
    }

    public static void remove(Level level, BlockPos pos) {
        Map<BlockPos, WardType> byPos = ACTIVE_SIGILS.get(level.dimension());
        if (byPos == null) {
            return;
        }
        byPos.remove(pos);
        if (byPos.isEmpty()) {
            ACTIVE_SIGILS.remove(level.dimension());
        }
    }

    public static void updateProjection(WardProjection projection) {
        removeProjection(projection.id());
        if (projection.isActive()) {
            ACTIVE_PROJECTIONS.computeIfAbsent(projection.dimension(), ignored -> new HashMap<>())
                    .put(projection.id(), projection);
        }
    }

    public static void removeProjection(UUID id) {
        ACTIVE_PROJECTIONS.values().forEach(entries -> entries.remove(id));
    }

    public static List<WardInstance> find(ServerLevel level, Vec3 center, WardType type, int range) {
        return find(level, center, EnumSet.of(type), range);
    }

    public static List<WardInstance> findAll(ServerLevel level, Vec3 center, int range) {
        return find(level, center, EnumSet.allOf(WardType.class), range);
    }

    public static List<WardInstance> find(ServerLevel level, Vec3 center, EnumSet<WardType> types, int range) {
        Map<BlockPos, WardType> byPos = ACTIVE_SIGILS.get(level.dimension());
        int safeRange = Math.max(1, range);
        double rangeSqr = safeRange * safeRange;
        List<WardInstance> result = new ArrayList<>();
        if (byPos != null) {
            byPos.entrySet().removeIf(entry -> !isStillActive(level, entry.getKey(), entry.getValue()));
            for (Map.Entry<BlockPos, WardType> entry : byPos.entrySet()) {
                if (!types.contains(entry.getValue()) || entry.getKey().distToCenterSqr(center.x, center.y, center.z) > rangeSqr) {
                    continue;
                }
                if (level.getBlockEntity(entry.getKey()) instanceof ArcaneSigilBlockEntity sigil) {
                    result.add(new WardInstance(entry.getKey(), sigil, entry.getValue()));
                }
            }
        }
        Map<UUID, WardProjection> projections = ACTIVE_PROJECTIONS.get(level.dimension());
        if (projections != null) {
            projections.values().removeIf(projection -> !projection.isActive());
            for (WardProjection projection : projections.values()) {
                if (types.contains(projection.getWardType())
                        && projection.pos().distToCenterSqr(center.x, center.y, center.z) <= rangeSqr
                        && level.isLoaded(projection.pos())) {
                    result.add(new WardInstance(projection.pos(), projection, projection.getWardType()));
                }
            }
        }
        return result;
    }

    private static boolean isStillActive(ServerLevel level, BlockPos pos, WardType indexedType) {
        return level.isLoaded(pos)
                && level.getBlockEntity(pos) instanceof ArcaneSigilBlockEntity sigil
                && sigil.isActive()
                && sigil.getWardType() == indexedType;
    }

    public record WardInstance(BlockPos pos, WardFieldSource sigil, WardType type) {
    }
}
