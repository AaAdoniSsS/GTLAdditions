package com.gtladd.gtladditions.mixin.gtlcore.api;

import org.gtlcore.gtlcore.api.machine.trait.NotifiableCircuitItemStackHandler;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;

import net.minecraft.world.item.crafting.Ingredient;

import com.gtladd.gtladditions.mixin.gtceu.api.recipe.ingredient.IntCircuitIngredientAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.function.Function;

@Mixin(NotifiableCircuitItemStackHandler.class)
public class NotifiableCircuitItemStackHandlerMixin extends NotifiableItemStackHandler {

    public NotifiableCircuitItemStackHandlerMixin(MetaMachine machine, int slots, @NotNull IO handlerIO, @NotNull IO capabilityIO, Function<Integer, ItemStackTransfer> transferFactory) {
        super(machine, slots, handlerIO, capabilityIO, transferFactory);
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, @Nullable String slotName, boolean simulate) {
        if (!simulate) return left;

        for (var it = left.iterator(); it.hasNext();) {
            var ingredient = it.next();

            if (gTLAdditions$getCircuit(ingredient) == IntCircuitBehaviour.getCircuitConfiguration(storage.getStackInSlot(0))) {
                it.remove();
                break;
            }
        }

        return left.isEmpty() ? null : left;
    }

    @Unique
    private int gTLAdditions$getCircuit(Ingredient ingredient) {
        if (ingredient instanceof IntCircuitIngredient c) return ((IntCircuitIngredientAccessor) c).getConfiguration();
        else if (ingredient instanceof SizedIngredient s && s.getInner() instanceof IntCircuitIngredient)
            return ((IntCircuitIngredientAccessor) s.getInner()).getConfiguration();
        return -1;
    }
}
