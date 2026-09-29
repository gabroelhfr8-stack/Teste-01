package com.seleris.selarium.client.gui;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.grimoire.SoulEssenceCategory;
import com.seleris.selarium.grimoire.WardingRuleSet;
import com.seleris.selarium.grimoire.WardingWardRule;
import com.seleris.selarium.network.SelariumNetwork;
import com.seleris.selarium.network.UpdateWardingGrimoirePacket;
import com.seleris.selarium.ward.WardDefinitions;
import com.seleris.selarium.ward.WardType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;

public class WardingGrimoireScreen extends Screen {
    private static final int PANEL_WIDTH = 392;
    private static final int PANEL_HEIGHT = 238;
    private static final int LIST_ROWS = 4;
    private static final int COLOR_FRAME = 0xEE201634;
    private static final int COLOR_PAPER = 0xFFF6F0FF;
    private static final int COLOR_PAPER_ALT = 0xFFE9DDF8;
    private static final int COLOR_INK = WorkshopUi.INK;
    private static final int COLOR_MUTED = WorkshopUi.MUTED;
    private static final int COLOR_GOOD = WorkshopUi.GOOD;
    private static final int COLOR_BAD = WorkshopUi.BAD;
    private static final int COLOR_ACCENT = 0xFF8B6244;

    private WardingRuleSet rules;
    private Tab activeTab = Tab.OVERVIEW;
    private EditBox playerEntry;
    private EditBox entityTypeEntry;
    private int playerAllowPage;
    private int playerDenyPage;
    private int typeAllowPage;
    private int typeDenyPage;
    private int knownTypePage;
    private int unlockedWardPage;
    private WardType selectedWard = WardType.NONE;
    private boolean wardRulesListMode;
    private Component feedback = Component.empty();
    private int feedbackColor = COLOR_MUTED;

    public WardingGrimoireScreen(WardingRuleSet rules) {
        super(Component.translatable("screen.selarium.warding_grimoire"));
        this.rules = rules.copy();
    }

    @Override
    protected void init() {
        rebuildGrimoireWidgets();
    }

    private void rebuildGrimoireWidgets() {
        clearWidgets();
        playerEntry = null;
        entityTypeEntry = null;
        clampPages();

        int left = panelLeft();
        int top = panelTop();
        int panelWidth = panelWidth();
        int contentLeft = left + 16;
        int contentTop = top + 52;
        addTabs(left, top, panelWidth);

        switch (activeTab) {
            case CATEGORIES -> buildCategoriesTab(contentLeft, contentTop, panelWidth - 32);
            case SOULS -> buildSoulKnowledgeTab(contentLeft, contentTop, panelWidth - 32);
            case WARD_RULES -> buildWardRulesTab(contentLeft, contentTop, panelWidth - 32);
            case PLAYERS -> buildPlayersTab(contentLeft, contentTop, panelWidth - 32);
            case ENTITY_TYPES -> buildEntityTypesTab(contentLeft, contentTop, panelWidth - 32);
            case ADVANCED -> buildAdvancedTab(contentLeft, contentTop, panelWidth - 32);
            default -> {
            }
        }

        addRenderableWidget(WorkshopButton.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(left + panelWidth - 68, top + 8, 52, 14)
                .build());
    }

    private void addTabs(int left, int top, int panelWidth) {
        Tab[] tabs = Tab.values();
        int[] widths = new int[tabs.length];
        int total = 0;
        for (int i = 0; i < tabs.length; i++) {
            widths[i] = font.width(tabs[i].label()) + 12;
            total += widths[i];
        }
        // share the leftover room (or the shortfall) so the row always spans the panel and no label is clipped
        int share = Math.floorDiv(panelWidth - 28 - total, tabs.length);
        int x = left + 14;
        for (int i = 0; i < tabs.length; i++) {
            Tab tab = tabs[i];
            int width = Math.max(28, widths[i] + share);
            Button button = WorkshopButton.builder(tab.label(), clicked -> {
                        activeTab = tab;
                        feedback = Component.empty();
                        rebuildGrimoireWidgets();
                    })
                    .bounds(x, top + 28, width - 2, 18)
                    .build();
            if (button instanceof WorkshopButton workshopButton) workshopButton.selected(activeTab == tab);
            addRenderableWidget(button);
            x += width;
        }
    }

    private void buildCategoriesTab(int x, int y, int width) {
        int leftButton = x + width / 2 - 54;
        int rightButton = x + width - 48;
        addToggleRow(leftButton, y + 17, "screen.selarium.warding_grimoire.toggle.hostiles", rules::affectHostileMobs, value -> rules.affectHostileMobs(value), SoulEssenceCategory.MONSTROUS);
        addToggleRow(leftButton, y + 33, "screen.selarium.warding_grimoire.toggle.passives", rules::affectPassiveMobs, value -> rules.affectPassiveMobs(value), SoulEssenceCategory.BESTIAL);
        addToggleRow(leftButton, y + 49, "screen.selarium.warding_grimoire.toggle.neutral", rules::affectNeutralMobs, value -> rules.affectNeutralMobs(value), SoulEssenceCategory.BESTIAL);
        addToggleRow(leftButton, y + 65, "screen.selarium.warding_grimoire.toggle.villagers", rules::affectVillagers, value -> rules.affectVillagers(value), SoulEssenceCategory.ANTHROPIC);
        addToggleRow(leftButton, y + 81, "screen.selarium.warding_grimoire.toggle.bosses", rules::affectBosses, value -> rules.affectBosses(value), SoulEssenceCategory.MONSTROUS);
        addToggleRow(leftButton, y + 116, "screen.selarium.warding_grimoire.toggle.players", rules::affectPlayers, value -> rules.affectPlayers(value), SoulEssenceCategory.ANTHROPIC);
        addToggleRow(leftButton, y + 132, "screen.selarium.warding_grimoire.toggle.owner_buffs", rules::affectOwner, value -> rules.affectOwner(value), null);
        addToggleRow(leftButton, y + 148, "screen.selarium.warding_grimoire.toggle.owner_harm", rules::affectOwnerWithNegativeWards, value -> rules.affectOwnerWithNegativeWards(value), SoulEssenceCategory.ANTHROPIC);
        addToggleRow(rightButton, y + 17, "screen.selarium.warding_grimoire.toggle.pets", rules::affectPets, value -> rules.affectPets(value), SoulEssenceCategory.BESTIAL);
        addToggleRow(rightButton, y + 33, "screen.selarium.warding_grimoire.toggle.ignore_pets", rules::ignoreTamedPets, value -> rules.ignoreTamedPets(value), SoulEssenceCategory.BESTIAL);
        addToggleRow(rightButton, y + 49, "screen.selarium.warding_grimoire.toggle.ignore_named", rules::ignoreNamedEntities, value -> rules.ignoreNamedEntities(value), null);
    }

