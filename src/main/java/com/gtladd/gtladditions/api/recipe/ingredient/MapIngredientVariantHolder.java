package com.gtladd.gtladditions.api.recipe.ingredient;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MapIngredientVariantHolder {

    @Nullable
    List<AbstractMapIngredient> variants(@NotNull RecipeCapability<?> cap);

    void setVariants(@NotNull List<AbstractMapIngredient> variants);
}
