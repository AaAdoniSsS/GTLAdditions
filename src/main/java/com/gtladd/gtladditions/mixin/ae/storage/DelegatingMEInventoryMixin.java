package com.gtladd.gtladditions.mixin.ae.storage;

import appeng.api.storage.MEStorage;
import appeng.me.storage.DelegatingMEInventory;
import com.gtladd.gtladditions.api.ae2.IMEStorageNode;
import com.gtladd.gtladditions.api.ae2.StorageFlatten;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(DelegatingMEInventory.class)
public abstract class DelegatingMEInventoryMixin implements IMEStorageNode {

    @Shadow(remap = false)
    private MEStorage delegate;

    @Shadow(remap = false)
    protected abstract MEStorage getDelegate();

    @Override
    public void collectChildStorages(Collection<MEStorage> out) {
        var delegate = this.getDelegate();
        if (delegate != null) out.add(delegate);
    }

    @Inject(method = "setDelegate", at = @At("HEAD"), remap = false)
    private void dropFlattenOnDelegateChange(MEStorage delegate, CallbackInfo ci) {
        if (this.delegate != delegate) StorageFlatten.invalidateTopology();
    }
}
