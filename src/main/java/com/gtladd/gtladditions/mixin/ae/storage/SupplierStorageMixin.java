package com.gtladd.gtladditions.mixin.ae.storage;

import appeng.api.storage.MEStorage;
import appeng.api.storage.SupplierStorage;
import com.gtladd.gtladditions.api.ae2.IMEStorageNode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.function.Supplier;

@Mixin(SupplierStorage.class)
public class SupplierStorageMixin implements IMEStorageNode {

    @Shadow(remap = false)
    @Final
    private Supplier<MEStorage> supplier;

    @Override
    public void collectChildStorages(Collection<MEStorage> out) {
        var delegate = this.supplier.get();
        if (delegate != null) out.add(delegate);
    }
}
