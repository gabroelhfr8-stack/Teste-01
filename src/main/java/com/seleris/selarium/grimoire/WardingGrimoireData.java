package com.seleris.selarium.grimoire;

import com.seleris.selarium.ward.WardFieldSource;
import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.registry.SelariumItems;
import com.seleris.selarium.ward.WardType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public final class WardingGrimoireData {
    public static final String ROOT_TAG = "WardingGrimoire";

    private WardingGrimoireData() {
    }

    public static WardingRuleSet rulesForSigil(WardFieldSource sigil) {
        if (sigil.getOwner() == null || !(sigil.getLevel() instanceof ServerLevel serverLevel)) {
            return WardingRuleSet.defaults(sigil.getOwner());
        }
        return WardingGrimoireSavedData.get(serverLevel).getRules(sigil.getOwner());
    }

    public static WardingRuleSet bindOrRead(ServerLevel level, ItemStack stack, ServerPlayer player) {
        boolean hasItemRules = stack.hasTag() && stack.getTag() != null && stack.getTag().contains(ROOT_TAG);
        WardingRuleSet rules = hasItemRules
                ? readFromItem(stack, player.getUUID())
                : WardingGrimoireSavedData.get(level).getRules(player.getUUID());
        if (rules.owner() == null) {
            rules.owner(player.getUUID());
        }
        if (hasItemRules && rules.owner() != null) {
            mergeSavedKnowledge(WardingGrimoireSavedData.get(level).getRules(rules.owner()), rules);
        }
        writeToItem(stack, rules);
        WardingGrimoireSavedData.get(level).putRules(rules);
        return rules.copy();
    }

    public static boolean canEdit(ItemStack stack, ServerPlayer player) {
        WardingRuleSet rules = readFromItem(stack, null);
        return rules.owner() == null
                || player.getUUID().equals(rules.owner())
                || !SelariumCommonConfig.WARDING_GRIMOIRE_OWNER_LOCKED.get();
    }

    public static boolean updateHeld(ServerPlayer player, WardingRuleSet incoming) {
        if (!SelariumCommonConfig.WARDING_GRIMOIRE_ENABLED.get()) {
            return false;
        }
        Optional<ItemStack> held = findHeldGrimoire(player);
        if (held.isEmpty() || !canEdit(held.get(), player)) {
            return false;
        }
        WardingRuleSet existing = bindOrRead(player.serverLevel(), held.get(), player);
        WardingRuleSet sanitized = incoming.copy();
        sanitized.owner(existing.owner() == null ? player.getUUID() : existing.owner());
        preserveLockedKnowledge(existing, sanitized);
        sanitized.sanitize();
        writeToItem(held.get(), sanitized);
        WardingGrimoireSavedData.get(player.serverLevel()).putRules(sanitized);
        return true;
    }

    public static WardingRuleSet readHeldOrSaved(ServerPlayer player) {
        Optional<ItemStack> held = findHeldGrimoire(player);
        if (held.isPresent()) {
            return bindOrRead(player.serverLevel(), held.get(), player);
        }
        return WardingGrimoireSavedData.get(player.serverLevel()).getRules(player.getUUID());
    }

    public static boolean resetHeldOrPlayer(ServerPlayer player) {
        WardingRuleSet defaults = WardingRuleSet.defaults(player.getUUID());
        findHeldGrimoire(player).ifPresent(stack -> writeToItem(stack, defaults));
        WardingGrimoireSavedData.get(player.serverLevel()).putRules(defaults);
        return true;
    }

    public static boolean recordSoulFromEntity(ServerPlayer player, ItemStack stack, Entity entity) {
        if (!SelariumCommonConfig.WARDING_GRIMOIRE_ENABLED.get() || !stack.is(SelariumItems.WARDING_GRIMOIRE.get())) {
            return false;
        }
        if (!canEdit(stack, player)) {
            player.displayClientMessage(Component.translatable("message.selarium.grimoire.owner_locked"), true);
            return true;
        }
        WardingRuleSet rules = bindOrRead(player.serverLevel(), stack, player);
        SoulEssenceCategory category = SoulEssenceClassifier.classify(entity);
        if (category == SoulEssenceCategory.UNKNOWN) {
            player.displayClientMessage(Component.translatable("message.selarium.grimoire.soul_unreadable"), true);
            return false;
        }

        ResourceLocation entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        boolean newSoul = rules.unlockSoul(category);
        boolean newType = entityTypeId != null && rules.addKnownEntityType(entityTypeId.toString());
        writeToItem(stack, rules);
        WardingGrimoireSavedData.get(player.serverLevel()).putRules(rules);

        if (newSoul) {
            player.displayClientMessage(Component.translatable("message.selarium.grimoire.soul_recorded", Component.translatable(category.getTranslationKey())), true);
        } else if (newType) {
            player.displayClientMessage(Component.translatable("message.selarium.grimoire.entity_recorded", entityTypeId.toString()), true);
        } else {
            player.displayClientMessage(Component.translatable("message.selarium.grimoire.soul_known"), true);
        }
        return true;
    }

    public static boolean unlockWardForOwner(ServerLevel level, UUID owner, WardType type) {
        if (owner == null || type == null || type == WardType.NONE) {
            return false;
        }
        WardingRuleSet rules = WardingGrimoireSavedData.get(level).getRules(owner);
        boolean changed = rules.unlockWard(type);
        if (changed) {
            WardingGrimoireSavedData.get(level).putRules(rules);
        }
        return changed;
    }

    public static boolean unlockSoulForPlayer(ServerPlayer player, SoulEssenceCategory category) {
        if (category == null || category == SoulEssenceCategory.UNKNOWN) {
            return false;
        }
        WardingRuleSet rules = readHeldOrSaved(player);
        boolean changed = rules.unlockSoul(category);
        if (changed) {
            findHeldGrimoire(player).ifPresent(stack -> writeToItem(stack, rules));
            WardingGrimoireSavedData.get(player.serverLevel()).putRules(rules);
        }
        return changed;
    }

    public static boolean unlockWardForPlayer(ServerPlayer player, WardType type) {
        if (type == null || type == WardType.NONE) {
            return false;
        }
        WardingRuleSet rules = readHeldOrSaved(player);
        boolean changed = rules.unlockWard(type);
        if (changed) {
            findHeldGrimoire(player).ifPresent(stack -> writeToItem(stack, rules));
            WardingGrimoireSavedData.get(player.serverLevel()).putRules(rules);
        }
        return changed;
    }

    public static Optional<ItemStack> findHeldGrimoire(ServerPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.is(SelariumItems.WARDING_GRIMOIRE.get())) {
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    public static WardingRuleSet readFromItem(ItemStack stack, UUID fallbackOwner) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(ROOT_TAG)) {
            return WardingRuleSet.defaults(fallbackOwner);
        }
        return WardingRuleSet.fromTag(tag.getCompound(ROOT_TAG), fallbackOwner);
    }

    public static void writeToItem(ItemStack stack, WardingRuleSet rules) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(ROOT_TAG, rules.toTag());
    }

    public static String summarize(WardingRuleSet rules) {
        return "owner=" + (rules.owner() == null ? "none" : rules.owner())
                + " souls=" + rules.unlockedSoulCategories()
                + " knownTypes=" + rules.knownEntityTypes()
                + " unlockedWards=" + rules.unlockedWardTypes()
                + " hostiles=" + rules.affectHostileMobs()
                + " passives=" + rules.affectPassiveMobs()
                + " neutral=" + rules.affectNeutralMobs()
                + " villagers=" + rules.affectVillagers()
                + " pets=" + rules.affectPets()
                + " players=" + rules.affectPlayers()
                + " bosses=" + rules.affectBosses()
                + " ignorePets=" + rules.ignoreTamedPets()
                + " ignoreNamed=" + rules.ignoreNamedEntities()
                + " allow=" + rules.allowPlayers()
                + " deny=" + rules.denyPlayers()
                + " allowTypes=" + rules.allowEntityTypeIds()
                + " denyTypes=" + rules.denyEntityTypeIds()
                + " wardRules=" + rules.wardRules().keySet();
    }

    public static String summarizeWardRule(WardingRuleSet rules, WardType type) {
        if (type == null || type == WardType.NONE) {
            return "ward=none";
        }
        WardingWardRule rule = rules.wardRuleOrNull(type);
        if (!rules.isWardUnlocked(type)) {
            return "ward=" + type.getSerializedName() + " locked";
        }
        if (rule == null) {
            return "ward=" + type.getSerializedName() + " no custom rule";
        }
        return "ward=" + type.getSerializedName()
                + " useGlobal=" + rule.useGlobalRules()
                + " hostiles=" + rule.affectHostiles()
                + " passives=" + rule.affectPassives()
                + " neutral=" + rule.affectNeutral()
                + " villagers=" + rule.affectVillagers()
                + " pets=" + rule.affectPets()
                + " players=" + rule.affectPlayers()
                + " owner=" + rule.affectOwner()
                + " allies=" + rule.affectAllies()
                + " bosses=" + rule.affectBosses()
                + " allow=" + rule.allowPlayers()
                + " deny=" + rule.denyPlayers()
                + " allowTypes=" + rule.allowEntityTypeIds()
                + " denyTypes=" + rule.denyEntityTypeIds();
    }

    public static void notifySaved(ServerPlayer player) {
        player.displayClientMessage(Component.translatable("message.selarium.grimoire.saved"), true);
    }

    private static void preserveLockedKnowledge(WardingRuleSet existing, WardingRuleSet incoming) {
        existing.unlockedSoulCategories().forEach(incoming::unlockSoul);
        existing.knownEntityTypes().forEach(incoming::addKnownEntityType);
        existing.unlockedWardTypes().forEach(incoming::unlockWard);
        if (!incoming.knowsSoul(SoulEssenceCategory.MONSTROUS)) {
            incoming.affectHostileMobs(existing.affectHostileMobs());
            incoming.affectBosses(existing.affectBosses());
        }
        if (!incoming.knowsSoul(SoulEssenceCategory.BESTIAL)) {
            incoming.affectPassiveMobs(existing.affectPassiveMobs());
            incoming.affectNeutralMobs(existing.affectNeutralMobs());
            incoming.affectPets(existing.affectPets());
            incoming.ignoreTamedPets(existing.ignoreTamedPets());
        }
        if (!incoming.knowsSoul(SoulEssenceCategory.ANTHROPIC)) {
            incoming.affectPlayers(existing.affectPlayers());
            incoming.affectVillagers(existing.affectVillagers());
            incoming.affectOwnerWithNegativeWards(existing.affectOwnerWithNegativeWards());
        }
        incoming.wardRules().entrySet().removeIf(entry -> !incoming.isWardUnlocked(entry.getKey()));
    }

    private static void mergeSavedKnowledge(WardingRuleSet saved, WardingRuleSet itemRules) {
        saved.unlockedSoulCategories().forEach(itemRules::unlockSoul);
        saved.knownEntityTypes().forEach(itemRules::addKnownEntityType);
        saved.unlockedWardTypes().forEach(itemRules::unlockWard);
        saved.wardRules().forEach((type, rule) -> itemRules.wardRules().putIfAbsent(type, rule.copy()));
        itemRules.sanitize();
    }
}
