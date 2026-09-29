package com.seleris.selarium.progression;

import com.seleris.selarium.dust.DustPurity;
import com.seleris.selarium.dust.DustType;
import com.seleris.selarium.mana.IPlayerMana;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import com.seleris.selarium.registry.SelariumBlocks;
import com.seleris.selarium.registry.SelariumItems;

import java.util.List;

public record AttunementMilestone(int tier, int limit, long spent, int distinct, int refined,
                                  List<DustIngredient> dusts, List<Item> catalysts, int ritualCost) {
    public static AttunementMilestone forTier(int tier) {
        return switch (tier) {
            case 1 -> new AttunementMilestone(1, 1000, 250, 3, 0,
                    List.of(basic(DustType.FOCUS), basic(DustType.VITAL), basic(DustType.AEGIS)),
                    List.of(SelariumItems.ARCANE_CRYSTAL.get()), 250);
            case 2 -> new AttunementMilestone(2, 2500, 1200, 6, 2,
                    List.of(refined(DustType.FOCUS), refined(DustType.VITAL), refined(DustType.BINDING)),
                    List.of(Items.BLAZE_ROD, SelariumBlocks.ARCANE_BLOCK.get().asItem()), 700);
            case 3 -> new AttunementMilestone(3, 5000, 4000, 10, 4,
                    List.of(refined(DustType.WARP), refined(DustType.VEIL), refined(DustType.FOCUS)),
                    List.of(Items.DRAGON_BREATH, Items.ENDER_PEARL), 1800);
            case 4 -> new AttunementMilestone(4, 10000, 10000, 16, 8,
                    List.of(refined(DustType.AEGIS), refined(DustType.ECHO), refined(DustType.CHRONO)),
                    List.of(Items.NETHER_STAR, Items.ECHO_SHARD), 4000);
            default -> throw new IllegalArgumentException("Unknown attunement tier: " + tier);
        };
    }

    public boolean hasProgress(ServerPlayer player, IPlayerMana mana) {
        AttunementProgressSavedData progress = AttunementProgressSavedData.get(player.serverLevel());
        return mana.getTotalManaSpent() >= spent
                && progress.distinctCount(player.getUUID()) >= distinct
                && progress.refinedCount(player.getUUID()) >= refined;
    }

    public String missingProgress(ServerPlayer player, IPlayerMana mana) {
        AttunementProgressSavedData progress = AttunementProgressSavedData.get(player.serverLevel());
        if (mana.getTotalManaSpent() < spent) return "message.selarium.attunement.mana_spent";
        if (progress.distinctCount(player.getUUID()) < distinct) return "message.selarium.attunement.wards";
        if (progress.refinedCount(player.getUUID()) < refined) return "message.selarium.attunement.refined";
        return "";
    }

    private static DustIngredient basic(DustType type) {
        return new DustIngredient(type, DustPurity.BASIC);
    }

    private static DustIngredient refined(DustType type) {
        return new DustIngredient(type, DustPurity.REFINED);
    }

    public record DustIngredient(DustType type, DustPurity purity) {
    }
}
