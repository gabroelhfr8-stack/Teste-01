package com.seleris.selarium.inscription;

import com.seleris.selarium.dust.DustDefinition;
import com.seleris.selarium.dust.DustPurity;
import com.seleris.selarium.dust.DustType;
import com.seleris.selarium.dust.DustUtil;
import com.seleris.selarium.mana.capability.ManaCapability;
import com.seleris.selarium.progression.AttunementMilestone;
import com.seleris.selarium.progression.AttunementProgressSavedData;
import com.seleris.selarium.registry.SelariumBlocks;
import com.seleris.selarium.registry.SelariumItems;
import com.seleris.selarium.ward.WardDefinition;
import com.seleris.selarium.ward.WardDefinitions;
import com.seleris.selarium.ward.WardRequirement;
import com.seleris.selarium.ward.WardTier;
import com.seleris.selarium.ward.WardType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class InscriptionRecipe {
    public static final int COUNT = 4 + 32;
    private final int index;
    private final AttunementMilestone milestone;
    private final WardDefinition ward;
    private final List<AttunementMilestone.DustIngredient> dusts;
    private final List<Item> catalysts;

    private InscriptionRecipe(int index, AttunementMilestone milestone, WardDefinition ward,
                              List<AttunementMilestone.DustIngredient> dusts, List<Item> catalysts) {
        this.index = index;
        this.milestone = milestone;
        this.ward = ward;
        this.dusts = dusts;
        this.catalysts = catalysts;
    }

    public static InscriptionRecipe byIndex(int index) {
        if (index >= 0 && index < 4) {
            AttunementMilestone milestone = AttunementMilestone.forTier(index + 1);
            return new InscriptionRecipe(index, milestone, null, milestone.dusts(), milestone.catalysts());
        }
        int wardIndex = index - 4;
        if (wardIndex < 0 || wardIndex >= WardDefinitions.all().size()) return null;
        WardDefinition ward = WardDefinitions.all().get(wardIndex);
        List<AttunementMilestone.DustIngredient> dusts = new ArrayList<>();
        for (WardRequirement requirement : ward.requirements()) {
            for (int count = 0; count < requirement.count(); count++) {
                dusts.add(new AttunementMilestone.DustIngredient(requirement.type(), requirement.minimumPurity()));
            }
        }
        Item catalyst = ward.tier() == WardTier.REFINED
                ? SelariumBlocks.ARCANE_BLOCK.get().asItem() : SelariumItems.ARCANE_CRYSTAL.get();
        return new InscriptionRecipe(index, null, ward, List.copyOf(dusts), List.of(catalyst));
    }

    public int index() { return index; }
    public boolean attunement() { return milestone != null; }
    public AttunementMilestone milestone() { return milestone; }
    public WardDefinition ward() { return ward; }
    public List<AttunementMilestone.DustIngredient> dusts() { return dusts; }
    public List<Item> catalysts() { return catalysts; }
    public Component name() {
        return milestone != null
                ? Component.translatable("gui.selarium.attunement_recipe", milestone.limit())
                : Component.translatable(ward.type().getTranslationKey());
    }

    public Status evaluate(ServerPlayer player, Container container) {
        if (!container.getItem(0).is(SelariumItems.EMPTY_SCROLL.get())) return Status.EMPTY_SCROLL;
        if (milestone != null) {
            var mana = ManaCapability.getMana(player).orElse(null);
            if (mana == null || mana.getUnlockedManaTier() != milestone.tier() - 1) return Status.TIER_ORDER;
            var progress = AttunementProgressSavedData.get(player.serverLevel());
            if (mana.getTotalManaSpent() < milestone.spent()) return Status.MANA_SPENT;
            if (progress.distinctCount(player.getUUID()) < milestone.distinct()) return Status.WARDS;
            if (progress.refinedCount(player.getUUID()) < milestone.refined()) return Status.REFINED;
        } else if (!AttunementProgressSavedData.get(player.serverLevel()).knows(player.getUUID(), ward.type())) {
            return Status.NOT_DISCOVERED;
        }
        if (dustSlots(container) == null) return Status.DUST;
        if (catalystSlots(container) == null) return Status.CATALYST;
        return Status.READY;
    }

    public ItemStack result(ServerPlayer player, Container container) {
        if (evaluate(player, container) != Status.READY) return ItemStack.EMPTY;
        return milestone != null ? ScrollData.attunement(player, milestone.tier())
                : ScrollData.ward(player, ward.type());
    }

    public boolean consume(ServerPlayer player, Container container) {
        if (evaluate(player, container) != Status.READY) return false;
        int[] dustSlots = dustSlots(container);
        int[] catalystSlots = catalystSlots(container);
        if (dustSlots == null || catalystSlots == null) return false;
        container.removeItem(0, 1);
        for (int slot : dustSlots) container.removeItem(slot, 1);
        for (int slot : catalystSlots) container.removeItem(slot, 1);
        return true;
    }

    private int[] dustSlots(Container container) {
        int[] remaining = new int[5];
        int[] selection = new int[dusts.size()];
        for (int i = 0; i < 5; i++) remaining[i] = container.getItem(i + 1).getCount();
        // Refined ingredients reserve refined stacks before flexible basic ingredients.
        for (DustPurity purity : new DustPurity[]{DustPurity.REFINED, DustPurity.BASIC}) {
            for (int i = 0; i < dusts.size(); i++) {
                var ingredient = dusts.get(i);
                if (ingredient.purity() != purity) continue;
                boolean found = false;
                for (int slot = 1; slot <= 5; slot++) {
                    if (remaining[slot - 1] <= 0) continue;
                    DustDefinition actual = DustUtil.getDefinition(container.getItem(slot)).orElse(null);
                    if (actual != null && actual.satisfies(ingredient.type(), ingredient.purity())) {
                        selection[i] = slot;
                        remaining[slot - 1]--;
                        found = true;
                        break;
                    }
                }
                if (!found) return null;
            }
        }
        return selection;
    }

    private int[] catalystSlots(Container container) {
        int[] remaining = new int[]{container.getItem(6).getCount(), container.getItem(7).getCount()};
        int[] selection = new int[catalysts.size()];
        for (int i = 0; i < catalysts.size(); i++) {
            boolean found = false;
            for (int slot = 6; slot <= 7; slot++) {
                if (remaining[slot - 6] > 0 && container.getItem(slot).is(catalysts.get(i))) {
                    remaining[slot - 6]--;
                    selection[i] = slot;
                    found = true;
                    break;
                }
            }
            if (!found) return null;
        }
        return selection;
    }

    public enum Status {
        SELECT("gui.selarium.inscription.select"), READY("gui.selarium.inscription.ready"),
        EMPTY_SCROLL("gui.selarium.inscription.empty_scroll"),
        TIER_ORDER("message.selarium.attunement.tier_order"),
        MANA_SPENT("message.selarium.attunement.mana_spent"),
        WARDS("message.selarium.attunement.wards"),
        REFINED("message.selarium.attunement.refined"),
        NOT_DISCOVERED("message.selarium.scroll.not_discovered"),
        DUST("gui.selarium.inscription.dust"),
        CATALYST("gui.selarium.inscription.catalyst");

        private final String key;
        Status(String key) { this.key = key; }
        public Component message() { return Component.translatable(key); }
    }
}
