package com.gtladd.gtladditions.mixin.ae.api;

import appeng.api.stacks.KeyCounter;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyCounter.class)
public interface KeyCounterAccessor {

    @Accessor(remap = false, value = "lists")
    Reference2ObjectMap getLists();
}
