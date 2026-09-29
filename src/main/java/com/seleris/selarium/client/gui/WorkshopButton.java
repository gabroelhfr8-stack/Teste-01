package com.seleris.selarium.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/** Compact pixel button shared by the two book screens. */
public final class WorkshopButton extends Button {
    private boolean selected;

    private WorkshopButton(Button.Builder builder) {
        super(builder);
    }

    public static Builder builder(Component label, OnPress onPress) {
        return new Builder(label, onPress);
    }

    public WorkshopButton selected(boolean selected) {
        this.selected = selected;
        return this;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();
        boolean lit = selected || (active && isHoveredOrFocused());
        int edge = lit ? WorkshopUi.CYAN : 0xFF6D463E;
        int face = lit ? 0xFF315665 : active ? 0xFF514758 : 0xFF665E69;
        graphics.fill(x, y, x + w, y + h, edge);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF231D2B);
        graphics.fill(x + 2, y + 2, x + w - 2, y + h - 2, face);
        graphics.fill(x + 2, y + 2, x + w - 2, y + 3, lit ? 0xFFB4E7E5 : 0xFFB98B67);
        graphics.fill(x + 2, y + h - 3, x + w - 2, y + h - 2, 0xFF2A2432);
        Font font = Minecraft.getInstance().font;
        String text = font.plainSubstrByWidth(getMessage().getString(), Math.max(4, w - 8));
        graphics.drawCenteredString(font, text, x + w / 2, y + (h - 8) / 2,
                active || selected ? 0xFFF3EADD : 0xFFC9C0C5);
    }

    public static final class Builder extends Button.Builder {

        private Builder(Component label, OnPress onPress) {
            super(label, onPress);
            super.tooltip(Tooltip.create(label));
        }

        public Builder bounds(int x, int y, int width, int height) {
            super.bounds(x, y, width, height);
            return this;
        }

        public WorkshopButton build() {
            return new WorkshopButton(this);
        }
    }
}
