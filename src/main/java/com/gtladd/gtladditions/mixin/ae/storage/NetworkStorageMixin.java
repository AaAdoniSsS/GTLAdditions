package com.gtladd.gtladditions.mixin.ae.storage;

import appeng.api.storage.MEStorage;
import appeng.me.storage.NetworkStorage;
import com.gtladd.gtladditions.api.ae2.IMEStorageNode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;

@Mixin(NetworkStorage.class)
public class NetworkStorageMixin implements IMEStorageNode {

    @Shadow(remap = false)
    @Final
    private NavigableMap<Integer, List<MEStorage>> priorityInventory;

    @Override
    public void collectChildStorages(Collection<MEStorage> out) {
        for (var storages : this.priorityInventory.values()) {
            out.addAll(storages);
        }
    }
}
