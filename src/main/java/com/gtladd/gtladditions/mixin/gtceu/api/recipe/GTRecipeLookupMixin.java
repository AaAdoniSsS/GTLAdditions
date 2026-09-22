package com.gtladd.gtladditions.mixin.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.recipe.lookup.GTRecipeLookup;

import com.gtladd.gtladditions.api.recipe.lookup.IBranchAddition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GTRecipeLookup.class)
public class GTRecipeLookupMixin {

    @Inject(method = "removeAllRecipes", at = @At("HEAD"), remap = false)
    private void clearBranchCache(CallbackInfo ci) {
        ((IBranchAddition) ((GTRecipeLookup) (Object) this).getLookup()).setMinDepth(0);
    }
}
