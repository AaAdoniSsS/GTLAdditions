package com.gtladd.gtladditions.mixin.ae.util;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.util.prioritylist.PrecisePriorityList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrecisePriorityList.class)
public class PrecisePriorityListMixin {

    @Unique
    private ObjectOpenHashSet<AEKey> keys;

    @Shadow(remap = false)
    @Mutable
    @Final
    private KeyCounter list;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    public void PrecisePriorityList(KeyCounter in, CallbackInfo ci) {
        this.keys = new ObjectOpenHashSet<>(list.keySet());
        this.list = null;
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public boolean isListed(AEKey input) {
        return keys.contains(input);
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public Iterable<AEKey> getItems() {
        return keys.clone();
    }
}
