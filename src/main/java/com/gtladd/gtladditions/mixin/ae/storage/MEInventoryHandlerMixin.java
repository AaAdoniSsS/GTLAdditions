package com.gtladd.gtladditions.mixin.ae.storage;

import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.me.storage.DelegatingMEInventory;
import appeng.me.storage.MEInventoryHandler;
import com.gtladd.gtladditions.api.ae2.IMEStorageFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MEInventoryHandler.class)
public abstract class MEInventoryHandlerMixin extends DelegatingMEInventory implements IMEStorageFilter {

    @Shadow(remap = false)
    private boolean filterAvailableContents;
    @Shadow(remap = false)
    private boolean allowExtraction;

    public MEInventoryHandlerMixin(MEStorage delegate) {
        super(delegate);
    }

    @Shadow(remap = false)
    protected abstract boolean canExtract(AEKey request);

    @Override
    public boolean canReport(AEKey key) {
        if (!this.filterAvailableContents) return true;
        if (!this.allowExtraction) return false;
        return canExtract(key);
    }
}
