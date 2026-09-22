package com.gtladd.gtladditions.api.machine.logic

import org.gtlcore.gtlcore.api.machine.ISuspendableMachine
import org.gtlcore.gtlcore.api.machine.trait.ILockRecipe
import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus
import org.gtlcore.gtlcore.api.recipe.IGTRecipe
import org.gtlcore.gtlcore.api.recipe.RecipeResult
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.handleRecipeOutput
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.matchRecipe

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic
import com.gregtechceu.gtceu.api.recipe.GTRecipe

import net.minecraft.nbt.CompoundTag

import com.gtladd.gtladditions.api.machine.ICoilMachine
import com.gtladd.gtladditions.api.machine.IEnergyMachine
import com.gtladd.gtladditions.api.machine.MultipleRecipeTypesMachine
import com.gtladd.gtladditions.api.recipe.OptimizedRecipeSearch
import com.gtladd.gtladditions.utils.GTRecipeUtils.getEU
import com.gtladd.gtladditions.utils.GTRecipeUtils.handleEUt
import com.gtladd.gtladditions.utils.GTRecipeUtils.matchEUt
import com.gtladd.gtladditions.utils.GTRecipeUtils.withSearchContext
import com.gtladd.gtladditions.utils.MathUtil.maxToInt

class MultiRecipeTypesLogic(private val multiTypeMachine: MultipleRecipeTypesMachine) :
    RecipeLogic(multiTypeMachine), ILockRecipe, IRecipeStatus {
    private var eut = 0L

    override fun getMachine(): MultipleRecipeTypesMachine {
        return super.getMachine() as MultipleRecipeTypesMachine
    }

    override fun findAndHandleRecipe() {
        lastRecipe = null
        lastOriginRecipe = null
        recipeStatus = null
        if (this.isLock && lockRecipe != null) {
            this.lastOriginRecipe = lockRecipe
            multiTypeMachine.modifyRecipe(lockRecipe)?.let { if (checkRecipe(it)) setupRecipe(it) }
        } else {
            multiTypeMachine.withSearchContext { ctx ->
                if (ctx != null) {
                    handleSearchingRecipes(OptimizedRecipeSearch.find(multiTypeMachine, OptimizedRecipeSearch.branchOf(multiTypeMachine.multiRecipeType.lookup), ::checkConditionsOnly))
                }
            }
        }
    }

    override fun setupRecipe(recipe: GTRecipe) {
        if (!this.machine.beforeWorking(recipe)) {
            this.status = Status.IDLE
            this.progress = 0
            this.duration = 0
            return
        }
        if (this.handleRecipeIO(recipe, IO.IN)) {
            if (this.lastRecipe != null && recipe != this.lastRecipe) {
                this.chanceCaches.clear()
            }
            this.eut = recipe.getEU
            this.lastRecipe = recipe
            this.status = Status.WORKING
            this.progress = 0
            this.duration = recipe.duration
        }
    }

    override fun handleRecipeWorking() {
        checkNotNull(this.lastRecipe)

        if (eut.matchEUt(multiTypeMachine as IEnergyMachine)) {
            this.status = Status.WORKING
            eut.handleEUt(multiTypeMachine)
            if (!this.machine.onWorking()) {
                this.interruptRecipe()
                return
            }
            ++this.progress
            ++this.totalContinuousRunningTime
        } else {
            this.setWaiting(null)
        }

        if (this.status == Status.WAITING) this.doDamping()
    }

    private fun handleSearchingRecipes(recipe: GTRecipe?): Boolean {
        recipe?.let {
            multiTypeMachine.modifyRecipe(it)?.let { modify ->
                if (checkRecipe(modify)) {
                    if (isLock) lockRecipe = it
                    lastOriginRecipe = it
                    setupRecipe(modify)
                    return true
                }
            }
        }
        return false
    }

    override fun onRecipeFinish() {
        machine.afterWorking()
        lastRecipe?.let { handleRecipeOutput(machine, it) }
        if (machine is ISuspendableMachine) {
            val ism = machine as ISuspendableMachine
            if (ism.`gtlcore$isSuspendAfterFinish`()) {
                this.status = Status.SUSPEND
                ism.`gtlcore$setSuspendAfterFinish`(false)
            } else {
                lastOriginRecipe?.let { if (handleSearchingRecipes(it)) return }
                status = Status.IDLE
            }
        }
        progress = 0
        duration = 0
    }

    override fun saveCustomPersistedData(tag: CompoundTag, forDrop: Boolean) {
        super.saveCustomPersistedData(tag, forDrop)
        tag.putLong("eut", eut)
    }

    override fun loadCustomPersistedData(tag: CompoundTag) {
        super.loadCustomPersistedData(tag)
        if (tag.contains("eut")) eut = tag.getLong("eut")
    }

    private fun checkConditionsOnly(recipe: GTRecipe): Boolean = IGTRecipe.of(recipe).euTier <= multiTypeMachine.tier && recipe.checkConditions(this).isSuccess &&
        (machine as? ICoilMachine)?.let { coilOk(it, recipe) } ?: true

    private fun coilOk(coil: ICoilMachine, recipe: GTRecipe): Boolean {
        val temp = coil.coilType.coilTemperature + 100L * (0 maxToInt (getMachine().getTier() - 2))
        if (temp < recipe.data.getInt("ebf_temp")) {
            RecipeResult.of(machine, RecipeResult.FAIL_NO_ENOUGH_TEMPERATURE)
            return false
        }
        return true
    }

    private fun checkRecipe(recipe: GTRecipe): Boolean = matchRecipe(this.machine, recipe) &&
        recipe.matchTickRecipe(machine).isSuccess && recipe.checkConditions(this).isSuccess && (machine as? ICoilMachine)?.let { coilOk(it, recipe) } ?: true
}
