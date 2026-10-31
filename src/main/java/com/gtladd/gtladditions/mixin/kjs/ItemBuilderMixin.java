package com.gtladd.gtladditions.mixin.kjs;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemBuilder.class)
public abstract class ItemBuilderMixin extends BuilderBase<Item> {

    @Shadow(remap = false)
    public int maxStackSize;
    @Shadow(remap = false)
    @Final
    public List<Component> tooltip;

    public ItemBuilderMixin(ResourceLocation i) {
        super(i);
    }

    @Inject(method = "createItemProperties", at = @At("HEAD"), remap = false)
    public void createItemProperties(CallbackInfoReturnable<Item.Properties> cir) {
        if (id.getPath().contains("hyperdimensional_drone")) {
            maxStackSize = 64;
            tooltip.add(Component.translatable("tooltip.gtladditions.item.max_stack_size"));
        }
    }
}
