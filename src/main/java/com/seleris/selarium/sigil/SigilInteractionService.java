package com.seleris.selarium.sigil;

import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.dust.DustDefinition;
import com.seleris.selarium.dust.DustType;
import com.seleris.selarium.dust.DustUtil;
import com.seleris.selarium.registry.SelariumItems;
import com.seleris.selarium.ward.WardActivationService;
import com.seleris.selarium.ward.WardDefinitions;
import com.seleris.selarium.ward.WardType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkHooks;

import java.util.Optional;

public final class SigilInteractionService {
    private SigilInteractionService() {
    }

    public static InteractionResult use(Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(SelariumItems.ATTUNEMENT_SCROLL.get()) || stack.is(SelariumItems.WARD_SCROLL.get())) {
            return InteractionResult.PASS;
        }
        Optional<DustDefinition> heldDust = DustUtil.getDefinition(stack);

        if (heldDust.isEmpty() && !level.isClientSide && player instanceof ServerPlayer serverPlayer
                && SigilSelectionService.confirm(serverPlayer, pos)) {
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown() && (heldDust.isEmpty() || DustUtil.isType(stack, DustType.ARCANE))) {
            if (!level.isClientSide) {
                toggleSigil(level, pos, player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (heldDust.isPresent()) {
            if (!level.isClientSide) {
                addComponent(level, pos, player, stack, heldDust.get());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof ArcaneSigilBlockEntity sigil) {
            NetworkHooks.openScreen(serverPlayer, sigil, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void toggleSigil(Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof ArcaneSigilBlockEntity sigil)) {
            return;
        }
        if (!player.getUUID().equals(sigil.getOwner())) {
            player.displayClientMessage(Component.translatable("message.selarium.sigil.owner_only"), true);
            return;
        }

        if (sigil.isActive()) {
            int cooldown = WardDefinitions.get(sigil.getWardType()).map(definition -> definition.cooldownTicks()).orElse(0);
            sigil.deactivateWard(cooldown);
            player.displayClientMessage(Component.translatable("message.selarium.sigil.deactivated"), true);
            return;
        }

        var resolvedDefinition = WardDefinitions.get(sigil.getWardType())
                .filter(definition -> definition.requirements().stream().allMatch(requirement -> requirement.matches(sigil)));
        if (resolvedDefinition.isEmpty() || resolvedDefinition.get().type() == WardType.NONE) {
            player.displayClientMessage(Component.translatable("message.selarium.sigil.missing_components"), true);
            return;
        }

        WardActivationService.ActivationResult result = WardActivationService.activate(sigil, level, pos, player, resolvedDefinition.get());
        if (!result.success()) {
            player.displayClientMessage(Component.translatable(result.messageKey()), true);
            return;
        }

        player.displayClientMessage(Component.translatable("message.selarium.sigil.activated", Component.translatable(resolvedDefinition.get().type().getTranslationKey())), true);
    }

    private static void addComponent(Level level, BlockPos pos, Player player, ItemStack stack, DustDefinition dustDefinition) {
        if (!(level.getBlockEntity(pos) instanceof ArcaneSigilBlockEntity sigil)) {
            return;
        }
        if (!player.getUUID().equals(sigil.getOwner())) {
            player.displayClientMessage(Component.translatable("message.selarium.sigil.owner_only"), true);
            return;
        }
        if (sigil.isActive()) {
            player.displayClientMessage(Component.translatable("message.selarium.sigil.deactivate_first"), true);
            return;
        }

        if (!sigil.addComponent(dustDefinition)) {
            player.displayClientMessage(Component.translatable("message.selarium.sigil.component_limit"), true);
            return;
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        player.displayClientMessage(Component.translatable("message.selarium.sigil.component_added", dustDefinition.getDisplayName()), true);
    }
}
