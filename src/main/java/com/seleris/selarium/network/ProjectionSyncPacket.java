package com.seleris.selarium.network;

import com.seleris.selarium.ward.ProjectionDisplayCache;
import com.seleris.selarium.ward.WardType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/** Display-only snapshot. The server retains authority over effects and collisions. */
public record ProjectionSyncPacket(UUID viewer, ResourceLocation dimension, List<View> views, Set<BlockPos> passableBlocks) {
    public static void encode(ProjectionSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.viewer);
        buffer.writeResourceLocation(packet.dimension);
        buffer.writeVarInt(packet.views.size());
        for (View view : packet.views) {
            buffer.writeUUID(view.owner);
            buffer.writeVarInt(view.type.ordinal());
            buffer.writeBlockPos(view.pos);
            buffer.writeVarInt(view.range);
            buffer.writeVarInt(view.remainingTicks);
            buffer.writeBoolean(view.mobile);
        }
        buffer.writeVarInt(packet.passableBlocks.size());
        for (BlockPos pos : packet.passableBlocks) buffer.writeBlockPos(pos);
    }

    public static ProjectionSyncPacket decode(FriendlyByteBuf buffer) {
        UUID viewer = buffer.readUUID();
        ResourceLocation dimension = buffer.readResourceLocation();
        int count = Math.min(256, Math.max(0, buffer.readVarInt()));
        List<View> views = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            UUID owner = buffer.readUUID();
            int ordinal = buffer.readVarInt();
            WardType type = ordinal >= 0 && ordinal < WardType.values().length ? WardType.values()[ordinal] : WardType.NONE;
            views.add(new View(owner, type, buffer.readBlockPos(), buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean()));
        }
        int blockCount = Math.min(4096, Math.max(0, buffer.readVarInt()));
        java.util.HashSet<BlockPos> passable = new java.util.HashSet<>();
        for (int i = 0; i < blockCount; i++) passable.add(buffer.readBlockPos());
        return new ProjectionSyncPacket(viewer, dimension, List.copyOf(views), Set.copyOf(passable));
    }

    public static void handle(ProjectionSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ProjectionDisplayCache.update(packet));
        context.setPacketHandled(true);
    }

    public record View(UUID owner, WardType type, BlockPos pos, int range, int remainingTicks, boolean mobile) { }
}
