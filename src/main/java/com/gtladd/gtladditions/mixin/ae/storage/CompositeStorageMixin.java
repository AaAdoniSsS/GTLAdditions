package com.gtladd.gtladditions.mixin.ae.storage;

import appeng.api.stacks.AEKeyType;
import appeng.api.storage.MEStorage;
import appeng.me.storage.CompositeStorage;
import com.gtladd.gtladditions.api.ae2.IMEStorageNode;
import com.gtladd.gtladditions.api.ae2.StorageFlatten;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Map;

@Mixin(CompositeStorage.class)
public class CompositeStorageMixin implements IMEStorageNode {

    @Shadow(remap = false)
    private Map<AEKeyType, MEStorage> storages;

    @Override
    public void collectChildStorages(Collection<MEStorage> out) {
        var storages = this.storages;
        if (storages != null) out.addAll(storages.values());
    }

    @Inject(method = "setStorages", at = @At("HEAD"), remap = false)
    private void dropFlattenOnStorageChange(Map<AEKeyType, MEStorage> storages, CallbackInfo ci) {
        if (this.storages != storages) StorageFlatten.invalidateTopology();
    }
}
