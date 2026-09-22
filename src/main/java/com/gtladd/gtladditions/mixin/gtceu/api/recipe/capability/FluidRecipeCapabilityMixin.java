package com.gtladd.gtladditions.mixin.gtceu.api.recipe.capability;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapFluidTagIngredient;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.gtladd.gtladditions.api.recipe.ingredient.MapIngredientVariantHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@Mixin(FluidRecipeCapability.class)
public class FluidRecipeCapabilityMixin {

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public List<AbstractMapIngredient> convertToMapIngredient(Object obj) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>(1);
        if (obj instanceof FluidIngredient ingredient) {
            for (var value : ingredient.values) {
                if (value instanceof FluidIngredient.TagValue tagValue)
                    ingredients.add(new MapFluidTagIngredient(tagValue.getTag()));
                else for (var fluid : value.getFluids())
                    ingredients.add(new MapFluidIngredient(
                            FluidStack.create(fluid, ingredient.getAmount(), ingredient.getNbt())));
            }
        } else if (obj instanceof FluidStack stack) {
            List<AbstractMapIngredient> list = new ObjectArrayList<>();
            if (stack.getFluid() instanceof MapIngredientVariantHolder holder) {
                var l = holder.variants(FluidRecipeCapability.CAP);
                if (l == null) {
                    var c = new ObjectArrayList<AbstractMapIngredient>(4);
                    c.add(new MapFluidIngredient(stack));
                    stack.getFluid().builtInRegistryHolder().tags().forEach(tag -> c.add(new MapFluidTagIngredient(tag)));
                    holder.setVariants(c);
                    l = c;
                }
                list = l;
            }
            ingredients = list;
        }

        return ingredients;
    }
}
