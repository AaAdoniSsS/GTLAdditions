package com.gtladd.gtladditions.mixin.gtceu.common.machine.part;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.integration.ae2.machine.MEInputHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingHatchPartMachine;

import com.gtladd.gtladditions.api.ae2.MEStockSyncCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MEStockingHatchPartMachine.class)
public class MEStockingHatchPartMachineMixin extends MEInputHatchPartMachine {

    public MEStockingHatchPartMachineMixin(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    protected void syncME() {
        var grid = this.getMainNode().getGrid();
        if (grid != null) {
            MEStockSyncCache.syncStock(grid.getStorageService(), this.aeFluidHandler.getInventory());
        }
    }
}
