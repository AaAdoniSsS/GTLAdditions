package com.gtladd.gtladditions.mixin.gtceu.api.recipe.capability;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.*;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.core.mixins.IngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.TagValueAccessor;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IntersectionIngredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import com.gtladd.gtladditions.api.recipe.ingredient.MapIngredientVariantHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@Mixin(ItemRecipeCapability.class)
public class ItemRecipeCapabilityMixin {

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public List<AbstractMapIngredient> convertToMapIngredient(Object obj) {
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
        } else if (obj instanceof ItemStack stack) {
            List<AbstractMapIngredient> list = new ObjectArrayList<>();
            if (stack.getItem() instanceof MapIngredientVariantHolder holder) {
                var l = holder.variants(ItemRecipeCapability.CAP);
                if (l == null) {
                    var c = new ObjectArrayList<AbstractMapIngredient>(4);
                    c.add(new MapItemStackIngredient(stack));
                    stack.getTags().forEach(tag -> c.add(new MapItemTagIngredient(tag)));
                    holder.setVariants(c);
                    l = c;
                }
                list.addAll(l);
            }
            if (stack.hasTag()) {
                if (GTItems.INTEGRATED_CIRCUIT.isIn(stack)) {
                    list.add(new MapItemStackNBTIngredient(stack, IntCircuitIngredient.circuitInput(IntCircuitBehaviour.getCircuitConfiguration(stack))));
                } else {
                    list.add(new MapItemStackNBTIngredient(stack, StrictNBTIngredient.of(stack)));
                }
            }
            ingredients = list;
        }
        return ingredients;
    }
}
