package com.seleris.selarium.ward.effect.hostile;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardFx;
import com.seleris.selarium.ward.WardTargetingService;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.ConfiguredWardEffect;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/** Hostile ward: siphons life from invaders into sigil mana (and optionally owner health / food). */
public final class DrainWardEffect extends ConfiguredWardEffect {
    public DrainWardEffect() {
        super(WardType.DRAIN);
    }

    @Override
    protected void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
        Optional<ServerPlayer> owner = WardTargetingService.findOwnerInRange(context.level(), context.pos(), context.sigil(), Math.max(config.range().get(), 64));
        int[] generatedThisCycle = {0};
        Vec3 sigilCenter = Vec3.atCenterOf(context.pos());
        int affected = WardEffectUtils.applyToInvaders(context, config.range().get(), config.affectPlayers().get(), config.affectBosses().get(),
                config.maxEntitiesPerCycle().get(), config.manaCost().get(), target -> {
                    float damage = config.strength().get().floatValue();
                    if (target.hurt(context.level().damageSources().magic(), damage)) {
                        int produced = Math.min(20, (int) Math.round(damage * config.chance().get()));
                        produced = Math.min(produced, Math.max(0, 100 - generatedThisCycle[0]));
                        generatedThisCycle[0] += context.sigil().addInternalMana(produced,
                                SelariumCommonConfig.AMBIENT_WARD_MAX_INTERNAL_BUFFER.get());
                        WardFx.trail(context.level(), target.getEyePosition(), sigilCenter, WardType.DRAIN, 8);
                        owner.ifPresent(player -> {
                            if (config.optionA().get()) {
                                player.heal(Math.max(0.5F, damage * 0.35F));
                            }
                            if (config.optionB().get()) {
                                player.getFoodData().eat(1, 0.1F);
                            }
                        });
                    }
                });
        context.sigil().recordWardDebug(affected, 0, affected > 0 ? "life drained" : "no invaders");
    }
}
