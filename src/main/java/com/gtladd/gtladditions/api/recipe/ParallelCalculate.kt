package com.gtladd.gtladditions.api.recipe

import org.gtlcore.gtlcore.api.machine.multiblock.ParallelMachine
import org.gtlcore.gtlcore.api.machine.trait.IRecipeCapabilityMachine
import org.gtlcore.gtlcore.api.machine.trait.MEPatternRecipeHandlePart
import org.gtlcore.gtlcore.api.machine.trait.RecipeHandlePart
import org.gtlcore.gtlcore.api.recipe.IParallelLogic

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.content.Content
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient
import com.gregtechceu.gtceu.utils.FluidStackHashStrategy.comparingAllButAmount
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy.comparingAllButCount

import com.lowdragmc.lowdraglib.side.fluid.FluidStack

import net.minecraft.world.item.crafting.Ingredient

import com.gtladd.gtladditions.api.machine.IRecipeSearchProvider
import com.gtladd.gtladditions.api.recipe.content.ContentStrategy
import com.gtladd.gtladditions.utils.GTRecipeUtils.amount
import com.gtladd.gtladditions.utils.GTRecipeUtils.euTier
import com.gtladd.gtladditions.utils.GTRecipeUtils.test
import com.gtladd.gtladditions.utils.MathUtil.minToLong
import com.gtladd.gtladditions.utils.O2LHashMap
import com.hepdd.gtmthings.common.block.machine.trait.CatalystFluidStackHandler
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2LongMap
import it.unimi.dsi.fastutil.objects.Object2LongMaps
import it.unimi.dsi.fastutil.objects.Object2LongMaps.fastIterable

import java.util.function.Predicate

@Suppress("UNCHECKED_CAST")
object ParallelCalculate {

    fun getParallel(machine: WorkableElectricMultiblockMachine, recipe: GTRecipe, parallelAmount: Long): Long {
        val ctx = (machine as? IRecipeSearchProvider)?.getActiveSearchContext()
        if (ctx != null) {
            val mp = ctx.getPoolParallel(recipe, parallelAmount)
            if (mp <= 0L) return 0L
            return IParallelLogic.getMinParallel(machine, recipe, mp) minToLong mp
        }
        if (parallelAmount <= 1) return parallelAmount
        val maxParallel = getMaxParallel(machine, recipe, parallelAmount)
        if (maxParallel == 0L) return 0L
        return IParallelLogic.getMinParallel(machine, recipe, maxParallel)
    }

    fun getMaxParallel(machine: WorkableElectricMultiblockMachine, recipe: GTRecipe, parallelAmount: Long): Long {
        val ctx = (machine as? IRecipeSearchProvider)?.getActiveSearchContext()
        if (ctx != null) return ctx.getPoolParallel(recipe, parallelAmount)
        var amount = parallelAmount
        for (cap in recipe.inputs.keys) {
            if (cap === ItemRecipeCapability.CAP) {
                amount = amount minToLong getInputItemParallel(machine, recipe, amount)
                if (amount == 0L) break
            } else if (cap === FluidRecipeCapability.CAP) {
                amount = amount minToLong getInputFluidParallel(machine, recipe, amount)
                if (amount == 0L) break
            }
        }
        return amount
    }

    fun getInputItemParallel(machine: WorkableElectricMultiblockMachine, recipe: GTRecipe, parallelAmount: Long): Long {
        if (parallelAmount <= 1) return parallelAmount
        if (machine !is IRecipeCapabilityMachine) return 1
        if (machine.emptyRecipeHandlePart()) return 0

        val countableMap = O2LHashMap(ContentStrategy(ItemRecipeCapability.CAP))
        val confirmMEStock = machine is ParallelMachine && machine.needConfirmMEStock()

        for (content in recipe.getInputContents(ItemRecipeCapability.CAP))
            if (content.chance > 0) countableMap.addTo(content, content.amount(ItemRecipeCapability.CAP))

        if (countableMap.isEmpty()) return parallelAmount

        val ingredientStacks = O2LHashMap(comparingAllButCount())

        when (val handle = machine.getActiveRecipeHandle(recipe)) {
            is MEPatternRecipeHandlePart -> fastIterable(handle.getMEContent(ItemRecipeCapability.CAP, recipe)).forEach { (i, l) -> ingredientStacks.addTo(i, l) }
            is RecipeHandlePart -> fastIterable(handle.getSelfContent(ItemRecipeCapability.CAP, confirmMEStock)).forEach { (i, l) -> ingredientStacks.addTo(i, l) }
            else -> machine.sharedRecipeHandlePart?.let { fastIterable(it.getSelfContent(ItemRecipeCapability.CAP, confirmMEStock)).forEach { (i, l) -> ingredientStacks.addTo(i, l) } }
        }
        return calculate(machine, recipe, parallelAmount, countableMap, ingredientStacks, machine.recipeLogic.chanceCaches[ItemRecipeCapability.CAP] as? Object2IntMap<Ingredient>)
    }

