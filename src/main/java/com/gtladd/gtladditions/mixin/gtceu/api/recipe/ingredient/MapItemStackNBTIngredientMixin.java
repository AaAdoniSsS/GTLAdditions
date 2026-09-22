package com.gtladd.gtladditions.mixin.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapItemStackIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapItemStackNBTIngredient;
import com.gregtechceu.gtceu.core.mixins.StrictNBTIngredientAccessor;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(MapItemStackNBTIngredient.class)
public class MapItemStackNBTIngredientMixin extends MapItemStackIngredient {

    @Shadow(remap = false)
    protected StrictNBTIngredient nbtIngredient;

    public MapItemStackNBTIngredientMixin(ItemStack stack) {
        super(stack);
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public boolean equals(Object obj) {
        if (this == obj) return true;
        else {
            if (obj instanceof MapItemStackNBTIngredient other) {
                var o = (MapItemStackNBTIngredientAccessor) other;
                if (this.stack.getItem() != ((MapItemStackIngredientAccessor) other).getStack().getItem()) return false;
                if (this.nbtIngredient != null) {
                    if (o.getNbtIngredient() != null) {
                        if (this.nbtIngredient instanceof IntCircuitIngredient ic && o.getNbtIngredient() instanceof IntCircuitIngredient) {
                            return ((IntCircuitIngredientAccessor) ic).getConfiguration() == ((IntCircuitIngredientAccessor) o.getNbtIngredient()).getConfiguration();
                        }
                        return matchItemStack(((StrictNBTIngredientAccessor) this.nbtIngredient).getStack(), ((StrictNBTIngredientAccessor) o.getNbtIngredient()).getStack());
                    }
                } else if (o.getNbtIngredient() != null) {
                    return o.getNbtIngredient().test(this.stack);
                }
            }
            return false;
        }
    }

    private static boolean matchItemStack(ItemStack stack1, ItemStack stack2) {
        if (stack1.getItem() == stack2.getItem()) {
            if (stack1.getTag() == null && stack2.getTag() == null) return true;
            if (stack1.getTag() != null && stack2.getTag() != null) return Objects.equals(stack1.getTag(), stack2.getTag());
        }
        return false;
    }
}
