package com.seleris.selarium.item;

import com.seleris.selarium.inscription.ScrollData;
import com.seleris.selarium.progression.AttunementProgressSavedData;
import com.seleris.selarium.ward.WardActivationService;
import com.seleris.selarium.ward.WardDefinition;
import com.seleris.selarium.ward.WardDefinitions;
import com.seleris.selarium.ward.WardProjectionSavedData;
import com.seleris.selarium.ward.WardStyles;
import com.seleris.selarium.ward.WardType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public final class WardScrollItem extends Item {
    private static final EnumSet<WardType> MOBILE = EnumSet.of(
            WardType.WHISPERING, WardType.SPECTRAL, WardType.BULWARK,
            WardType.REJUVENATION, WardType.FEATHERWEIGHT, WardType.GROUNDING,
            WardType.MAGNETISM, WardType.CLOAKING, WardType.AQUALUNG,
            WardType.IMMORTAL, WardType.DEFLECTION, WardType.PHASING);

    public WardScrollItem(Properties properties) { super(properties); }
    public static boolean isMobile(WardType type) { return MOBILE.contains(type); }

    @Override public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        if (!(context.getPlayer() instanceof ServerPlayer player)) return InteractionResult.FAIL;
        WardType type = ScrollData.ward(context.getItemInHand());
        BlockPos pos = isMobile(type) ? player.blockPosition()
                : context.getClickedPos().relative(context.getClickedFace());
        return cast(player, context.getItemInHand(), pos) ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.fail(stack);
        WardType type = ScrollData.ward(stack);
        BlockPos pos = player.blockPosition();
        if (!isMobile(type)) {
            HitResult hit = player.pick(24.0D, 0.0F, false);
            if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
                player.displayClientMessage(Component.translatable("message.selarium.scroll.point_at_block"), true);
                return InteractionResultHolder.fail(stack);
            }
            pos = blockHit.getBlockPos().relative(blockHit.getDirection());
        }
        return cast(serverPlayer, stack, pos) ? InteractionResultHolder.consume(stack) : InteractionResultHolder.fail(stack);
    }

    private static boolean cast(ServerPlayer player, ItemStack stack, BlockPos target) {
        if (!player.getUUID().equals(ScrollData.creator(stack))) return fail(player, "message.selarium.scroll.foreign");
        WardType type = ScrollData.ward(stack);
        WardDefinition definition = WardDefinitions.get(type).orElse(null);
        if (definition == null || !WardActivationService.isWardEnabled(type)) return fail(player, "message.selarium.scroll.invalid");
        if (!AttunementProgressSavedData.get(player.serverLevel()).knows(player.getUUID(), type)) {
            return fail(player, "message.selarium.scroll.not_discovered");
        }
        WardProjectionSavedData.CastResult result = WardProjectionSavedData.get(player.serverLevel())
                .cast(player, definition, target, isMobile(type));
        if (!result.success()) return fail(player, result.messageKey());
        stack.shrink(1);
        player.displayClientMessage(Component.translatable("message.selarium.scroll.cast",
                Component.translatable(type.getTranslationKey())), true);
        return true;
    }

    private static boolean fail(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
        return false;
    }

    @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        WardType type = ScrollData.ward(stack);
        if (type != WardType.NONE) {
            lines.add(Component.translatable(type.getTranslationKey())
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(WardStyles.primary(type)))));
            lines.add(Component.translatable("tooltip.selarium.scroll.category",
                    Component.translatable(WardStyles.category(type).getTranslationKey())).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable(isMobile(type) ? "tooltip.selarium.scroll.mobile" : "tooltip.selarium.scroll.fixed"));
            WardDefinitions.get(type).ifPresent(definition -> lines.add(Component.translatable("tooltip.selarium.scroll.duration",
                    Math.max(1, definition.durationTicks() / 2) / 20, definition.upkeepCostValue())));
        }
        String creator = ScrollData.creatorName(stack);
        if (creator != null) lines.add(Component.translatable("tooltip.selarium.scroll.creator", creator).withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, level, lines, flag);
    }
}
