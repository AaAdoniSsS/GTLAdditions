package com.gtladd.gtladditions.mixin.gtceu.common.machine;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.ResearchStationMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ObjectHolderMachine;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.gregtechceu.gtceu.common.machine.multiblock.part.ObjectHolderMachine$ObjectHolderHandler", remap = false)
public class ObjectHolderHandlerMixin {

    @Shadow
    @Final
    private ObjectHolderMachine this$0;

    private static final ResourceLocation DATA_MODULE = new ResourceLocation("gtceu", "data_module");
    private static final ResourceLocation DATA_ORB = new ResourceLocation("gtceu", "data_orb");

    @Inject(method = "extractItem", at = @At("HEAD"), cancellable = true)
    private void gtladditions$blockExtractWhileResearching(int slot, int amount, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        boolean researching = this$0.getControllers().stream().anyMatch(controller -> controller instanceof ResearchStationMachine station &&
                ((WorkableElectricMultiblockMachine) station).getRecipeLogic().isWorking());
        if (researching) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(method = "isItemValid", at = @At("HEAD"), cancellable = true)
    private void gtladditions$restrictDataItemsToFirstSlot(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot == 1 || stack.isEmpty()) return;
        Item item = stack.getItem();
        if (item == BuiltInRegistries.ITEM.get(DATA_MODULE) || item == BuiltInRegistries.ITEM.get(DATA_ORB)) {
            cir.setReturnValue(false);
        }
    }
}
