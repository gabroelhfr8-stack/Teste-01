package com.seleris.selarium.blockentity;

import com.seleris.selarium.inscription.InscriptionMenu;
import com.seleris.selarium.registry.SelariumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class InscriptionBenchBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int INPUT_COUNT = 8;
    private NonNullList<ItemStack> items = NonNullList.withSize(INPUT_COUNT, ItemStack.EMPTY);

    public InscriptionBenchBlockEntity(BlockPos pos, BlockState state) {
        super(SelariumBlockEntities.INSCRIPTION_BENCH.get(), pos, state);
    }

    @Override public Component getDisplayName() { return Component.translatable("block.selarium.inscription_bench"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new InscriptionMenu(id, inventory, this, worldPosition);
    }

    @Override public int getContainerSize() { return INPUT_COUNT; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return slot >= 0 && slot < INPUT_COUNT ? items.get(slot) : ItemStack.EMPTY; }
    @Override public ItemStack removeItem(int slot, int count) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, count);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        return slot >= 0 && slot < INPUT_COUNT ? ContainerHelper.takeItem(items, slot) : ItemStack.EMPTY;
    }
    @Override public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < INPUT_COUNT) {
            items.set(slot, stack.copy());
            setChanged();
        }
    }
    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= 64;
    }
    @Override public void clearContent() {
        for (int i = 0; i < INPUT_COUNT; i++) items.set(i, ItemStack.EMPTY);
        setChanged();
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        items = NonNullList.withSize(INPUT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
    }
}
