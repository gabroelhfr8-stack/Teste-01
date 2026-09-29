package com.seleris.selarium.mana.capability;

import com.seleris.selarium.mana.IPlayerMana;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;

public final class ManaCapability {
    public static final Capability<IPlayerMana> PLAYER_MANA = CapabilityManager.get(new CapabilityToken<>() {
    });

    private ManaCapability() {
    }

    public static LazyOptional<IPlayerMana> getMana(Player player) {
        return player.getCapability(PLAYER_MANA);
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IPlayerMana.class);
    }
}
