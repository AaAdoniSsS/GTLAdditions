package com.gtladd.gtladditions.mixin.gtlcore.integration.jade;

import org.gtlcore.gtlcore.integration.jade.provider.RecipeMultiplierProvider;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;

import net.minecraft.nbt.CompoundTag;

import com.gtladd.gtladditions.api.machine.IGTLAddMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RecipeMultiplierProvider.class)
public abstract class RecipeMultiplierProviderMixin {

    @Redirect(
              method = "write(Lnet/minecraft/nbt/CompoundTag;Lcom/gregtechceu/gtceu/api/machine/feature/IRecipeLogicMachine;)V",
              at = @At(
                       value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/machine/feature/multiblock/IMaintenanceMachine;getDurationMultiplier()F"),
              require = 1,
              remap = false)
    private float gtladditions$ignoreMaintenanceMultiplierInExtremeMode(
                                                                        IMaintenanceMachine maintenanceMachine,
                                                                        CompoundTag data,
                                                                        IRecipeLogicMachine capability) {
        if (capability instanceof IGTLAddMachine machine && !machine.isMultipleMode()) {
            return 1.0F;
        }
        return maintenanceMachine.getDurationMultiplier();
    }
}
