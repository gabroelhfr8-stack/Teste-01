package com.seleris.selarium.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ManaSyncPacket(int currentMana, int maxMana, double manaExperience, int unlockedTier) {
    public static void encode(ManaSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.currentMana);
        buffer.writeVarInt(packet.maxMana);
        buffer.writeDouble(packet.manaExperience);
        buffer.writeVarInt(packet.unlockedTier);
    }

    public static ManaSyncPacket decode(FriendlyByteBuf buffer) {
        return new ManaSyncPacket(buffer.readVarInt(), buffer.readVarInt(), buffer.readDouble(), buffer.readVarInt());
    }

    public static void handle(ManaSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> updateClientMana(packet)));
        context.setPacketHandled(true);
    }

    private static void updateClientMana(ManaSyncPacket packet) {
        try {
            Class<?> clientData = Class.forName("com.seleris.selarium.client.ClientManaData");
            clientData.getMethod("update", int.class, int.class, double.class, int.class)
                    .invoke(null, packet.currentMana, packet.maxMana, packet.manaExperience, packet.unlockedTier);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to update client mana data", exception);
        }
    }
}
