package com.seleris.selarium.mana.capability;

import com.seleris.selarium.mana.IPlayerMana;
import com.seleris.selarium.mana.PlayerMana;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

public class PlayerManaProvider implements ICapabilitySerializable<CompoundTag> {
    private final PlayerMana mana = new PlayerMana();
    private final LazyOptional<IPlayerMana> optional = LazyOptional.of(() -> mana);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ManaCapability.PLAYER_MANA) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return mana.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        mana.deserializeNBT(tag);
    }

    public void invalidate() {
        optional.invalidate();
    }
}