    fun getInputFluidParallel(machine: WorkableElectricMultiblockMachine, recipe: GTRecipe, parallelAmount: Long): Long {
        if (parallelAmount <= 1) return parallelAmount
        if (machine !is IRecipeCapabilityMachine) return 1
        if (machine.emptyRecipeHandlePart()) return 0

        val fluidCountMap = O2LHashMap(ContentStrategy(FluidRecipeCapability.CAP))
        val confirmMEStock = machine is ParallelMachine && machine.needConfirmMEStock()

        for (content in recipe.getInputContents(FluidRecipeCapability.CAP))
            if (content.chance > 0) fluidCountMap.addTo(content, content.amount(FluidRecipeCapability.CAP))

        if (fluidCountMap.isEmpty()) return parallelAmount

        val ingredientStacks = O2LHashMap(comparingAllButAmount())

        when (val handle = machine.getActiveRecipeHandle(recipe)) {
            is MEPatternRecipeHandlePart -> fastIterable(handle.getMEContent(FluidRecipeCapability.CAP, recipe)).forEach { (f, l) -> ingredientStacks.addTo(f, l) }
            is RecipeHandlePart -> fastIterable(
                if (machine.isDistinct) {
                    handle.getSelfContent(FluidRecipeCapability.CAP, confirmMEStock)
                } else {
                    handle.getContentWithShared(FluidRecipeCapability.CAP, confirmMEStock)
                }
            ).forEach { (f, l) -> ingredientStacks.addTo(f, l) }
            else -> machine.sharedRecipeHandlePart?.let {
                for (handler in it.getCapability(FluidRecipeCapability.CAP)) {
                    if (handler is CatalystFluidStackHandler) continue
                    for (o in handler.contents) if (o is FluidStack) ingredientStacks.addTo(o, o.amount)
                }
            }
        }
        return calculate(machine, recipe, parallelAmount, fluidCountMap, ingredientStacks, machine.recipeLogic.chanceCaches[FluidRecipeCapability.CAP] as? Object2IntMap<FluidIngredient>)
    }

    private fun <I : Predicate<S>, S> calculate(machine: WorkableElectricMultiblockMachine, recipe: GTRecipe, parallelLimit: Long, countableMap: Object2LongMap<Content>, ingredientStacks: Object2LongMap<S>, cacheMap: Object2IntMap<I>?): Long {
        var parallelLimit = parallelLimit
        if (ingredientStacks.isEmpty()) return 0

        for ((k, v) in fastIterable(countableMap)) {
            var available = 0L

            val stackEntry = Object2LongMaps.fastIterator(ingredientStacks)
            while (stackEntry.hasNext()) {
                val input = stackEntry.next()
                if (k.test(input.key)) {
                    available = input.longValue
                    stackEntry.remove()
                    break
                }
            }
            if (available < v) return 0
            val bChance = recipe.recipeType.chanceFunction.getBoostedChance(k, recipe.euTier, machine.tier)
            if (k.chance >= k.maxChance || bChance >= k.maxChance) {
                parallelLimit = parallelLimit minToLong (available / v)
            } else {
                val cache = cacheMap?.computeIfAbsent(k.content as I) { GTValues.RNG.nextInt(k.maxChance) } ?: GTValues.RNG.nextInt(k.maxChance)
                val maxParallel = ((available / v + 1.0) * k.maxChance - (1 + cache)) / k.chance
                if (maxParallel < 0) return 0
                parallelLimit = parallelLimit minToLong maxParallel
            }
        }
        return parallelLimit
    }
}
