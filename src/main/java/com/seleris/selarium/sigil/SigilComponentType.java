package com.seleris.selarium.sigil;

import com.seleris.selarium.registry.SelariumItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.Optional;

public enum SigilComponentType {
    ARCANE("arcane"),
    AEGIS("aegis"),
    VITAL("vital"),
    FOCUS("focus"),
    BINDING("binding");

    private final String serializedName;

    SigilComponentType(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public Component getDisplayName() {
        return Component.translatable("sigil_component.selarium." + serializedName);
    }

    public static SigilComponentType bySerializedName(String name) {
        return fromSerializedName(name).orElse(ARCANE);
    }

    public static Optional<SigilComponentType> fromSerializedName(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        for (SigilComponentType type : values()) {
            if (type.serializedName.equals(normalized)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    public static Optional<SigilComponentType> fromItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        Item item = stack.getItem();
        if (item == SelariumItems.ARCANE_DUST.get()) {
            return Optional.of(ARCANE);
        }
        if (item == SelariumItems.AEGIS_DUST.get()) {
            return Optional.of(AEGIS);
        }
        if (item == SelariumItems.VITAL_DUST.get()) {
            return Optional.of(VITAL);
        }
        if (item == SelariumItems.FOCUS_DUST.get()) {
            return Optional.of(FOCUS);
        }
        if (item == SelariumItems.BINDING_DUST.get()) {
            return Optional.of(BINDING);
        }
        return Optional.empty();
    }
}
