package com.gtladd.gtladditions.mixin.gtceu.api.recipe.capability;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapFluidTagIngredient;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.gtladd.gtladditions.api.recipe.ingredient.MapIngredientVariantHolder;
import com.gtladd.gtladditions.api.recipe.ingredient.MapIngredientVariants;
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
        if (obj instanceof FluidIngredient) {
            return MapIngredientVariants.of(FluidRecipeCapability.CAP, obj);
        } else if (obj instanceof FluidStack stack) {
            if (stack.getFluid() == null || stack.getAmount() <= 0L) return List.of();
            List<AbstractMapIngredient> list = new ObjectArrayList<>();
            if (stack.getFluid() instanceof MapIngredientVariantHolder holder) {
                var cached = holder.variants(FluidRecipeCapability.CAP);
                if (cached == null) {
                    FluidStack representative = FluidStack.create(stack.getFluid(), 1);
                    var built = new ObjectArrayList<AbstractMapIngredient>(4);
                    built.add(new MapFluidIngredient(representative));
                    stack.getFluid().builtInRegistryHolder().tags().forEach(tag -> built.add(new MapFluidTagIngredient(tag)));
                    cached = MapIngredientVariants.store(holder, List.copyOf(built));
                }
                list.addAll(cached);
            } else {
                list.add(new MapFluidIngredient(stack));
            }
            if (stack.getTag() != null) {
                list.add(new MapFluidIngredient(stack));
            }
            ingredients = list;
        }
        return ingredients;
    }
}
