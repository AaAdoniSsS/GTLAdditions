package com.gtladd.gtladditions.mixin.gtceu.api.recipe.capability;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.*;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import com.gtladd.gtladditions.api.recipe.ingredient.MapIngredientVariantHolder;
import com.gtladd.gtladditions.api.recipe.ingredient.MapIngredientVariants;
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
        if (obj instanceof Ingredient) {
            return MapIngredientVariants.of(ItemRecipeCapability.CAP, obj);
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
