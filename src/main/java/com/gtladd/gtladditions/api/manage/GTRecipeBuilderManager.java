package com.gtladd.gtladditions.api.manage;

import org.gtlcore.gtlcore.api.recipe.ingredient.LongIngredient;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.utils.FluidStackHashStrategy;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.Map;
import java.util.WeakHashMap;

public final class GTRecipeBuilderManager {

    static Map<ItemStack, Ingredient> ITEM_STACK_INGREDIENT_MAP;
    static Map<TagKey<Item>, Ingredient> ITEM_TAG_INGREDIENT_MAP;
    static Map<FluidStack, FluidIngredient> FLUID_STACK_INGREDIENT_MAP;
    static Map<TagKey<Fluid>, FluidIngredient> FLUID_TAG_INGREDIENT_MAP;

    public static void init() {
        ITEM_STACK_INGREDIENT_MAP = new Object2ObjectOpenCustomHashMap<>(8192, 0.5f, ItemStackHashStrategy.comparingAllButCount());
        ITEM_TAG_INGREDIENT_MAP = new Reference2ObjectOpenHashMap<>(2048, 0.5f);
        FLUID_STACK_INGREDIENT_MAP = new Object2ObjectOpenCustomHashMap<>(8192, 0.5f, FluidStackHashStrategy.comparingAllButAmount());
        FLUID_TAG_INGREDIENT_MAP = new Reference2ObjectOpenHashMap<>(2048, 0.5f);
    }

    public static void clear() {
        ITEM_STACK_INGREDIENT_MAP = new WeakHashMap<>();
        ITEM_TAG_INGREDIENT_MAP = new WeakHashMap<>();
        FLUID_STACK_INGREDIENT_MAP = new WeakHashMap<>();
        FLUID_TAG_INGREDIENT_MAP = new WeakHashMap<>();
    }

    public static Ingredient getIngredient(ItemStack itemStack) {
        var ingredient = ITEM_STACK_INGREDIENT_MAP.computeIfAbsent(itemStack, Ingredient::of);
        return LongIngredient.create(ingredient, (long) itemStack.getCount());
    }

    public static Ingredient getIngredient(TagKey<Item> tagKey, long amount) {
        var ingredient = ITEM_TAG_INGREDIENT_MAP.computeIfAbsent(tagKey, Ingredient::of);
        return LongIngredient.create(ingredient, amount);
    }

    public static FluidIngredient getFluidIngredient(FluidStack fluidStack) {
        var ingredient = FLUID_STACK_INGREDIENT_MAP.computeIfAbsent(fluidStack, FluidIngredient::of);
        if (ingredient.getAmount() != fluidStack.getAmount()) {
            ingredient = ingredient.copy();
            ingredient.setAmount(fluidStack.getAmount());
        }
        return ingredient;
    }

    public static FluidIngredient getFluidIngredient(TagKey<Fluid> tagKey, long amount) {
        var ingredient = FLUID_TAG_INGREDIENT_MAP.computeIfAbsent(tagKey, k -> FluidIngredient.of(k, amount));
        if (ingredient.getAmount() != amount) {
            ingredient = ingredient.copy();
            ingredient.setAmount(amount);
        }
        return ingredient;
    }
}
