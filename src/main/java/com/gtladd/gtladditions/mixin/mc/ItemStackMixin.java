package com.gtladd.gtladditions.mixin.mc;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Redirect(method = "is(Lnet/minecraft/tags/TagKey;)Z",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder$Reference;is(Lnet/minecraft/tags/TagKey;)Z"))
    public boolean is(Holder.Reference<Item> instance, TagKey<Item> tagKey) {
        return instance.tags().anyMatch(t -> t == tagKey);
    }
}
