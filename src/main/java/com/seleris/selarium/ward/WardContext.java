package com.seleris.selarium.ward;

import com.seleris.selarium.ward.WardFieldSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public record WardContext(ServerLevel level, BlockPos pos, BlockState state, WardFieldSource sigil) {
}

