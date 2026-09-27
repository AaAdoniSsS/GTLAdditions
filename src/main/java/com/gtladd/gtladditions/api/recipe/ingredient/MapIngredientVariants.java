package com.gtladd.gtladditions.api.recipe.ingredient;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.*;
import com.gregtechceu.gtceu.core.mixins.IngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.TagValueAccessor;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IntersectionIngredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public final class MapIngredientVariants {

    public static List<AbstractMapIngredient> of(RecipeCapability<?> cap, Object content) {
        var key = variantKey(content);
        if (key instanceof MapIngredientVariantHolder holder) {
            var cached = holder.variants(cap);
            if (cached != null) return cached;
            var computed = convertToMapIngredient(content);
            holder.setVariants(computed);
            return computed;
        }
        return convertToMapIngredient(content);
    }

    static Object variantKey(Object content) {
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

    static List<AbstractMapIngredient> convertToMapIngredient(Object obj) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>(1);
        if (obj instanceof Ingredient ingredient) {
            switch (ingredient) {
                case StrictNBTIngredient nbt -> {
                    if (ingredient instanceof IntCircuitIngredient cir) {
                        ingredients.add(new MapItemStackNBTIngredient(cir.kjs$getFirst(), cir));
                    } else ingredients.addAll(MapItemStackNBTIngredient.from(nbt));
                }
                case PartialNBTIngredient nbt -> ingredients.addAll(MapItemStackPartialNBTIngredient.from(nbt));
                case SizedIngredient sized -> {
                    switch (sized.getInner()) {
                        case StrictNBTIngredient nbt -> {
                            if (sized.getInner() instanceof IntCircuitIngredient cir) {
                                ingredients.add(new MapItemStackNBTIngredient(cir.kjs$getFirst(), cir));
                            } else ingredients.addAll(MapItemStackNBTIngredient.from(nbt));
                        }
                        case PartialNBTIngredient nbt -> ingredients.addAll(MapItemStackPartialNBTIngredient.from(nbt));
                        case IntersectionIngredient intersection -> ingredients.add(new MapIntersectionIngredient(intersection));
                        case null, default -> {
                            for (var value : ((IngredientAccessor) sized.getInner()).getValues()) {
                                if (value instanceof Ingredient.TagValue tagValue)
                                    ingredients.add(new MapItemTagIngredient(((TagValueAccessor) tagValue).getTag()));
                                else for (var stack : value.getItems()) {
                                    ingredients.add(new MapItemStackIngredient(stack, sized.getInner()));
                                }
                            }
                        }
                    }
                }
                case IntProviderIngredient intProvider -> {
                    switch (intProvider.getInner()) {
                        case StrictNBTIngredient nbt -> {
                            if (intProvider.getInner() instanceof IntCircuitIngredient cir) {
                                ingredients.add(new MapItemStackNBTIngredient(cir.kjs$getFirst(), cir));
                            } else ingredients.addAll(MapItemStackNBTIngredient.from(nbt));
                        }
                        case PartialNBTIngredient nbt -> ingredients.addAll(MapItemStackPartialNBTIngredient.from(nbt));
                        case IntersectionIngredient intersection -> ingredients.add(new MapIntersectionIngredient(intersection));
                        case null, default -> {
                            for (var value : ((IngredientAccessor) intProvider.getInner()).getValues()) {
                                if (value instanceof Ingredient.TagValue tagValue)
                                    ingredients.add(new MapItemTagIngredient(((TagValueAccessor) tagValue).getTag()));
                                else for (var stack : value.getItems())
                                    ingredients.add(new MapItemStackIngredient(stack, intProvider.getInner()));
                            }
                        }
                    }
                }
                case IntersectionIngredient intersection -> ingredients.add(new MapIntersectionIngredient(intersection));
                default -> {
                    for (var value : ((IngredientAccessor) ingredient).getValues()) {
                        if (value instanceof Ingredient.TagValue tagValue)
                            ingredients.add(new MapItemTagIngredient(((TagValueAccessor) tagValue).getTag()));
                        else for (var stack : value.getItems())
                            ingredients.add(new MapItemStackIngredient(stack, ingredient));
                    }
                }
            }
        } else if (obj instanceof FluidIngredient ingredient) {
            for (var value : ingredient.values) {
                if (value instanceof FluidIngredient.TagValue tagValue)
                    ingredients.add(new MapFluidTagIngredient(tagValue.getTag()));
                else for (var fluid : value.getFluids())
                    ingredients.add(new MapFluidIngredient(
                            FluidStack.create(fluid, ingredient.getAmount(), ingredient.getNbt())));
            }
        }
        return ingredients;
    }
}
