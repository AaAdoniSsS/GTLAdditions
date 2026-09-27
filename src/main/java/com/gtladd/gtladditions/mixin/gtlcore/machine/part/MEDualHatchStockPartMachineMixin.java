package com.gtladd.gtladditions.mixin.gtlcore.machine.part;

import org.gtlcore.gtlcore.common.machine.multiblock.part.MEDualHatchStockPartMachine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;

import com.gtladd.gtladditions.api.ae2.MEStockSyncCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MEDualHatchStockPartMachine.class)
public abstract class MEDualHatchStockPartMachineMixin extends MEBusPartMachine {

    @Shadow(remap = false)
    public ExportOnlyAEItemList aeItemHandler;
    @Shadow(remap = false)
    public ExportOnlyAEFluidList aeFluidHandler;
    @Shadow(remap = false)
    private int autoPullMode;

    public MEDualHatchStockPartMachineMixin(IMachineBlockEntity holder, IO io, Object... args) {
        super(holder, io, args);
    }

    @Shadow(remap = false)
    private void markMEStockChanged() {}

    @Redirect(method = "autoIO",
              at = @At(value = "INVOKE",
                       target = "Lorg/gtlcore/gtlcore/common/machine/multiblock/part/MEDualHatchStockPartMachine;updateMEStatus()Z"),
              remap = false)
    public boolean autoIO(MEDualHatchStockPartMachine instance) {
        return instance.updateMEStatus() && autoPullMode == 0;
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    protected void syncME() {
        var grid = this.getMainNode().getGrid();
        if (grid == null) return;
        if (MEStockSyncCache.syncStock(grid.getStorageService(), this.aeItemHandler.getInventory(), this.aeFluidHandler.getInventory())) {
            markMEStockChanged();
        }
    }
}
