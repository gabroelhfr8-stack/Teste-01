package com.seleris.selarium.client.gui;

import com.seleris.selarium.dust.DustDefinition;
import com.seleris.selarium.dust.DustUtil;
import com.seleris.selarium.inscription.InscriptionMenu;
import com.seleris.selarium.inscription.InscriptionRecipe;
import com.seleris.selarium.registry.SelariumItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class InscriptionScreen extends AbstractContainerScreen<InscriptionMenu> {
    private static final int VISIBLE_RECIPES = 7;
    private int scrollOffset;

    public InscriptionScreen(InscriptionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 300;
        imageHeight = 234;
        titleLabelX = 13;
        titleLabelY = 10;
        inventoryLabelX = 71;
        inventoryLabelY = 141;
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (mouseX >= leftPos + 12 && mouseX < leftPos + 108 && mouseY >= topPos + 31
                && mouseY < topPos + 31 + VISIBLE_RECIPES * 14) {
            int index = scrollOffset + (mouseY - topPos - 31) / 14;
            InscriptionRecipe recipe = InscriptionRecipe.byIndex(index);
            if (recipe != null) graphics.renderTooltip(font, recipe.name(), mouseX, mouseY);
        }
    }

    @Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        WorkshopUi.frame(g, x, y, imageWidth, imageHeight, false);
        WorkshopUi.recess(g, x + 10, y + 28, 101, 103);
        WorkshopUi.parchment(g, x + 115, y + 28, 175, 103);
        WorkshopUi.parchment(g, x + 65, y + 151, 170, 57);
        WorkshopUi.parchment(g, x + 65, y + 209, 170, 23);
        for (int row = 0; row < VISIBLE_RECIPES; row++) {
            int index = scrollOffset + row;
            if (index >= InscriptionRecipe.COUNT) break;
            int top = y + 31 + row * 14;
            if (index == menu.selectedIndex()) {
                g.fill(x + 12, top, x + 108, top + 13, 0xFF267D8D);
                g.fill(x + 12, top, x + 14, top + 13, 0xFFA6E7E1);
            } else if (index % 2 == 0) {
                g.fill(x + 12, top, x + 108, top + 13, 0xFF403449);
            }
        }
        for (int slot = 0; slot < menu.slots.size(); slot++) {
            var current = menu.slots.get(slot);
            WorkshopUi.slot(g, x + current.x, y + current.y, slot == 8);
        }
        g.fill(x + 147, y + 72, x + 249, y + 74, WorkshopUi.COPPER);
        g.fill(x + 216, y + 89, x + 252, y + 91, WorkshopUi.COPPER);
        g.fill(x + 248, y + 86, x + 253, y + 94, WorkshopUi.COPPER);
        renderGhostIngredients(g);
    }

    private void renderGhostIngredients(GuiGraphics g) {
        InscriptionRecipe recipe = menu.recipe();
        if (recipe == null) return;
        if (menu.slots.get(0).getItem().isEmpty())
            g.renderFakeItem(new ItemStack(SelariumItems.EMPTY_SCROLL.get()), leftPos + 126, topPos + 82);
        for (int i = 0; i < Math.min(5, recipe.dusts().size()); i++) {
            if (!menu.slots.get(i + 1).getItem().isEmpty()) continue;
            var dust = recipe.dusts().get(i);
            ItemStack icon = DustUtil.itemFor(new DustDefinition(dust.type(), dust.purity()));
            if (!icon.isEmpty()) g.renderFakeItem(icon, leftPos + 147 + i * 21, topPos + 43);
        }
        for (int i = 0; i < Math.min(2, recipe.catalysts().size()); i++) {
            if (menu.slots.get(i + 6).getItem().isEmpty())
                g.renderFakeItem(new ItemStack(recipe.catalysts().get(i)), leftPos + 173 + i * 21, topPos + 82);
        }
    }

    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, 0xFFF1E7D8, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, WorkshopUi.INK, false);
        for (int row = 0; row < VISIBLE_RECIPES; row++) {
            InscriptionRecipe recipe = InscriptionRecipe.byIndex(scrollOffset + row);
            if (recipe == null) break;
            WorkshopUi.smallText(g, font, recipe.name(), 17, 34 + row * 14, 87, 0xFFF5EBDD);
        }
        InscriptionRecipe recipe = menu.recipe();
        if (recipe == null) {
            g.drawString(font, Component.translatable("gui.selarium.inscription.select"), 123, 35, WorkshopUi.MUTED, false);
            return;
        }
        WorkshopUi.smallText(g, font, recipe.name(), 123, 33, 155, WorkshopUi.INK);
        g.drawString(font, Component.translatable("gui.selarium.inscription.ingredients"), 124, 63, WorkshopUi.MUTED, false);
        g.drawString(font, Component.translatable("gui.selarium.inscription.status"), 122, 104, WorkshopUi.MUTED, false);
        WorkshopUi.smallText(g, font, menu.status().message(), 122, 116, 160,
                menu.status() == InscriptionRecipe.Status.READY ? WorkshopUi.GOOD : WorkshopUi.BAD);
        Component detail = recipe.attunement()
                ? Component.translatable("gui.selarium.inscription.milestone", recipe.milestone().spent(),
                recipe.milestone().distinct(), recipe.milestone().refined())
                : Component.translatable("gui.selarium.inscription.field", recipe.ward().range(),
                recipe.ward().upkeepCostValue(), Math.max(1, recipe.ward().durationTicks() / 40));
        WorkshopUi.smallText(g, font, detail, 12, 135, 276, WorkshopUi.MUTED);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= leftPos + 12 && mouseX < leftPos + 108
                && mouseY >= topPos + 31 && mouseY < topPos + 31 + VISIBLE_RECIPES * 14) {
            int index = scrollOffset + ((int) mouseY - topPos - 31) / 14;
            if (index < InscriptionRecipe.COUNT && minecraft != null && minecraft.gameMode != null && minecraft.player != null) {
                menu.clickMenuButton(minecraft.player, index);
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (mouseX >= leftPos + 10 && mouseX < leftPos + 111 && mouseY >= topPos + 28 && mouseY < topPos + 131) {
            scrollOffset = Math.max(0, Math.min(InscriptionRecipe.COUNT - VISIBLE_RECIPES,
                    scrollOffset - (int) Math.signum(amount)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
}
