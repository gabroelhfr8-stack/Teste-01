package com.seleris.selarium.block;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.worldgen.ArcaneTreeGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ArcaneLeavesBlock extends LeavesBlock {
    public ArcaneLeavesBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (!level.getBlockState(pos).is(this) || !SelariumCommonConfig.ARCANE_PETALS_ENABLED.get()) {
            return;
        }
        double chance = Math.max(0.0D, Math.min(1.0D, SelariumCommonConfig.ARCANE_PETALS_GENERATION_CHANCE.get()));
        if (chance <= 0.0D || random.nextDouble() >= chance) {
            return;
        }
        int radius = Math.max(1, SelariumCommonConfig.ARCANE_PETALS_SEARCH_RADIUS.get());
        int maxNearby = Math.max(0, SelariumCommonConfig.ARCANE_PETALS_MAX_NEARBY.get());
        if (maxNearby <= 0 || ArcaneTreeGenerator.countNearbyPetals(level, pos, radius) >= maxNearby) {
            return;
        }
        ArcaneTreeGenerator.tryPlacePetalsNear(level, random, pos, Math.max(4, radius * 3), radius);
    }
}
