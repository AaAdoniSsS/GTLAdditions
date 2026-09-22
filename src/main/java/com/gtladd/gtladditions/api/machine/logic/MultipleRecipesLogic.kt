package com.gtladd.gtladditions.api.machine.logic

import org.gtlcore.gtlcore.api.machine.ISuspendableMachine
import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus
import org.gtlcore.gtlcore.api.recipe.RecipeResult
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.*
import org.gtlcore.gtlcore.common.machine.trait.MultipleRecipesLogic

import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.lookup.GTRecipeLookup

import com.gtladd.gtladditions.api.machine.ICoilMachine
import com.gtladd.gtladditions.api.machine.MultipleRecipesMachine
import com.gtladd.gtladditions.api.recipe.FastRecipeModify
import com.gtladd.gtladditions.api.recipe.OptimizedRecipeSearch
import com.gtladd.gtladditions.api.recipe.ledger.RecipeSearchContext
import com.gtladd.gtladditions.utils.GTRecipeUtils.euTier
import com.gtladd.gtladditions.utils.GTRecipeUtils.getMultipleRecipe
import com.gtladd.gtladditions.utils.GTRecipeUtils.getOverclockRecipe
import com.gtladd.gtladditions.utils.GTRecipeUtils.withSearchContext
import com.gtladd.gtladditions.utils.MathUtil.maxToInt
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

open class MultipleRecipesLogic(private val gtlAddMachine: MultipleRecipesMachine) :
    MultipleRecipesLogic(gtlAddMachine), IRecipeStatus {

    override fun findAndHandleRecipe() {
        lastRecipe = null
        recipeStatus = null
        gtlAddMachine.withSearchContext { ctx ->
            (if (gtlAddMachine.isMultipleMode) getMultipleRecipe(ctx) else getOverclockRecipe(ctx))?.let { recipe ->
                if (matchRecipeOutput(machine, recipe)) setupRecipe(recipe)
            }
        }
    }

    protected fun getMultipleRecipe(ctx: RecipeSearchContext?): GTRecipe? = gtlAddMachine.getMultipleRecipe(lookupRecipeIterator(ctx), gtlAddMachine::testBefore, gtlAddMachine::modifyRecipe, gtlAddMachine.getThread(), gtlAddMachine.limitedDuration, ctx)

    private fun getOverclockRecipe(ctx: RecipeSearchContext?): GTRecipe? = gtlAddMachine.getOverclockRecipe(::findAndModifyRecipe, gtlAddMachine::testBefore, gtlAddMachine.getThread(), gtlAddMachine.limitedDuration, ctx)

    private fun lookupRecipeIterator(ctx: RecipeSearchContext?): MutableSet<GTRecipe> {
        if (this.isLock) {
            val recipe = when {
                lockRecipe == null -> OptimizedRecipeSearch.find(gtlAddMachine, OptimizedRecipeSearch.branchOf(getLookup()), ::checkConditionsOnly)
                checkRecipe(lockRecipe) -> lockRecipe
                else -> return mutableSetOf<GTRecipe>()
            } ?: return mutableSetOf<GTRecipe>()
            return mutableSetOf(recipe)
        } else {
            val recipeSet = ObjectOpenHashSet<GTRecipe>()
            val lookup = getLookup()
            if (ctx != null) {
                recipeSet.addAll(OptimizedRecipeSearch.collectCandidates(gtlAddMachine, OptimizedRecipeSearch.branchOf(lookup), ::checkConditionsOnly))
            }
            recipeSet.remove(null)
            return recipeSet
        }
    }

    private fun findAndModifyRecipe(parallel: Long): GTRecipe? {
        val ctx = gtlAddMachine.getActiveSearchContext()
        if (this.isLock) {
            val recipe = when {
                lockRecipe == null -> OptimizedRecipeSearch.find(gtlAddMachine, OptimizedRecipeSearch.branchOf(getLookup()), ::checkConditionsOnly)
                checkRecipe(lockRecipe) -> lockRecipe
                else -> return null
            } ?: return null
            return FastRecipeModify.modify(
                gtlAddMachine,
                recipe,
                parallel,
                ocResult = gtlAddMachine.getOverClock(),
                reResult = gtlAddMachine::modifyRecipe
            )?.let { if (checkRecipe(it)) it else null }
        }
        val recipe = if (ctx != null) {
            OptimizedRecipeSearch.find(gtlAddMachine, OptimizedRecipeSearch.branchOf(getLookup()), ::checkConditionsOnly)
        } else {
            null
        } ?: return null
        var p = parallel
        var attempts = 0
        while (true) {
            val modified = FastRecipeModify.modify(
                gtlAddMachine,
                recipe,
                p,
                ocResult = gtlAddMachine.getOverClock(),
                reResult = gtlAddMachine::modifyRecipe
            ) ?: return null
            if (ctx == null) return modified
            val shrink = ctx.preCheckRecipe(modified)
            if (shrink >= 1.0) return modified
            var np = (p * shrink).toLong()
            if (np >= p) np = p - 1
            if (np < 1 || ++attempts >= 4) return null
            p = np
        }
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
                val continued = gtlAddMachine.withSearchContext { ctx ->
                    (if (gtlAddMachine.isMultipleMode) getMultipleRecipe(ctx) else getOverclockRecipe(ctx))?.let { recipe ->
                        if (matchRecipeOutput(machine, recipe)) {
                            setupRecipe(recipe)
                            return@withSearchContext true
                        }
                    }
                    false
                }
                if (continued) return
                status = Status.IDLE
            }
        }
        progress = 0
        duration = 0
    }

    open fun checkConditionsOnly(recipe: GTRecipe): Boolean = recipe.euTier <= getMachine().getTier() && recipe.checkConditions(machine.recipeLogic).isSuccess &&
        (machine as? ICoilMachine)?.let { coilOk(it, recipe) } ?: true

    private fun coilOk(coil: ICoilMachine, recipe: GTRecipe): Boolean {
        val temp = coil.coilType.coilTemperature + 100L * (0 maxToInt (getMachine().getTier() - 2))
        if (temp < recipe.data.getInt("ebf_temp")) {
            RecipeResult.of(machine, RecipeResult.FAIL_NO_ENOUGH_TEMPERATURE)
            return false
        }
        return true
    }

    protected fun checkRecipe(recipe: GTRecipe) = matchRecipe(machine, recipe) && checkConditionsOnly(recipe)
}
