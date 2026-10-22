package com.gtladd.gtladditions.mixin.gtlcore.machine;

import org.gtlcore.gtlcore.api.recipe.IAdvancedContentModifier;
import org.gtlcore.gtlcore.api.recipe.RecipeMultiplierTracker;
import org.gtlcore.gtlcore.common.machine.multiblock.electric.AdvancedInfiniteDrillMachine;
import org.gtlcore.gtlcore.common.machine.trait.AdvancedInfiniteDrillLogic;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.gtladd.gtladditions.common.machine.hatch.OreProcessorHatch;
import com.gtladd.gtladditions.utils.DrillOverclock;
import com.gtladd.gtladditions.utils.SpectralTuning;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AdvancedInfiniteDrillLogic.class)
public abstract class AdvancedInfiniteDrillLogicMixin {

    @ModifyArg(method = "getFluidDrillRecipe",
               at = @At(value = "INVOKE",
                        target = "Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;EUt(J)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;"),
               index = 0,
               remap = false)
    private long gtladditions$applyEnergy(long eut) {
        AdvancedInfiniteDrillMachine machine = ((AdvancedInfiniteDrillLogic) (Object) this).getMachine();
        for (var part : machine.getParts()) {
            if (part instanceof OreProcessorHatch hatch) {
                return (long) (eut * SpectralTuning.energyFactor(hatch.getSecondTier()));
            }
        }
        return eut;
    }

    @Inject(method = "getFluidDrillRecipe", at = @At("RETURN"), cancellable = true, remap = false)
    private void gtladditions$overclock(CallbackInfoReturnable<GTRecipe> cir) {
        GTRecipe recipe = cir.getReturnValue();
        if (recipe == null) return;
        AdvancedInfiniteDrillMachine machine = ((AdvancedInfiniteDrillLogic) (Object) this).getMachine();
        for (var part : machine.getParts()) {
            if (part instanceof OreProcessorHatch hatch) {
                RecipeMultiplierTracker.captureReduction(machine, recipe,
                        SpectralTuning.energyFactor(hatch.getSecondTier()), 1.0D);
                long factor = DrillOverclock.factor(DrillOverclock.level(machine, hatch, machine.getRate()));
                if (factor > 1L) {
                    cir.setReturnValue(recipe.copy(IAdvancedContentModifier.preciseMultiplier(factor), false));
                }
                return;
            }
        }
    }
}
