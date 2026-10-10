package com.gtladd.gtladditions.api.machine.trait;

import org.gtlcore.gtlcore.api.machine.computation.ComputationConnections;
import org.gtlcore.gtlcore.api.machine.computation.ComputationMath;
import org.gtlcore.gtlcore.api.machine.computation.ComputationNetwork;
import org.gtlcore.gtlcore.api.machine.computation.ComputationNode;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableComputationContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.gtladd.gtladditions.common.machine.CloudOpticalComputationMonitorMachine;
import com.gtladd.gtladditions.common.machine.hatch.CloudOpticalComputationHatchMachine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CloudOpticalComputationContainer extends NotifiableComputationContainer implements ComputationNode {

    public int lastResearch;

    public CloudOpticalComputationContainer(MetaMachine machine, IO handlerIO, boolean transmitter) {
        super(machine, handlerIO, transmitter);
    }

    private UUID getUUID() {
        return this.machine instanceof CloudOpticalComputationHatchMachine h ? h.getPlayer() : null;
    }

    @Override
    public List<com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider> gtlcore$computationLinks() {
        return CloudOpticalComputationMonitorMachine.computationLinks(getUUID());
    }

    @Override
    public boolean gtlcore$computationOnline() {
        return ComputationConnections.loaded(this.machine) && getUUID() != null;
    }

    @Override
    public void gtlcore$computationTransferred(long amount) {
        lastResearch = ComputationMath.toInt(amount);
    }

    @Override
    public boolean gtlcore$requiresComputationBridge() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left, @Nullable String slotName, boolean simulate) {
        return (List<Integer>) ComputationConnections.legacyInput(
                (com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider) this, io, recipe, left, simulate);
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider> seen) {
        if (this.handlerIO == IO.IN && !this.isTransmitter()) {
            return ComputationNetwork.request((com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider) this, cwut, simulate);
        }
        return 0;
    }

    @Override
    public boolean canBridge() {
        return ComputationNetwork.canBridge(List.of((com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider) this));
    }
}
