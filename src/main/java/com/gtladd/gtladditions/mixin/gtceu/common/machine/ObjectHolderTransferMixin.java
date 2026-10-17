package com.gtladd.gtladditions.mixin.gtceu.common.machine;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "com.gregtechceu.gtceu.common.machine.multiblock.part.ObjectHolderMachine$ObjectHolderHandler$1", remap = false)
public class ObjectHolderTransferMixin {

    private static final ResourceLocation DATA_MODULE = new ResourceLocation("gtceu", "data_module");
    private static final ResourceLocation DATA_ORB = new ResourceLocation("gtceu", "data_orb");
    private static final ResourceLocation DATA_STICK = new ResourceLocation("gtceu", "data_stick");

    public boolean isItemValid(int slot, ItemStack stack) {
        if (slot == 1 || stack.isEmpty()) {
            return true;
        }
        Item item = stack.getItem();
        return item != BuiltInRegistries.ITEM.get(DATA_MODULE) && item != BuiltInRegistries.ITEM.get(DATA_ORB) && item != BuiltInRegistries.ITEM.get(DATA_STICK);
    }
}
