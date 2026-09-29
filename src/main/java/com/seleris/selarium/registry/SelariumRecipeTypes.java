package com.seleris.selarium.registry;

import com.seleris.selarium.Selarium;
import com.seleris.selarium.grinder.recipe.ArcaneGrindingRecipe;
import com.seleris.selarium.grinder.recipe.ArcaneGrindingRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class SelariumRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, Selarium.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Selarium.MOD_ID);

    public static final RegistryObject<RecipeType<ArcaneGrindingRecipe>> ARCANE_GRINDING_TYPE = RECIPE_TYPES.register("arcane_grinding",
            () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Selarium.MOD_ID + ":arcane_grinding";
                }
            });

    public static final RegistryObject<RecipeSerializer<ArcaneGrindingRecipe>> ARCANE_GRINDING_SERIALIZER = RECIPE_SERIALIZERS.register("arcane_grinding",
            ArcaneGrindingRecipeSerializer::new);

    private SelariumRecipeTypes() {
    }

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
