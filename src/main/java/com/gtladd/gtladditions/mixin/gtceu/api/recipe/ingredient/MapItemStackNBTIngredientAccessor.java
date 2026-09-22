package com.gtladd.gtladditions.mixin.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.api.recipe.lookup.MapItemStackNBTIngredient;

import net.minecraftforge.common.crafting.StrictNBTIngredient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MapItemStackNBTIngredient.class)
public interface MapItemStackNBTIngredientAccessor {

    @Accessor(remap = false, value = "nbtIngredient")
    StrictNBTIngredient getNbtIngredient();
}
