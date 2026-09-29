package com.seleris.selarium.grimoire;

import com.seleris.selarium.registry.SelariumItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class WardingGrimoireEventHandler {
    private WardingGrimoireEventHandler() {
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer player) || !player.isShiftKeyDown()) {
            return;
        }

        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(SelariumItems.WARDING_GRIMOIRE.get())) {
            return;
        }

        if (WardingGrimoireData.recordSoulFromEntity(player, stack, event.getTarget())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}
