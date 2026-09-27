package com.gtladd.gtladditions.mixin.kjs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import com.google.gson.JsonElement;
import com.gtladd.gtladditions.api.manage.GTRecipeBuilderManager;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipesEventJS.class)
public class RecipesEventJSMixin {

    @Inject(method = "post", at = @At("TAIL"), remap = false)
    public void post(RecipeManager recipeManager, Map<ResourceLocation, JsonElement> datapackRecipeMap, CallbackInfo ci) {
        GTRecipeBuilderManager.clear();
    }
}
