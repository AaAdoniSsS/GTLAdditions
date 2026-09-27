package com.gtladd.gtladditions.mixin.gtceu.common.machine.part;

import org.gtlcore.gtlcore.api.machine.trait.MEPart.IModifiableSyncOffset;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.integration.ae2.machine.MEInputBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingBusPartMachine;

import com.gtladd.gtladditions.api.ae2.MEStockSyncCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = MEStockingBusPartMachine.class, priority = 800)
public abstract class MEStockingBusPartMachineMixin extends MEInputBusPartMachine implements IModifiableSyncOffset {

    @Shadow(remap = false)
    private boolean autoPull;

    @Shadow(remap = false)
    protected abstract void refreshList();

    public MEStockingBusPartMachineMixin(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public void autoIO() {
        if (this.isWorkingEnabled()) {
            if (this.getOffsetTimer() % (this.getOffset() == 0 ? 100L : this.getOffset()) == 0L) {
                if (this.autoPull) this.refreshList();
                else if (this.updateMEStatus()) {
                    this.syncME();
                    this.updateInventorySubscription();
                }
            }
        }
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    protected void syncME() {
        var grid = this.getMainNode().getGrid();
        if (grid != null) {
            MEStockSyncCache.syncStock(grid.getStorageService(), this.aeItemHandler.getInventory());
        }
    }
}
