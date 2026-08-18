package com.gtladd.gtladditions.common.machine.multiblock.controller

import org.gtlcore.gtlcore.api.machine.ISuspendableMachine
import org.gtlcore.gtlcore.api.machine.multiblock.ParallelMachine
import org.gtlcore.gtlcore.api.machine.trait.ILockRecipe
import org.gtlcore.gtlcore.api.machine.trait.IRecipeCapabilityMachine
import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus
import org.gtlcore.gtlcore.api.recipe.RecipeResult
import org.gtlcore.gtlcore.api.recipe.RecipeResult.fail
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.handleRecipeOutput
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.matchRecipe
import org.gtlcore.gtlcore.common.data.GTLRecipeModifiers

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability.CAP
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMufflerMachine
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component

import com.gtladd.gtladditions.api.machine.gui.MultiblockDisplayText
import com.gtladd.gtladditions.api.recipe.FastRecipeModify
import com.gtladd.gtladditions.api.recipe.FastRecipeModify.OverClockFactor
import com.gtladd.gtladditions.api.recipe.FastRecipeModify.getNoPerfectOverclock
import com.gtladd.gtladditions.api.recipe.FastRecipeModify.getPerfectOverclock
import com.gtladd.gtladditions.common.machine.hatch.OreProcessorHatch
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import com.gtladd.gtladditions.utils.GTRecipeUtils.getEU
import com.gtladd.gtladditions.utils.GTRecipeUtils.getFastMultipleRecipe
import com.gtladd.gtladditions.utils.GTRecipeUtils.getOverclockRecipe
import com.gtladd.gtladditions.utils.MathUtil.maxToInt