    private int addToggleRow(int buttonX, int y, String key, BooleanSupplier getter, BooleanConsumer setter, SoulEssenceCategory requiredSoul) {
        Button button = WorkshopButton.builder(toggleLabel(getter.getAsBoolean()), clicked -> {
                    if (!canEdit()) {
                        setFeedback("screen.selarium.warding_grimoire.feedback.read_only", COLOR_BAD);
                        return;
                    }
                    if (!canEditSoul(requiredSoul)) {
                        setFeedback("screen.selarium.warding_grimoire.feedback.soul_locked", COLOR_BAD);
                        return;
                    }
                    setter.accept(!getter.getAsBoolean());
                    sendUpdate(Component.translatable("screen.selarium.warding_grimoire.feedback.saved"), COLOR_GOOD);
                    rebuildGrimoireWidgets();
                })
                .bounds(buttonX, y - 3, 44, 16)
                .build();
        button.active = canEdit() && canEditSoul(requiredSoul);
        addRenderableWidget(button);
        return y + 15;
    }

    private void buildSoulKnowledgeTab(int x, int y, int width) {
        addPageButtons(x, y + 137, width, knownTypePage, rules.knownEntityTypes().size(), () -> knownTypePage = Math.max(0, knownTypePage - 1), () -> knownTypePage++);
    }

    private void buildWardRulesTab(int x, int y, int width) {
        List<WardType> unlocked = unlockedWards();
        if (selectedWard == WardType.NONE && !unlocked.isEmpty()) {
            selectedWard = unlocked.get(0);
        }
        if (selectedWard != WardType.NONE && !unlocked.contains(selectedWard)) {
            selectedWard = unlocked.isEmpty() ? WardType.NONE : unlocked.get(0);
        }
        int listX = x;
        int listY = y + 18;
        int listWidth = 112;
        int start = unlockedWardPage * LIST_ROWS;
        int end = Math.min(unlocked.size(), start + LIST_ROWS);
        for (int index = start; index < end; index++) {
            WardType type = unlocked.get(index);
            Button button = WorkshopButton.builder(Component.translatable(type.getTranslationKey()), clicked -> {
                        selectedWard = type;
                        wardRulesListMode = false;
                        rebuildGrimoireWidgets();
                    })
                    .bounds(listX, listY + (index - start) * 20, listWidth, 18)
                    .build();
            button.active = selectedWard != type;
            addRenderableWidget(button);
        }
        addPageButtons(listX, listY + 84, listWidth, unlockedWardPage, unlocked.size(), () -> unlockedWardPage = Math.max(0, unlockedWardPage - 1), () -> unlockedWardPage++);

        if (selectedWard != WardType.NONE && rules.isWardUnlocked(selectedWard)) {
            WardingWardRule rule = rules.wardRule(selectedWard);
            int modeX = x + 126;
            addModeButton(modeX, y + 18, 54, Component.translatable("screen.selarium.warding_grimoire.ward.mode.toggles"), false);
            addModeButton(modeX + 58, y + 18, 54, Component.translatable("screen.selarium.warding_grimoire.ward.mode.lists"), true);
            if (wardRulesListMode) {
                buildWardRuleLists(modeX, y + 42, width - 126, rule);
            } else {
                int buttonX = x + width - 48;
                int rowY = y + 48;
                rowY = addWardToggleRow(buttonX, rowY, "screen.selarium.warding_grimoire.ward.use_global", rule::useGlobalRules, rule::useGlobalRules, null);
                rowY += 6;
                rowY = addWardToggleRow(buttonX, rowY, "screen.selarium.warding_grimoire.toggle.hostiles", rule::affectHostiles, rule::affectHostiles, SoulEssenceCategory.MONSTROUS);
                rowY = addWardToggleRow(buttonX, rowY, "screen.selarium.warding_grimoire.toggle.passives", rule::affectPassives, rule::affectPassives, SoulEssenceCategory.BESTIAL);
                rowY = addWardToggleRow(buttonX, rowY, "screen.selarium.warding_grimoire.toggle.neutral", rule::affectNeutral, rule::affectNeutral, SoulEssenceCategory.BESTIAL);
                rowY = addWardToggleRow(buttonX, rowY, "screen.selarium.warding_grimoire.toggle.villagers", rule::affectVillagers, rule::affectVillagers, SoulEssenceCategory.ANTHROPIC);
                rowY = addWardToggleRow(buttonX, rowY, "screen.selarium.warding_grimoire.toggle.players", rule::affectPlayers, rule::affectPlayers, SoulEssenceCategory.ANTHROPIC);
                rowY = addWardToggleRow(buttonX, rowY, "screen.selarium.warding_grimoire.toggle.pets", rule::affectPets, rule::affectPets, SoulEssenceCategory.BESTIAL);
                rowY = addWardToggleRow(buttonX, rowY, "screen.selarium.warding_grimoire.toggle.bosses", rule::affectBosses, rule::affectBosses, SoulEssenceCategory.MONSTROUS);
                rowY = addWardToggleRow(buttonX, rowY, "screen.selarium.warding_grimoire.toggle.owner_target", rule::affectOwner, rule::affectOwner, null);
                addWardToggleRow(buttonX, rowY, "screen.selarium.warding_grimoire.toggle.allies", rule::affectAllies, rule::affectAllies, SoulEssenceCategory.ANTHROPIC);
            }
        }
    }

