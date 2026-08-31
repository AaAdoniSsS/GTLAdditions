package com.gtladd.gtladditions.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableComputationContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.gtladd.gtladditions.common.machine.CloudOpticalComputationMonitorMachine;
import com.gtladd.gtladditions.common.machine.hatch.CloudOpticalComputationHatchMachine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CloudOpticalComputationContainer extends NotifiableComputationContainer {

    public int lastResearch;

    public CloudOpticalComputationContainer(MetaMachine machine, IO handlerIO, boolean transmitter) {
        super(machine, handlerIO, transmitter);
    }

    private UUID getUUID() {
        return this.machine instanceof CloudOpticalComputationHatchMachine h ? h.getPlayer() : null;
    }

    @Override
    public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left, @Nullable String slotName, boolean simulate) {
        int sum = left.stream().reduce(0, Integer::sum);
        if (io == IO.IN) {
            UUID uuid = getUUID();
            int availableCWU = (int) CloudOpticalComputationMonitorMachine.requestCWU(uuid, Integer.MAX_VALUE, true);
            if (availableCWU >= sum) {
                if (recipe.data.getBoolean("duration_is_total_cwu")) {
                    int drawn = simulate ? availableCWU : (int) CloudOpticalComputationMonitorMachine.requestCWU(uuid, availableCWU, false);
                    if (!simulate) {
                        if (this.machine instanceof IRecipeLogicMachine rlm) {
                            rlm.getRecipeLogic().setProgress(rlm.getRecipeLogic().getProgress() - 1 + drawn);
                        } else if (this.machine instanceof IMultiPart multiPart) {
                            for (var c : multiPart.getControllers()) {
                                if (c instanceof IRecipeLogicMachine rlm) {
                                    rlm.getRecipeLogic().setProgress(rlm.getRecipeLogic().getProgress() - 1 + drawn);
                                }
                            }
                        }
                    }
                    sum -= (lastResearch = drawn);
                } else {
                    sum -= (int) CloudOpticalComputationMonitorMachine.requestCWU(uuid, sum, simulate);;
                }
            }
        }

        return sum <= 0 ? null : Collections.singletonList(sum);
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider> seen) {
        if (this.handlerIO == IO.IN && !this.isTransmitter()) {
            return (int) CloudOpticalComputationMonitorMachine.requestCWU(getUUID(), cwut, simulate);
        }
        return 0;
    }

    @Override
    public boolean canBridge() {
        if (this.machine instanceof IOpticalComputationProvider provider) {
            return provider.canBridge();
        } else {
            if (this.machine instanceof IMultiPart part) {
                for (var c : part.getControllers()) {
                    if (c instanceof IOpticalComputationProvider provider) return provider.canBridge();
                }
            }
        }
        return false;
    }
}
