package com.seleris.selarium.item;

import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.inscription.ScrollData;
import com.seleris.selarium.mana.capability.ManaCapability;
import com.seleris.selarium.progression.AttunementMilestone;
import com.seleris.selarium.ward.WardManaService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class AttunementScrollItem extends Item {
    public AttunementScrollItem(Properties properties) { super(properties); }

    @Override public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        if (!(context.getPlayer() instanceof ServerPlayer player)) return InteractionResult.FAIL;
        ItemStack stack = context.getItemInHand();
        if (!player.getUUID().equals(ScrollData.creator(stack))) return fail(player, "message.selarium.scroll.foreign");
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ArcaneSigilBlockEntity sigil)
                || !player.getUUID().equals(sigil.getOwner())) return fail(player, "message.selarium.attunement.own_sigil");
        if (sigil.isActive()) return fail(player, "message.selarium.attunement.inactive_sigil");
        int tier = ScrollData.tier(stack);
        if (tier < 1 || tier > 4) return fail(player, "message.selarium.scroll.invalid");
        AttunementMilestone milestone = AttunementMilestone.forTier(tier);
        var mana = ManaCapability.getMana(player).orElse(null);
        if (mana == null) return fail(player, "message.selarium.ward.not_enough_mana");
        if (mana.getUnlockedManaTier() != tier - 1) return fail(player, "message.selarium.attunement.tier_order");
        String missing = milestone.missingProgress(player, mana);
        if (!missing.isEmpty()) return fail(player, missing);
        if (!WardManaService.consumeRitual(player.serverLevel(), context.getClickedPos(), sigil, milestone.ritualCost())) {
            return fail(player, "message.selarium.ward.not_enough_mana");
        }
        mana.setUnlockedManaTier(tier);
        stack.shrink(1);
        player.displayClientMessage(Component.translatable("message.selarium.attunement.success", milestone.limit()), true);
        return InteractionResult.CONSUME;
    }

    private static InteractionResult fail(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
        return InteractionResult.FAIL;
    }

    @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        int tier = ScrollData.tier(stack);
        if (tier >= 1 && tier <= 4) {
            AttunementMilestone milestone = AttunementMilestone.forTier(tier);
            lines.add(Component.translatable("tooltip.selarium.attunement.limit", milestone.limit()));
            lines.add(Component.translatable("tooltip.selarium.attunement.requirements",
                    milestone.spent(), milestone.distinct(), milestone.refined()));
            lines.add(Component.translatable("tooltip.selarium.attunement.cost", milestone.ritualCost()));
        }
        if (ScrollData.creator(stack) != null) lines.add(Component.translatable("tooltip.selarium.scroll.creator", ScrollData.creator(stack).toString()));
        super.appendHoverText(stack, level, lines, flag);
    }
}
