package com.gtladd.gtladditions.mixin.mc;

import org.gtlcore.gtlcore.common.data.GTLItems;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import com.google.common.collect.ImmutableMap;
import com.gtladd.gtladditions.api.async.AsyncFluidTransform;
import com.gtladd.gtladditions.common.register.GTLAddMaterial;
import com.gtladd.gtladditions.utils.MathUtil;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockState.class)
public abstract class BlockStateMixin extends BlockBehaviour.BlockStateBase {

    protected BlockStateMixin(Block owner, ImmutableMap<Property<?>, Comparable<?>> values, MapCodec<BlockState> propertiesCodec) {
        super(owner, values, propertiesCodec);
    }

    @Override
    public void onPlace(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean movedByPiston) {
        super.onPlace(level, pos, oldState, movedByPiston);
        if (!(level instanceof ServerLevel serverLevel)) return;
        var fluidState = this.getFluidState();
        Fluid expectedFluid = null;
        ItemStack itemStack = ItemStack.EMPTY;
        if (fluidState.isSource()) {
            if (fluidState.is(GTLAddMaterial.MINING_ESSENCE.getFluid())) {
                itemStack = MathUtil.INSTANCE.random(100) >= 30 ? GTLItems.MINING_CRYSTAL.asStack() : ItemStack.EMPTY;
                expectedFluid = GTLAddMaterial.MINING_ESSENCE.getFluid();
            } else if (fluidState.is(GTLAddMaterial.TREASURES_ESSENCE.getFluid())) {
                itemStack = MathUtil.INSTANCE.random(100) >= 30 ? GTLItems.TREASURES_CRYSTAL.asStack() : ItemStack.EMPTY;
                expectedFluid = GTLAddMaterial.TREASURES_ESSENCE.getFluid();
            }
        }
        if (expectedFluid != null) {
            AsyncFluidTransform.register(serverLevel, pos, new AsyncFluidTransform(serverLevel, pos, itemStack, expectedFluid));
        } else if (isEssenceSource(oldState.getFluidState())) {
            AsyncFluidTransform.unregister(serverLevel, pos);
        }
    }

    private static boolean isEssenceSource(FluidState fluidState) {
        return fluidState.isSource() && (fluidState.is(GTLAddMaterial.MINING_ESSENCE.getFluid()) || fluidState.is(GTLAddMaterial.TREASURES_ESSENCE.getFluid()));
    }
}
