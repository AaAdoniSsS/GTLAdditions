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

@Mixin(MEDualHatchStockPartMachine.class)
public abstract class MEDualHatchStockPartMachineMixin extends MEBusPartMachine {

    @Shadow(remap = false)
    public ExportOnlyAEItemList aeItemHandler;
    @Shadow(remap = false)
    public ExportOnlyAEFluidList aeFluidHandler;

    public MEDualHatchStockPartMachineMixin(IMachineBlockEntity holder, IO io, Object... args) {
        super(holder, io, args);
    }

    @Shadow(remap = false)
    private void markMEStockChanged() {}

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
