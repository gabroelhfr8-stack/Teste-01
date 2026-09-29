package com.seleris.selarium.client.gui;

import com.seleris.selarium.ward.WardDefinitions;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class SelariumCodexScreen extends Screen {
    private static final int PANEL_WIDTH = 300;
    private static final int PANEL_HEIGHT = 210;
    private static final List<CodexPage> PAGES = createPages();
    private int selectedPage;
    private int scrollOffset;
    private int pageOffset;

    public SelariumCodexScreen() {
        super(Component.translatable("screen.selarium.codex"));
    }

    @Override
    protected void init() {
        rebuildButtons();
    }

    private void rebuildButtons() {
        clearWidgets();
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        for (int index = pageOffset; index < Math.min(PAGES.size(), pageOffset + 9); index++) {
            int pageIndex = index;
            addRenderableWidget(WorkshopButton.builder(PAGES.get(index).title(), button -> {
                        selectedPage = pageIndex;
                        scrollOffset = 0;
                        rebuildButtons();
                    })
                    .bounds(left + 10, top + 24 + (index - pageOffset) * 18, 86, 16)
                    .build().selected(index == selectedPage));
        }
        addRenderableWidget(WorkshopButton.builder(Component.literal("↑"), button -> {
                    pageOffset = Math.max(0, pageOffset - 1);
                    rebuildButtons();
                }).bounds(left + 10, top + 190, 40, 15).build());
        addRenderableWidget(WorkshopButton.builder(Component.literal("↓"), button -> {
                    pageOffset = Math.min(Math.max(0, PAGES.size() - 9), pageOffset + 1);
                    rebuildButtons();
                }).bounds(left + 56, top + 190, 40, 15).build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 263) {
            selectedPage = Math.floorMod(selectedPage - 1, PAGES.size());
            if (selectedPage < pageOffset) pageOffset = selectedPage;
            scrollOffset = 0;
            rebuildButtons();
            return true;
        }
        if (keyCode == 262) {
            selectedPage = (selectedPage + 1) % PAGES.size();
            if (selectedPage >= pageOffset + 9) pageOffset = selectedPage - 8;
            scrollOffset = 0;
            rebuildButtons();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        WorkshopUi.frame(graphics, left, top, PANEL_WIDTH, PANEL_HEIGHT, true);
        WorkshopUi.recess(graphics, left + 8, top + 24, 91, 160);
        WorkshopUi.parchment(graphics, left + 108, top + 25, 184, 177);
        graphics.drawCenteredString(font, title, width / 2, top + 10, 0xFFF3E8D9);

        CodexPage page = PAGES.get(selectedPage);
        WorkshopUi.smallText(graphics, font, page.title(), left + 114, top + 32, 170, WorkshopUi.INK);
        List<FormattedCharSequence> contentLines = wrappedLines(page);
        int y = top + 48;
        int visibleLines = 14;
        int maxScroll = Math.max(0, contentLines.size() - visibleLines);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        for (int index = scrollOffset; index < contentLines.size() && index < scrollOffset + visibleLines; index++) {
            graphics.drawString(font, contentLines.get(index), left + 114, y, WorkshopUi.INK, false);
            y += 10;
        }
        if (maxScroll > 0) {
            graphics.drawString(font, Component.literal((scrollOffset + 1) + "/" + (maxScroll + 1)), left + PANEL_WIDTH - 54, top + PANEL_HEIGHT - 16, WorkshopUi.MUTED, false);
        }
        graphics.drawString(font, Component.literal("< / >"), left + PANEL_WIDTH - 36, top + PANEL_HEIGHT - 16, WorkshopUi.MUTED, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private List<FormattedCharSequence> wrappedLines(CodexPage page) {
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (Component paragraph : page.paragraphs()) {
            lines.addAll(font.split(paragraph, 170));
            lines.add(FormattedCharSequence.EMPTY);
        }
        return lines;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollOffset = Math.max(0, scrollOffset - (int) Math.signum(delta));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static List<CodexPage> createPages() {
        return List.of(
                page("start", 4),
                page("mana", 3),
                page("grinder", 2),
                page("recipes", 3),
                page("dusts", 2),
                page("sigils", 3),
                page("progression", 6),
                page("scrolls", 6),
                page("grimoire", 4),
                new CodexPage(Component.translatable("screen.selarium.codex.wards.title"), wardParagraphs()),
                page("tanks", 2),
                page("geodes", 3)
        );
    }

    private static CodexPage page(String key, int count) {
        List<Component> paragraphs = new ArrayList<>();
        for (int index = 1; index <= count; index++) {
            paragraphs.add(Component.translatable("screen.selarium.codex." + key + ".p" + index));
        }
        return new CodexPage(Component.translatable("screen.selarium.codex." + key + ".title"), paragraphs);
    }

    private static List<Component> wardParagraphs() {
        List<Component> paragraphs = new ArrayList<>();
        paragraphs.add(Component.translatable("screen.selarium.codex.wards.p1"));
        paragraphs.add(Component.translatable("screen.selarium.codex.wards.p2"));
        for (var definition : WardDefinitions.all()) {
            String requirements = definition.requirements().stream()
                    .map(requirement -> requirement.count() + "× "
                            + Component.translatable("dust.selarium."
                            + requirement.minimumPurity().getSerializedName() + "_"
                            + requirement.type().getSerializedName()).getString())
                    .reduce((first, second) -> first + ", " + second).orElse("");
            paragraphs.add(Component.translatable("screen.selarium.codex.ward.entry",
                    Component.translatable(definition.type().getTranslationKey()),
                    requirements, definition.range(), definition.upkeepCostValue(), definition.tickInterval()));
            paragraphs.add(Component.translatable("screen.selarium.codex.ward_note."
                    + definition.type().getSerializedName()));
        }
        return paragraphs;
    }

    private record CodexPage(Component title, List<Component> paragraphs) { }
}
