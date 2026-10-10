package com.gtladd.gtladditions.mixin.gtceu.api.recipe.capability;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.*;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import net.minecraft.world.item.Item;
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
            if (stack.isEmpty()) return List.of();
            List<AbstractMapIngredient> list = new ObjectArrayList<>();
            Item item = stack.getItem();
            if (item instanceof MapIngredientVariantHolder holder) {
                var cached = holder.variants(ItemRecipeCapability.CAP);
                if (cached == null) {
                    ItemStack representative = new ItemStack(item, 1);
                    var built = new ObjectArrayList<AbstractMapIngredient>(4);
                    built.add(new MapItemStackIngredient(representative));
                    representative.getTags().forEach(tag -> built.add(new MapItemTagIngredient(tag)));
                    cached = MapIngredientVariants.store(holder, List.copyOf(built));
                }
                list.addAll(cached);
            } else {
                list.add(new MapItemStackIngredient(stack));
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
