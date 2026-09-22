package com.gtladd.gtladditions.mixin.gtlcore.machine;

import org.gtlcore.gtlcore.common.machine.VirtualIngredientSupplyMachine;

import appeng.api.stacks.AEKey;
import com.gtladd.gtladditions.api.ae2.IMEStorage;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VirtualIngredientSupplyMachine.class)
public abstract class VirtualIngredientSupplyMachineMixin implements IMEStorage {

    @Shadow(remap = false)
    @Final
    private Object2LongMap<AEKey> published;

    @Override
    public Object2LongMap<AEKey> getStorageMap() {
        return this.published;
    }
}
