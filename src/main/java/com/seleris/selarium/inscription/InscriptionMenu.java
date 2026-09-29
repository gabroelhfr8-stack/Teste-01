package com.seleris.selarium.inscription;

import com.seleris.selarium.blockentity.InscriptionBenchBlockEntity;
import com.seleris.selarium.dust.DustUtil;
import com.seleris.selarium.registry.SelariumItems;
import com.seleris.selarium.registry.SelariumMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class InscriptionMenu extends AbstractContainerMenu {
    private final Container input;
    private final SimpleContainer output = new SimpleContainer(1);
    private final Player player;
    private final DataSlot selected = DataSlot.standalone();
    private final DataSlot status = DataSlot.standalone();
    private final BlockPos pos;

    public InscriptionMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, resolve(inventory, pos), pos);
    }

    public InscriptionMenu(int id, Inventory inventory, Container input, BlockPos pos) {
        super(SelariumMenus.INSCRIPTION_BENCH.get(), id);
        checkContainerSize(input, 8);
        this.input = input;
        this.player = inventory.player;
        this.pos = pos;
        addSlot(new Slot(input, 0, 126, 82) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(SelariumItems.EMPTY_SCROLL.get()); }
        });
        for (int i = 0; i < 5; i++) {
                addSlot(new Slot(input, i + 1, 147 + i * 21, 43) {
                @Override public boolean mayPlace(ItemStack stack) { return DustUtil.getDefinition(stack).isPresent(); }
            });
        }
        addSlot(new Slot(input, 6, 173, 82));
        addSlot(new Slot(input, 7, 194, 82));
        addSlot(new ResultSlot());
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 69 + column * 18, 154 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 69 + column * 18, 212));
        }
        addDataSlot(selected);
        addDataSlot(status);
        refresh();
    }

    private static Container resolve(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof InscriptionBenchBlockEntity bench) return bench;
        return new SimpleContainer(8);
    }

    public int selectedIndex() { return selected.get(); }
    public InscriptionRecipe recipe() { return InscriptionRecipe.byIndex(selected.get()); }
    public InscriptionRecipe.Status status() {
        int code = status.get();
        InscriptionRecipe.Status[] values = InscriptionRecipe.Status.values();
        return code >= 0 && code < values.length ? values[code] : InscriptionRecipe.Status.SELECT;
    }

    @Override public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || id >= InscriptionRecipe.COUNT) return false;
        selected.set(id);
        refresh();
        broadcastChanges();
        return true;
    }

    @Override public void slotsChanged(Container container) {
        super.slotsChanged(container);
        refresh();
    }

    private void refresh() {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        InscriptionRecipe recipe = recipe();
        if (recipe == null) {
            output.setItem(0, ItemStack.EMPTY);
            status.set(InscriptionRecipe.Status.SELECT.ordinal());
            return;
        }
        InscriptionRecipe.Status result = recipe.evaluate(serverPlayer, input);
        status.set(result.ordinal());
        output.setItem(0, result == InscriptionRecipe.Status.READY
                ? recipe.result(serverPlayer, input) : ItemStack.EMPTY);
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == 8) {
            if (!moveItemStackTo(stack, 9, slots.size(), true)) return ItemStack.EMPTY;
            slot.onTake(player, stack);
            return original;
        }
        if (index < 8) {
            if (!moveItemStackTo(stack, 9, slots.size(), false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, 8, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }

    @Override public boolean stillValid(Player player) {
        return input.stillValid(player);
    }

    public BlockPos pos() { return pos; }

    private final class ResultSlot extends Slot {
        private ResultSlot() { super(output, 0, 262, 82); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) {
            return player.level().isClientSide ? hasItem()
                    : player instanceof ServerPlayer serverPlayer && recipe() != null
                    && recipe().evaluate(serverPlayer, input) == InscriptionRecipe.Status.READY;
        }
        @Override public void onTake(Player player, ItemStack stack) {
            if (player instanceof ServerPlayer serverPlayer && recipe() != null) {
                recipe().consume(serverPlayer, input);
                refresh();
            }
            super.onTake(player, stack);
        }
    }
}
