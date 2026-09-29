package com.seleris.selarium.client;

import com.seleris.selarium.client.gui.SelariumCodexScreen;
import com.seleris.selarium.client.gui.WardingGrimoireScreen;
import com.seleris.selarium.grimoire.WardingRuleSet;
import net.minecraft.client.Minecraft;

public final class SelariumClientHooks {
    private SelariumClientHooks() {
    }

    public static void openCodex() {
        Minecraft.getInstance().setScreen(new SelariumCodexScreen());
    }

    public static void openWardingGrimoire(WardingRuleSet rules) {
        Minecraft.getInstance().setScreen(new WardingGrimoireScreen(rules));
    }
}