    private void addModeButton(int x, int y, int width, Component label, boolean listMode) {
        Button button = WorkshopButton.builder(label, clicked -> {
                    wardRulesListMode = listMode;
                    rebuildGrimoireWidgets();
                })
                .bounds(x, y, width, 17)
                .build();
        if (button instanceof WorkshopButton workshopButton) workshopButton.selected(wardRulesListMode == listMode);
        addRenderableWidget(button);
    }

    private void buildWardRuleLists(int x, int y, int width, WardingWardRule rule) {
        playerEntry = new EditBox(font, x, y + 12, width - 126, 18, Component.translatable("screen.selarium.warding_grimoire.player_entry"));
        playerEntry.setMaxLength(WardingRuleSet.MAX_IDENTIFIER_LENGTH);
        playerEntry.active = canEdit() && canEditSoul(SoulEssenceCategory.ANTHROPIC);
        addRenderableWidget(playerEntry);
        addSmallButton(x + width - 120, y + 12, 36, Component.translatable("screen.selarium.warding_grimoire.allow_short"), () -> addWardPlayer(rule, true));
        addSmallButton(x + width - 82, y + 12, 36, Component.translatable("screen.selarium.warding_grimoire.deny_short"), () -> addWardPlayer(rule, false));
        addSmallButton(x + width - 44, y + 12, 40, Component.translatable("screen.selarium.warding_grimoire.remove_short"), () -> removeWardPlayer(rule));

        entityTypeEntry = new EditBox(font, x, y + 46, width - 126, 18, Component.translatable("screen.selarium.warding_grimoire.entity_type_entry"));
        entityTypeEntry.setMaxLength(WardingRuleSet.MAX_IDENTIFIER_LENGTH);
        entityTypeEntry.active = canEdit();
        addRenderableWidget(entityTypeEntry);
        addSmallButton(x + width - 120, y + 46, 36, Component.translatable("screen.selarium.warding_grimoire.allow_short"), () -> addWardEntityType(rule, true));
        addSmallButton(x + width - 82, y + 46, 36, Component.translatable("screen.selarium.warding_grimoire.deny_short"), () -> addWardEntityType(rule, false));
        addSmallButton(x + width - 44, y + 46, 40, Component.translatable("screen.selarium.warding_grimoire.remove_short"), () -> removeWardEntityType(rule));
    }

    private int addWardToggleRow(int buttonX, int y, String key, BooleanSupplier getter, BooleanConsumer setter, SoulEssenceCategory requiredSoul) {
        Button button = WorkshopButton.builder(toggleLabel(getter.getAsBoolean()), clicked -> {
                    if (!canEdit()) {
                        setFeedback("screen.selarium.warding_grimoire.feedback.read_only", COLOR_BAD);
                        return;
                    }
                    if (!canEditSoul(requiredSoul)) {
                        setFeedback("screen.selarium.warding_grimoire.feedback.soul_locked", COLOR_BAD);
                        return;
                    }
                    setter.accept(!getter.getAsBoolean());
                    sendUpdate(Component.translatable("screen.selarium.warding_grimoire.feedback.saved"), COLOR_GOOD);
                    rebuildGrimoireWidgets();
                })
                .bounds(buttonX, y - 2, 44, 12)
                .build();
        button.active = canEdit() && canEditSoul(requiredSoul);
        addRenderableWidget(button);
        return y + 12;
    }

    private void buildPlayersTab(int x, int y, int width) {
        playerEntry = new EditBox(font, x, y + 14, width - 126, 18, Component.translatable("screen.selarium.warding_grimoire.player_entry"));
        playerEntry.setMaxLength(WardingRuleSet.MAX_IDENTIFIER_LENGTH);
        playerEntry.active = canEdit();
        addRenderableWidget(playerEntry);
        addSmallButton(x + width - 120, y + 14, 56, Component.translatable("screen.selarium.warding_grimoire.allow"), () -> addPlayer(true));
        addSmallButton(x + width - 60, y + 14, 56, Component.translatable("screen.selarium.warding_grimoire.deny"), () -> addPlayer(false));
        int listY = y + 54;
        buildListButtons(new ArrayList<>(rules.allowPlayers()), playerAllowPage, x, listY, (width / 2) - 8, true, value -> removePlayer(value));
        buildListButtons(new ArrayList<>(rules.denyPlayers()), playerDenyPage, x + (width / 2) + 8, listY, (width / 2) - 8, true, value -> removePlayer(value));
        addPageButtons(x, listY + 82, (width / 2) - 8, playerAllowPage, rules.allowPlayers().size(), () -> playerAllowPage = Math.max(0, playerAllowPage - 1), () -> playerAllowPage++);
        addPageButtons(x + (width / 2) + 8, listY + 82, (width / 2) - 8, playerDenyPage, rules.denyPlayers().size(), () -> playerDenyPage = Math.max(0, playerDenyPage - 1), () -> playerDenyPage++);
    }

