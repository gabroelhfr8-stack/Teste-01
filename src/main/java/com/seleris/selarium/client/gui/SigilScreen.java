package com.seleris.selarium.client.gui;

import com.seleris.selarium.Selarium;
import com.seleris.selarium.dust.DustDefinition;
import com.seleris.selarium.dust.DustUtil;
import com.seleris.selarium.sigil.SigilMenu;
import com.seleris.selarium.ward.WardDefinitions;
import com.seleris.selarium.ward.WardType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class SigilScreen extends AbstractContainerScreen<SigilMenu> {
    private static final int VISIBLE_COMPONENTS = 12;
    private static final ResourceLocation BASE = ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID,
            "textures/block/arcane_sigil.png");
    private int componentOffset;

    public SigilScreen(SigilMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 300;
        imageHeight = 234;
        titleLabelX = 13;
        titleLabelY = 10;
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
        List<Map.Entry<DustDefinition, Integer>> entries = components();
        int hit = componentAt(mouseX, mouseY);
        if (hit >= 0 && componentOffset + hit < entries.size())
            g.renderTooltip(font, entries.get(componentOffset + hit).getKey().getDisplayName(), mouseX, mouseY);
    }

    @Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        WorkshopUi.frame(g, x, y, imageWidth, imageHeight, false);
        WorkshopUi.recess(g, x + 10, y + 29, 128, 108);
        WorkshopUi.parchment(g, x + 143, y + 29, 147, 108);
        WorkshopUi.parchment(g, x + 10, y + 143, 280, 81);

        g.blit(BASE, x + 42, y + 51, 0, 0, 64, 64, 64, 64);
        var sigil = menu.sigil();
        if (sigil != null && sigil.getWardType() != WardType.NONE) {
            ResourceLocation ward = ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID,
                    "textures/block/arcane_sigil_" + sigil.getWardType().getSerializedName() + ".png");
            g.blit(ward, x + 42, y + 51, 0, 0, 64, 64, 64, 64);
        }

        List<Map.Entry<DustDefinition, Integer>> components = components();
        for (int shown = 0; shown < VISIBLE_COMPONENTS; shown++) {
            int index = componentOffset + shown;
            if (index >= components.size()) break;
            int sx = x + 17 + (shown % 6) * 22;
            int sy = y + 166 + (shown / 6) * 24;
            WorkshopUi.slot(g, sx, sy, false);
            ItemStack icon = DustUtil.itemFor(components.get(index).getKey());
            if (!icon.isEmpty()) {
                g.renderFakeItem(icon, sx, sy);
                g.renderItemDecorations(font, icon, sx, sy, Integer.toString(components.get(index).getValue()));
            }
        }
        WorkshopUi.recess(g, x + 156, y + 165, 126, 50);
        g.fill(x + 160, y + 189, x + 278, y + 190, WorkshopUi.COPPER);
    }

    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, 0xFFF3E9DA, false);
        WorkshopUi.smallText(g, font, Component.translatable("gui.selarium.sigil.browse_hint"),
                17, 34, 114, 0xFFF0E4D3);
        WorkshopUi.smallText(g, font, Component.translatable("gui.selarium.sigil.bind_hint"),
                17, 118, 114, 0xFFB6EEE9);

        var sigil = menu.sigil();
        WardType type = sigil == null ? WardType.NONE : sigil.getWardType();
        WorkshopUi.smallText(g, font, Component.translatable(type.getTranslationKey()),
                149, 35, 134, WorkshopUi.INK);
        if (sigil != null) {
            WardDefinitions.get(type).ifPresent(definition -> {
                WorkshopUi.smallText(g, font, Component.translatable("gui.selarium.sigil.stat_range", definition.range()),
                        149, 49, 134, WorkshopUi.MUTED);
                WorkshopUi.smallText(g, font, Component.translatable("gui.selarium.sigil.stat_upkeep", definition.upkeepCostValue()),
                        149, 59, 134, WorkshopUi.MUTED);
                WorkshopUi.smallText(g, font, Component.translatable("gui.selarium.sigil.stat_duration", definition.durationTicks() / 20),
                        149, 69, 134, WorkshopUi.MUTED);
                g.drawString(font, Component.translatable("gui.selarium.sigil.requirements"),
                        149, 80, WorkshopUi.INK, false);
                int line = 0;
                for (var requirement : definition.requirements()) {
                    String key = "dust.selarium." + requirement.minimumPurity().getSerializedName()
                            + "_" + requirement.type().getSerializedName();
                    WorkshopUi.smallText(g, font, Component.literal(requirement.count() + "× ")
                            .append(Component.translatable(key)), 149, 91 + line * 9, 133,
                            requirement.matches(sigil) ? WorkshopUi.GOOD : WorkshopUi.BAD);
                    line++;
                }
            });
            if (type == WardType.NONE) {
                WorkshopUi.smallText(g, font, Component.translatable("gui.selarium.sigil.unbound_hint"),
                        149, 55, 133, WorkshopUi.MUTED);
            }
        }

        g.drawString(font, Component.translatable("gui.selarium.sigil.components"),
                15, 150, WorkshopUi.INK, false);
        if (sigil != null) {
            WorkshopUi.smallText(g, font, Component.translatable(sigil.isActive()
                    ? "gui.selarium.sigil.active" : "gui.selarium.sigil.inactive"),
                    161, 169, 113, 0xFFF0E4D3);
            WorkshopUi.smallText(g, font, Component.translatable("gui.selarium.sigil.mana",
                    sigil.getInternalManaBuffer()), 161, 180, 113, 0xFFB6EEE9);
            WorkshopUi.smallText(g, font, Component.translatable("gui.selarium.sigil.quick_action"),
                    161, 196, 113, 0xFFF0E4D3);
        }
    }

    private List<Map.Entry<DustDefinition, Integer>> components() {
        var sigil = menu.sigil();
        if (sigil == null) return List.of();
        return sigil.getComponents().entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getSerializedName())).toList();
    }

    private int componentAt(double mouseX, double mouseY) {
        if (mouseX < leftPos + 17 || mouseX >= leftPos + 149 || mouseY < topPos + 166 || mouseY >= topPos + 206)
            return -1;
        int column = ((int) mouseX - leftPos - 17) / 22;
        int row = ((int) mouseY - topPos - 166) / 24;
        return column < 6 && row < 2
                && ((int) mouseX - leftPos - 17) % 22 < 16
                && ((int) mouseY - topPos - 166) % 24 < 16 ? row * 6 + column : -1;
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && minecraft != null && minecraft.gameMode != null) {
            int hit = componentAt(mouseX, mouseY);
            List<Map.Entry<DustDefinition, Integer>> entries = components();
            if (hit >= 0 && componentOffset + hit < entries.size()) {
                DustDefinition definition = entries.get(componentOffset + hit).getKey();
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                        100 + definition.type().ordinal() * 2 + definition.purity().ordinal());
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (mouseX >= leftPos + 10 && mouseX < leftPos + 151 && mouseY >= topPos + 143 && mouseY < topPos + 224) {
            componentOffset = Math.max(0, Math.min(Math.max(0, components().size() - VISIBLE_COMPONENTS),
                    componentOffset - (int) Math.signum(amount) * 6));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
}
