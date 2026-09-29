package com.seleris.selarium.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Shared pixel-drawn stone, copper and parchment furniture for Selarium screens. */
public final class WorkshopUi {
    public static final int INK = 0xFF34263C;
    public static final int MUTED = 0xFF725E70;
    public static final int COPPER = 0xFFAE754C;
    public static final int CYAN = 0xFF3296A3;
    public static final int GOOD = 0xFF288776;
    public static final int BAD = 0xFFA04C61;
    public static final int PAPER = 0xFFF0E6D4;
    private static final int SHADOW = 0xFF1D1726;
    private static final int STONE = 0xFF74697B;
    private static final int STONE_LIGHT = 0xFFCFC5C7;
    private static final int COPPER_DARK = 0xFF6D463E;

    private WorkshopUi() { }

    public static void frame(GuiGraphics g, int x, int y, int w, int h, boolean book) {
        g.fill(x + 3, y + 3, x + w + 3, y + h + 3, 0x880B0713);
        g.fill(x + 2, y, x + w - 2, y + h, SHADOW);
        g.fill(x, y + 2, x + w, y + h - 2, SHADOW);
        g.fill(x + 3, y + 2, x + w - 3, y + h - 2, COPPER_DARK);
        g.fill(x + 2, y + 3, x + w - 2, y + h - 3, COPPER_DARK);
        g.fill(x + 4, y + 4, x + w - 4, y + h - 4, STONE);
        g.fill(x + 6, y + 6, x + w - 6, y + h - 6, book ? PAPER : STONE_LIGHT);
        g.fill(x + 8, y + 8, x + w - 8, y + 20, 0xFF332638);
        g.fill(x + 8, y + 20, x + w - 8, y + 22, COPPER);
        for (int cornerX : new int[]{x + 4, x + w - 8}) {
            for (int cornerY : new int[]{y + 4, y + h - 8}) {
                g.fill(cornerX, cornerY, cornerX + 4, cornerY + 4, COPPER);
                g.fill(cornerX + 1, cornerY + 1, cornerX + 3, cornerY + 3, 0xFFE2B78A);
            }
        }
    }

    public static void recess(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, COPPER_DARK);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF443C4E);
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, 0xFF30283B);
        g.fill(x + 2, y + h - 3, x + w - 2, y + h - 2, STONE);
    }

    public static void parchment(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, COPPER_DARK);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, PAPER);
        g.fill(x + 2, y + 2, x + w - 2, y + 3, 0xFFF9F1E1);
        g.fill(x + 2, y + h - 3, x + w - 2, y + h - 2, 0xFFD2BDA7);
    }

    public static void slot(GuiGraphics g, int x, int y, boolean output) {
        g.fill(x - 2, y - 2, x + 18, y + 18, output ? CYAN : COPPER_DARK);
        g.fill(x - 1, y - 1, x + 17, y + 17, output ? 0xFF76C8CF : STONE);
        g.fill(x, y, x + 16, y + 16, 0xFF30283B);
        g.fill(x + 1, y + 1, x + 15, y + 2, 0xFF20192D);
    }

    public static void bar(GuiGraphics g, int x, int y, int width, int filled) {
        recess(g, x, y, width, 8);
        int fill = Math.max(0, Math.min(width - 4, filled));
        if (fill > 0) {
            g.fill(x + 2, y + 2, x + 2 + fill, y + 6, CYAN);
            g.fill(x + 2, y + 2, x + 2 + fill, y + 3, 0xFFAAE8E5);
        }
    }

    /** Text that is shrunk (down to this scale) before it is cut, so long translations stay readable. */
    private static final float MIN_TEXT_SCALE = 0.75F;

    /** Draws {@code text} at (x, y); if it is wider than {@code maxWidth} it is shrunk, and only then cut with an ellipsis. */
    public static void smallText(GuiGraphics g, Font font, Component text, int x, int y, int maxWidth, int color) {
        fitted(g, font, text.getString(), x, y, maxWidth, color, false);
    }

    /** Like {@link #smallText} but centred on {@code centerX}. */
    public static void centeredText(GuiGraphics g, Font font, String text, int centerX, int y, int maxWidth, int color) {
        fitted(g, font, text, centerX, y, maxWidth, color, true);
    }

    private static void fitted(GuiGraphics g, Font font, String full, int x, int y, int maxWidth, int color, boolean centered) {
        int limit = Math.max(4, maxWidth);
        int width = font.width(full);
        if (width <= limit) {
            g.drawString(font, full, centered ? x - width / 2 : x, y, color, false);
            return;
        }
        float scale = Math.max(MIN_TEXT_SCALE, limit / (float) width);
        String shown = full;
        if (width * scale > limit + 0.5F) {
            int room = Math.max(4, (int) (limit / scale) - font.width("\u2026"));
            shown = font.plainSubstrByWidth(full, room) + "\u2026";
        }
        float shownWidth = font.width(shown) * scale;
        var pose = g.pose();
        pose.pushPose();
        pose.translate(centered ? x - shownWidth / 2.0F : x, y + (1.0F - scale) * 4.0F, 0.0F);   // stay centred on the line
        pose.scale(scale, scale, 1.0F);
        g.drawString(font, shown, 0, 0, color, false);
        pose.popPose();
    }
}
