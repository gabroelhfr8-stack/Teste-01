package com.seleris.selarium.blockentity;

import com.seleris.selarium.block.ArcaneGrinderBlock;
import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.grinder.menu.ArcaneGrinderMenu;
import com.seleris.selarium.grinder.recipe.ArcaneGrindingRecipe;
import com.seleris.selarium.registry.SelariumBlockEntities;
import com.seleris.selarium.registry.SelariumRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.NonNullList;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ArcaneGrinderBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int REAGENT_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int SLOT_COUNT = 3;

    private static final int[] INPUT_SLOTS = new int[]{INPUT_SLOT, REAGENT_SLOT};
    private static final int[] OUTPUT_SLOTS = new int[]{OUTPUT_SLOT};

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                progress = Math.max(0, value);
            } else if (index == 1) {
                maxProgress = Math.max(0, value);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    private int progress;
    private int maxProgress;
    @Nullable
    private ResourceLocation activeRecipeId;

    public ArcaneGrinderBlockEntity(BlockPos pos, BlockState state) {
        super(SelariumBlockEntities.ARCANE_GRINDER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ArcaneGrinderBlockEntity grinder) {
        if (!SelariumCommonConfig.ARCANE_GRINDER_ENABLED.get()) {
            grinder.resetProgress();
            setLit(level, pos, state, false);
            return;
        }

        Optional<ArcaneGrindingRecipe> recipe = grinder.findRecipe(level);
        if (recipe.isEmpty() || !grinder.canCraft(recipe.get())) {
            grinder.resetProgress();
            setLit(level, pos, state, false);
            return;
        }

        ArcaneGrindingRecipe currentRecipe = recipe.get();
        if (!currentRecipe.getId().equals(grinder.activeRecipeId)) {
            grinder.progress = 0;
            grinder.activeRecipeId = currentRecipe.getId();
        }

        grinder.maxProgress = currentRecipe.processingTime();
        grinder.progress++;
        setLit(level, pos, state, true);
        if (grinder.progress >= grinder.maxProgress) {
            grinder.craft(currentRecipe);
            grinder.progress = 0;
            grinder.activeRecipeId = null;
        }
        grinder.setChanged();
    }

    /** Mirrors the "working" flag into the block state so it drives light, model and particles. */
    private static void setLit(Level level, BlockPos pos, BlockState state, boolean lit) {
        if (state.hasProperty(ArcaneGrinderBlock.LIT) && state.getValue(ArcaneGrinderBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(ArcaneGrinderBlock.LIT, lit), 3);
        }
    }

    public int getProgress() {
        return progress;
    }

    public boolean canBoostProgress() {
        return progress > 0 && maxProgress > 1 && progress < maxProgress - 1;
    }

    public boolean boostProgress(int amount) {
        if (amount <= 0 || !canBoostProgress()) return false;
        progress = Math.min(maxProgress - 1, progress + amount);
        setChanged();
        return true;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public Optional<ResourceLocation> getActiveRecipeId() {
        return Optional.ofNullable(activeRecipeId);
    }

    public Optional<ResourceLocation> getDetectedRecipeId() {
        if (level == null) {
            return Optional.empty();
        }
        return findRecipe(level).map(ArcaneGrindingRecipe::getId);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.selarium.arcane_grinder");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ArcaneGrinderMenu(containerId, playerInventory, this, dataAccess, worldPosition);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= items.size()) {
            return ItemStack.EMPTY;
        }
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            if (slot == INPUT_SLOT || slot == REAGENT_SLOT) {
                resetProgress();
            }
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= items.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = ContainerHelper.takeItem(items, slot);
        if (!removed.isEmpty() && (slot == INPUT_SLOT || slot == REAGENT_SLOT)) {
            resetProgress();
        }
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= items.size()) {
            return;
        }

        ItemStack previous = items.get(slot);
        ItemStack stored = stack.copy();
        if (stored.getCount() > getMaxStackSize()) {
            stored.setCount(getMaxStackSize());
        }
        items.set(slot, stored);

        if ((slot == INPUT_SLOT || slot == REAGENT_SLOT) && !ItemStack.matches(previous, stored)) {
            resetProgress();
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == INPUT_SLOT || slot == REAGENT_SLOT;
    }

    @Override
    public void clearContent() {
        items.clear();
        resetProgress();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? OUTPUT_SLOTS : INPUT_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return SelariumCommonConfig.ARCANE_GRINDER_ALLOW_AUTOMATION.get() && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return SelariumCommonConfig.ARCANE_GRINDER_ALLOW_AUTOMATION.get() && slot == OUTPUT_SLOT;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("Progress", Math.max(0, progress));
        tag.putInt("MaxProgress", Math.max(0, maxProgress));
        if (activeRecipeId != null) {
            tag.putString("ActiveRecipe", activeRecipeId.toString());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        if (tag.contains("Items", Tag.TAG_LIST)) {
            ContainerHelper.loadAllItems(tag, items);
        } else {
            tag.put("Items", new ListTag());
        }
        progress = Math.max(0, tag.getInt("Progress"));
        maxProgress = Math.max(0, tag.getInt("MaxProgress"));
        activeRecipeId = null;
        if (tag.contains("ActiveRecipe", Tag.TAG_STRING)) {
            activeRecipeId = ResourceLocation.tryParse(tag.getString("ActiveRecipe"));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    private Optional<ArcaneGrindingRecipe> findRecipe(Level level) {
        return level.getRecipeManager().getRecipeFor(SelariumRecipeTypes.ARCANE_GRINDING_TYPE.get(), this, level);
    }

    private boolean canCraft(ArcaneGrindingRecipe recipe) {
        ItemStack result = recipe.getResultItem(level == null ? null : level.registryAccess());
        if (result.isEmpty()) {
            return false;
        }

        ItemStack output = getItem(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }

        return ItemStack.isSameItemSameTags(output, result) && output.getCount() + result.getCount() <= Math.min(output.getMaxStackSize(), getMaxStackSize());
    }

    private void craft(ArcaneGrindingRecipe recipe) {
        if (!canCraft(recipe)) {
            return;
        }

        ItemStack result = recipe.getResultItem(level == null ? null : level.registryAccess()).copy();
        removeItem(INPUT_SLOT, 1);
        if (recipe.hasReagent()) {
            removeItem(REAGENT_SLOT, 1);
        }

        ItemStack output = getItem(OUTPUT_SLOT);
        if (output.isEmpty()) {
            items.set(OUTPUT_SLOT, result);
        } else {
            output.grow(result.getCount());
        }
        setChanged();
    }

    private void resetProgress() {
        if (progress != 0 || maxProgress != 0 || activeRecipeId != null) {
            progress = 0;
            maxProgress = 0;
            activeRecipeId = null;
            setChanged();
        }
    }
}
