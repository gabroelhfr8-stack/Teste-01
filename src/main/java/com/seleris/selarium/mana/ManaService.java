package com.seleris.selarium.mana;

import com.seleris.selarium.mana.capability.ManaCapability;
import net.minecraft.world.entity.player.Player;

public final class ManaService {
    private ManaService() {
    }

    public static boolean spend(Player player, int amount) {
        return ManaCapability.getMana(player)
                .map(mana -> mana.consumeMana(amount))
                .orElse(false);
    }

    public static int add(Player player, int amount) {
        return ManaCapability.getMana(player)
                .map(mana -> mana.addMana(amount))
                .orElse(0);
    }
}

