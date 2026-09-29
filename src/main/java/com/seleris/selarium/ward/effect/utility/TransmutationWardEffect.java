package com.seleris.selarium.ward.effect.utility;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardArea;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardFx;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.ConfiguredWardEffect;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/** Utility ward: converts a few dropped items and ages copper blocks. */
public final class TransmutationWardEffect extends ConfiguredWardEffect {
    public TransmutationWardEffect() {
        super(WardType.TRANSMUTATION);
    }

    @Override
    protected void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
        int maxBlocks = WardEffectUtils.blockCap(config.maxBlocksPerCycle().get());
        int range = config.range().get();
        int transformedItems = 0;
        if (config.optionA().get() && config.maxEntitiesPerCycle().get() > 0) {
            WardArea itemArea = WardArea.of(context, Math.max(1, Math.min(range, 3)));
            for (ItemEntity itemEntity : itemArea.entities(ItemEntity.class, item -> item.isAlive())) {
                if (transformedItems >= WardEffectUtils.entityCap(config.maxEntitiesPerCycle().get())) {
                    break;
                }
                ItemStack stack = itemEntity.getItem();
                Optional<Item> result = transmuteItem(stack.getItem());
                if (stack.isEmpty() || result.isEmpty() || !canUseFieldMana(context, config.manaCost().get())) {
                    continue;
                }
                stack.shrink(1);
                ItemStack output = new ItemStack(result.get());
                if (stack.isEmpty()) {
                    itemEntity.discard();
                }
                context.level().addFreshEntity(new ItemEntity(context.level(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), output));
                WardFx.burst(context.level(), itemEntity.position().add(0.0D, 0.3D, 0.0D), WardType.TRANSMUTATION, 5, 0.2D);
                transformedItems++;
            }
        }

        int applied = 0;
        WardArea area = WardArea.of(context, range);
        for (BlockPos pos : BlockPos.betweenClosed(context.pos().offset(-range, -range, -range), context.pos().offset(range, range, range))) {
            if (maxBlocks <= 0 || applied >= maxBlocks) {
                break;
            }
            if (!area.contains(pos)) {
                continue;
            }
            BlockState state = context.level().getBlockState(pos);
            Optional<BlockState> nextCopper = config.optionB().get()
                    ? WeatheringCopper.getNext(state.getBlock()).map(block -> block.withPropertiesOf(state))
                    : Optional.empty();
            if (nextCopper.isEmpty()) {
                continue;
            }
            if (!canUseFieldMana(context, config.manaCost().get())) {
                break;
            }
            context.level().setBlock(pos, nextCopper.get(), Block.UPDATE_ALL);
            WardFx.burst(context.level(), Vec3.atCenterOf(pos), WardType.TRANSMUTATION, 4, 0.35D);
            applied++;
        }
        context.sigil().recordWardDebug(transformedItems, applied, transformedItems + applied > 0 ? "transmutation applied" : "no valid item/block");
    }

    private static Optional<Item> transmuteItem(Item item) {
        if (item == Items.COBBLESTONE) {
            return Optional.of(Items.STONE);
        }
        if (item == Items.STONE) {
            return Optional.of(Items.SMOOTH_STONE);
        }
        if (item == Items.SAND) {
            return Optional.of(Items.GLASS);
        }
        if (item == Items.ROTTEN_FLESH) {
            return Optional.of(Items.BONE_MEAL);
        }
        return Optional.empty();
    }
}
