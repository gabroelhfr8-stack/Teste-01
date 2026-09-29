package com.seleris.selarium.client.hud;

import com.seleris.selarium.client.ClientManaData;
import com.seleris.selarium.config.SelariumClientConfig;
import com.seleris.selarium.registry.SelariumItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/** The mana gauge: a gilded crystal-topped bar with a travelling shimmer and gain / spend flashes. */
public final class ManaHudOverlay {
    private static final int BAR_WIDTH = 76;
    private static final int BAR_HEIGHT = 9;
    private static final int ICON = 16;
    private static final int GOLD = 0xFFB98A3A;
    private static final int GOLD_LIGHT = 0xFFE8C46A;
    private static final int PLATE = 0xE0120C22;

    private static ItemStack icon;

    private ManaHudOverlay() {
    }

    public static void render(Gui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!SelariumClientConfig.ENABLE_MANA_HUD.get() || !ClientManaData.isInitialized() || minecraft.options.hideGui) {
            return;
        }

        int totalWidth = ICON + 2 + BAR_WIDTH;
        int x = Math.min(Math.max(0, SelariumClientConfig.MANA_HUD_X.get()), Math.max(0, screenWidth - totalWidth));
        int y = Math.min(Math.max(0, SelariumClientConfig.MANA_HUD_Y.get()), Math.max(0, screenHeight - ICON - 12));
        float ratio = Mth.clamp(ClientManaData.getCurrentMana() / (float) ClientManaData.getMaxMana(), 0.0F, 1.0F);
        long time = minecraft.level == null ? 0L : minecraft.level.getGameTime();

        if (icon == null) {
            icon = new ItemStack(SelariumItems.ARCANE_CRYSTAL.get());
        }
        graphics.renderFakeItem(icon, x, y);

        int bx = x + ICON + 2;
        int by = y + (ICON - BAR_HEIGHT) / 2;
        // frame: gilded outline around a dark plate
        graphics.fill(bx - 1, by - 1, bx + BAR_WIDTH + 1, by + BAR_HEIGHT + 1, GOLD);
        graphics.fill(bx - 1, by - 1, bx + BAR_WIDTH + 1, by, GOLD_LIGHT);
        graphics.fill(bx, by, bx + BAR_WIDTH, by + BAR_HEIGHT, PLATE);

        int fill = Math.round((BAR_WIDTH - 2) * ratio);
        if (fill > 0) {
            int fx = bx + 1;
            int fy = by + 1;
            int fh = BAR_HEIGHT - 2;
            graphics.fillGradient(fx, fy, fx + fill, fy + fh, 0xFF8FEBF7, 0xFF1B6FC4);
            graphics.fill(fx, fy, fx + fill, fy + 1, 0x88FFFFFF);
            // shimmer sweeping across the filled portion
            int sweep = (int) ((time * 2L + (long) (partialTick * 2.0F)) % (BAR_WIDTH + 24)) - 12;
            int sx0 = Math.max(fx, fx + sweep);
            int sx1 = Math.min(fx + fill, fx + sweep + 7);
            if (sx1 > sx0) {
                graphics.fill(sx0, fy, sx1, fy + fh, 0x44FFFFFF);
            }
            // tick marks every 10 %
            for (int i = 1; i < 10; i++) {
                int tx = bx + 1 + (BAR_WIDTH - 2) * i / 10;
                if (tx < fx + fill) {
                    graphics.fill(tx, fy + 1, tx + 1, fy + fh - 1, 0x33000000);
                }
            }
        }

        if (SelariumClientConfig.MANA_HUD_SHOW_GROWTH_FLASH.get() && ClientManaData.getFlashTicks() > 0) {
            float strength = ClientManaData.getFlashTicks() / 30.0F;
            int alpha = (int) (0x70 * strength);
            int color = ClientManaData.getFlashDirection() >= 0 ? 0x66FFB4 : 0xFF6E7C;
            graphics.fill(bx - 1, by - 1, bx + BAR_WIDTH + 1, by + BAR_HEIGHT + 1, (alpha << 24) | color);
        }

        if (SelariumClientConfig.MANA_HUD_SHOW_NUMBERS.get()) {
            String text = ClientManaData.getCurrentMana() + " / " + ClientManaData.getMaxMana();
            graphics.drawString(minecraft.font, text, bx, by + BAR_HEIGHT + 3, 0xFFD7F4FF, true);
        }

        ClientManaData.tickHud();
    }
}
