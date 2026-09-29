package com.seleris.selarium.ward;

import com.seleris.selarium.ward.WardFieldSource;
import com.seleris.selarium.blockentity.ManaTankBlockEntity;
import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.mana.capability.ManaCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class WardManaService {
    private WardManaService() {
    }

    public static boolean consume(Level level, BlockPos pos, WardFieldSource sigil, int amount) {
        return consume(level, pos, sigil, amount,
                sigil instanceof WardProjection || SelariumCommonConfig.WARDS_CAN_USE_OWNER_MANA.get());
    }

    public static boolean consumeRitual(Level level, BlockPos pos, WardFieldSource sigil, int amount) {
        return consume(level, pos, sigil, amount, true);
    }

    private static boolean consume(Level level, BlockPos pos, WardFieldSource sigil, int amount, boolean useOwner) {
        if (amount <= 0) {
            return true;
        }

        if (available(level, pos, sigil, useOwner) < amount) {
            return false;
        }

        int remaining = amount;
        remaining -= sigil.extractInternalMana(remaining);

        for (Direction direction : Direction.values()) {
            if (remaining <= 0) {
                return true;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos.relative(direction));
            if (blockEntity instanceof ManaTankBlockEntity tank) {
                remaining -= tank.extractMana(remaining, false);
            }
        }

        if (remaining > 0 && useOwner && level instanceof ServerLevel serverLevel && sigil.getOwner() != null) {
            ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(sigil.getOwner());
            if (owner != null) {
                int toDrain = remaining;
                remaining -= ManaCapability.getMana(owner).map(mana -> mana.drainMana(toDrain, true)).orElse(0);
            }
        }

        return remaining <= 0;
    }

    private static int available(Level level, BlockPos pos, WardFieldSource sigil, boolean useOwner) {
        int available = sigil.getInternalManaBuffer();

        for (Direction direction : Direction.values()) {
            BlockEntity blockEntity = level.getBlockEntity(pos.relative(direction));
            if (blockEntity instanceof ManaTankBlockEntity tank) {
                available += tank.getStoredMana();
            }
        }

        if (useOwner && level instanceof ServerLevel serverLevel && sigil.getOwner() != null) {
            ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(sigil.getOwner());
            if (owner != null) {
                available += ManaCapability.getMana(owner).map(mana -> mana.getCurrentMana()).orElse(0);
            }
        }

        return available;
    }
}

