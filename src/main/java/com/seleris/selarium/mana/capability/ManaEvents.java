package com.seleris.selarium.mana.capability;

import com.seleris.selarium.Selarium;
import com.seleris.selarium.mana.IPlayerMana;
import com.seleris.selarium.network.SelariumNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ManaEvents {
    private static final ResourceLocation PLAYER_MANA_ID = ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID, "player_mana");
    private static final Map<UUID, ManaSyncState> LAST_SYNCED = new HashMap<>();

    private ManaEvents() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            PlayerManaProvider provider = new PlayerManaProvider();
            event.addCapability(PLAYER_MANA_ID, provider);
            event.addListener(provider::invalidate);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        ManaCapability.getMana(event.getOriginal()).ifPresent(oldMana ->
                ManaCapability.getMana(event.getEntity()).ifPresent(newMana -> {
                    newMana.copyFrom(oldMana);
                    if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                        syncMana(serverPlayer, newMana, true);
                    }
                }));
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide || !(event.player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ManaCapability.getMana(serverPlayer).ifPresent(mana -> {
            mana.tickRegen(serverPlayer);
            syncMana(serverPlayer, mana, false);
        });
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ManaCapability.getMana(serverPlayer).ifPresent(mana -> syncMana(serverPlayer, mana, true));
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_SYNCED.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ManaCapability.getMana(serverPlayer).ifPresent(mana -> syncMana(serverPlayer, mana, true));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ManaCapability.getMana(serverPlayer).ifPresent(mana -> syncMana(serverPlayer, mana, true));
        }
    }

    private static void syncMana(ServerPlayer player, IPlayerMana mana, boolean force) {
        ManaSyncState state = new ManaSyncState(mana.getCurrentMana(), mana.getMaxMana(), mana.getManaExperience(), mana.getUnlockedManaTier());
        ManaSyncState previous = LAST_SYNCED.get(player.getUUID());
        if (force || !state.equals(previous)) {
            SelariumNetwork.sendManaSync(player, mana);
            LAST_SYNCED.put(player.getUUID(), state);
        }
    }

    private record ManaSyncState(int currentMana, int maxMana, double manaExperience, int unlockedManaTier) {
    }
}
