package com.seleris.selarium.ward.effect.utility;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardAccessService;
import com.seleris.selarium.ward.WardArea;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardFx;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.ConfiguredWardEffect;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Utility ward: bonemeals crops in the field and ages baby animals. */
public final class AcceleratingWardEffect extends ConfiguredWardEffect {
    public AcceleratingWardEffect() {
        super(WardType.ACCELERATING);
    }

    @Override
    protected void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
        int maxBlocks = WardEffectUtils.blockCap(config.maxBlocksPerCycle().get());
        int grownBlocks = 0;
        int range = config.range().get();
        WardArea area = WardArea.of(context, range);
        for (BlockPos pos : BlockPos.betweenClosed(context.pos().offset(-range, -1, -range), context.pos().offset(range, range, range))) {
            if (grownBlocks >= maxBlocks) {
                break;
            }
            if (!area.contains(pos)) {
                continue;
            }
            BlockState state = context.level().getBlockState(pos);
            if (!(state.getBlock() instanceof BonemealableBlock bonemealable)
                    || !bonemealable.isValidBonemealTarget(context.level(), pos, state, false)
                    || !bonemealable.isBonemealSuccess(context.level(), context.level().random, pos, state)
                    || !canUseFieldMana(context, config.manaCost().get())) {
                continue;
            }
            bonemealable.performBonemeal(context.level(), context.level().random, pos, state);
            WardFx.burst(context.level(), Vec3.atCenterOf(pos), WardType.ACCELERATING, 3, 0.25D);
            grownBlocks++;
        }

        int grownAnimals = 0;
        if (config.optionB().get() && config.maxEntitiesPerCycle().get() > 0 && WardAccessService.rulesFor(context.sigil()).affectPassiveMobs()) {
            for (AgeableMob baby : area.entities(AgeableMob.class, mob -> mob.isAlive() && mob.isBaby())) {
                if (grownAnimals >= WardEffectUtils.entityCap(config.maxEntitiesPerCycle().get())) {
                    break;
                }
                if (canUseFieldMana(context, config.manaCost().get())) {
                    baby.ageUp(60, true);
                    WardFx.touch(context.level(), baby, WardType.ACCELERATING, 3);
                    grownAnimals++;
                }
            }
        }
        context.sigil().recordWardDebug(grownAnimals, grownBlocks, grownBlocks + grownAnimals > 0 ? "growth boosted" : "no bonemealable target");
    }
}
