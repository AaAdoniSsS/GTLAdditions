package com.gtladd.gtladditions.mixin.ae.storage;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.helpers.externalstorage.GenericStackInv;
import com.gtladd.gtladditions.api.ae2.IMEStorage;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.agrona.collections.ObjLongConsumer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GenericStackInv.class)
public abstract class GenericStackInvMixin implements IMEStorage {

    @Shadow(remap = false)
    @Final
    protected GenericStack[] stacks;

    @Override
    public Object2LongMap<AEKey> getStorageMap() {
        return null;
    }

    @Override
    public void forEachAvailableStack(ObjLongConsumer<AEKey> sink) {
        var stacks = this.stacks;
        for (var stack : stacks) {
            if (stack != null && stack.amount() > 0) {
                sink.accept(stack.what(), stack.amount());
            }
        }
    }
}