    private void buildEntityTypesTab(int x, int y, int width) {
        entityTypeEntry = new EditBox(font, x, y + 14, width - 126, 18, Component.translatable("screen.selarium.warding_grimoire.entity_type_entry"));
        entityTypeEntry.setMaxLength(WardingRuleSet.MAX_IDENTIFIER_LENGTH);
        entityTypeEntry.active = canEdit();
        addRenderableWidget(entityTypeEntry);
        addSmallButton(x + width - 120, y + 14, 56, Component.translatable("screen.selarium.warding_grimoire.allow"), () -> addEntityType(true));
        addSmallButton(x + width - 60, y + 14, 56, Component.translatable("screen.selarium.warding_grimoire.deny"), () -> addEntityType(false));
        int listY = y + 54;
        buildListButtons(new ArrayList<>(rules.allowEntityTypeIds()), typeAllowPage, x, listY, (width / 2) - 8, false, value -> removeEntityType(value));
        buildListButtons(new ArrayList<>(rules.denyEntityTypeIds()), typeDenyPage, x + (width / 2) + 8, listY, (width / 2) - 8, false, value -> removeEntityType(value));
        addPageButtons(x, listY + 82, (width / 2) - 8, typeAllowPage, rules.allowEntityTypeIds().size(), () -> typeAllowPage = Math.max(0, typeAllowPage - 1), () -> typeAllowPage++);
        addPageButtons(x + (width / 2) + 8, listY + 82, (width / 2) - 8, typeDenyPage, rules.denyEntityTypeIds().size(), () -> typeDenyPage = Math.max(0, typeDenyPage - 1), () -> typeDenyPage++);
    }

    private void buildAdvancedTab(int x, int y, int width) {
        addWideButton(x, y + 44, 124, Component.translatable("screen.selarium.warding_grimoire.clear_players"), () -> {
            rules.clearPlayerLists();
            playerAllowPage = 0;
            playerDenyPage = 0;
            sendUpdate(Component.translatable("screen.selarium.warding_grimoire.feedback.players_cleared"), COLOR_GOOD);
            rebuildGrimoireWidgets();
        });
        addWideButton(x + 134, y + 44, 136, Component.translatable("screen.selarium.warding_grimoire.clear_entity_types"), () -> {
            rules.clearEntityTypeLists();
            typeAllowPage = 0;
            typeDenyPage = 0;
            sendUpdate(Component.translatable("screen.selarium.warding_grimoire.feedback.types_cleared"), COLOR_GOOD);
            rebuildGrimoireWidgets();
        });
    }

    private void addSmallButton(int x, int y, int width, Component label, Runnable action) {
        Button button = WorkshopButton.builder(label, clicked -> {
                    if (!canEdit()) {
                        setFeedback("screen.selarium.warding_grimoire.feedback.read_only", COLOR_BAD);
                        return;
                    }
                    action.run();
                })
                .bounds(x, y, width, 18)
                .build();
        button.active = canEdit();
        addRenderableWidget(button);
    }

    private void addWideButton(int x, int y, int width, Component label, Runnable action) {
        Button button = WorkshopButton.builder(label, clicked -> {
                    if (!canEdit()) {
                        setFeedback("screen.selarium.warding_grimoire.feedback.read_only", COLOR_BAD);
                        return;
                    }
                    action.run();
                })
                .bounds(x, y, width, 18)
                .build();
        button.active = canEdit();
        addRenderableWidget(button);
    }

    private void buildListButtons(List<String> values, int page, int x, int y, int width, boolean playerList, ListAction action) {
        int start = Math.max(0, page) * LIST_ROWS;
        int end = Math.min(values.size(), start + LIST_ROWS);
        for (int index = start; index < end; index++) {
            String value = values.get(index);
            int rowY = y + 14 + (index - start) * 16;
            Button remove = WorkshopButton.builder(Component.literal("x"), clicked -> {
                        if (!canEdit()) {
                            setFeedback("screen.selarium.warding_grimoire.feedback.read_only", COLOR_BAD);
                            return;
                        }
                        action.run(value);
                        sendUpdate(Component.translatable(playerList
                                ? "screen.selarium.warding_grimoire.feedback.player_removed"
                                : "screen.selarium.warding_grimoire.feedback.type_removed"), COLOR_GOOD);
                        rebuildGrimoireWidgets();
                    })
                    .bounds(x + width - 18, rowY - 3, 16, 14)
                    .build();
            remove.active = canEdit();
            addRenderableWidget(remove);
        }
    }

    private void addPageButtons(int x, int y, int width, int page, int total, Runnable previous, Runnable next) {
        if (total <= LIST_ROWS) {
            return;
        }
        int maxPage = Math.max(0, (int) Math.ceil(total / (double) LIST_ROWS) - 1);
        Button prev = WorkshopButton.builder(Component.literal("<"), clicked -> {
                    previous.run();
                    rebuildGrimoireWidgets();
                })
                .bounds(x + width - 40, y, 18, 15)
                .build();
        Button nextButton = WorkshopButton.builder(Component.literal(">"), clicked -> {
                    next.run();
                    rebuildGrimoireWidgets();
                })
                .bounds(x + width - 20, y, 18, 15)
                .build();
        prev.active = page > 0;
        nextButton.active = page < maxPage;
        addRenderableWidget(prev);
        addRenderableWidget(nextButton);
    }

    private void clampPages() {
        playerAllowPage = clampPage(playerAllowPage, rules.allowPlayers().size());
        playerDenyPage = clampPage(playerDenyPage, rules.denyPlayers().size());
        typeAllowPage = clampPage(typeAllowPage, rules.allowEntityTypeIds().size());
        typeDenyPage = clampPage(typeDenyPage, rules.denyEntityTypeIds().size());
        knownTypePage = clampPage(knownTypePage, rules.knownEntityTypes().size());
        unlockedWardPage = clampPage(unlockedWardPage, rules.unlockedWardTypes().size());
    }

    private int clampPage(int page, int total) {
        int maxPage = Math.max(0, (int) Math.ceil(total / (double) LIST_ROWS) - 1);
        return Math.max(0, Math.min(page, maxPage));
    }

    private void addPlayer(boolean allow) {
        String value = playerEntry == null ? "" : WardingRuleSet.sanitizeIdentifier(playerEntry.getValue());
        if (value.isBlank()) {
            setFeedback("screen.selarium.warding_grimoire.feedback.invalid_player", COLOR_BAD);
            return;
        }
        if (allow) {
            rules.addAllowedPlayer(value);
            setFeedback("screen.selarium.warding_grimoire.feedback.player_allowed", COLOR_GOOD);
        } else {
            rules.addDeniedPlayer(value);
            setFeedback("screen.selarium.warding_grimoire.feedback.player_denied", COLOR_GOOD);
        }
        playerEntry.setValue("");
        sendUpdate(null, COLOR_GOOD);
        rebuildGrimoireWidgets();
    }

