package com.seleris.selarium.network;

import com.seleris.selarium.grimoire.WardingGrimoireData;
import com.seleris.selarium.grimoire.WardingRuleSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UpdateWardingGrimoirePacket(WardingRuleSet rules) {
    public static void encode(UpdateWardingGrimoirePacket packet, FriendlyByteBuf buffer) {
        packet.rules.encode(buffer);
    }

    public static UpdateWardingGrimoirePacket decode(FriendlyByteBuf buffer) {
        return new UpdateWardingGrimoirePacket(WardingRuleSet.decode(buffer));
    }

    public static void handle(UpdateWardingGrimoirePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null && WardingGrimoireData.updateHeld(sender, packet.rules)) {
                WardingGrimoireData.notifySaved(sender);
            }
        });
        context.setPacketHandled(true);
    }
}
