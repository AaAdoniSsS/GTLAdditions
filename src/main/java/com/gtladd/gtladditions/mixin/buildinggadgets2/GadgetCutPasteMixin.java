package com.gtladd.gtladditions.mixin.buildinggadgets2;

import com.direwolf20.buildinggadgets2.common.items.GadgetCutPaste;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(GadgetCutPaste.class)
public class GadgetCutPasteMixin {

    @ModifyConstant(method = "cutAndStore", constant = @Constant(intValue = 100000), remap = false)
    public int cutAndStore(int constant) {
        return constant * 10;
    }
}
