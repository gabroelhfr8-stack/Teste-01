package com.seleris.selarium.ward.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

/** Vanilla's saved cooking fields provide a stable, bounded progress gateway. */
final class FurnaceProgressAccess {
    private FurnaceProgressAccess() { }

    static boolean canBoost(AbstractFurnaceBlockEntity furnace) {
        if (furnace.getItem(0).isEmpty()) return false;
        CompoundTag tag = furnace.saveWithoutMetadata();
        return tag.getInt("BurnTime") > 0 && tag.getInt("CookTimeTotal") > 1
                && tag.getInt("CookTime") < tag.getInt("CookTimeTotal") - 1;
    }

    static boolean boost(AbstractFurnaceBlockEntity furnace, int amount) {
        if (amount <= 0 || !canBoost(furnace)) return false;
        CompoundTag tag = furnace.saveWithoutMetadata();
        int total = tag.getInt("CookTimeTotal");
        tag.putInt("CookTime", Math.min(total - 1, tag.getInt("CookTime") + amount));
        furnace.load(tag);
        furnace.setChanged();
        return true;
    }
}
