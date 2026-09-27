package com.gtladd.gtladditions.mixin.ae.storage;

import appeng.api.storage.MEStorage;
import appeng.me.storage.NetworkStorage;
import com.gtladd.gtladditions.api.ae2.IMEStorageNode;
import com.gtladd.gtladditions.api.ae2.StorageFlatten;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.function.Function;

@Mixin(NetworkStorage.class)
public class NetworkStorageMixin implements IMEStorageNode {

    @Shadow(remap = false)
    @Final
    private NavigableMap<Integer, List<MEStorage>> priorityInventory;

    @Unique
    private StorageFlatten flatten;

    @Override
    public void collectChildStorages(Collection<MEStorage> out) {
        for (var storages : this.priorityInventory.values()) {
            out.addAll(storages);
        }
    }

    @Override
    public StorageFlatten flattenCache() {
        var flatten = this.flatten;
        if (flatten != null && flatten.isUpToDate()) return flatten;
        return this.flatten = StorageFlatten.build(this);
    }

    @ModifyArg(method = "mount",
               at = @At(value = "INVOKE",
                        target = "Ljava/util/NavigableMap;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"),
               index = 1,
               remap = false)
    public Function mount(Function par2) {
        return (a) -> new ObjectArrayList<>();
    }

    @Inject(method = "mount", at = @At("HEAD"), remap = false)
    private void dropFlattenOnMount(int priority, MEStorage inventory, CallbackInfo ci) {
        this.flatten = null;
    }

    @Inject(method = "unmount", at = @At("HEAD"), remap = false)
    private void dropFlattenOnUnmount(MEStorage inventory, CallbackInfo ci) {
        this.flatten = null;
    }
}