@Suppress("CAST_NEVER_SUCCEEDS")
class OreProcessorMachine(holder: IMachineBlockEntity, private val isAdvanced: Boolean) :
    WorkableElectricMultiblockMachine(holder), ParallelMachine {

    private var opHatch: OreProcessorHatch? = null
    private var muffler: IMufflerMachine? = null

    override fun onStructureFormed() {
        super.onStructureFormed()
        parts.forEach {
            when (it) {
                is OreProcessorHatch -> this.opHatch = it
                is IMufflerMachine -> this.muffler = it
            }
        }
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        this.opHatch = null
        this.muffler = null
    }

    override fun createRecipeLogic(vararg args: Any): RecipeLogic = OreProcessorRecipeLogic(this)

    override fun getRecipeLogic(): OreProcessorRecipeLogic = super.recipeLogic as OreProcessorRecipeLogic

    override fun attachConfigurators(configuratorPanel: ConfiguratorPanel) {
        configuratorPanel.attachConfigurators(
            IFancyConfiguratorButton.Toggle(
                GuiTextures.BUTTON_POWER.getSubTexture(0.0, 0.0, 1.0, 0.5),
                GuiTextures.BUTTON_POWER.getSubTexture(0.0, 0.5, 1.0, 0.5),
                { this.isWorkingEnabled },
                { _, pressed -> this.isWorkingEnabled = pressed }
            )
                .setTooltipsSupplier { listOf((if (it) "behaviour.soft_hammer.enabled" else "behaviour.soft_hammer.disabled").toComponent) }
        )
        IRecipeCapabilityMachine.attachConfigurators(configuratorPanel, self() as WorkableElectricMultiblockMachine)
    }

    override fun addDisplayText(textList: MutableList<Component>) {
        MultiblockDisplayText.builder(textList, isFormed())
            .setWorkingStatus(recipeLogic.isWorkingEnabled, recipeLogic.isActive)
            .addEnergyUsageLine(energyContainer)
            .addEnergyTierLine(tier)
            .addMachineModeLine(recipeType)
            .addParallelsLine(maxParallel)
            .addWorkingStatusLine()
            .addProgressLine(recipeLogic.progressPercent)
            .addRecipeStatus(recipeLogic as IRecipeStatus)
    }

    override fun getMaxParallel(): Int = if (this.isAdvanced) Int.MAX_VALUE else GTLRecipeModifiers.getHatchParallel(this)

    class OreProcessorRecipeLogic(private val opMachine: OreProcessorMachine) : RecipeLogic(opMachine), IRecipeStatus, ILockRecipe {
        private var eut: Long = 0

        override fun findAndHandleRecipe() {
            this.lastRecipe = null
            this.lastOriginRecipe = null
            this.recipeStatus = null
            handleGTRecipe()
            this.recipeDirty = false
        }

        private fun handleGTRecipe(): Boolean {
            if (opMachine.opHatch == null) {
                if (opMachine.isAdvanced) {
                    opMachine.getFastMultipleRecipe(
                        ::findAndModifyRecipe,
                        64,
                        20
                    )?.let { return handleRecipe(it) }
                } else {
                    (lastOriginRecipe ?: opMachine.recipeType.lookup.find(opMachine, this::checkRecipe))?.let { recipe ->
                        if (!opMachine.isAdvanced && !opMachine.muffler?.isFrontFaceFree!!) return false
                        FastRecipeModify.modify(opMachine, recipe, opMachine.maxParallel.toLong(), false, getNoPerfectOverclock()) { FastRecipeModify.ReduceResult(1.0, getMaintenanceModify) }?.let {
                            if (checkRecipe(it)) {
                                this.lastOriginRecipe = recipe
                                setupRecipe(it)
                                return true
                            }
                        }
                    }
                }
            } else {
                if (getSub) {
                    opMachine.getOverclockRecipe(::findAndModifyRecipe, maxThread = getMachineThread, minDuration = getRecipeDuration)?.let { return handleRecipe(it) }
                } else {
                    opMachine.getFastMultipleRecipe(::findAndModifyRecipe, getMachineThread, getRecipeDuration)?.let { return handleRecipe(it) }
                }
            }
            return false
        }

        private val getMaintenanceModify: Double get() = (opMachine as IRecipeCapabilityMachine).maintenanceMachine?.durationMultiplier?.toDouble() ?: 1.0

        private val getRecipeDuration: Int get() = opMachine.opHatch?.matchAll?.let { if (it) 10 else 20 } ?: 20

        private val getSub: Boolean get() = opMachine.opHatch?.matchAll == true

        private val getOverClock: OverClockFactor get() = if (opMachine.opHatch?.matchAll == true) getPerfectOverclock() else getNoPerfectOverclock()

        private val getMachineThread: Int get() =
            when (opMachine.opHatch!!.firstTier) {
                3 -> if (opMachine.isAdvanced) 144 else 10
                2 -> if (opMachine.isAdvanced) 128 else 8
                1 -> if (opMachine.isAdvanced) 96 else 6
                else -> if (opMachine.isAdvanced) 72 else 4
            }

        private val getRecipeChance: Int get() = opMachine.opHatch!!.secondTier + 1

        private val getRecipeReduceTime: Double get() =
            when (opMachine.opHatch!!.thirdTier) {
                3 -> .4
                2 -> .5
                1 -> .65
                else -> .8
            }

        private val getRecipeReduceFluid: Double get() =
            when (opMachine.opHatch!!.thirdTier) {
                3 -> .4
                2 -> .5
                1 -> .65
                else -> .8
            }

        private fun handleRecipe(recipe: GTRecipe): Boolean {
            if (matchRecipe(opMachine, recipe)) {
                setupRecipe(recipe)
                return true
            }
            return false
        }

        private fun findAndModifyRecipe(parallel: Long): GTRecipe? {
            takeIf { opMachine.isAdvanced || opMachine.muffler?.isFrontFaceFree!! }?.let {
                opMachine.recipeType.lookup.find(opMachine, this::checkRecipe)?.let { recipe ->
                    FastRecipeModify.copyModify(
                        opMachine,
                        recipe,
                        parallel,
                        getSub,
                        getSub,
                        getOverClock,
                        ::modifyRecipe
                    )?.let { if (checkRecipe(it)) return it }
                }
            }
            return null
        }

        private fun modifyRecipe(recipe: GTRecipe): GTRecipe {
            takeIf { opMachine.opHatch != null }?.let {
                if (!opMachine.isAdvanced) {
                    recipe.duration = 1 maxToInt (recipe.duration * getMaintenanceModify * getRecipeReduceTime).toInt()
                } else {
                    recipe.inputs[CAP]?.forEach { (it.content as FluidIngredient).let { ing -> ing.amount = (ing.amount * getRecipeReduceFluid).toLong() } }
                }
                recipe.outputs[ItemRecipeCapability.CAP]?.forEach { it.tierChanceBoost *= getRecipeChance }
            }
            return recipe
        }

        override fun setupRecipe(recipe: GTRecipe) {
            if (this.handleRecipeIO(recipe, IO.IN)) {
                if (this.lastRecipe != null && recipe != this.lastRecipe) this.chanceCaches.clear()
                this.eut = recipe.getEU
                this.lastRecipe = recipe
                this.status = Status.WORKING
                this.progress = 0
                this.duration = recipe.duration
            }
        }

        override fun handleRecipeWorking() {
            checkNotNull(this.lastRecipe)

            if (eut > 0 && eut <= this.opMachine.energyContainer.energyStored) {
                this.status = Status.WORKING
                this.opMachine.energyContainer.changeEnergy(-eut)
                ++this.progress
                ++this.totalContinuousRunningTime
            } else {
                this.setWaiting(null)
            }

            if (this.status == Status.WAITING) this.doDamping()
        }

        override fun onRecipeFinish() {
            opMachine.afterWorking()
            lastRecipe?.let { handleRecipeOutput(opMachine, it) }
            if (machine is ISuspendableMachine) {
                val ism = machine as ISuspendableMachine
                if (ism.`gtlcore$isSuspendAfterFinish`()) {
                    this.status = Status.SUSPEND
                    ism.`gtlcore$setSuspendAfterFinish`(false)
                } else {
                    if (handleGTRecipe()) return
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

        private fun checkRecipe(recipe: GTRecipe): Boolean {
            if (recipe.data.contains("handle") && !opMachine.isAdvanced) {
                RecipeResult.of(opMachine, fail("gtceu.integrated_ore_processor.advanced".toComponent))
                return false
            }
            return matchRecipe(this.opMachine, recipe) && recipe.matchTickRecipe(opMachine).isSuccess
        }
    }
}
