package com.seleris.selarium.grinder.recipe;

import com.google.gson.JsonObject;
import com.seleris.selarium.config.SelariumCommonConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ArcaneGrindingRecipeSerializer implements RecipeSerializer<ArcaneGrindingRecipe> {
    @Override
    public ArcaneGrindingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
        Optional<Ingredient> reagent = json.has("reagent")
                ? Optional.of(Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "reagent")))
                : Optional.empty();
        ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
        int processingTime = Math.max(1, GsonHelper.getAsInt(json, "processingTime", SelariumCommonConfig.ARCANE_GRINDER_DEFAULT_PROCESSING_TIME.get()));
        boolean useConfiguredCrystalOutput = GsonHelper.getAsBoolean(json, "useConfiguredCrystalOutput", false);
        return new ArcaneGrindingRecipe(recipeId, input, reagent, result, processingTime, useConfiguredCrystalOutput);
    }

    @Nullable
    @Override
    public ArcaneGrindingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        Ingredient input = Ingredient.fromNetwork(buffer);
        Optional<Ingredient> reagent = buffer.readBoolean() ? Optional.of(Ingredient.fromNetwork(buffer)) : Optional.empty();
        ItemStack result = buffer.readItem();
        int processingTime = buffer.readVarInt();
        boolean useConfiguredCrystalOutput = buffer.readBoolean();
        return new ArcaneGrindingRecipe(recipeId, input, reagent, result, processingTime, useConfiguredCrystalOutput);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, ArcaneGrindingRecipe recipe) {
        recipe.input().toNetwork(buffer);
        buffer.writeBoolean(recipe.reagent().isPresent());
        recipe.reagent().ifPresent(ingredient -> ingredient.toNetwork(buffer));
        buffer.writeItem(recipe.getResultItem(null));
        buffer.writeVarInt(recipe.processingTime());
        buffer.writeBoolean(recipe.useConfiguredCrystalOutput());
    }
}
