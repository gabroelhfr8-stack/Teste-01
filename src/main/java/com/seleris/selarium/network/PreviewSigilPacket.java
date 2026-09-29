package com.seleris.selarium.network;

import com.seleris.selarium.sigil.SigilSelectionService;
import com.seleris.selarium.ward.WardType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PreviewSigilPacket(BlockPos pos, WardType type) {
    public static void encode(PreviewSigilPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeEnum(packet.type);
    }

    public static PreviewSigilPacket decode(FriendlyByteBuf buffer) {
        return new PreviewSigilPacket(buffer.readBlockPos(), buffer.readEnum(WardType.class));
    }

    public static void handle(PreviewSigilPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) SigilSelectionService.preview(sender, packet.pos, packet.type);
        });
        context.setPacketHandled(true);
    }
}
