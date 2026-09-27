package com.gtladd.gtladditions.mixin.gtceu.common;

import com.gregtechceu.gtceu.common.data.GTRecipes;

import net.minecraft.data.recipes.FinishedRecipe;

import com.gtladd.gtladditions.api.manage.GTRecipeBuilderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(GTRecipes.class)
public class GTRecipesMixin {

    @Inject(method = "recipeAddition", at = @At("HEAD"), remap = false)
    private static void recipeAddition(Consumer<FinishedRecipe> originalConsumer, CallbackInfo ci) {
        GTRecipeBuilderManager.init();
    }
}
