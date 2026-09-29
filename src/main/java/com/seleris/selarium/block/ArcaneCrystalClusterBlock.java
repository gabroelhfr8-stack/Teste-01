package com.seleris.selarium.block;

import com.seleris.selarium.registry.SelariumBlocks;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ArcaneCrystalClusterBlock extends AmethystClusterBlock {
    private final GrowthStage growthStage;

    public ArcaneCrystalClusterBlock(int height, int width, GrowthStage growthStage, BlockBehaviour.Properties properties) {
        super(height, width, properties);
        this.growthStage = growthStage;
    }

    public GrowthStage growthStage() {
        return growthStage;
    }

    public boolean canAdvance() {
        return growthStage != GrowthStage.CLUSTER;
    }

    public Block nextStageBlock() {
        return switch (growthStage) {
            case SMALL -> SelariumBlocks.MEDIUM_ARCANE_CRYSTAL_BUD.get();
            case MEDIUM -> SelariumBlocks.LARGE_ARCANE_CRYSTAL_BUD.get();
            case LARGE -> SelariumBlocks.ARCANE_CRYSTAL_CLUSTER.get();
            case CLUSTER -> this;
        };
    }

    public enum GrowthStage {
        SMALL,
        MEDIUM,
        LARGE,
        CLUSTER
    }
}
