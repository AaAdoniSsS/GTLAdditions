package com.gtladd.gtladditions.mixin.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.GTRecipeLookup;

import com.gtladd.gtladditions.api.recipe.lookup.IBranchAddition;
import com.gtladd.gtladditions.api.recipe.lookup.RecipeTreeGeneration;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = GTRecipeLookup.class, priority = 800)
public class GTRecipeLookupMixin {

    @Inject(method = "fromRecipe", at = @At("HEAD"), remap = false, cancellable = true)
    protected void fromRecipe(GTRecipe r, CallbackInfoReturnable<List<List<AbstractMapIngredient>>> cir) {
        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>(r.inputs.size());
        r.inputs.forEach((cap, contents) -> {
            if (cap.isRecipeSearchFilter() && !contents.isEmpty()) {
                for (var ingredient : cap.compressIngredients(contents.stream().map(Content::getContent).toList())) {
                    list.add(cap.convertToMapIngredient(ingredient));
                }
            }
        });
        cir.setReturnValue(list);
    }

    @Inject(method = "removeAllRecipes", at = @At("HEAD"), remap = false)
    private void clearBranchCache(CallbackInfo ci) {
        ((IBranchAddition) ((GTRecipeLookup) (Object) this).getLookup()).setMinDepth(0);
        RecipeTreeGeneration.bump();
    }
}
