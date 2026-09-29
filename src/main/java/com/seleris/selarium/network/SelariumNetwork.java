package com.seleris.selarium.network;

import com.seleris.selarium.Selarium;
import com.seleris.selarium.mana.IPlayerMana;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class SelariumNetwork {
    private static final String PROTOCOL_VERSION = "3";
    private static int packetId;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private SelariumNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(ManaSyncPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ManaSyncPacket::encode)
                .decoder(ManaSyncPacket::decode)
                .consumerMainThread(ManaSyncPacket::handle)
                .add();
        CHANNEL.messageBuilder(OpenWardingGrimoirePacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenWardingGrimoirePacket::encode)
                .decoder(OpenWardingGrimoirePacket::decode)
                .consumerMainThread(OpenWardingGrimoirePacket::handle)
                .add();
        CHANNEL.messageBuilder(UpdateWardingGrimoirePacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(UpdateWardingGrimoirePacket::encode)
                .decoder(UpdateWardingGrimoirePacket::decode)
                .consumerMainThread(UpdateWardingGrimoirePacket::handle)
                .add();
        CHANNEL.messageBuilder(ProjectionSyncPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ProjectionSyncPacket::encode)
                .decoder(ProjectionSyncPacket::decode)
                .consumerMainThread(ProjectionSyncPacket::handle)
                .add();
        CHANNEL.messageBuilder(PreviewSigilPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(PreviewSigilPacket::encode)
                .decoder(PreviewSigilPacket::decode)
                .consumerMainThread(PreviewSigilPacket::handle)
                .add();
    }

    public static void sendManaSync(ServerPlayer player, IPlayerMana mana) {
        CHANNEL.sendTo(new ManaSyncPacket(mana.getCurrentMana(), mana.getMaxMana(), mana.getManaExperience(), mana.getUnlockedManaTier()), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendOpenGrimoire(ServerPlayer player, com.seleris.selarium.grimoire.WardingRuleSet rules) {
        CHANNEL.sendTo(new OpenWardingGrimoirePacket(rules), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendProjectionSync(ServerPlayer player, ProjectionSyncPacket packet) {
        CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    private static int nextId() {
        return packetId++;
    }
}
