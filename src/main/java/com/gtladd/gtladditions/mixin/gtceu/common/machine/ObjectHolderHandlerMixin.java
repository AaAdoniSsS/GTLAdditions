package com.gtladd.gtladditions.mixin.gtceu.common.machine;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.gregtechceu.gtceu.common.machine.multiblock.part.ObjectHolderMachine$ObjectHolderHandler", remap = false)
public class ObjectHolderHandlerMixin {

    private static final ResourceLocation DATA_MODULE = new ResourceLocation("gtceu", "data_module");
    private static final ResourceLocation DATA_ORB = new ResourceLocation("gtceu", "data_orb");
    private static final ResourceLocation DATA_STICK = new ResourceLocation("gtceu", "data_stick");

    @Inject(method = "isItemValid", at = @At("HEAD"), cancellable = true)
    private void gtladditions$restrictDataItemsToFirstSlot(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot == 1 || stack.isEmpty()) return;
        Item item = stack.getItem();
        if (item == BuiltInRegistries.ITEM.get(DATA_MODULE) || item == BuiltInRegistries.ITEM.get(DATA_ORB) || item == BuiltInRegistries.ITEM.get(DATA_STICK)) {
            cir.setReturnValue(false);
        }
    }
}
