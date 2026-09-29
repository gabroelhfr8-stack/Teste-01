package com.seleris.selarium.sigil;

import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.dust.DustDefinition;
import com.seleris.selarium.dust.DustPurity;
import com.seleris.selarium.dust.DustType;
import com.seleris.selarium.dust.DustUtil;
import com.seleris.selarium.registry.SelariumMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public final class SigilMenu extends AbstractContainerMenu {
    private final Player player;
    private final BlockPos pos;

    public SigilMenu(int id, Inventory inventory, BlockPos pos) {
        super(SelariumMenus.ARCANE_SIGIL.get(), id);
        this.player = inventory.player;
        this.pos = pos;
    }

    public ArcaneSigilBlockEntity sigil() {
        return player.level().getBlockEntity(pos) instanceof ArcaneSigilBlockEntity sigil ? sigil : null;
    }

    @Override public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer)) return id >= 100 && id < 120;
        ArcaneSigilBlockEntity sigil = sigil();
        if (sigil == null || sigil.isActive() || !player.getUUID().equals(sigil.getOwner())) return false;
        int component = id - 100;
        if (component < 0 || component >= DustType.values().length * 2) return false;
        DustType type = DustType.values()[component / 2];
        DustPurity purity = component % 2 == 0 ? DustPurity.BASIC : DustPurity.REFINED;
        DustDefinition definition = new DustDefinition(type, purity);
        if (!sigil.removeOneComponent(definition)) return false;
        ItemStack returned = DustUtil.itemFor(definition);
        if (!returned.isEmpty() && !serverPlayer.getInventory().add(returned)) {
            serverPlayer.drop(returned, false);
        }
        return true;
    }

    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) {
        return sigil() != null && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }
}
