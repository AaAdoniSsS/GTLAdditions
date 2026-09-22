package com.gtladd.gtladditions.mixin.ae.storage;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.me.storage.ExternalStorageFacade;
import com.gtladd.gtladditions.api.ae2.IMEStorage;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.agrona.collections.ObjLongConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ExternalStorageFacade.class)
public abstract class ExternalStorageFacadeMixin implements IMEStorage {

    @Shadow(remap = false)
    protected boolean extractableOnly;

    @Override
    public Object2LongMap<AEKey> getStorageMap() {
        return null;
    }

    @Override
    public void forEachAvailableStack(ObjLongConsumer<AEKey> sink) {
        var self = (ExternalStorageFacade) (Object) this;
        if (this.extractableOnly) {
            var counter = new KeyCounter();
            ((MEStorage) this).getAvailableStacks(counter);
            for (var entry : counter) {
                sink.accept(entry.getKey(), entry.getLongValue());
            }
            return;
        }
        for (int i = 0, slots = self.getSlots(); i < slots; i++) {
            var stack = self.getStackInSlot(i);
            if (stack != null && stack.amount() > 0) {
                sink.accept(stack.what(), stack.amount());
            }
        }
    }
}
