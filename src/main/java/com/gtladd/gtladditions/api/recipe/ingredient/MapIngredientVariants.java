package com.gtladd.gtladditions.api.recipe.ingredient;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;

import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class MapIngredientVariants {

    @NotNull
    public static List<AbstractMapIngredient> of(@NotNull RecipeCapability<?> cap, @NotNull Object content) {
        var key = variantKey(content);
        if (key instanceof MapIngredientVariantHolder holder) {
            var cached = holder.variants(cap);
            if (cached != null) return cached;
            var computed = cap.convertToMapIngredient(content);
            holder.setVariants(computed);
            return computed;
        }
        return cap.convertToMapIngredient(content);
    }

    @NotNull
    private static Object variantKey(@NotNull Object content) {
        Ingredient inner = null;
        if (content instanceof SizedIngredient sized) {
            inner = sized.getInner();
        } else if (content instanceof IntProviderIngredient provider) {
            inner = provider.getInner();
        }
        if (inner == null) return content;
        if (inner instanceof SizedIngredient || inner instanceof IntProviderIngredient) return content;
        return inner;
    }
}
