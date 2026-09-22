package com.gtladd.gtladditions.mixin.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(IntCircuitIngredient.class)
public class IntCircuitIngredientMixin {

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public IntCircuitIngredient copy() {
        return (IntCircuitIngredient) (Object) this;
    }
}
