package com.gtladd.gtladditions.mixin.gtlcore.integration;

import org.gtlcore.gtlcore.integration.ae2.handler.MERecipePatternHelper;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.gtladd.gtladditions.common.machine.multiblock.controller.TimeSpaceDistorter;
import com.gtladd.gtladditions.common.machine.multiblock.controller.df.RecursiveReverseForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(MERecipePatternHelper.class)
public class MERecipePatternHelperMixin {

    @Inject(method = "getActiveRecipeTypes", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gtladditions$blockExcludedHosts(List<IMultiController> controllers,
                                                        CallbackInfoReturnable<Set<GTRecipeType>> cir) {
        for (IMultiController controller : controllers) {
            var self = controller.self();
            if (self instanceof TimeSpaceDistorter || self instanceof RecursiveReverseForge) {
                cir.setReturnValue(Set.of());
                return;
            }
        }
    }
}
