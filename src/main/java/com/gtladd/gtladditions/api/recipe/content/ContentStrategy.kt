package com.gtladd.gtladditions.api.recipe.content

import org.gtlcore.gtlcore.api.recipe.ingredient.CacheHashStrategies.FluidIngredientHashStrategy
import org.gtlcore.gtlcore.api.recipe.ingredient.CacheHashStrategies.IngredientHashStrategy

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability
import com.gregtechceu.gtceu.api.recipe.content.Content
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient

import net.minecraft.world.item.crafting.Ingredient

import it.unimi.dsi.fastutil.Hash

import java.util.Objects

class ContentStrategy(val cap: RecipeCapability<*>) : Hash.Strategy<Content> {

    override fun hashCode(content: Content?) = when (cap) {
        ItemRecipeCapability.CAP -> IngredientHashStrategy.INSTANCE.hashCode(content?.content as Ingredient) + Objects.hash(content.chance, content.maxChance, content.tierChanceBoost)
        FluidRecipeCapability.CAP -> FluidIngredientHashStrategy.INSTANCE.hashCode(content?.content as FluidIngredient) + Objects.hash(content.chance, content.maxChance, content.tierChanceBoost)
        else -> 0
    }

    override fun equals(a: Content?, b: Content?) = when (cap) {
        ItemRecipeCapability.CAP -> IngredientHashStrategy.INSTANCE.equals(a?.content as Ingredient, b?.content as Ingredient)
        FluidRecipeCapability.CAP -> FluidIngredientHashStrategy.INSTANCE.equals(a?.content as FluidIngredient, b?.content as FluidIngredient)
        else -> false
    }
}
