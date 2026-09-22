package com.gtladd.gtladditions.mixin.mc;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.core.mixins.ItemValueAccessor;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import com.gtladd.gtladditions.api.recipe.ingredient.MapIngredientVariantHolder;
import com.gtladd.gtladditions.utils.SingleStream;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Mixin(Ingredient.class)
public abstract class IngredientMixin implements MapIngredientVariantHolder {

    @Unique
    private volatile List<AbstractMapIngredient> variants;

    @Shadow
    private ItemStack[] itemStacks;
    @Shadow
    @Final
    private Ingredient.Value[] values;

    @Override
    public @Nullable List<AbstractMapIngredient> variants(@NotNull RecipeCapability<?> cap) {
        return ItemRecipeCapability.CAP == cap ? variants : null;
    }

    @Override
    public void setVariants(@NotNull List<AbstractMapIngredient> variants) {
        this.variants = variants;
    }

    @Inject(method = "invalidate", at = @At("HEAD"), remap = false)
    private void invalidateVariants(CallbackInfo ci) {
        variants = null;
    }

    @ModifyArg(method = "of(Lnet/minecraft/tags/TagKey;)Lnet/minecraft/world/item/crafting/Ingredient;",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/item/crafting/Ingredient;fromValues(Ljava/util/stream/Stream;)Lnet/minecraft/world/item/crafting/Ingredient;"),
               remap = false)
    private static Stream of(Stream stream, @Local(name = "tag") TagKey<Item> tag) {
        return SingleStream.Companion.createSingle(new Ingredient.TagValue[] { new Ingredient.TagValue(tag) });
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite
    public ItemStack[] getItems() {
        if (this.itemStacks == null) {
            if (this.values.length == 1) {
                if (this.values[0] instanceof ItemValueAccessor item) {
                    this.itemStacks = new ItemStack[] { item.getItem() };
                } else if (this.values[0] instanceof Ingredient.TagValue tag) {
                    this.itemStacks = tag.getItems().toArray(new ItemStack[0]);
                }
            } else {
                this.itemStacks = Arrays.stream(this.values).flatMap((value) -> value.getItems().stream()).distinct().toArray(ItemStack[]::new);
            }
        }

        return this.itemStacks;
    }
}
