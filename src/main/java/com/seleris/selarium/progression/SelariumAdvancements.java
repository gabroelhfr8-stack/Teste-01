package com.seleris.selarium.progression;

import com.seleris.selarium.Selarium;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Grants the advancements that no vanilla trigger can express (placing a sigil, activating a ward, attuning to a
 * mana tier). Obtaining an item is handled by plain data-pack criteria in {@code data/selarium/advancements}.
 */
public final class SelariumAdvancements {
    private static final String CRITERION = "code";

    private SelariumAdvancements() {
    }

    public static void grant(Player player, String id) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        var advancement = serverPlayer.server.getAdvancements().getAdvancement(ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID, id));
        if (advancement != null) {
            serverPlayer.getAdvancements().award(advancement, CRITERION);
        }
    }
}
