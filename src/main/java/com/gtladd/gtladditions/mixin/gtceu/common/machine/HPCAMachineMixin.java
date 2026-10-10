package com.gtladd.gtladditions.mixin.gtceu.common.machine;

import org.gtlcore.gtlcore.api.machine.computation.ComputationNetwork;
import org.gtlcore.gtlcore.api.machine.computation.ComputationSource;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.HPCAMachine;

import com.gtladd.gtladditions.api.machine.trait.IOpticalComputationProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(HPCAMachine.class)
public abstract class HPCAMachineMixin extends WorkableElectricMultiblockMachine implements IOpticalComputationProvider {

    @Shadow(remap = false)
    @Final
    private HPCAMachine.HPCAGridHandler hpcaHandler;

    public HPCAMachineMixin(IMachineBlockEntity holder, Object... args) {
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
        return !this.isFormed() || this.hpcaHandler.hasHPCABridge();
    }
}
