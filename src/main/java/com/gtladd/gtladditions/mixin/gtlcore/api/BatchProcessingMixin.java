package com.gtladd.gtladditions.mixin.gtlcore.api;

import org.gtlcore.gtlcore.api.recipe.BatchProcessing;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.gtladd.gtladditions.GTLAdditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BatchProcessing.class)
public abstract class BatchProcessingMixin {

    @Inject(method = "isBatchDisabledDefinition", at = @At("RETURN"), cancellable = true, remap = false)
    private static void gtladditions$addOwnDisabledDefinitions(MetaMachine machine,
                                                               CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(cir.getReturnValue())) return;
        var id = machine.getDefinition().getId();
        if (id.getNamespace().equals(GTLAdditions.MOD_ID) || id.getPath().contains("ore_processor")) {
            cir.setReturnValue(true);
        }
    }
}
