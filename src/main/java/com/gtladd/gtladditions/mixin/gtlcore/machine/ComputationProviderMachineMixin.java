package com.gtladd.gtladditions.mixin.gtlcore.machine;

import org.gtlcore.gtlcore.common.machine.multiblock.electric.ComputationProviderMachine;
import org.gtlcore.gtlcore.utils.Registries;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;

import net.minecraft.world.item.ItemStack;

import com.gtladd.gtladditions.api.machine.trait.IOpticalComputationProvider;
import com.gtladd.gtladditions.utils.MathUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ComputationProviderMachine.class)
public abstract class ComputationProviderMachineMixin extends WorkableElectricMultiblockMachine implements IOpticalComputationProvider {

    private static final ItemStack OPTICAL_MAINFRAME = Registries.getItemStack("kubejs:optical_mainframe", 8);
    private static final ItemStack EXOTIC_MAINFRAME = Registries.getItemStack("kubejs:exotic_mainframe", 8);
    private static final ItemStack COSMIC_MAINFRAME = Registries.getItemStack("kubejs:cosmic_mainframe", 8);
    private static final ItemStack SUPRACAUSAL_MAINFRAME = Registries.getItemStack("kubejs:supracausal_mainframe", 8);

    @Shadow(remap = false)
    boolean canProvideCWUt;
    @Shadow(remap = false)
    public int maxCWUt;
    @Shadow(remap = false)
    private boolean inf;

    public ComputationProviderMachineMixin(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Shadow(remap = false)
    protected abstract int allocatedCWUt(int cwut, boolean simulate);

    @Override
    public long requestCWU(long cwu, boolean simulate) {
        return !this.canProvideCWUt ? 0 : this.allocatedCWUt(MathUtil.INSTANCE.getSafeToInt(cwu), simulate);
    }

    @Override
    public long getMaxCWU() {
        if (this.inf) {
            return Integer.MAX_VALUE;
        } else if (this.maxCWUt == 0) {
            switch (this.getTier()) {
                case 11:
                    if (checkItem(OPTICAL_MAINFRAME)) return 1024;
                    break;
                case 12:
                    if (checkItem(EXOTIC_MAINFRAME)) return 2048;
                    break;
                case 13:
                    if (checkItem(COSMIC_MAINFRAME)) return 4096;
                    break;
                case 14:
                    if (checkItem(SUPRACAUSAL_MAINFRAME)) return 8192;
            }

            return 0;
        } else {
            return this.maxCWUt;
        }
    }

    private boolean checkItem(ItemStack stack) {
        for (var p : this.getParts()) {
            for (var t : p.getRecipeHandlers()) {
                if (t instanceof NotifiableItemStackHandler ih) for (int i = 0; i < ih.getSlots(); i++) {
                    var item = ih.getStackInSlot(i);
                    if (!item.isEmpty() && ItemStack.isSameItem(item, stack) && item.getCount() >= stack.getCount())
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canBridge() {
        return true;
    }
}
