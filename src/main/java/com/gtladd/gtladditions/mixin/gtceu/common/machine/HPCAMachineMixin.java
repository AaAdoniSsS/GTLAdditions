package com.gtladd.gtladditions.mixin.gtceu.common.machine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.HPCAMachine;

import com.gtladd.gtladditions.api.machine.trait.IOpticalComputationProvider;
import com.gtladd.gtladditions.utils.MathUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HPCAMachine.class)
public abstract class HPCAMachineMixin extends WorkableElectricMultiblockMachine implements IOpticalComputationProvider {

    @Shadow(remap = false)
    @Final
    private HPCAMachine.HPCAGridHandler hpcaHandler;
    @Shadow(remap = false)
    private boolean hasNotEnoughEnergy;

    public HPCAMachineMixin(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public long requestCWU(long cwu, boolean simulate) {
        return this.isActive() && this.isWorkingEnabled() && !this.hasNotEnoughEnergy ? this.hpcaHandler.allocateCWUt(MathUtil.INSTANCE.getSafeToInt(cwu), simulate) : 0;
    }

    @Override
    public long getMaxCWU() {
        return this.isActive() && this.isWorkingEnabled() ? this.hpcaHandler.getMaxCWUt() : 0;
    }

    @Override
    public boolean canBridge() {
        return !this.isFormed() || this.hpcaHandler.hasHPCABridge();
    }
}
