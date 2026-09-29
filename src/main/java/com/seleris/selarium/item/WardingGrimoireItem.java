package com.seleris.selarium.item;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.grimoire.WardingGrimoireData;
import com.seleris.selarium.grimoire.WardingRuleSet;
import com.seleris.selarium.network.SelariumNetwork;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WardingGrimoireItem extends Item {
    public WardingGrimoireItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!SelariumCommonConfig.WARDING_GRIMOIRE_ENABLED.get()) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            boolean editable = WardingGrimoireData.canEdit(stack, serverPlayer);
            if (!editable) {
                serverPlayer.displayClientMessage(Component.translatable("message.selarium.grimoire.owner_locked"), true);
            }
            WardingRuleSet rules = WardingGrimoireData.bindOrRead(serverPlayer.serverLevel(), stack, serverPlayer);
            SelariumNetwork.sendOpenGrimoire(serverPlayer, rules);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
