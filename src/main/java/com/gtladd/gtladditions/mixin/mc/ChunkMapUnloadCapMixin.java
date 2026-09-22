package com.gtladd.gtladditions.mixin.mc;

import net.minecraft.server.level.ChunkMap;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;
import java.util.function.BooleanSupplier;

@Mixin(ChunkMap.class)
public abstract class ChunkMapUnloadCapMixin {

    @Unique
    private int gtladditions$unloadBudget;

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At("HEAD"))
    private void gtladditions$resetBudget(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        this.gtladditions$unloadBudget = 16;
    }

    @WrapOperation(method = "processUnloads(Ljava/util/function/BooleanSupplier;)V",
                   at = @At(value = "INVOKE", target = "Ljava/util/Queue;poll()Ljava/lang/Object;"))
    private Object gtladditions$capUnloadQueue(Queue<Runnable> queue, Operation<Object> original) {
        if (this.gtladditions$unloadBudget-- <= 0) return null;
        return original.call(queue);
    }
}
