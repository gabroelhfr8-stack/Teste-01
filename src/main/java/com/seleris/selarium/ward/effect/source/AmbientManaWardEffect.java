package com.seleris.selarium.ward.effect.source;

import com.seleris.selarium.blockentity.ManaTankBlockEntity;
import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardFx;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.IWardEffect;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/** Source ward: condenses ambient mana into the sigil's buffer and feeds adjacent tanks. */
public final class AmbientManaWardEffect implements IWardEffect {
    @Override
    public void tick(WardContext context) {
        if (!SelariumCommonConfig.AMBIENT_WARD_ENABLED.get()) {
            context.sigil().deactivateWard(SelariumCommonConfig.AMBIENT_WARD_COOLDOWN_TICKS.get());
            return;
        }

        int generated = Math.max(0, SelariumCommonConfig.AMBIENT_WARD_MANA_GENERATED_PER_CYCLE.get());
        int maxBuffer = Math.max(0, SelariumCommonConfig.AMBIENT_WARD_MAX_INTERNAL_BUFFER.get());
        int transferLimit = Math.max(0, SelariumCommonConfig.AMBIENT_WARD_TRANSFER_TO_TANK_PER_CYCLE.get());
        int remainingGeneratedForActivation = Math.max(0, SelariumCommonConfig.AMBIENT_WARD_MAX_MANA_GENERATED_PER_ACTIVATION.get() - context.sigil().getManaGeneratedThisActivation());

        if (remainingGeneratedForActivation <= 0) {
            context.sigil().deactivateWard(SelariumCommonConfig.AMBIENT_WARD_COOLDOWN_TICKS.get());
            return;
        }

        int accepted = context.sigil().addInternalMana(Math.min(generated, remainingGeneratedForActivation), maxBuffer);
        context.sigil().recordManaGeneratedThisActivation(accepted);

        if (transferLimit > 0) {
            int remainingTransfer = Math.min(transferLimit, context.sigil().getInternalManaBuffer());
            for (Direction direction : Direction.values()) {
                if (remainingTransfer <= 0) {
                    break;
                }

                BlockEntity blockEntity = context.level().getBlockEntity(context.pos().relative(direction));
                if (blockEntity instanceof ManaTankBlockEntity manaTank) {
                    int transferred = manaTank.receiveMana(remainingTransfer, false);
                    context.sigil().extractInternalMana(transferred);
                    remainingTransfer -= transferred;
                    if (transferred > 0) {
                        WardFx.trail(context.level(), Vec3.atCenterOf(context.pos()), Vec3.atCenterOf(manaTank.getBlockPos()),
                                WardType.AMBIENT_MANA, 5);
                    }
                }
            }
        }

        if (accepted <= 0 && SelariumCommonConfig.AMBIENT_WARD_EXPIRE_WHEN_BUFFER_FULL.get() && context.sigil().getInternalManaBuffer() >= maxBuffer) {
            context.sigil().deactivateWard(SelariumCommonConfig.AMBIENT_WARD_COOLDOWN_TICKS.get());
        }
    }
}
