package com.gtladd.gtladditions.api.machine.logic

import org.gtlcore.gtlcore.api.machine.ISuspendableMachine
import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus
import org.gtlcore.gtlcore.api.recipe.RecipeResult
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.*
import org.gtlcore.gtlcore.common.machine.trait.MultipleRecipesLogic

import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.lookup.GTRecipeLookup

import com.gtladd.gtladditions.api.machine.GTLAddWorkableElectricMultipleRecipesMachine
import com.gtladd.gtladditions.api.machine.ICoilMachine
import com.gtladd.gtladditions.api.recipe.FastRecipeModify
import com.gtladd.gtladditions.api.recipe.MultiGTRecipeLookup
import com.gtladd.gtladditions.utils.GTRecipeUtils.euTier
import com.gtladd.gtladditions.utils.GTRecipeUtils.getMultipleRecipe
import com.gtladd.gtladditions.utils.GTRecipeUtils.getOverclockRecipe
import com.gtladd.gtladditions.utils.MathUtil.maxToInt
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

open class GTLAddMultipleRecipesLogic(private val gtlAddMachine: GTLAddWorkableElectricMultipleRecipesMachine) :
    MultipleRecipesLogic(gtlAddMachine), IRecipeStatus {

    override fun findAndHandleRecipe() {
        lastRecipe = null
        recipeStatus = null
        (if (gtlAddMachine.isMultipleMode) this.getMultipleRecipe else this.getOverclockRecipe)?.let { recipe ->
            if (matchRecipeOutput(machine, recipe)) setupRecipe(recipe)
        }
    }

    val getMultipleRecipe: GTRecipe?
        get() = gtlAddMachine.getMultipleRecipe(lookupRecipeIterator(), gtlAddMachine::testBefore, gtlAddMachine::modifyRecipe, gtlAddMachine.getThread(), gtlAddMachine.limitedDuration)

    val getOverclockRecipe: GTRecipe?
        get() = gtlAddMachine.getOverclockRecipe(::findAndModifyRecipe, gtlAddMachine::testBefore, gtlAddMachine.getThread(), gtlAddMachine.limitedDuration)

    private fun lookupRecipeIterator(): MutableSet<GTRecipe> {
        if (this.isLock) {
            val recipe = when {
                lockRecipe == null -> getLookup().find(machine, ::checkRecipe)
                checkRecipe(lockRecipe) -> lockRecipe
                else -> return mutableSetOf<GTRecipe>()
            } ?: return mutableSetOf<GTRecipe>()
            return mutableSetOf(recipe)
        } else {
            val recipeSet = ObjectOpenHashSet<GTRecipe>()
            val lookup = getLookup()
            if (lookup is MultiGTRecipeLookup) {
                recipeSet.addAll(lookup.getMultiRecipeIterator(machine, ::checkRecipe).multipleRecipes)
            } else {
                val iterator = lookup.getRecipeIterator(machine, ::checkRecipe)
                while (iterator.hasNext()) recipeSet.add(iterator.next())
            }
            recipeSet.remove(null)
            return recipeSet
        }
    }

    private fun findAndModifyRecipe(parallel: Long): GTRecipe? {
        if (this.isLock) {
            val recipe = when {
                lockRecipe == null -> getLookup().find(machine, ::checkRecipe)
                checkRecipe(lockRecipe) -> lockRecipe
                else -> return null
            } ?: return null
            FastRecipeModify.modify(
                gtlAddMachine,
                recipe,
                parallel,
                ocResult = gtlAddMachine.getOverClock(),
                reResult = gtlAddMachine::modifyRecipe
            )?.let { if (checkRecipe(it)) return it }
        } else {
            getLookup().find(machine, ::checkRecipe)?.let { recipe ->
                FastRecipeModify.modify(
                    gtlAddMachine,
                    recipe,
                    parallel,
                    ocResult = gtlAddMachine.getOverClock(),
                    reResult = gtlAddMachine::modifyRecipe
                )?.let { if (checkRecipe(it)) return it }
            }
        }
        return null
    }

    open fun getLookup(): GTRecipeLookup = machine.recipeType.lookup

    override fun onRecipeFinish() {
        lastRecipe?.let { handleRecipeOutput(machine, it) }
        if (machine is ISuspendableMachine) {
            val ism = machine as ISuspendableMachine
            if (ism.`gtlcore$isSuspendAfterFinish`()) {
                this.status = Status.SUSPEND
                ism.`gtlcore$setSuspendAfterFinish`(false)
            } else {
                (if (gtlAddMachine.isMultipleMode) this.getMultipleRecipe else this.getOverclockRecipe)?.let { recipe ->
                    if (matchRecipeOutput(machine, recipe)) {
                        setupRecipe(recipe)
                        return
                    }
                }
                status = Status.IDLE
            }
        }
        progress = 0
        duration = 0
    }

    open fun checkRecipe(recipe: GTRecipe) = matchRecipe(machine, recipe) &&
        recipe.euTier <= getMachine().getTier() && recipe.checkConditions(machine.recipeLogic).isSuccess &&
        (machine as? ICoilMachine)?.let {
            val temp = it.coilType.coilTemperature + 100L * (0 maxToInt (getMachine().getTier() - 2))
            if (temp < recipe.data.getInt("ebf_temp")) {
                RecipeResult.of(machine, RecipeResult.FAIL_NO_ENOUGH_TEMPERATURE)
                return@let false
            }
            return@let true
        } ?: true
}
