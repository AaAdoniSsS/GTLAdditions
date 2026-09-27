package com.gtladd.gtladditions.mixin.gtlcore.machine.part;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "org.gtlcore.gtlcore.common.machine.multiblock.part.ae.MEPatternBufferPartMachineBase$InternalSlot")
public interface InternalSlotAccessor {

    @Accessor(remap = false, value = "slotIndex")
    int getSlotIndex();
}
