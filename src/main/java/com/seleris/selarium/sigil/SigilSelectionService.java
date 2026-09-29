package com.seleris.selarium.sigil;

import com.seleris.selarium.Selarium;
import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.ward.WardDefinition;
import com.seleris.selarium.ward.WardDefinitions;
import com.seleris.selarium.ward.WardType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Selarium.MOD_ID)
public final class SigilSelectionService {
    private static final int PREVIEW_LIFETIME_TICKS = 600;
    private static final Map<UUID, PendingSelection> PENDING = new ConcurrentHashMap<>();

    private SigilSelectionService() { }

    public static void preview(ServerPlayer player, BlockPos pos, WardType type) {
        PENDING.remove(player.getUUID());
        if (!near(player, pos) || !(player.level().getBlockEntity(pos) instanceof ArcaneSigilBlockEntity sigil)) return;
        if (!player.getUUID().equals(sigil.getOwner()) || sigil.isActive()) return;
        WardDefinition definition = WardDefinitions.get(type).orElse(null);
        if (definition == null || !matches(sigil, definition)) {
            player.displayClientMessage(Component.translatable("message.selarium.sigil.preview_incompatible"), true);
            return;
        }
        PENDING.put(player.getUUID(), new PendingSelection(pos.immutable(), player.level().dimension(),
                type, player.level().getGameTime() + PREVIEW_LIFETIME_TICKS));
    }

    public static boolean confirm(ServerPlayer player, BlockPos pos) {
        PendingSelection pending = PENDING.remove(player.getUUID());
        if (pending == null || !pending.pos().equals(pos) || !pending.dimension().equals(player.level().dimension())) return false;
        if (player.level().getGameTime() > pending.expiresAt() || !near(player, pos)) {
            player.displayClientMessage(Component.translatable("message.selarium.sigil.preview_expired"), true);
            return true;
        }
        if (!(player.level().getBlockEntity(pos) instanceof ArcaneSigilBlockEntity sigil)) return true;
        if (!player.getUUID().equals(sigil.getOwner())) {
            player.displayClientMessage(Component.translatable("message.selarium.sigil.owner_only"), true);
            return true;
        }
        if (sigil.isActive()) {
            player.displayClientMessage(Component.translatable("message.selarium.sigil.deactivate_first"), true);
            return true;
        }
        WardDefinition definition = WardDefinitions.get(pending.type()).orElse(null);
        if (definition == null || !matches(sigil, definition)) {
            player.displayClientMessage(Component.translatable("message.selarium.sigil.preview_incompatible"), true);
            return true;
        }
        sigil.setWardType(definition.type());
        player.displayClientMessage(Component.translatable("message.selarium.sigil.bound",
                Component.translatable(definition.type().getTranslationKey())), true);
        return true;
    }

    private static boolean matches(ArcaneSigilBlockEntity sigil, WardDefinition definition) {
        return definition.requirements().stream().allMatch(requirement -> requirement.matches(sigil));
    }

    private static boolean near(ServerPlayer player, BlockPos pos) {
        return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PENDING.remove(event.getEntity().getUUID());
    }

    private record PendingSelection(BlockPos pos, ResourceKey<Level> dimension, WardType type, long expiresAt) { }
}
