package com.gtladd.gtladditions.mixin.gtlcore.machine.part;

import org.gtlcore.gtlcore.common.machine.multiblock.part.ae.MEPatternBufferPartMachineBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MEPatternBufferPartMachineBase.class)
public interface MEPatternBufferPartMachineBaseInvoker {

    @Invoker(remap = false, value = "getActiveInternalSlots")
    Iterable getActiveSlots();
}
