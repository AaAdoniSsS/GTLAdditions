package com.gtladd.gtladditions.mixin.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.Branch;
import com.gregtechceu.gtceu.api.recipe.lookup.GTRecipeLookup;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(GTRecipeLookup.class)
public interface IGTRecipeLookupInvoker {

    @Invoker(value = "recurseIngredientTreeAdd", remap = false)
    boolean useIngredientTreeAdd(@NotNull GTRecipe recipe,
                                 @NotNull List<List<AbstractMapIngredient>> ingredients,
                                 @NotNull Branch branchMap, int index, int count);
}