    private void removePlayer(String value) {
        rules.removePlayer(value);
    }

    private void addEntityType(boolean allow) {
        String value = entityTypeEntry == null ? "" : WardingRuleSet.sanitizeIdentifier(entityTypeEntry.getValue());
        if (!isValidEntityTypeId(value)) {
            setFeedback("screen.selarium.warding_grimoire.feedback.invalid_entity_type", COLOR_BAD);
            return;
        }
        if (allow) {
            rules.addAllowedEntityType(value);
            setFeedback("screen.selarium.warding_grimoire.feedback.type_allowed", COLOR_GOOD);
        } else {
            rules.addDeniedEntityType(value);
            setFeedback("screen.selarium.warding_grimoire.feedback.type_denied", COLOR_GOOD);
        }
        entityTypeEntry.setValue("");
        sendUpdate(null, COLOR_GOOD);
        rebuildGrimoireWidgets();
    }

    private void removeEntityType(String value) {
        rules.removeEntityType(value);
    }

    private void addWardPlayer(WardingWardRule rule, boolean allow) {
        if (!canEditSoul(SoulEssenceCategory.ANTHROPIC)) {
            setFeedback("screen.selarium.warding_grimoire.feedback.soul_locked", COLOR_BAD);
            return;
        }
        String value = playerEntry == null ? "" : WardingRuleSet.sanitizeIdentifier(playerEntry.getValue());
        if (value.isBlank()) {
            setFeedback("screen.selarium.warding_grimoire.feedback.invalid_player", COLOR_BAD);
            return;
        }
        if (allow) {
            rule.addAllowedPlayer(value);
            setFeedback("screen.selarium.warding_grimoire.feedback.player_allowed", COLOR_GOOD);
        } else {
            rule.addDeniedPlayer(value);
            setFeedback("screen.selarium.warding_grimoire.feedback.player_denied", COLOR_GOOD);
        }
        playerEntry.setValue("");
        sendUpdate(null, COLOR_GOOD);
        rebuildGrimoireWidgets();
    }

    private void removeWardPlayer(WardingWardRule rule) {
        if (!canEditSoul(SoulEssenceCategory.ANTHROPIC)) {
            setFeedback("screen.selarium.warding_grimoire.feedback.soul_locked", COLOR_BAD);
            return;
        }
        String value = playerEntry == null ? "" : WardingRuleSet.sanitizeIdentifier(playerEntry.getValue());
        if (value.isBlank()) {
            setFeedback("screen.selarium.warding_grimoire.feedback.invalid_player", COLOR_BAD);
            return;
        }
        rule.removePlayer(value);
        playerEntry.setValue("");
        sendUpdate(Component.translatable("screen.selarium.warding_grimoire.feedback.player_removed"), COLOR_GOOD);
        rebuildGrimoireWidgets();
    }

    private void addWardEntityType(WardingWardRule rule, boolean allow) {
        String value = entityTypeEntry == null ? "" : WardingRuleSet.sanitizeIdentifier(entityTypeEntry.getValue());
        if (!isValidEntityTypeId(value)) {
            setFeedback("screen.selarium.warding_grimoire.feedback.invalid_entity_type", COLOR_BAD);
            return;
        }
        if (allow) {
            rule.addAllowedEntityType(value);
            setFeedback("screen.selarium.warding_grimoire.feedback.type_allowed", COLOR_GOOD);
        } else {
            rule.addDeniedEntityType(value);
            setFeedback("screen.selarium.warding_grimoire.feedback.type_denied", COLOR_GOOD);
        }
        entityTypeEntry.setValue("");
        sendUpdate(null, COLOR_GOOD);
        rebuildGrimoireWidgets();
    }

    private void removeWardEntityType(WardingWardRule rule) {
        String value = entityTypeEntry == null ? "" : WardingRuleSet.sanitizeIdentifier(entityTypeEntry.getValue());
        if (!isValidEntityTypeId(value)) {
            setFeedback("screen.selarium.warding_grimoire.feedback.invalid_entity_type", COLOR_BAD);
            return;
        }
        rule.removeEntityType(value);
        entityTypeEntry.setValue("");
        sendUpdate(Component.translatable("screen.selarium.warding_grimoire.feedback.type_removed"), COLOR_GOOD);
        rebuildGrimoireWidgets();
    }

    private boolean isValidEntityTypeId(String value) {
        return WardingRuleSet.isValidResourceId(value) && ResourceLocation.tryParse(value) != null;
    }

    private Component toggleLabel(boolean value) {
        return Component.translatable(value ? "screen.selarium.warding_grimoire.on" : "screen.selarium.warding_grimoire.off");
    }

    private void sendUpdate(Component success, int color) {
        if (!canEdit()) {
            setFeedback("screen.selarium.warding_grimoire.feedback.read_only", COLOR_BAD);
            return;
        }
        rules.sanitize();
        SelariumNetwork.CHANNEL.sendToServer(new UpdateWardingGrimoirePacket(rules.copy()));
        if (success != null) {
            feedback = success;
            feedbackColor = color;
        }
    }

    private void setFeedback(String key, int color) {
        feedback = Component.translatable(key);
        feedbackColor = color;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        float scale = uiScale();
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0F);
        int left = panelLeft();
        int top = panelTop();
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        WorkshopUi.frame(graphics, left, top, panelWidth, panelHeight, true);
        WorkshopUi.parchment(graphics, left + 8, top + 52, panelWidth - 16, panelHeight - 61);
        graphics.drawCenteredString(font, title, virtualWidth() / 2, top + 10, 0xFFF5EADD);

