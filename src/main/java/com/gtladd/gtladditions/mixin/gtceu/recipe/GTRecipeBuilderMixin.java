package com.gtladd.gtladditions.mixin.gtceu.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;

import com.gtladd.gtladditions.api.manage.GTRecipeBuilderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(GTRecipeBuilder.class)
public abstract class GTRecipeBuilderMixin {

    @Shadow(remap = false)
    public ResourceLocation id;

    @Shadow(remap = false)
    public abstract <T> GTRecipeBuilder input(RecipeCapability<T> capability, T... obj);

    @Shadow(remap = false)
    public abstract <T> GTRecipeBuilder output(RecipeCapability<T> capability, T... obj);

    @Redirect(method = "inputItems(Lnet/minecraft/world/item/ItemStack;)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;",
              at = @At(value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/recipe/ingredient/SizedIngredient;create(Lnet/minecraft/world/item/ItemStack;)Lcom/gregtechceu/gtceu/api/recipe/ingredient/SizedIngredient;"),
              remap = false)
    public SizedIngredient inputItemStack(ItemStack input) {
        return (SizedIngredient) GTRecipeBuilderManager.getIngredient(input);
    }

    @Inject(method = "inputItems([Lnet/minecraft/world/item/ItemStack;)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;", at = @At("HEAD"), remap = false, cancellable = true)
    public void inputItems(ItemStack[] inputs, CallbackInfoReturnable<GTRecipeBuilder> cir) {
        for (var itemStack : inputs) {
            if (itemStack.isEmpty()) GTCEu.LOGGER.error("gt recipe {} input items is empty", this.id);
        }
        this.input(ItemRecipeCapability.CAP, Arrays.stream(inputs).map(GTRecipeBuilderManager::getIngredient).toArray(Ingredient[]::new));
        cir.setReturnValue((GTRecipeBuilder) (Object) this);
    }

    @Redirect(method = "inputItems(Lnet/minecraft/tags/TagKey;I)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;",
              at = @At(value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/recipe/ingredient/SizedIngredient;create(Lnet/minecraft/tags/TagKey;I)Lcom/gregtechceu/gtceu/api/recipe/ingredient/SizedIngredient;"),
              remap = false)
    public SizedIngredient inputItemTag(TagKey<Item> tag, int amount) {
        return (SizedIngredient) GTRecipeBuilderManager.getIngredient(tag, amount);
    }

    @Redirect(method = "inputItems(Lnet/minecraft/world/item/Item;)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;",
              at = @At(value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/recipe/ingredient/SizedIngredient;create(Lnet/minecraft/world/item/ItemStack;)Lcom/gregtechceu/gtceu/api/recipe/ingredient/SizedIngredient;"),
              remap = false)
    public SizedIngredient inputItem(ItemStack inner) {
        return (SizedIngredient) GTRecipeBuilderManager.getIngredient(inner);
    }

    @Redirect(method = "outputItems(Lnet/minecraft/world/item/ItemStack;)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;",
              at = @At(value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/recipe/ingredient/SizedIngredient;create(Lnet/minecraft/world/item/ItemStack;)Lcom/gregtechceu/gtceu/api/recipe/ingredient/SizedIngredient;"),
              remap = false)
    public SizedIngredient outputItems(ItemStack output) {
        return (SizedIngredient) GTRecipeBuilderManager.getIngredient(output);
    }

    @Inject(method = "outputItems([Lnet/minecraft/world/item/ItemStack;)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;", at = @At("HEAD"), remap = false, cancellable = true)
    public void outputItems(ItemStack[] outputs, CallbackInfoReturnable<GTRecipeBuilder> cir) {
        for (var itemStack : outputs) {
            if (itemStack.isEmpty()) GTCEu.LOGGER.error("gt recipe {} output items is empty", this.id);
        }
        this.output(ItemRecipeCapability.CAP, Arrays.stream(outputs).map(GTRecipeBuilderManager::getIngredient).toArray(Ingredient[]::new));
        cir.setReturnValue((GTRecipeBuilder) (Object) this);
    }

    @Redirect(method = "notConsumableFluid(Lcom/lowdragmc/lowdraglib/side/fluid/FluidStack;)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;",
              at = @At(value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/recipe/ingredient/FluidIngredient;of(Lnet/minecraft/tags/TagKey;J)Lcom/gregtechceu/gtceu/api/recipe/ingredient/FluidIngredient;"),
              remap = false)
    public FluidIngredient notConsumableFluid(TagKey<Fluid> tag, long amount) {
        return GTRecipeBuilderManager.getFluidIngredient(tag, amount);
    }

    @Redirect(method = "inputFluids(Lcom/lowdragmc/lowdraglib/side/fluid/FluidStack;)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;",
              at = @At(value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/recipe/ingredient/FluidIngredient;of(Lnet/minecraft/tags/TagKey;J)Lcom/gregtechceu/gtceu/api/recipe/ingredient/FluidIngredient;"),
              remap = false)
    public FluidIngredient inputFluids(TagKey<Fluid> tag, long amount) {
        return GTRecipeBuilderManager.getFluidIngredient(tag, amount);
    }

    @Inject(method = "inputFluids([Lcom/lowdragmc/lowdraglib/side/fluid/FluidStack;)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;", at = @At("HEAD"), remap = false, cancellable = true)
    public void inputFluids(FluidStack[] inputs, CallbackInfoReturnable<GTRecipeBuilder> cir) {
        this.input(FluidRecipeCapability.CAP, Arrays.stream(inputs).map((fluid) -> GTRecipeBuilderManager.getFluidIngredient(TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(fluid.getFluid()).getPath()), fluid.getAmount())).toArray(FluidIngredient[]::new));
        cir.setReturnValue((GTRecipeBuilder) (Object) this);
    }

    @Redirect(method = "outputFluids(Lcom/lowdragmc/lowdraglib/side/fluid/FluidStack;)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;",
              at = @At(value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/recipe/ingredient/FluidIngredient;of([Lcom/lowdragmc/lowdraglib/side/fluid/FluidStack;)Lcom/gregtechceu/gtceu/api/recipe/ingredient/FluidIngredient;"),
              remap = false)
    public FluidIngredient outputFluid(FluidStack[] stacks) {
        return GTRecipeBuilderManager.getFluidIngredient(stacks[0]);
    }

    @Inject(method = "outputFluids([Lcom/lowdragmc/lowdraglib/side/fluid/FluidStack;)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;", at = @At("HEAD"), remap = false, cancellable = true)
    public void outputFluids(FluidStack[] outputs, CallbackInfoReturnable<GTRecipeBuilder> cir) {
        this.output(FluidRecipeCapability.CAP, Arrays.stream(outputs).map(GTRecipeBuilderManager::getFluidIngredient).toArray(FluidIngredient[]::new));
        cir.setReturnValue((GTRecipeBuilder) (Object) this);
    }
}
