package com.gtladd.gtladditions.api.machine.logic

import org.gtlcore.gtlcore.api.machine.ISuspendableMachine
import org.gtlcore.gtlcore.api.machine.trait.ILockRecipe
import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus
import org.gtlcore.gtlcore.api.recipe.RecipeResult
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.handleRecipeOutput
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.matchRecipe

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic
import com.gregtechceu.gtceu.api.recipe.GTRecipe

import net.minecraft.nbt.CompoundTag

import com.gtladd.gtladditions.api.machine.GTLAddWorkableElectricMultipleRecipeTypeMachine
import com.gtladd.gtladditions.api.machine.ICoilMachine
import com.gtladd.gtladditions.api.machine.IEnergyMachine
import com.gtladd.gtladditions.utils.GTRecipeUtils.getEU
import com.gtladd.gtladditions.utils.MathUtil.maxToInt

class GTLAddMultiRecipeTypeLogic(private val multiTypeMachine: GTLAddWorkableElectricMultipleRecipeTypeMachine) :
    RecipeLogic(multiTypeMachine), ILockRecipe, IRecipeStatus {
    private var eut = 0L

    override fun getMachine(): GTLAddWorkableElectricMultipleRecipeTypeMachine {
        return super.getMachine() as GTLAddWorkableElectricMultipleRecipeTypeMachine
    }

    override fun findAndHandleRecipe() {
        lastRecipe = null
        lastOriginRecipe = null
        recipeStatus = null
        if (this.isLock && lockRecipe != null) {
            this.lastOriginRecipe = lockRecipe
            multiTypeMachine.modifyRecipe(lockRecipe)?.let { if (checkRecipe(it)) setupRecipe(it) }
        } else {
            handleSearchingRecipes(multiTypeMachine.multiRecipeType.lookup.find(this.machine, this::checkRecipe))
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

        val energyMachine = multiTypeMachine as IEnergyMachine
        if (eut > 0 && eut <= energyMachine.energyContainerList.energyStored) {
            this.status = Status.WORKING
            energyMachine.energyContainerList.changeEnergy(-eut)
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

    private fun checkRecipe(recipe: GTRecipe): Boolean = matchRecipe(this.machine, recipe) &&
        recipe.matchTickRecipe(machine).isSuccess && recipe.checkConditions(this).isSuccess &&
        (machine as? ICoilMachine)?.let {
            val temp = it.coilType.coilTemperature + 100L * (0 maxToInt (getMachine().getTier() - 2))
            if (temp < recipe.data.getInt("ebf_temp")) {
                RecipeResult.of(machine, RecipeResult.FAIL_NO_ENOUGH_TEMPERATURE)
                return@let false
            }
            return@let true
        } ?: true
}
