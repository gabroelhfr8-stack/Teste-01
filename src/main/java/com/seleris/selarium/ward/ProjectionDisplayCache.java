package com.seleris.selarium.ward;

import com.seleris.selarium.network.ProjectionSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Client-side read cache; never used to authorize mana or ward effects. */
public final class ProjectionDisplayCache {
    private static ResourceLocation dimension;
    private static UUID viewer;
    private static List<ProjectionSyncPacket.View> views = List.of();
    private static Set<BlockPos> passableBlocks = Set.of();

    private ProjectionDisplayCache() { }

    public static void update(ProjectionSyncPacket packet) {
        dimension = packet.dimension();
        viewer = packet.viewer();
        views = packet.views();
        passableBlocks = packet.passableBlocks();
    }

    public static boolean canPass(Level level, BlockPos pos, UUID player) {
        return dimension != null && player.equals(viewer)
                && level.dimension().location().equals(dimension) && passableBlocks.contains(pos);
    }

    public static boolean canPhase(Level level, BlockPos pos, UUID player) {
        if (dimension == null || !level.dimension().location().equals(dimension)) return false;
        for (ProjectionSyncPacket.View view : views) {
            if (view.type() == WardType.PHASING && player.equals(view.owner())
                    && view.pos().distToCenterSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                    <= view.range() * view.range()) return true;
        }
        return false;
    }

    public static List<ProjectionSyncPacket.View> views(Level level) {
        return dimension != null && level.dimension().location().equals(dimension) ? views : List.of();
    }
}
