package com.gtladd.gtladditions.mixin.ae.storage;

import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.me.storage.DelegatingMEInventory;
import appeng.me.storage.MEInventoryHandler;
import com.gtladd.gtladditions.api.ae2.IMEStorageFilter;
import com.gtladd.gtladditions.api.ae2.StorageFlatten;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Inject(method = "setAllowExtraction", at = @At("HEAD"), remap = false)
    private void dropFlattenOnAccessChange(boolean allowExtraction, CallbackInfo ci) {
        if (this.allowExtraction != allowExtraction) StorageFlatten.invalidateTopology();
    }
}
