package com.gtladd.gtladditions.mixin.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;

import net.minecraft.world.item.crafting.Ingredient;

import com.gtladd.gtladditions.utils.SingleStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.stream.Stream;

@Mixin(SizedIngredient.class)
public class SizedIngredientMixin {

    @ModifyArg(method = "<init>(Lnet/minecraft/world/item/crafting/Ingredient;I)V",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/item/crafting/Ingredient;<init>(Ljava/util/stream/Stream;)V"),
               remap = false)
    private static Stream SizedIngredient(Stream par1) {
        return SingleStream.Companion.createSingle(new Ingredient.Value[0]);
    }
}
