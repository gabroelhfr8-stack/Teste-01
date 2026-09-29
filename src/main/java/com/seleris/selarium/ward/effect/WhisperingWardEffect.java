package com.seleris.selarium.ward.effect;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardTargetingService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class WhisperingWardEffect implements IWardEffect {
    @Override
    public void tick(WardContext context) {
        if (context.sigil().getAlertCooldownRemainingTicks() > 0) {
            return;
        }

        var targets = WardTargetingService.findInvaders(
                context.level(),
                context.pos(),
                context.sigil(),
                SelariumCommonConfig.WHISPERING_WARD_RANGE.get(),
                SelariumCommonConfig.WHISPERING_WARD_DETECT_HOSTILE_MOBS.get(),
                SelariumCommonConfig.WHISPERING_WARD_DETECT_PLAYERS.get());

        if (targets.isEmpty() || context.sigil().getOwner() == null) {
            return;
        }

        ServerPlayer owner = context.level().getServer().getPlayerList().getPlayer(context.sigil().getOwner());
        if (owner == null) {
            return;
        }

        owner.displayClientMessage(Component.translatable("message.selarium.ward.whispering_alert", targets.size()), true);
        context.sigil().recordWardDebug(targets.size(), 0, "whispering alert sent");
        context.sigil().setAlertCooldownRemainingTicks(SelariumCommonConfig.WHISPERING_WARD_ALERT_COOLDOWN_TICKS.get());
    }
}
