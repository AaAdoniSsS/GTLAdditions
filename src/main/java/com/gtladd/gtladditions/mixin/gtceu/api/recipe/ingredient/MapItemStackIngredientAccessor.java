package com.gtladd.gtladditions.mixin.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.api.recipe.lookup.MapItemStackIngredient;

import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MapItemStackIngredient.class)
public interface MapItemStackIngredientAccessor {

    @Accessor(remap = false, value = "stack")
    ItemStack getStack();
}
