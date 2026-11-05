package com.gtladd.gtladditions.mixin.gtceu.common.machine;

import com.gregtechceu.gtceu.utils.ResearchManager;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStackTransfer.class)
public class ObjectHolderTransferMixin {

    private static final String EXTERNAL_HANDLER = "com.gregtechceu.gtceu.common.machine.multiblock.part.ObjectHolderMachine$ObjectHolderHandler$1";
    private static final ResourceLocation DATA_MODULE = ResourceLocation.fromNamespaceAndPath("gtceu", "data_module");
    private static final ResourceLocation DATA_ORB = ResourceLocation.fromNamespaceAndPath("gtceu", "data_orb");

    @Inject(method = "isItemValid", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtladditions$restrictExternalDataItems(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!getClass().getName().equals(EXTERNAL_HANDLER) || stack.isEmpty()) return;
        Item item = stack.getItem();
        boolean allowedData = item == BuiltInRegistries.ITEM.get(DATA_MODULE) || item == BuiltInRegistries.ITEM.get(DATA_ORB);
        if (slot == 1) {
            if (!allowedData) cir.setReturnValue(false);
        } else if (slot == 0 && ResearchManager.isStackDataItem(stack, true)) {
            cir.setReturnValue(false);
        }
    }
}
