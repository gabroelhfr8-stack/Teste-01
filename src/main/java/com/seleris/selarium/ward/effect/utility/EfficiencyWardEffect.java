package com.seleris.selarium.ward.effect.utility;

import com.seleris.selarium.blockentity.ArcaneGrinderBlockEntity;
import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardArea;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardFx;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.ConfiguredWardEffect;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/** Utility ward: speeds up working furnaces and Arcane Grinders inside the field. */
public final class EfficiencyWardEffect extends ConfiguredWardEffect {
    public EfficiencyWardEffect() {
        super(WardType.EFFICIENCY);
    }

    @Override
    protected void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
        int maxBlocks = WardEffectUtils.blockCap(config.maxBlocksPerCycle().get());
        if (maxBlocks <= 0 || !config.optionA().get()) {
            context.sigil().recordWardDebug(0, 0, "efficiency disabled");
            return;
        }

        int affected = 0;
        int range = config.range().get();
        WardArea area = WardArea.of(context, range);
        for (BlockPos pos : BlockPos.betweenClosed(context.pos().offset(-range, -range, -range), context.pos().offset(range, range, range))) {
            if (affected >= maxBlocks) {
                break;
            }
            if (!area.contains(pos)) {
                continue;
            }
            BlockEntity blockEntity = context.level().getBlockEntity(pos);
            boolean eligible = blockEntity instanceof AbstractFurnaceBlockEntity furnace && FurnaceProgressAccess.canBoost(furnace)
                    || blockEntity instanceof ArcaneGrinderBlockEntity grinder && grinder.canBoostProgress();
            if (!eligible || !canUseFieldMana(context, config.manaCost().get())) {
                continue;
            }
            int boost = Math.max(1, config.amplifier().get());
            boolean boosted = false;
            if (blockEntity instanceof AbstractFurnaceBlockEntity furnace) {
                boosted = FurnaceProgressAccess.boost(furnace, boost);
            } else if (blockEntity instanceof ArcaneGrinderBlockEntity grinder) {
                boosted = grinder.boostProgress(boost);
            }
            if (boosted) {
                WardFx.burst(context.level(), Vec3.atCenterOf(pos).add(0.0D, 0.4D, 0.0D), WardType.EFFICIENCY, 2, 0.2D);
                affected++;
            }
        }
        context.sigil().recordWardDebug(0, affected, affected > 0 ? "machines boosted" : "no active cooking progress");
    }
}
