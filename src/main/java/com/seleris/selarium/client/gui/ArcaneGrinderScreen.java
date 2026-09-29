package com.seleris.selarium.client.gui;

import com.seleris.selarium.grinder.menu.ArcaneGrinderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ArcaneGrinderScreen extends AbstractContainerScreen<ArcaneGrinderMenu> {
    public ArcaneGrinderScreen(ArcaneGrinderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 166;
        titleLabelX = 12;
        titleLabelY = 10;
        inventoryLabelX = 9;
        inventoryLabelY = 72;
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
        for (int i = 0; i < 3; i++) {
            var slot = menu.slots.get(i);
            if (mouseX >= leftPos + slot.x && mouseX < leftPos + slot.x + 16
                    && mouseY >= topPos + slot.y && mouseY < topPos + slot.y + 16) {
                String key = switch (i) {
                    case 0 -> "gui.selarium.grinder.input";
                    case 1 -> "gui.selarium.grinder.reagent";
                    default -> "gui.selarium.grinder.result";
                };
                g.renderTooltip(font, Component.translatable(key), mouseX, mouseY);
                break;
            }
        }
    }

    @Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        WorkshopUi.frame(g, x, y, imageWidth, imageHeight, false);
        WorkshopUi.recess(g, x + 14, y + 24, 148, 44);
        WorkshopUi.parchment(g, x + 5, y + 81, 166, 79);
        g.fill(x + 66, y + 43, x + 72, y + 45, WorkshopUi.COPPER);
        g.fill(x + 106, y + 43, x + 117, y + 45, WorkshopUi.COPPER);
        g.fill(x + 114, y + 40, x + 120, y + 48, WorkshopUi.COPPER);
        WorkshopUi.bar(g, x + 75, y + 39, 32, menu.getScaledProgress());
        for (var slot : menu.slots) WorkshopUi.slot(g, x + slot.x, y + slot.y, slot.index == 2);
    }

    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        WorkshopUi.smallText(g, font, title, titleLabelX, titleLabelY, 150, 0xFFF3E9DB);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, WorkshopUi.INK, false);
        WorkshopUi.smallText(g, font, Component.translatable("gui.selarium.grinder.input"), 64, 28, 56, 0xFFF1E7D9);
        WorkshopUi.smallText(g, font, Component.translatable("gui.selarium.grinder.reagent"), 64, 53, 56, 0xFFF1E7D9);
    }
}
