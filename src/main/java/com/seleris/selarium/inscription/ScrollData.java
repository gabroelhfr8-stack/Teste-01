package com.seleris.selarium.inscription;

import com.seleris.selarium.registry.SelariumItems;
import com.seleris.selarium.ward.WardType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public final class ScrollData {
    private static final String CREATOR = "Creator";
    private static final String CREATOR_NAME = "CreatorName";

    private ScrollData() { }

    public static ItemStack attunement(Player creator, int tier) {
        ItemStack stack = new ItemStack(SelariumItems.ATTUNEMENT_SCROLL.get());
        stamp(stack, creator);
        stack.getOrCreateTag().putInt("Tier", tier);
        return stack;
    }

    public static ItemStack ward(Player creator, WardType type) {
        ItemStack stack = new ItemStack(SelariumItems.WARD_SCROLL.get());
        stamp(stack, creator);
        stack.getOrCreateTag().putString("Ward", type.getSerializedName());
        return stack;
    }

    /** The id decides who may use the scroll; the name is only there so tooltips do not show a raw UUID. */
    private static void stamp(ItemStack stack, Player creator) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID(CREATOR, creator.getUUID());
        tag.putString(CREATOR_NAME, creator.getGameProfile().getName());
    }

    public static UUID creator(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.hasUUID(CREATOR) ? tag.getUUID(CREATOR) : null;
    }

    /** Display name of the creator, or null when the scroll has none. Scrolls from before names were stored show a short id. */
    public static String creatorName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.hasUUID(CREATOR)) return null;
        if (tag.contains(CREATOR_NAME, Tag.TAG_STRING) && !tag.getString(CREATOR_NAME).isBlank()) {
            return tag.getString(CREATOR_NAME);
        }
        return tag.getUUID(CREATOR).toString().substring(0, 8);
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
