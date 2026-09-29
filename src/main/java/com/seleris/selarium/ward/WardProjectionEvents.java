package com.seleris.selarium.ward;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class WardProjectionEvents {
    private WardProjectionEvents() { }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel level
                && level.dimension() == Level.OVERWORLD) {
            WardProjectionSavedData.get(level).tick(level);
        }
    }

    /** The active-ward indexes are static; drop them so a new world never inherits an old one's fields. */
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ActiveWardIndex.clear();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !event.isCanceled()) {
            WardProjectionSavedData.get(player.serverLevel()).stopMobile(player.getUUID(), player.serverLevel());
        }
    }
}
