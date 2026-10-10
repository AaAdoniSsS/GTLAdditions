package com.gtladd.gtladditions.mixin.gtlcore.machine;

import org.gtlcore.gtlcore.api.machine.computation.ComputationNetwork;
import org.gtlcore.gtlcore.api.machine.computation.ComputationSource;
import org.gtlcore.gtlcore.common.machine.multiblock.electric.ComputationProviderMachine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import com.gtladd.gtladditions.api.machine.trait.IOpticalComputationProvider;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(ComputationProviderMachine.class)
public abstract class ComputationProviderMachineMixin extends WorkableElectricMultiblockMachine implements IOpticalComputationProvider {

    public ComputationProviderMachineMixin(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public long requestCWU(long cwu, boolean simulate) {
        return ComputationNetwork.request(List.of((com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider) this), cwu, simulate);
    }

    @Override
    public long remainCWU() {
        return ((ComputationSource) (Object) this).gtlcore$availableComputation();
    }

    @Override
    public long getMaxCWU() {
        return ((ComputationSource) (Object) this).gtlcore$computationCapacity();
    }

    @Override
    public boolean canBridge() {
        return true;
    }
}
