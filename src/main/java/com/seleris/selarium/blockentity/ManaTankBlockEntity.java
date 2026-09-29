package com.seleris.selarium.blockentity;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.mana.IManaStorage;
import com.seleris.selarium.registry.SelariumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ManaTankBlockEntity extends BlockEntity implements IManaStorage {
    private int storedMana;
    private int lastSyncedFillStep = -1;

    public ManaTankBlockEntity(BlockPos pos, BlockState state) {
        super(SelariumBlockEntities.MANA_TANK.get(), pos, state);
    }

    @Override
    public int receiveMana(int amount, boolean simulate) {
        int capacity = getManaCapacity();
        if (amount <= 0 || storedMana >= capacity) {
            return 0;
        }

        int accepted = Math.min(amount, capacity - storedMana);
        if (!simulate) {
            storedMana += accepted;
            setChangedAndSync();
        }
        return accepted;
    }

    @Override
    public int extractMana(int amount, boolean simulate) {
        if (amount <= 0 || storedMana <= 0) {
            return 0;
        }

        int extracted = Math.min(amount, storedMana);
        if (!simulate) {
            storedMana -= extracted;
            setChangedAndSync();
        }
        return extracted;
    }

    @Override
    public int getStoredMana() {
        return Math.max(0, Math.min(getManaCapacity(), storedMana));
    }

    @Override
    public int getManaCapacity() {
        return Math.max(1, SelariumCommonConfig.MANA_TANK_CAPACITY.get());
    }

    public int getVisualFillStep() {
        int stored = getStoredMana();
        if (stored <= 0) {
            return 0;
        }
        if (stored >= getManaCapacity()) {
            return 20;
        }
        return Math.max(1, Math.min(19, (int) Math.ceil(stored * 20.0D / getManaCapacity())));
    }

    public float getVisualFillRatio() {
        return getVisualFillStep() / 20.0F;
    }

    public void setStoredMana(int storedMana) {
        this.storedMana = Math.max(0, Math.min(getManaCapacity(), storedMana));
        setChangedAndSync();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("StoredMana", getStoredMana());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        storedMana = Math.max(0, Math.min(getManaCapacity(), tag.getInt("StoredMana")));
        lastSyncedFillStep = getVisualFillStep();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            int fillStep = getVisualFillStep();
            if (fillStep != lastSyncedFillStep) {
                lastSyncedFillStep = fillStep;
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }
}
