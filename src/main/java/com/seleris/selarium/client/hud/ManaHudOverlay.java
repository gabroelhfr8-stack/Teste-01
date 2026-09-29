package com.seleris.selarium.client.hud;

import com.seleris.selarium.client.ClientManaData;
import com.seleris.selarium.config.SelariumCommonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class ManaHudOverlay {
    private static final int BAR_WIDTH = 82;
    private static final int BAR_HEIGHT = 8;

    private ManaHudOverlay() {
    }

    public static void render(net.minecraft.client.gui.Gui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        if (!SelariumCommonConfig.ENABLE_MANA_HUD.get() || !ClientManaData.isInitialized()) {
            return;
        }

        int x = Math.min(Math.max(0, SelariumCommonConfig.MANA_HUD_X.get()), Math.max(0, screenWidth - BAR_WIDTH));
        int y = Math.min(Math.max(0, SelariumCommonConfig.MANA_HUD_Y.get()), Math.max(0, screenHeight - BAR_HEIGHT - 10));
        float ratio = Math.min(1.0F, Math.max(0.0F, ClientManaData.getCurrentMana() / (float) ClientManaData.getMaxMana()));
        int fillWidth = Math.round((BAR_WIDTH - 2) * ratio);

        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xAA0A0D16);
        graphics.fill(x + 1, y + 1, x + 1 + fillWidth, y + BAR_HEIGHT - 1, 0xFF44BDE5);

        if (SelariumCommonConfig.MANA_HUD_SHOW_GROWTH_FLASH.get() && ClientManaData.getFlashTicks() > 0) {
            int color = ClientManaData.getFlashDirection() >= 0 ? 0x66A6FFB4 : 0x66FF7E8A;
            graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, color);
        }

        if (SelariumCommonConfig.MANA_HUD_SHOW_NUMBERS.get()) {
            String text = ClientManaData.getCurrentMana() + "/" + ClientManaData.getMaxMana();
            graphics.drawString(Minecraft.getInstance().font, text, x + 2, y + BAR_HEIGHT + 2, 0xDDEBFFFF, true);
        }

        ClientManaData.tickHud();
    }
}

