package com.gtladd.gtladditions.mixin.buildinggadgets2;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.direwolf20.buildinggadgets2.common.events.ServerBuildList;
import com.direwolf20.buildinggadgets2.common.events.ServerTickHandler;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerTickHandler.class)
public class ServerTickHandlerMixin {

    @Inject(method = "build",
            at = @At(value = "INVOKE",
                     target = "Lcom/direwolf20/buildinggadgets2/common/blockentities/RenderBlockBE;setRenderData(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;B)V"),
            remap = false)
    private static void build(ServerBuildList serverBuildList, Player player, CallbackInfo ci, @Local(name = "be") RenderBlockBE be, @Local(name = "blockState") BlockState blockState) {
        if (blockState.getBlock() instanceof MetaMachineBlock) {
            var uuid = new CompoundTag();
            uuid.putUUID("uuid", player.getUUID());
            be.setBlockEntityData(uuid);
        }
    }

    @Inject(method = "exchange",
            at = @At(value = "INVOKE",
                     target = "Ljava/lang/Object;equals(Ljava/lang/Object;)Z",
                     ordinal = 2),
            remap = false)
    private static void exchange(ServerBuildList serverBuildList, Player player, CallbackInfo ci, @Local(name = "be") RenderBlockBE be, @Local(name = "blockState") BlockState blockState) {
        if (blockState.getBlock() instanceof MetaMachineBlock) {
            var uuid = new CompoundTag();
            uuid.putUUID("uuid", player.getUUID());
            be.setBlockEntityData(uuid);
        }
    }

    @Inject(method = "cut",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/level/Level;removeBlockEntity(Lnet/minecraft/core/BlockPos;)V",
                     shift = At.Shift.BEFORE),
            remap = false)
    private static void cut(ServerBuildList serverBuildList, Player player, CallbackInfo ci, @Local(name = "blockPos") BlockPos blockPos, @Local(name = "level") Level level) {
        var be = level.getBlockEntity(blockPos);
        if (be instanceof MetaMachineBlockEntity mbe) {
            if (mbe.getMetaMachine() instanceof IRecipeLogicMachine r) {
                r.getRecipeLogic().resetRecipeLogic();
            }
        }
    }
}
