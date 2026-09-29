package com.seleris.selarium.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.seleris.selarium.blockentity.ArcaneGrinderBlockEntity;
import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.blockentity.ManaTankBlockEntity;
import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.dust.DustDefinition;
import com.seleris.selarium.dust.DustType;
import com.seleris.selarium.grimoire.SoulEssenceCategory;
import com.seleris.selarium.grimoire.SoulEssenceClassifier;
import com.seleris.selarium.grimoire.WardingGrimoireData;
import com.seleris.selarium.grimoire.WardingRuleSet;
import com.seleris.selarium.mana.IPlayerMana;
import com.seleris.selarium.mana.capability.ManaCapability;
import com.seleris.selarium.registry.SelariumSoundEvents;
import com.seleris.selarium.util.TargetingUtil;
import com.seleris.selarium.ward.ActiveWardIndex;
import com.seleris.selarium.ward.WardActivationService;
import com.seleris.selarium.ward.WardAccessService;
import com.seleris.selarium.ward.WardDefinition;
import com.seleris.selarium.ward.WardDefinitions;
import com.seleris.selarium.ward.WardType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class SelariumCommands {
    private static final int MAX_DEBUG_MANA_AMOUNT = 1_000_000;

    private SelariumCommands() {
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("selarium")
                .requires(source -> source.hasPermission(2) && SelariumCommonConfig.DEBUG_COMMANDS_ENABLED.get())
                .then(Commands.literal("mana")
                        .then(Commands.literal("get")
                                .executes(context -> manaGet(context.getSource(), context.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> manaGet(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, MAX_DEBUG_MANA_AMOUNT))
                                                .executes(context -> manaSet(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount"))))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, MAX_DEBUG_MANA_AMOUNT))
                                                .executes(context -> manaAdd(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount"))))))
                        .then(Commands.literal("drain")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, MAX_DEBUG_MANA_AMOUNT))
                                                .executes(context -> manaDrain(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount"))))))
                        .then(Commands.literal("max")
                                .then(Commands.literal("get")
                                        .executes(context -> manaMaxGet(context.getSource(), context.getSource().getPlayerOrException()))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(context -> manaMaxGet(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, MAX_DEBUG_MANA_AMOUNT))
                                                        .executes(context -> manaMaxSet(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount")))))))
                        .then(Commands.literal("tier")
                                .then(Commands.literal("get")
                                        .executes(context -> manaTierGet(context.getSource(), context.getSource().getPlayerOrException()))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(context -> manaTierGet(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("tier", IntegerArgumentType.integer(0, 4))
                                                        .executes(context -> manaTierSet(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "tier")))))))
                        .then(Commands.literal("xp")
                                .then(Commands.literal("get")
                                        .executes(context -> manaXpGet(context.getSource(), context.getSource().getPlayerOrException()))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(context -> manaXpGet(context.getSource(), EntityArgument.getPlayer(context, "player")))))))
                .then(Commands.literal("sigil")
                        .then(Commands.literal("info").executes(context -> sigilInfo(context.getSource())))
                        .then(Commands.literal("activate").executes(context -> sigilActivate(context.getSource())))
                        .then(Commands.literal("deactivate").executes(context -> sigilDeactivate(context.getSource()))))
                .then(Commands.literal("ward")
                        .then(Commands.literal("list").executes(context -> wardList(context.getSource())))
                        .then(Commands.literal("check").executes(context -> wardCheck(context.getSource())))
                        .then(Commands.literal("active").executes(context -> wardActive(context.getSource())))
                        .then(Commands.literal("debug").executes(context -> wardDebug(context.getSource())))
                        .then(Commands.literal("target")
                                .then(Commands.argument("entity", EntityArgument.entity())
                                        .executes(context -> wardTarget(context.getSource(), EntityArgument.getEntity(context, "entity"), null))
                                        .then(Commands.argument("ward", StringArgumentType.word())
                                                .executes(context -> wardTarget(context.getSource(), EntityArgument.getEntity(context, "entity"), StringArgumentType.getString(context, "ward"))))))
                        .then(Commands.literal("deactivate").executes(context -> sigilDeactivate(context.getSource()))))
                .then(Commands.literal("grimoire")
                        .then(Commands.literal("info")
                                .executes(context -> grimoireInfo(context.getSource(), null))
                                .then(Commands.argument("ward", StringArgumentType.word())
                                        .executes(context -> grimoireInfo(context.getSource(), StringArgumentType.getString(context, "ward")))))
                        .then(Commands.literal("unlock")
                                .then(Commands.literal("soul")
                                        .then(Commands.argument("category", StringArgumentType.word())
                                                .executes(context -> grimoireUnlockSoul(context.getSource(), StringArgumentType.getString(context, "category")))))
                                .then(Commands.literal("ward")
                                        .then(Commands.argument("ward", StringArgumentType.word())
                                                .executes(context -> grimoireUnlockWard(context.getSource(), StringArgumentType.getString(context, "ward"))))))
                        .then(Commands.literal("reset").executes(context -> grimoireReset(context.getSource()))))
                .then(Commands.literal("grinder")
                        .then(Commands.literal("info").executes(context -> grinderInfo(context.getSource()))))
                .then(Commands.literal("sound")
                        .then(Commands.literal("arcane_crystal_place")
                                .executes(context -> playDebugSound(context.getSource(), SelariumSoundEvents.ARCANE_CRYSTAL_PLACE.get(), "block.arcane_crystal.place")))
                        .then(Commands.literal("arcane_crystal_break")
                                .executes(context -> playDebugSound(context.getSource(), SelariumSoundEvents.ARCANE_CRYSTAL_BREAK.get(), "block.arcane_crystal.break")))
                        .then(Commands.literal("arcane_crystal_shimmer")
                                .executes(context -> playDebugSound(context.getSource(), SelariumSoundEvents.ARCANE_CRYSTAL_SHIMMER.get(), "block.arcane_crystal.shimmer"))))
                .then(Commands.literal("tank")
                        .then(Commands.literal("info").executes(context -> tankInfo(context.getSource())))
                        .then(Commands.literal("set")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, MAX_DEBUG_MANA_AMOUNT))
                                        .executes(context -> tankSet(context.getSource(), IntegerArgumentType.getInteger(context, "amount")))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, MAX_DEBUG_MANA_AMOUNT))
                                        .executes(context -> tankAdd(context.getSource(), IntegerArgumentType.getInteger(context, "amount")))))
                        .then(Commands.literal("drain")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, MAX_DEBUG_MANA_AMOUNT))
                                        .executes(context -> tankDrain(context.getSource(), IntegerArgumentType.getInteger(context, "amount")))))));
    }

    private static int manaGet(CommandSourceStack source, ServerPlayer player) {
        return withMana(source, player, mana -> {
            source.sendSuccess(() -> Component.translatable("command.selarium.mana.get", player.getGameProfile().getName(), mana.getCurrentMana(), mana.getMaxMana()), false);
            return 1;
        });
    }

    private static int manaSet(CommandSourceStack source, ServerPlayer player, int amount) {
        return withMana(source, player, mana -> {
            mana.setCurrentMana(amount);
            source.sendSuccess(() -> Component.translatable("command.selarium.mana.set", player.getGameProfile().getName(), mana.getCurrentMana()), true);
            return 1;
        });
    }

    private static int manaAdd(CommandSourceStack source, ServerPlayer player, int amount) {
        return withMana(source, player, mana -> {
            int accepted = mana.addMana(amount);
            source.sendSuccess(() -> Component.translatable("command.selarium.mana.add", accepted, player.getGameProfile().getName(), mana.getCurrentMana(), mana.getMaxMana()), true);
            return accepted;
        });
    }

    private static int manaDrain(CommandSourceStack source, ServerPlayer player, int amount) {
        return withMana(source, player, mana -> {
            int drained = mana.drainMana(amount, true);
            source.sendSuccess(() -> Component.translatable("command.selarium.mana.drain", drained, player.getGameProfile().getName(), mana.getCurrentMana(), mana.getMaxMana()), true);
            return drained;
        });
    }

    private static int manaMaxGet(CommandSourceStack source, ServerPlayer player) {
        return withMana(source, player, mana -> {
            source.sendSuccess(() -> Component.translatable("command.selarium.mana.max.get", player.getGameProfile().getName(), mana.getMaxMana()), false);
            return 1;
        });
    }

    private static int manaMaxSet(CommandSourceStack source, ServerPlayer player, int amount) {
        return withMana(source, player, mana -> {
            mana.setMaxMana(amount);
            source.sendSuccess(() -> Component.translatable("command.selarium.mana.max.set", player.getGameProfile().getName(), mana.getMaxMana()), true);
            return 1;
        });
    }

    private static int manaTierGet(CommandSourceStack source, ServerPlayer player) {
        return withMana(source, player, mana -> {
            source.sendSuccess(() -> Component.translatable("command.selarium.mana.tier.get", player.getGameProfile().getName(), mana.getUnlockedManaTier()), false);
            return 1;
        });
    }

    private static int manaTierSet(CommandSourceStack source, ServerPlayer player, int tier) {
        return withMana(source, player, mana -> {
            mana.setUnlockedManaTier(tier);
            source.sendSuccess(() -> Component.translatable("command.selarium.mana.tier.set", player.getGameProfile().getName(), mana.getUnlockedManaTier()), true);
            return 1;
        });
    }

    private static int manaXpGet(CommandSourceStack source, ServerPlayer player) {
        return withMana(source, player, mana -> {
            source.sendSuccess(() -> Component.translatable("command.selarium.mana.xp.get", player.getGameProfile().getName(), String.format("%.2f", mana.getManaExperience())), false);
            return 1;
        });
    }

    private static int sigilInfo(CommandSourceStack source) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.translatable("command.selarium.player_only"));
            return 0;
        }

        Optional<ArcaneSigilBlockEntity> sigil = findTargetBlockEntity(player, ArcaneSigilBlockEntity.class);
        if (sigil.isEmpty()) {
            source.sendFailure(Component.translatable("command.selarium.sigil.not_found"));
            return 0;
        }

        ArcaneSigilBlockEntity blockEntity = sigil.get();
        String components = blockEntity.getComponents().entrySet().stream()
                .map(SelariumCommands::formatComponentEntry)
                .collect(Collectors.joining(", "));
        String detected = WardDefinitions.resolve(blockEntity)
                .map(definition -> definition.type().getSerializedName())
                .orElse(WardType.NONE.getSerializedName());
        String visualLayers = formatVisualLayers(blockEntity, detected);
        Optional<WardDefinition> currentDefinition = WardDefinitions.get(blockEntity.getWardType());
        if (currentDefinition.isEmpty()) {
            currentDefinition = WardDefinitions.resolve(blockEntity);
        }
        String wardDetails = currentDefinition
                .map(definition -> "range=" + definition.range() + " activationCost=" + definition.activationCostValue() + " upkeepCost=" + definition.upkeepCostValue() + " upkeepInterval=" + definition.tickInterval())
                .orElse("none");
        source.sendSuccess(() -> Component.translatable("command.selarium.sigil.info",
                blockEntity.getOwner() == null ? "none" : blockEntity.getOwner().toString(),
                blockEntity.getWardType().getSerializedName(),
                blockEntity.isActive(),
                blockEntity.getInternalManaBuffer(),
                blockEntity.getWardDurationRemainingTicks(),
                blockEntity.getManaGeneratedThisActivation(),
                blockEntity.getWardCooldownRemainingTicks(),
                detected,
                components.isBlank() ? "none" : components,
                wardDetails,
                visualLayers), false);
        return 1;
    }

    private static int sigilActivate(CommandSourceStack source) {
        return setTargetSigilActive(source, true);
    }

    private static int sigilDeactivate(CommandSourceStack source) {
        return setTargetSigilActive(source, false);
    }

    private static int tankInfo(CommandSourceStack source) {
        Optional<ManaTankBlockEntity> tank = findTargetTank(source);
        if (tank.isEmpty()) {
            return 0;
        }

        ManaTankBlockEntity blockEntity = tank.get();
        source.sendSuccess(() -> Component.translatable("command.selarium.tank.info", blockEntity.getStoredMana(), blockEntity.getManaCapacity()), false);
        return 1;
    }

    private static int tankSet(CommandSourceStack source, int amount) {
        Optional<ManaTankBlockEntity> tank = findTargetTank(source);
        if (tank.isEmpty()) {
            return 0;
        }

        ManaTankBlockEntity blockEntity = tank.get();
        blockEntity.setStoredMana(amount);
        source.sendSuccess(() -> Component.translatable("command.selarium.tank.set", blockEntity.getStoredMana(), blockEntity.getManaCapacity()), true);
        return 1;
    }

    private static int tankAdd(CommandSourceStack source, int amount) {
        Optional<ManaTankBlockEntity> tank = findTargetTank(source);
        if (tank.isEmpty()) {
            return 0;
        }

        ManaTankBlockEntity blockEntity = tank.get();
        int accepted = blockEntity.receiveMana(amount, false);
        source.sendSuccess(() -> Component.translatable("command.selarium.tank.add", accepted, blockEntity.getStoredMana(), blockEntity.getManaCapacity()), true);
        return accepted;
    }

    private static int tankDrain(CommandSourceStack source, int amount) {
        Optional<ManaTankBlockEntity> tank = findTargetTank(source);
        if (tank.isEmpty()) {
            return 0;
        }

        ManaTankBlockEntity blockEntity = tank.get();
        int extracted = blockEntity.extractMana(amount, false);
        source.sendSuccess(() -> Component.translatable("command.selarium.tank.drain", extracted, blockEntity.getStoredMana(), blockEntity.getManaCapacity()), true);
        return extracted;
    }

    private static Optional<ManaTankBlockEntity> findTargetTank(CommandSourceStack source) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.translatable("command.selarium.player_only"));
            return Optional.empty();
        }

        Optional<ManaTankBlockEntity> tank = findTargetBlockEntity(player, ManaTankBlockEntity.class);
        if (tank.isEmpty()) {
            source.sendFailure(Component.translatable("command.selarium.tank.not_found"));
        }
        return tank;
    }

    private static int grinderInfo(CommandSourceStack source) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.translatable("command.selarium.player_only"));
            return 0;
        }

        Optional<ArcaneGrinderBlockEntity> grinder = findTargetBlockEntity(player, ArcaneGrinderBlockEntity.class);
        if (grinder.isEmpty()) {
            source.sendFailure(Component.translatable("command.selarium.grinder.not_found"));
            return 0;
        }

        ArcaneGrinderBlockEntity blockEntity = grinder.get();
        String recipe = blockEntity.getDetectedRecipeId()
                .map(ResourceLocation::toString)
                .orElse("none");
        source.sendSuccess(() -> Component.translatable("command.selarium.grinder.info",
                formatStack(blockEntity.getItem(ArcaneGrinderBlockEntity.INPUT_SLOT)),
                formatStack(blockEntity.getItem(ArcaneGrinderBlockEntity.REAGENT_SLOT)),
                formatStack(blockEntity.getItem(ArcaneGrinderBlockEntity.OUTPUT_SLOT)),
                blockEntity.getProgress(),
                blockEntity.getMaxProgress(),
                recipe), false);
        return 1;
    }

    private static int playDebugSound(CommandSourceStack source, SoundEvent sound, String eventName) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.translatable("command.selarium.player_only"));
            return 0;
        }

        player.level().playSound(null, player.blockPosition(), sound, SoundSource.BLOCKS, 3.0F, 1.0F);
        source.sendSuccess(() -> Component.translatable("command.selarium.sound.played", eventName), false);
        return 1;
    }

    private static int grimoireInfo(CommandSourceStack source, String wardName) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.translatable("command.selarium.player_only"));
            return 0;
        }

        WardingRuleSet rules = WardingGrimoireData.readHeldOrSaved(player);
        if (wardName != null) {
            WardType type = WardType.bySerializedName(wardName);
            if (type == WardType.NONE) {
                source.sendFailure(Component.literal("Invalid Ward: " + wardName));
                return 0;
            }
            source.sendSuccess(() -> Component.translatable("command.selarium.grimoire.info",
                    WardingGrimoireData.summarize(rules) + " | " + WardingGrimoireData.summarizeWardRule(rules, type)), false);
            return 1;
        }
        source.sendSuccess(() -> Component.translatable("command.selarium.grimoire.info", WardingGrimoireData.summarize(rules)), false);
        return 1;
    }

    private static int grimoireReset(CommandSourceStack source) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.translatable("command.selarium.player_only"));
            return 0;
        }

        WardingGrimoireData.resetHeldOrPlayer(player);
        source.sendSuccess(() -> Component.translatable("command.selarium.grimoire.reset"), true);
        return 1;
    }

    private static int grimoireUnlockSoul(CommandSourceStack source, String categoryName) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.translatable("command.selarium.player_only"));
            return 0;
        }

        SoulEssenceCategory category = SoulEssenceCategory.bySerializedName(categoryName);
        if (category == SoulEssenceCategory.UNKNOWN || !WardingGrimoireData.unlockSoulForPlayer(player, category)) {
            source.sendFailure(Component.literal("Soul category was already known or invalid."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Unlocked soul category: " + category.getSerializedName()), true);
        return 1;
    }

    private static int grimoireUnlockWard(CommandSourceStack source, String wardName) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.translatable("command.selarium.player_only"));
            return 0;
        }

        WardType type = WardType.bySerializedName(wardName);
        if (type == WardType.NONE || !WardingGrimoireData.unlockWardForPlayer(player, type)) {
            source.sendFailure(Component.literal("Ward was already known or invalid."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Unlocked ward rule: " + type.getSerializedName()), true);
        return 1;
    }

    private static int wardList(CommandSourceStack source) {
        String wards = WardDefinitions.all().stream()
                .map(definition -> definition.type().getSerializedName() + "[" + definition.requirements().stream().map(requirement -> requirement.describe()).collect(Collectors.joining(" + ")) + "]")
                .collect(Collectors.joining("; "));
        source.sendSuccess(() -> Component.translatable("command.selarium.ward.list", wards), false);
        return 1;
    }

    private static int wardCheck(CommandSourceStack source) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.translatable("command.selarium.player_only"));
            return 0;
        }

        Optional<ArcaneSigilBlockEntity> sigil = findTargetBlockEntity(player, ArcaneSigilBlockEntity.class);
        if (sigil.isEmpty()) {
            source.sendFailure(Component.translatable("command.selarium.sigil.not_found"));
            return 0;
        }

        String detected = WardDefinitions.resolve(sigil.get())
                .map(definition -> definition.type().getSerializedName())
                .orElse(WardType.NONE.getSerializedName());
        source.sendSuccess(() -> Component.translatable("command.selarium.ward.check", detected), false);
        return 1;
    }

    private static int wardActive(CommandSourceStack source) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.translatable("command.selarium.player_only"));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        var wards = ActiveWardIndex.findAll(level, player.position(), 64);
        if (wards.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Active wards nearby: none"), false);
            return 0;
        }

        String details = wards.stream()
                .sorted(Comparator.comparing(ward -> ward.type().getSerializedName()))
                .map(ward -> ward.type().getSerializedName()
                        + " @ " + formatPos(ward.pos())
                        + " range=" + ward.sigil().getRange()
                        + " buffer=" + ward.sigil().getInternalManaBuffer())
                .collect(Collectors.joining("; "));
        source.sendSuccess(() -> Component.literal("Active wards nearby: " + details), false);
        return wards.size();
    }

    private static int wardDebug(CommandSourceStack source) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.translatable("command.selarium.player_only"));
            return 0;
        }

        Optional<ArcaneSigilBlockEntity> sigil = findTargetBlockEntity(player, ArcaneSigilBlockEntity.class);
        if (sigil.isEmpty()) {
            source.sendFailure(Component.translatable("command.selarium.sigil.not_found"));
            return 0;
        }

        ArcaneSigilBlockEntity blockEntity = sigil.get();
        String detected = WardDefinitions.resolve(blockEntity)
                .map(definition -> definition.type().getSerializedName())
                .orElse(WardType.NONE.getSerializedName());
        String config = formatWardConfig(blockEntity.getWardType());
        source.sendSuccess(() -> Component.literal("Ward debug: type=" + blockEntity.getWardType().getSerializedName()
                + " detected=" + detected
                + " active=" + blockEntity.isActive()
                + " pos=" + formatPos(blockEntity.getBlockPos())
                + " range=" + blockEntity.getRange()
                + " buffer=" + blockEntity.getInternalManaBuffer()
                + " tempBlocks=" + blockEntity.getTemporaryWardBlockCount()
                + " upkeepCost=" + blockEntity.getLastUpkeepCost()
                + " lastUpkeepTick=" + blockEntity.getLastUpkeepTick()
                + " lastUpkeepPaid=" + blockEntity.wasLastUpkeepPaid()
                + " lastUpkeep=" + blockEntity.getLastUpkeepDebug()
                + " lastTick=" + blockEntity.getLastWardTick()
                + " lastEntities=" + blockEntity.getLastWardEntitiesAffected()
                + " lastBlocks=" + blockEntity.getLastWardBlocksAffected()
                + " lastReason=" + blockEntity.getLastWardDebug()
                + " " + config), false);
        return 1;
    }

    private static int wardTarget(CommandSourceStack source, Entity entity, String wardName) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.translatable("command.selarium.player_only"));
            return 0;
        }
        Optional<ArcaneSigilBlockEntity> sigil = findTargetBlockEntity(player, ArcaneSigilBlockEntity.class);
        if (sigil.isEmpty()) {
            source.sendFailure(Component.translatable("command.selarium.sigil.not_found"));
            return 0;
        }
        if (!(entity instanceof LivingEntity living)) {
            source.sendFailure(Component.literal("Target is not a living entity."));
            return 0;
        }
        ArcaneSigilBlockEntity blockEntity = sigil.get();
        WardType debugWard = wardName == null ? blockEntity.getWardType() : WardType.bySerializedName(wardName);
        if (debugWard == WardType.NONE && wardName != null) {
            source.sendFailure(Component.literal("Invalid Ward: " + wardName));
            return 0;
        }
        boolean includePlayers = true;
        boolean includeBosses = true;
        try {
            SelariumCommonConfig.MvpWardConfig config = SelariumCommonConfig.mvpWard(debugWard);
            includePlayers = config.affectPlayers().get();
            includeBosses = config.affectBosses().get();
        } catch (IllegalArgumentException ignored) {
        }
        WardAccessService.TargetDecision decision = WardAccessService.explainTarget(blockEntity, living, debugWard, true, includePlayers, includeBosses);
        source.sendSuccess(() -> Component.literal("Ward target: entity=" + entity.getDisplayName().getString()
                + " " + decision.summary()), false);
        return 1;
    }

    private static int setTargetSigilActive(CommandSourceStack source, boolean active) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.translatable("command.selarium.player_only"));
            return 0;
        }

        Optional<ArcaneSigilBlockEntity> sigil = findTargetBlockEntity(player, ArcaneSigilBlockEntity.class);
        if (sigil.isEmpty()) {
            source.sendFailure(Component.translatable("command.selarium.sigil.not_found"));
            return 0;
        }

        ArcaneSigilBlockEntity blockEntity = sigil.get();
        if (active) {
            Optional<WardDefinition> resolvedDefinition = WardDefinitions.resolve(blockEntity);
            if (resolvedDefinition.isEmpty() || resolvedDefinition.get().type() == WardType.NONE) {
                source.sendFailure(Component.translatable("command.selarium.sigil.missing_components"));
                return 0;
            }
            WardActivationService.ActivationResult result = WardActivationService.activate(blockEntity, player.level(), blockEntity.getBlockPos(), player, resolvedDefinition.get());
            if (!result.success()) {
                source.sendFailure(Component.translatable(result.messageKey()));
                return 0;
            }
        } else {
            blockEntity.deactivateWard(0);
        }
        source.sendSuccess(() -> Component.translatable(active ? "command.selarium.sigil.activated" : "command.selarium.sigil.deactivated"), true);
        return 1;
    }

    private static <T extends BlockEntity> Optional<T> findTargetBlockEntity(ServerPlayer player, Class<T> type) {
        return TargetingUtil.getLookedAtBlock(player, 6.0D)
                .map(pos -> player.level().getBlockEntity(pos))
                .filter(type::isInstance)
                .map(type::cast);
    }

    private static int withMana(CommandSourceStack source, ServerPlayer player, ManaCommand action) {
        Optional<IPlayerMana> mana = ManaCapability.getMana(player).resolve();
        if (mana.isEmpty()) {
            source.sendFailure(Component.translatable("command.selarium.mana.missing"));
            return 0;
        }
        return action.run(mana.get());
    }

    private static String formatComponentEntry(Map.Entry<DustDefinition, Integer> entry) {
        return entry.getKey().getSerializedName() + " x" + entry.getValue();
    }

    private static String formatVisualLayers(ArcaneSigilBlockEntity sigil, String detectedWard) {
        ArrayList<String> layers = new ArrayList<>();
        layers.add("base");

        Map<DustType, Integer> counts = collectVisualDustCounts(sigil);
        int maxLayers = Math.max(0, SelariumCommonConfig.SIGIL_MAX_VISIBLE_COMPONENT_LAYERS.get());
        int[] visible = {0};
        counts.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> visualPriority(entry.getKey())))
                .forEach(entry -> {
                    if (visible[0] < maxLayers) {
                        String suffix = entry.getValue() > 1 ? " x" + entry.getValue() : "";
                        String extra = visible[0] >= 3 ? " extra" : "";
                        layers.add("component:" + entry.getKey().getSerializedName() + suffix + extra);
                    }
                    visible[0]++;
                });

        int hidden = Math.max(0, counts.size() - maxLayers);
        if (!WardType.NONE.getSerializedName().equals(detectedWard)) {
            layers.add("ward:" + detectedWard);
        } else if (!counts.isEmpty()) {
            layers.add("ward:incomplete");
        }
        if (hidden > 0) {
            layers.add("hiddenComponents:" + hidden);
        }
        layers.add("active:" + sigil.isActive());
        return String.join(", ", layers);
    }

    private static Map<DustType, Integer> collectVisualDustCounts(ArcaneSigilBlockEntity sigil) {
        Map<DustType, Integer> counts = new EnumMap<>(DustType.class);
        for (Map.Entry<DustDefinition, Integer> entry : sigil.getComponents().entrySet()) {
            int count = Math.max(0, entry.getValue());
            if (count > 0) {
                counts.merge(entry.getKey().type(), count, Integer::sum);
            }
        }

        int arcaneCount = counts.getOrDefault(DustType.ARCANE, 0);
        if (arcaneCount <= 1) {
            counts.remove(DustType.ARCANE);
        } else {
            counts.put(DustType.ARCANE, arcaneCount - 1);
        }
        counts.entrySet().removeIf(entry -> entry.getValue() <= 0);
        return counts;
    }

    private static int visualPriority(DustType type) {
        return switch (type) {
            case FOCUS -> 0;
            case AEGIS -> 1;
            case VITAL -> 2;
            case ECHO -> 3;
            case BINDING -> 4;
            case DENSITY -> 5;
            case WARP -> 6;
            case VEIL -> 7;
            case CHRONO -> 8;
            case ARCANE -> 9;
        };
    }

    private static String formatStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return "empty";
        }
        return stack.getCount() + "x " + BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    private static String formatPos(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static String formatWardConfig(WardType type) {
        if (type == WardType.NONE) {
            return "config=none";
        }
        try {
            SelariumCommonConfig.MvpWardConfig config = SelariumCommonConfig.mvpWard(type);
            return "config(enabled=" + config.enabled().get()
                    + ", tickInterval=" + config.tickInterval().get()
                    + ", manaCost=" + config.manaCost().get()
                    + ", maxEntities=" + config.maxEntitiesPerCycle().get()
                    + ", maxBlocks=" + config.maxBlocksPerCycle().get()
                    + ")";
        } catch (IllegalArgumentException ignored) {
            return "config=legacyWard";
        }
    }

    @FunctionalInterface
    private interface ManaCommand {
        int run(IPlayerMana mana);
    }
}
