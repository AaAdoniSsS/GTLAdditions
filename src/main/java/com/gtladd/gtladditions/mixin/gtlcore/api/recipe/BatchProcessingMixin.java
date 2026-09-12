package com.gtladd.gtladditions.mixin.gtlcore.api.recipe;

import org.gtlcore.gtlcore.api.recipe.BatchProcessing;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.gtladd.gtladditions.api.recipe.GTLAddBatchDisabledDefinitions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BatchProcessing.class)
public abstract class BatchProcessingMixin {

    @Inject(method = "isBatchDisabledDefinition", at = @At("RETURN"), cancellable = true, remap = false)
    private static void gtladditions$addOwnDisabledDefinitions(MetaMachine machine,
                                                               CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(cir.getReturnValue())) {
            return;
        }
        var definition = machine.getDefinition();
        if (definition != null && GTLAddBatchDisabledDefinitions.isDisabled(definition.getId().toString())) {
            cir.setReturnValue(true);
        }
    }
}
