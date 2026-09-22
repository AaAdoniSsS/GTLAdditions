package com.gtladd.gtladditions.mixin.gtceu.api.machine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.hooks.ticking.TickHandler;
import com.hepdd.gtmthings.api.capability.IBindable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MetaMachineBlockEntity.class)
public abstract class MetaMachineBlockEntityMixin extends BlockEntity implements IMachineBlockEntity {

    @Shadow(remap = false)
    @Final
    public MetaMachine metaMachine;

    public MetaMachineBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (metaMachine instanceof IBindable b && tag.contains("uuid")) {
            b.setUUID(tag.getUUID("uuid"));
            metaMachine.onLoad();
        }
    }

    @Override
    public long getOffsetTimer() {
        return this.level() == null ? this.getOffset() : TickHandler.instance().getCurrentTick() + this.getOffset();
    }
}
