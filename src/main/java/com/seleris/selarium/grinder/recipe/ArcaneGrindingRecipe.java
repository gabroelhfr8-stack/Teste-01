package com.seleris.selarium.grinder.recipe;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.registry.SelariumBlocks;
import com.seleris.selarium.registry.SelariumRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class ArcaneGrindingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient input;
    private final Optional<Ingredient> reagent;
    private final ItemStack result;
    private final int processingTime;
    private final boolean useConfiguredCrystalOutput;

    public ArcaneGrindingRecipe(ResourceLocation id, Ingredient input, Optional<Ingredient> reagent, ItemStack result, int processingTime, boolean useConfiguredCrystalOutput) {
        this.id = id;
        this.input = input;
        this.reagent = reagent;
        this.result = result.copy();
        this.processingTime = Math.max(1, processingTime);
        this.useConfiguredCrystalOutput = useConfiguredCrystalOutput;
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (!input.test(container.getItem(0))) {
            return false;
        }

        ItemStack reagentStack = container.getItem(1);
        return reagent.map(ingredient -> ingredient.test(reagentStack)).orElse(reagentStack.isEmpty());
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return configuredResult();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return configuredResult();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SelariumRecipeTypes.ARCANE_GRINDING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return SelariumRecipeTypes.ARCANE_GRINDING_TYPE.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(input);
        reagent.ifPresent(ingredients::add);
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(SelariumBlocks.ARCANE_GRINDER.get());
    }

    public Ingredient input() {
        return input;
    }

    public Optional<Ingredient> reagent() {
        return reagent;
    }

    public boolean hasReagent() {
        return reagent.isPresent();
    }

    public int processingTime() {
        return processingTime;
    }

    public boolean useConfiguredCrystalOutput() {
        return useConfiguredCrystalOutput;
    }

    private ItemStack configuredResult() {
        ItemStack stack = result.copy();
        if (useConfiguredCrystalOutput) {
            stack.setCount(Math.min(stack.getMaxStackSize(), Math.max(1, SelariumCommonConfig.ARCANE_CRYSTAL_TO_DUST_OUTPUT_COUNT.get())));
        }
        return stack;
    }
}
