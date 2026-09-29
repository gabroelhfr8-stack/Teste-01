package com.seleris.selarium.ward.effect.structure;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.registry.SelariumBlocks;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.ConfiguredWardEffect;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Structural wards that raise temporary blocks around the sigil: Citadel (a four-sided wall) and
 * Tangible (a full shell including floor and ceiling).
 */
public final class TemporaryWallWardEffect extends ConfiguredWardEffect {
    private final Supplier<Block> block;
    private final boolean fullShell;

    private TemporaryWallWardEffect(WardType type, Supplier<Block> block, boolean fullShell) {
        super(type);
        this.block = block;
        this.fullShell = fullShell;
    }

    public static TemporaryWallWardEffect citadel() {
        return new TemporaryWallWardEffect(WardType.CITADEL, SelariumBlocks.TEMPORARY_CITADEL_WALL, false);
    }

    public static TemporaryWallWardEffect tangible() {
        return new TemporaryWallWardEffect(WardType.TANGIBLE, SelariumBlocks.TANGIBLE_BARRIER_BLOCK, true);
    }

    @Override
    protected void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
        if (context.sigil().getTemporaryWardBlockCount() > 0) {
            context.sigil().recordWardDebug(0, context.sigil().getTemporaryWardBlockCount(), "temporary shell active");
            return;
        }
        if (!canUseFieldMana(context, config.manaCost().get())) {
            context.sigil().recordWardDebug(0, 0, "no mana");
            return;
        }

        Block wallBlock = block.get();
        int range = config.range().get();
        int height = Math.max(1, config.amplifier().get());
        int maxBlocks = WardEffectUtils.blockCap(config.maxBlocksPerCycle().get());
        int placed = 0;
        for (int y = fullShell ? 0 : 1; y <= height && placed < maxBlocks; y++) {
            for (int x = -range; x <= range && placed < maxBlocks; x++) {
                if (fullShell && (y == 0 || y == height)) {
                    for (int z = -range; z <= range && placed < maxBlocks; z++) {
                        placed += tryPlaceTemporary(context, wallBlock, context.pos().offset(x, y, z)) ? 1 : 0;
                    }
                } else {
                    placed += tryPlaceTemporary(context, wallBlock, context.pos().offset(x, y, -range)) ? 1 : 0;
                    placed += tryPlaceTemporary(context, wallBlock, context.pos().offset(x, y, range)) ? 1 : 0;
                }
            }
            if (!fullShell || (y > 0 && y < height)) {
                for (int z = -range + 1; z < range && placed < maxBlocks; z++) {
                    placed += tryPlaceTemporary(context, wallBlock, context.pos().offset(-range, y, z)) ? 1 : 0;
                    placed += tryPlaceTemporary(context, wallBlock, context.pos().offset(range, y, z)) ? 1 : 0;
                }
            }
        }
        context.sigil().recordWardDebug(0, placed, placed > 0 ? "temporary shell created" : "no replaceable positions");
    }

    private static boolean tryPlaceTemporary(WardContext context, Block block, BlockPos pos) {
        if (!context.level().getWorldBorder().isWithinBounds(pos) || pos.getY() <= context.level().getMinBuildHeight() || pos.getY() >= context.level().getMaxBuildHeight()) {
            return false;
        }
        BlockState oldState = context.level().getBlockState(pos);
        BlockEntity blockEntity = context.level().getBlockEntity(pos);
        boolean replaceable = context.level().isLoaded(pos) && oldState.isAir();
        if (!replaceable || blockEntity != null || !oldState.getFluidState().isEmpty()) {
            return false;
        }

        context.level().setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
        context.sigil().trackTemporaryWardBlock(pos);
        return true;
    }
}
