package com.seleris.selarium.client.gui;

import com.seleris.selarium.Selarium;
import com.seleris.selarium.ward.WardDefinition;
import com.seleris.selarium.ward.WardDefinitions;
import com.seleris.selarium.ward.WardStyles;
import com.seleris.selarium.ward.WardType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class SelariumCodexScreen extends Screen {
    private static final int PANEL_WIDTH = 300;
    private static final int PANEL_HEIGHT = 210;
    private static final int ATLAS_COLUMNS = 8;
    private static final int ATLAS_CELL = 22;
    private static final List<CodexPage> PAGES = createPages();
    private WardType atlasSelection = WardType.NONE;
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
        if (page.atlas()) {
            renderAtlas(graphics, left, top, mouseX, mouseY);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }
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
            String counter = (scrollOffset + 1) + "/" + (maxScroll + 1);
            graphics.drawString(font, counter, left + PANEL_WIDTH - 42 - font.width(counter), top + PANEL_HEIGHT - 16, WorkshopUi.MUTED, false);
        }
        graphics.drawString(font, Component.literal("< / >"), left + PANEL_WIDTH - 36, top + PANEL_HEIGHT - 16, WorkshopUi.MUTED, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private static ResourceLocation glyph(WardType type) {
        return ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID, "textures/gui/ward_glyph/" + type.getSerializedName() + ".png");
    }

    private WardType selectedWard(List<WardDefinition> wards) {
        return atlasSelection != WardType.NONE ? atlasSelection : wards.isEmpty() ? WardType.NONE : wards.get(0).type();
    }

    /** A grid with every ward's glyph; the selected ward's recipe and effect are written underneath. */
    private void renderAtlas(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        List<WardDefinition> wards = WardDefinitions.all();
        WardType selected = selectedWard(wards);
        WardType hovered = WardType.NONE;
        int gridLeft = left + 112;
        int gridTop = top + 46;
        for (int index = 0; index < wards.size(); index++) {
            WardType type = wards.get(index).type();
            int x = gridLeft + (index % ATLAS_COLUMNS) * ATLAS_CELL;
            int y = gridTop + (index / ATLAS_COLUMNS) * ATLAS_CELL;
            boolean over = mouseX >= x && mouseX < x + 20 && mouseY >= y && mouseY < y + 20;
            if (over) {
                hovered = type;
            }
            graphics.fill(x, y, x + 20, y + 20, type == selected ? WorkshopUi.CYAN : 0xFF6D463E);
            graphics.fill(x + 1, y + 1, x + 19, y + 19, over ? 0xFF3D3350 : 0xFF241D2E);
            int rgb = WardStyles.primary(type);
            graphics.setColor(((rgb >> 16) & 0xFF) / 255.0F, ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F, 1.0F);
            graphics.blit(glyph(type), x + 2, y + 2, 16, 16, 0.0F, 0.0F, 32, 32, 32, 32);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        int textLeft = left + 114;
        int y = gridTop + 4 * ATLAS_CELL + 4;
        WardDefinitions.get(selected).ifPresent(definition -> {
            int rgb = WardStyles.primary(selected);
            graphics.drawString(font, Component.translatable(selected.getTranslationKey()), textLeft, y, inkFor(rgb), false);
            graphics.drawString(font, Component.translatable("tooltip.selarium.scroll.category",
                    Component.translatable(WardStyles.category(selected).getTranslationKey())), textLeft, y + 10, WorkshopUi.MUTED, false);
            int line = y + 22;
            for (FormattedCharSequence sequence : font.split(Component.translatable("screen.selarium.codex.atlas.detail",
                    requirements(definition), definition.range(), definition.upkeepCostValue(),
                    definition.tickInterval()), 170)) {
                graphics.drawString(font, sequence, textLeft, line, WorkshopUi.INK, false);
                line += 10;
            }
            for (FormattedCharSequence sequence : font.split(Component.translatable("screen.selarium.codex.ward_note."
                    + selected.getSerializedName()), 170)) {
                graphics.drawString(font, sequence, textLeft, line + 2, WorkshopUi.MUTED, false);
                line += 10;
            }
        });
        if (hovered != WardType.NONE) {
            graphics.renderTooltip(font, Component.translatable(hovered.getTranslationKey()), mouseX, mouseY);
        }
    }

    /** The ward's colour darkened enough to read on parchment. */
    private static int inkFor(int rgb) {
        int red = (int) (((rgb >> 16) & 0xFF) * 0.5F);
        int green = (int) (((rgb >> 8) & 0xFF) * 0.5F);
        int blue = (int) ((rgb & 0xFF) * 0.5F);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static String requirements(WardDefinition definition) {
        return definition.requirements().stream()
                .map(requirement -> requirement.count() + "× "
                        + Component.translatable("dust.selarium." + requirement.minimumPurity().getSerializedName() + "_"
                        + requirement.type().getSerializedName()).getString())
                .reduce((first, second) -> first + ", " + second).orElse("");
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && PAGES.get(selectedPage).atlas()) {
            int gridLeft = (width - PANEL_WIDTH) / 2 + 112;
            int gridTop = (height - PANEL_HEIGHT) / 2 + 46;
            List<WardDefinition> wards = WardDefinitions.all();
            for (int index = 0; index < wards.size(); index++) {
                int x = gridLeft + (index % ATLAS_COLUMNS) * ATLAS_CELL;
                int y = gridTop + (index / ATLAS_COLUMNS) * ATLAS_CELL;
                if (mouseX >= x && mouseX < x + 20 && mouseY >= y && mouseY < y + 20) {
                    atlasSelection = wards.get(index).type();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
                page("wards", 2),
                new CodexPage(Component.translatable("screen.selarium.codex.atlas.title"), List.of(), true),
                page("tanks", 2),
                page("geodes", 3)
        );
    }

    private static CodexPage page(String key, int count) {
        List<Component> paragraphs = new ArrayList<>();
        for (int index = 1; index <= count; index++) {
            paragraphs.add(Component.translatable("screen.selarium.codex." + key + ".p" + index));
        }
        return new CodexPage(Component.translatable("screen.selarium.codex." + key + ".title"), paragraphs, false);
    }

    private record CodexPage(Component title, List<Component> paragraphs, boolean atlas) { }
}
