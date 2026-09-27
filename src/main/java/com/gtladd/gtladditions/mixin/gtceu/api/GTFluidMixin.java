package com.gtladd.gtladditions.mixin.gtceu.api;

import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.GTFluid;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.Lazy;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(GTFluid.class)
public class GTFluidMixin {

    @Shadow(remap = false)
    @Mutable
    @Final
    private Supplier<? extends Item> bucketItem;
    @Shadow(remap = false)
    @Mutable
    @Final
    private Supplier<? extends Fluid> stillFluid;
    @Shadow(remap = false)
    @Mutable
    @Final
    private Supplier<? extends Fluid> flowingFluid;
    @Shadow(remap = false)
    @Mutable
    @Final
    private Supplier<? extends LiquidBlock> block;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    public void GTFluid(FluidState state, Supplier<? extends Fluid> stillFluid, Supplier<? extends Fluid> flowingFluid, Supplier<? extends LiquidBlock> block, Supplier<? extends Item> bucket, int burnTime, CallbackInfo ci) {
        this.stillFluid = Lazy.of(stillFluid);
        this.flowingFluid = Lazy.of(flowingFluid);
        this.block = Lazy.of(block);
        this.bucketItem = Lazy.of(bucket);
    }
}
