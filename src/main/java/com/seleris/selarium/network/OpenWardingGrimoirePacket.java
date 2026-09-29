package com.seleris.selarium.network;

import com.seleris.selarium.grimoire.WardingRuleSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

public record OpenWardingGrimoirePacket(WardingRuleSet rules) {
    public static void encode(OpenWardingGrimoirePacket packet, FriendlyByteBuf buffer) {
        packet.rules.encode(buffer);
    }

    public static OpenWardingGrimoirePacket decode(FriendlyByteBuf buffer) {
        return new OpenWardingGrimoirePacket(WardingRuleSet.decode(buffer));
    }

    public static void handle(OpenWardingGrimoirePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> openClient(packet.rules)));
        context.setPacketHandled(true);
    }

    private static void openClient(WardingRuleSet rules) {
        try {
            Class<?> hooks = Class.forName("com.seleris.selarium.client.SelariumClientHooks");
            hooks.getMethod("openWardingGrimoire", WardingRuleSet.class).invoke(null, rules);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Unable to open Warding Grimoire screen", exception);
        }
    }
}
