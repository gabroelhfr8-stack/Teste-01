package com.seleris.selarium.inscription;

import com.seleris.selarium.registry.SelariumItems;
import com.seleris.selarium.ward.WardType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public final class ScrollData {
    private ScrollData() { }

    public static ItemStack attunement(UUID creator, int tier) {
        ItemStack stack = new ItemStack(SelariumItems.ATTUNEMENT_SCROLL.get());
        stack.getOrCreateTag().putUUID("Creator", creator);
        stack.getOrCreateTag().putInt("Tier", tier);
        return stack;
    }

    public static ItemStack ward(UUID creator, WardType type) {
        ItemStack stack = new ItemStack(SelariumItems.WARD_SCROLL.get());
        stack.getOrCreateTag().putUUID("Creator", creator);
        stack.getOrCreateTag().putString("Ward", type.getSerializedName());
        return stack;
    }

    public static UUID creator(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.hasUUID("Creator") ? tag.getUUID("Creator") : null;
    }

    public static int tier(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt("Tier");
    }

    public static WardType ward(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? WardType.NONE : WardType.bySerializedName(tag.getString("Ward"));
    }
}
