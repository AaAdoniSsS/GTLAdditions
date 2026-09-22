package com.gtladd.gtladditions.mixin.ae.api;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "appeng.api.stacks.VariantCounter")
public class VariantCounterMixin {

    @Redirect(method = "addAll",
              at = @At(value = "INVOKE",
                       target = "Lit/unimi/dsi/fastutil/objects/ObjectSet;iterator()Lit/unimi/dsi/fastutil/objects/ObjectIterator;"),
              remap = false)
    public ObjectIterator addAll(ObjectSet instance) {
        return instance instanceof Object2LongMap.FastEntrySet f ? f.fastIterator() : instance.iterator();
    }
}
