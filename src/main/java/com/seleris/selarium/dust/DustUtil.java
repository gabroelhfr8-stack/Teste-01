package com.seleris.selarium.dust;

import com.seleris.selarium.item.DustItem;
import com.seleris.selarium.Selarium;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class DustUtil {
    private DustUtil() {
    }

    public static Optional<DustDefinition> getDefinition(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof DustItem dustItem)) {
            return Optional.empty();
        }
        return Optional.of(dustItem.getDefinition());
    }

    public static boolean isType(ItemStack stack, DustType type) {
        return getDefinition(stack).map(definition -> definition.type() == type).orElse(false);
    }

    public static ItemStack itemFor(DustDefinition definition) {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(
                Selarium.MOD_ID, definition.getSerializedName() + "_dust"));
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }
}

