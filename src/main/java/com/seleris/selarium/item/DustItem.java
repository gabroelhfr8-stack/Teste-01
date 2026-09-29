package com.seleris.selarium.item;

import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.dust.DustDefinition;
import com.seleris.selarium.dust.DustType;
import com.seleris.selarium.progression.SelariumAdvancements;
import com.seleris.selarium.registry.SelariumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DustItem extends Item {
    private final DustDefinition definition;

    public DustItem(DustDefinition definition, Properties properties) {
        super(properties);
        this.definition = definition;
    }

    public DustDefinition getDefinition() {
        return definition;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (definition.type() != DustType.ARCANE) {
            return super.useOn(context);
        }

        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Player player = context.getPlayer();

        if (level.getBlockState(clickedPos).is(SelariumBlocks.ARCANE_SIGIL.get())) {
            return InteractionResult.PASS;
        }

        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }

        BlockState supportState = level.getBlockState(clickedPos);
        if (!supportState.isFaceSturdy(level, clickedPos, Direction.UP)) {
            if (player != null && !level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.selarium.sigil.invalid_ground"), true);
            }
            return InteractionResult.FAIL;
        }

        BlockPos sigilPos = clickedPos.above();
        if (!level.getBlockState(sigilPos).canBeReplaced()) {
            if (player != null && !level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.selarium.sigil.already_exists"), true);
            }
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            level.setBlock(sigilPos, SelariumBlocks.ARCANE_SIGIL.get().defaultBlockState(), Block.UPDATE_ALL);
            if (level.getBlockEntity(sigilPos) instanceof ArcaneSigilBlockEntity sigil) {
                if (player != null) {
                    sigil.setOwner(player.getUUID());
                }
                sigil.addComponent(definition, false);
                sigil.setCreatedGameTime(level.getGameTime());
            }

            if (player != null && !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }

            if (player != null) {
                player.displayClientMessage(Component.translatable("message.selarium.sigil.created"), true);
                SelariumAdvancements.grant(player, "sigil");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}

