package com.gtladd.gtladditions.mixin.mc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;

@Mixin(CompoundTag.class)
public class CompoundTagMixin {

    @ModifyArg(method = "<init>()V",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/nbt/CompoundTag;<init>(Ljava/util/Map;)V"),
               remap = false)
    private static Map<String, Tag> newMap(Map<String, Tag> tags) {
        return new Object2ObjectOpenHashMap<>();
    }
}
