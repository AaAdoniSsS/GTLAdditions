package com.gtladd.gtladditions.mixin.ae.storage;

import appeng.api.storage.MEStorage;
import appeng.me.storage.DelegatingMEInventory;
import com.gtladd.gtladditions.api.ae2.IMEStorageNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;

@Mixin(DelegatingMEInventory.class)
public abstract class DelegatingMEInventoryMixin implements IMEStorageNode {

    @Shadow(remap = false)
    protected abstract MEStorage getDelegate();

    @Override
    public void collectChildStorages(Collection<MEStorage> out) {
        var delegate = this.getDelegate();
        if (delegate != null) out.add(delegate);
    }
}