        switch (activeTab) {
            case OVERVIEW -> renderOverview(graphics, left + 16, top + 56, panelWidth - 32);
            case CATEGORIES -> renderCategories(graphics, left + 16, top + 56, panelWidth - 32);
            case SOULS -> renderSoulKnowledge(graphics, left + 16, top + 56, panelWidth - 32);
            case WARD_RULES -> renderWardRules(graphics, left + 16, top + 56, panelWidth - 32);
            case PLAYERS -> renderPlayers(graphics, left + 16, top + 56, panelWidth - 32);
            case ENTITY_TYPES -> renderEntityTypes(graphics, left + 16, top + 56, panelWidth - 32);
            case ADVANCED -> renderAdvanced(graphics, left + 16, top + 56, panelWidth - 32);
        }

        if (!feedback.getString().isBlank()) {
            WorkshopUi.smallText(graphics, font, feedback, left + 80, top + panelHeight - 19,
                    panelWidth - 166, feedbackColor);
        }
        super.render(graphics, (int) (mouseX / scale), (int) (mouseY / scale), partialTick);
        graphics.pose().popPose();
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX / uiScale(), mouseY / uiScale(), button);
    }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX / uiScale(), mouseY / uiScale(), button);
    }

    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        float scale = uiScale();
        return super.mouseDragged(mouseX / scale, mouseY / scale, button, dragX / scale, dragY / scale);
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return super.mouseScrolled(mouseX / uiScale(), mouseY / uiScale(), amount);
    }

    private void renderOverview(GuiGraphics graphics, int x, int y, int width) {
        graphics.drawString(font, Component.translatable("screen.selarium.warding_grimoire.overview.title"), x, y, COLOR_ACCENT, false);
        graphics.drawString(font, Component.translatable("screen.selarium.warding_grimoire.owner", ownerDisplay()), x, y + 20, COLOR_INK, false);
        graphics.drawString(font, Component.translatable(canEdit()
                ? "screen.selarium.warding_grimoire.status.editable"
                : "screen.selarium.warding_grimoire.status.read_only"), x, y + 35, canEdit() ? COLOR_GOOD : COLOR_BAD, false);
        graphics.drawWordWrap(font, Component.translatable("screen.selarium.warding_grimoire.overview.text"), x, y + 58, width, COLOR_MUTED);
        graphics.drawWordWrap(font, Component.translatable("screen.selarium.warding_grimoire.overview.safety"), x, y + 92, width, COLOR_MUTED);
    }

    private void renderCategories(GuiGraphics graphics, int x, int y, int width) {
        int rightX = x + width / 2 + 8;
        graphics.drawString(font, Component.translatable("screen.selarium.warding_grimoire.group.mobs"), x, y, COLOR_ACCENT, false);
        String[] mobs = {"hostiles", "passives", "neutral", "villagers", "bosses"};
        for (int i = 0; i < mobs.length; i++) WorkshopUi.smallText(graphics, font,
                Component.translatable("screen.selarium.warding_grimoire.toggle." + mobs[i]), x, y + 17 + i * 16, 118, COLOR_INK);
        graphics.drawString(font, Component.translatable("screen.selarium.warding_grimoire.group.players"), x, y + 100, COLOR_ACCENT, false);
        String[] players = {"players", "owner_buffs", "owner_harm"};
        for (int i = 0; i < players.length; i++) WorkshopUi.smallText(graphics, font,
                Component.translatable("screen.selarium.warding_grimoire.toggle." + players[i]), x, y + 116 + i * 16, 118, COLOR_INK);
        graphics.drawString(font, Component.translatable("screen.selarium.warding_grimoire.group.protection"), rightX, y, COLOR_ACCENT, false);
        String[] protection = {"pets", "ignore_pets", "ignore_named"};
        for (int i = 0; i < protection.length; i++) WorkshopUi.smallText(graphics, font,
                Component.translatable("screen.selarium.warding_grimoire.toggle." + protection[i]), rightX, y + 17 + i * 16, 118, COLOR_INK);
        graphics.drawWordWrap(font, Component.translatable("screen.selarium.warding_grimoire.categories.note"),
                rightX, y + 81, width / 2 - 12, COLOR_MUTED);
    }

    private int renderGroupTitle(GuiGraphics graphics, int x, int y, String key) {
        graphics.drawString(font, Component.translatable(key), x, y, COLOR_ACCENT, false);
        return y + 15;
    }

    private int renderToggleLabel(GuiGraphics graphics, int x, int y, String key) {
        WorkshopUi.smallText(graphics, font, Component.translatable(key), x, y, 120, COLOR_INK);
        return y + 12;
    }

    private void renderSoulKnowledge(GuiGraphics graphics, int x, int y, int width) {
        graphics.drawString(font, Component.translatable("screen.selarium.warding_grimoire.souls.title"), x, y, COLOR_ACCENT, false);
        int rowY = y + 20;
        for (SoulEssenceCategory category : List.of(SoulEssenceCategory.MONSTROUS, SoulEssenceCategory.BESTIAL, SoulEssenceCategory.ANTHROPIC)) {
            boolean known = rules.knowsSoul(category);
            graphics.drawString(font, Component.translatable(category.getTranslationKey()), x, rowY, known ? COLOR_GOOD : COLOR_BAD, false);
            graphics.drawString(font, Component.translatable(known ? "screen.selarium.warding_grimoire.unlocked" : "screen.selarium.warding_grimoire.locked"), x + 104, rowY, known ? COLOR_GOOD : COLOR_BAD, false);
            rowY += 15;
        }
        graphics.drawWordWrap(font, Component.translatable("screen.selarium.warding_grimoire.souls.hint"), x + 190, y + 18, width - 190, COLOR_MUTED);
        renderList(graphics, Component.translatable("screen.selarium.warding_grimoire.known_types"), rules.knownEntityTypes(), knownTypePage, x, y + 72, width - 10, COLOR_ACCENT);
    }

    private void renderWardRules(GuiGraphics graphics, int x, int y, int width) {
        graphics.drawString(font, Component.translatable("screen.selarium.warding_grimoire.ward_rules.title"), x, y, COLOR_ACCENT, false);
        List<WardType> unlocked = unlockedWards();
        if (unlocked.isEmpty()) {
            graphics.drawWordWrap(font, Component.translatable("screen.selarium.warding_grimoire.ward_rules.none"), x, y + 24, width, COLOR_MUTED);
            return;
        }
        graphics.drawString(font, Component.translatable("screen.selarium.warding_grimoire.ward_rules.selected",
                selectedWard == WardType.NONE ? "-" : Component.translatable(selectedWard.getTranslationKey()).getString()), x + 126, y, COLOR_INK, false);
        if (selectedWard != WardType.NONE) {
            if (wardRulesListMode) {
                WardingWardRule rule = rules.wardRule(selectedWard);
                int rightX = x + 126;
                int rightWidth = width - 126;
                graphics.drawString(font, Component.translatable("screen.selarium.warding_grimoire.ward.lists.title"), rightX, y + 28, COLOR_ACCENT, false);
                graphics.drawString(font, Component.translatable("screen.selarium.warding_grimoire.players.input"), rightX, y + 38, COLOR_MUTED, false);
                graphics.drawString(font, Component.translatable("screen.selarium.warding_grimoire.entity_types.input"), rightX, y + 72, COLOR_MUTED, false);
                renderInlineSet(graphics, Component.translatable("screen.selarium.warding_grimoire.ward.allowed_players"), rule.allowPlayers(), rightX, y + 111, rightWidth, COLOR_GOOD);
                renderInlineSet(graphics, Component.translatable("screen.selarium.warding_grimoire.ward.denied_players"), rule.denyPlayers(), rightX, y + 126, rightWidth, COLOR_BAD);
                renderInlineSet(graphics, Component.translatable("screen.selarium.warding_grimoire.ward.allowed_types"), rule.allowEntityTypeIds(), rightX, y + 141, rightWidth, COLOR_GOOD);
                renderInlineSet(graphics, Component.translatable("screen.selarium.warding_grimoire.ward.denied_types"), rule.denyEntityTypeIds(), rightX, y + 156, rightWidth, COLOR_BAD);
                graphics.drawWordWrap(font, wardConfigNote(selectedWard), x, y + 120, 112, COLOR_MUTED);
            } else {
                int rowY = y + 48;
                rowY = renderToggleLabel(graphics, x + 126, rowY, "screen.selarium.warding_grimoire.ward.use_global");
                rowY += 6;
                rowY = renderToggleLabel(graphics, x + 126, rowY, "screen.selarium.warding_grimoire.toggle.hostiles");
                rowY = renderToggleLabel(graphics, x + 126, rowY, "screen.selarium.warding_grimoire.toggle.passives");
                rowY = renderToggleLabel(graphics, x + 126, rowY, "screen.selarium.warding_grimoire.toggle.neutral");
                rowY = renderToggleLabel(graphics, x + 126, rowY, "screen.selarium.warding_grimoire.toggle.villagers");
                rowY = renderToggleLabel(graphics, x + 126, rowY, "screen.selarium.warding_grimoire.toggle.players");
                rowY = renderToggleLabel(graphics, x + 126, rowY, "screen.selarium.warding_grimoire.toggle.pets");
                rowY = renderToggleLabel(graphics, x + 126, rowY, "screen.selarium.warding_grimoire.toggle.bosses");
                rowY = renderToggleLabel(graphics, x + 126, rowY, "screen.selarium.warding_grimoire.toggle.owner_target");
                renderToggleLabel(graphics, x + 126, rowY, "screen.selarium.warding_grimoire.toggle.allies");
                graphics.drawWordWrap(font, wardConfigNote(selectedWard), x + 252, y + 48, width - 252, COLOR_MUTED);
            }
        }
    }

    private void renderInlineSet(GuiGraphics graphics, Component title, Set<String> values, int x, int y, int width, int color) {
        String text = values.isEmpty()
                ? Component.translatable("screen.selarium.warding_grimoire.none").getString()
                : String.join(", ", values);
        graphics.drawString(font, title, x, y, color, false);
        graphics.drawString(font, Component.literal(shorten(text, width - 76)), x + 74, y, COLOR_INK, false);
    }

    private void renderPlayers(GuiGraphics graphics, int x, int y, int width) {
        graphics.drawString(font, Component.translatable("screen.selarium.warding_grimoire.players.input"), x, y, COLOR_ACCENT, false);
        graphics.drawString(font, Component.translatable("screen.selarium.warding_grimoire.players.note"), x, y + 36, COLOR_MUTED, false);
        int listY = y + 54;
        int columnWidth = (width / 2) - 8;
        renderList(graphics, Component.translatable("screen.selarium.warding_grimoire.allowed_players"), rules.allowPlayers(), playerAllowPage, x, listY, columnWidth, COLOR_GOOD);
        renderList(graphics, Component.translatable("screen.selarium.warding_grimoire.denied_players"), rules.denyPlayers(), playerDenyPage, x + (width / 2) + 8, listY, columnWidth, COLOR_BAD);
    }

    private void renderEntityTypes(GuiGraphics graphics, int x, int y, int width) {
        graphics.drawString(font, Component.translatable("screen.selarium.warding_grimoire.entity_types.input"), x, y, COLOR_ACCENT, false);
        graphics.drawString(font, Component.translatable("screen.selarium.warding_grimoire.entity_types.note"), x, y + 36, COLOR_MUTED, false);
        int listY = y + 54;
        int columnWidth = (width / 2) - 8;
        renderList(graphics, Component.translatable("screen.selarium.warding_grimoire.allowed_types"), rules.allowEntityTypeIds(), typeAllowPage, x, listY, columnWidth, COLOR_GOOD);
        renderList(graphics, Component.translatable("screen.selarium.warding_grimoire.denied_types"), rules.denyEntityTypeIds(), typeDenyPage, x + (width / 2) + 8, listY, columnWidth, COLOR_BAD);
    }

    private void renderAdvanced(GuiGraphics graphics, int x, int y, int width) {
        graphics.drawString(font, Component.translatable("screen.selarium.warding_grimoire.advanced.title"), x, y, COLOR_ACCENT, false);
        graphics.drawWordWrap(font, Component.translatable("screen.selarium.warding_grimoire.advanced.text"), x, y + 18, width, COLOR_MUTED);
        graphics.drawString(font, Component.translatable("screen.selarium.warding_grimoire.advanced.counts",
                rules.allowPlayers().size(), rules.denyPlayers().size(), rules.allowEntityTypeIds().size(), rules.denyEntityTypeIds().size()), x, y + 82, COLOR_INK, false);
        if (!canEdit()) {
            graphics.drawWordWrap(font, Component.translatable("screen.selarium.warding_grimoire.read_only_note"), x, y + 104, width, COLOR_BAD);
        }
    }

    private void renderList(GuiGraphics graphics, Component title, Set<String> values, int page, int x, int y, int width, int color) {
        graphics.fill(x, y, x + width, y + 78, 0x22FFFFFF);
        graphics.drawString(font, title, x + 4, y + 3, color, false);
        List<String> list = new ArrayList<>(values);
        int start = Math.max(0, page) * LIST_ROWS;
        if (start >= list.size() && !list.isEmpty()) {
            start = Math.max(0, list.size() - LIST_ROWS);
        }
        int end = Math.min(list.size(), start + LIST_ROWS);
        if (list.isEmpty()) {
            graphics.drawString(font, Component.translatable("screen.selarium.warding_grimoire.none"), x + 4, y + 21, COLOR_MUTED, false);
            return;
        }
        for (int index = start; index < end; index++) {
            String value = shorten(list.get(index), width - 30);
            graphics.drawString(font, Component.literal(value), x + 4, y + 19 + (index - start) * 16, COLOR_INK, false);
        }
        graphics.drawString(font, Component.literal((page + 1) + "/" + Math.max(1, (int) Math.ceil(list.size() / (double) LIST_ROWS))), x + 4, y + 83, COLOR_MUTED, false);
    }

    private String ownerDisplay() {
        UUID owner = rules.owner();
        if (owner == null) {
            return Component.translatable("screen.selarium.warding_grimoire.unbound").getString();
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null) {
            PlayerInfo info = minecraft.getConnection().getPlayerInfo(owner);
            if (info != null) {
                return info.getProfile().getName() + " (" + abbreviate(owner) + ")";
            }
        }
        return abbreviate(owner);
    }

    private boolean canEdit() {
        UUID owner = rules.owner();
        Minecraft minecraft = Minecraft.getInstance();
        if (owner == null || minecraft.player == null) {
            return true;
        }
        return minecraft.player.getUUID().equals(owner) || !SelariumCommonConfig.WARDING_GRIMOIRE_OWNER_LOCKED.get();
    }

    private boolean canEditSoul(SoulEssenceCategory requiredSoul) {
        return requiredSoul == null || rules.knowsSoul(requiredSoul);
    }

    private Component wardConfigNote(WardType type) {
        try {
            SelariumCommonConfig.MvpWardConfig config = SelariumCommonConfig.mvpWard(type);
            List<String> blocked = new ArrayList<>();
            if (!config.affectPlayers().get()) {
                blocked.add(Component.translatable("screen.selarium.warding_grimoire.config.players").getString());
            }
            if (!config.affectBosses().get()) {
                blocked.add(Component.translatable("screen.selarium.warding_grimoire.config.bosses").getString());
            }
            if (!blocked.isEmpty()) {
                return Component.translatable("screen.selarium.warding_grimoire.ward.config_blocked", String.join(", ", blocked));
            }
        } catch (IllegalArgumentException ignored) {
        }
        return Component.translatable("screen.selarium.warding_grimoire.ward.config_note");
    }

    private List<WardType> unlockedWards() {
        return WardDefinitions.all().stream()
                .map(definition -> definition.type())
                .filter(rules::isWardUnlocked)
                .toList();
    }

    private String abbreviate(UUID uuid) {
        String text = uuid.toString();
        return text.substring(0, 8) + "..." + text.substring(text.length() - 6);
    }

    private String shorten(String value, int maxWidth) {
        if (font.width(value) <= maxWidth) {
            return value;
        }
        String shortened = value;
        while (shortened.length() > 8 && font.width(shortened + "...") > maxWidth) {
            shortened = shortened.substring(0, shortened.length() - 1);
        }
        return shortened + "...";
    }

    private int panelWidth() {
        return PANEL_WIDTH;
    }

    private int panelHeight() {
        return PANEL_HEIGHT;
    }

    private int panelLeft() {
        return (virtualWidth() - panelWidth()) / 2;
    }

    private int panelTop() {
        return (virtualHeight() - panelHeight()) / 2;
    }

    private float uiScale() {
        return Math.min(1.0F, Math.min((width - 8.0F) / PANEL_WIDTH, (height - 8.0F) / PANEL_HEIGHT));
    }

    private int virtualWidth() { return Math.round(width / uiScale()); }
    private int virtualHeight() { return Math.round(height / uiScale()); }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private enum Tab {
        OVERVIEW("screen.selarium.warding_grimoire.tab.overview"),
        CATEGORIES("screen.selarium.warding_grimoire.tab.categories"),
        SOULS("screen.selarium.warding_grimoire.tab.souls"),
        WARD_RULES("screen.selarium.warding_grimoire.tab.ward_rules"),
        PLAYERS("screen.selarium.warding_grimoire.tab.players"),
        ENTITY_TYPES("screen.selarium.warding_grimoire.tab.entity_types"),
        ADVANCED("screen.selarium.warding_grimoire.tab.advanced");

        private final String key;

        Tab(String key) {
            this.key = key;
        }

        private Component label() {
            return Component.translatable(key);
        }
    }

    @FunctionalInterface
    private interface BooleanConsumer {
        void accept(boolean value);
    }

    @FunctionalInterface
    private interface ListAction {
        void run(String value);
    }
}
