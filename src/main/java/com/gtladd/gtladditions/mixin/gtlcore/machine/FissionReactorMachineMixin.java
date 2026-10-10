package com.gtladd.gtladditions.mixin.gtlcore.machine;

import org.gtlcore.gtlcore.common.machine.multiblock.electric.FissionReactorMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import com.gtladd.gtladditions.api.async.StagedBlastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FissionReactorMachine.class)
public abstract class FissionReactorMachineMixin {

    @Inject(method = "doExplosion", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtladditions$stagedExplosion(BlockPos pos, float explosionPower, CallbackInfo ci) {
        var self = (FissionReactorMachine) (Object) this;
        if (!(self.getLevel() instanceof ServerLevel level)) return;
        level.removeBlock(self.getPos(), false);
        StagedBlastManager.spawn(level, pos, Math.max(4, Math.round(explosionPower * 0.125f)));
        ci.cancel();
    }
}
