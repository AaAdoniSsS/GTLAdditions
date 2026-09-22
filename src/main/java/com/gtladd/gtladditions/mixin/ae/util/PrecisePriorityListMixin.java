package com.gtladd.gtladditions.mixin.ae.util;

import appeng.api.stacks.KeyCounter;
import appeng.util.prioritylist.PrecisePriorityList;
import com.gtladd.gtladditions.mixin.ae.api.KeyCounterAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PrecisePriorityList.class)
public class PrecisePriorityListMixin {

    @Shadow(remap = false)
    @Final
    private KeyCounter list;

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public boolean isEmpty() {
        return ((KeyCounterAccessor) (Object) list).getLists().isEmpty();
    }
}
