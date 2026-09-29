package com.seleris.selarium.grinder.menu;

import com.seleris.selarium.blockentity.ArcaneGrinderBlockEntity;
import com.seleris.selarium.registry.SelariumMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ArcaneGrinderMenu extends AbstractContainerMenu {
    private static final int GRINDER_SLOT_COUNT = 3;
    private static final int PLAYER_INVENTORY_START = GRINDER_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final Container container;
    private final ContainerData data;
    private final BlockPos pos;

    public ArcaneGrinderMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, resolveContainer(playerInventory, pos), new SimpleContainerData(2), pos);
    }

    public ArcaneGrinderMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, BlockPos pos) {
        super(SelariumMenus.ARCANE_GRINDER.get(), containerId);
        checkContainerSize(container, GRINDER_SLOT_COUNT);
        checkContainerDataCount(data, 2);
        this.container = container;
        this.data = data;
        this.pos = pos;

        addSlot(new Slot(container, ArcaneGrinderBlockEntity.INPUT_SLOT, 44, 26));
        addSlot(new Slot(container, ArcaneGrinderBlockEntity.REAGENT_SLOT, 44, 50));
        addSlot(new ResultSlot(container, ArcaneGrinderBlockEntity.OUTPUT_SLOT, 124, 38));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }

        addDataSlots(data);
    }

    public int getScaledProgress() {
        int progress = data.get(0);
        int maxProgress = data.get(1);
        if (progress <= 0 || maxProgress <= 0) {
            return 0;
        }
        return Math.min(24, progress * 24 / maxProgress);
    }

    public boolean isCrafting() {
        return data.get(0) > 0 && data.get(1) > 0;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return result;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();
        if (index == ArcaneGrinderBlockEntity.OUTPUT_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, result);
        } else if (index < GRINDER_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, ArcaneGrinderBlockEntity.INPUT_SLOT, ArcaneGrinderBlockEntity.OUTPUT_SLOT, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    private static Container resolveContainer(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof ArcaneGrinderBlockEntity grinder) {
            return grinder;
        }
        return new SimpleContainer(GRINDER_SLOT_COUNT);
    }

    private static class ResultSlot extends Slot {
        private ResultSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
