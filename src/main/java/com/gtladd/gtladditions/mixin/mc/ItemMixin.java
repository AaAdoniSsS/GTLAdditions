package com.gtladd.gtladditions.mixin.mc;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;

import net.minecraft.world.item.Item;

import com.gtladd.gtladditions.api.recipe.ingredient.MapIngredientVariantHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(Item.class)
public abstract class ItemMixin implements MapIngredientVariantHolder {

    @Unique
    private List<AbstractMapIngredient> variants;

    @Override
    public @Nullable List<AbstractMapIngredient> variants(@NotNull RecipeCapability<?> cap) {
        return cap == ItemRecipeCapability.CAP ? variants : null;
    }

    @Override
    public void setVariants(@NotNull List<AbstractMapIngredient> variants) {
        this.variants = variants;
    }
}
