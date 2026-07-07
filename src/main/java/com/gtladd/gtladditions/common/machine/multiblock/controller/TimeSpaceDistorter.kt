package com.gtladd.gtladditions.common.machine.multiblock.controller

import org.gtlcore.gtlcore.api.machine.ISuspendableMachine
import org.gtlcore.gtlcore.api.machine.multiblock.ParallelMachine
import org.gtlcore.gtlcore.api.machine.trait.ICheckPatternMachine
import org.gtlcore.gtlcore.api.machine.trait.IRecipeCapabilityMachine
import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus
import org.gtlcore.gtlcore.api.recipe.IGTRecipe
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper
import org.gtlcore.gtlcore.common.data.GTLMaterials.*
import org.gtlcore.gtlcore.common.data.GTLRecipeModifiers
import org.gtlcore.gtlcore.common.data.GTLRecipeTypes.DISTORT_RECIPES

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CombinedDirectionalFancyConfigurator
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier
import com.gregtechceu.gtceu.api.recipe.lookup.GTRecipeLookup

import com.lowdragmc.lowdraglib.gui.util.ClickData
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget.withButton
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack

import com.gtladd.gtladditions.api.machine.IEnergyMachine
import com.gtladd.gtladditions.api.machine.IMultipleRecipeTypeMachine
import com.gtladd.gtladditions.api.machine.gui.MultiblockDisplayText
import com.gtladd.gtladditions.api.recipe.FastRecipeModify
import com.gtladd.gtladditions.common.machine.multiblock.controller.Resource.Hypercube
import com.gtladd.gtladditions.common.machine.multiblock.controller.Resource.QuantumAnomaly
import com.gtladd.gtladditions.common.recipe.GTLAddRecipesTypes
import com.gtladd.gtladditions.common.recipe.GTLAddRecipesTypes.TimeSpaceDistorterType
import com.gtladd.gtladditions.utils.ComponentUtil.literal
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import com.gtladd.gtladditions.utils.GTRecipeUtils.getEU
import com.gtladd.gtladditions.utils.GTRecipeUtils.getOverclockRecipe
import com.gtladd.gtladditions.utils.GTRecipeUtils.longParallel
import com.gtladd.gtladditions.utils.GTRecipeUtils.modify
import com.gtladd.gtladditions.utils.MachineUtil.inputFluidStack
import com.gtladd.gtladditions.utils.MachineUtil.inputItemStack
import com.gtladd.gtladditions.utils.MachineUtil.maintenance
import com.gtladd.gtladditions.utils.MathUtil.maxToLong
import com.gtladd.gtladditions.utils.MathUtil.pow
import com.gtladd.gtladditions.utils.MathUtil.safeToInt
import it.unimi.dsi.fastutil.ints.IntArrayList

open class TimeSpaceDistorter(holder: IMachineBlockEntity) :
    WorkableElectricMultiblockMachine(holder), IMultipleRecipeTypeMachine, ParallelMachine {
    @Persisted
    private var config = 1

    @Persisted
    private var isMultiple = false

    override fun createRecipeLogic(vararg args: Any): TimeSpaceDistorterRecipeLogic = TimeSpaceDistorterRecipeLogic(this)

    override fun getRecipeLogic(): TimeSpaceDistorterRecipeLogic = super.getRecipeLogic() as TimeSpaceDistorterRecipeLogic

    override fun getFieldHolder(): ManagedFieldHolder = MANAGED_FIELD_HOLDER

    override fun attachConfigurators(configuratorPanel: ConfiguratorPanel) {
        configuratorPanel.attachConfigurators(
            IFancyConfiguratorButton.Toggle(
                GuiTextures.BUTTON_POWER.getSubTexture(0.0, 0.0, 1.0, 0.5),
                GuiTextures.BUTTON_POWER.getSubTexture(0.0, 0.5, 1.0, 0.5),
                { this.isWorkingEnabled },
                { _, pressed -> this.isWorkingEnabled = pressed }
            )
                .setTooltipsSupplier { listOf(if (it) "behaviour.soft_hammer.enabled".toComponent else "behaviour.soft_hammer.disabled".toComponent) }
        )
        ICheckPatternMachine.attachConfigurators(configuratorPanel, self())
        IRecipeCapabilityMachine.attachConfigurators(configuratorPanel, self() as WorkableElectricMultiblockMachine)
    }

    override fun addDisplayText(textList: MutableList<Component>) {
        MultiblockDisplayText.builder(textList, isFormed())
            .setWorkingStatus(recipeLogic.isWorkingEnabled, recipeLogic.isActive)
            .addEnergyUsageLine(energyContainer)
            .addEnergyTierLine(tier)
            .addMachineModeLine(multiRecipeType)
            .addParallelsLine(maxParallel)
            .addWorkingStatusLine()
            .addProgressLine(recipeLogic.progressPercent)
            .addRecipeStatus(recipeLogic as IRecipeStatus)
            .addComponent(
                Component.translatable("gui.gtladditions.time_space_distorter_amplification_amount", this.config),
                "gui.gtladditions.time_space_distorter.modification_amplification".toComponent
                    .append(withButton("[-] ".literal, "ocSub"))
                    .append(withButton("[+]".literal, "ocAdd")),
                "gtceu.machine.multiple_recipe.gui.0".toComponent.append(": ")
                    .append(
                        withButton(
                            "[".literal
                                .append((if (this.isMultiple) "gtceu.machine.on" else "gtceu.machine.off").toComponent)
                                .append("]".literal),
                            "multiple_switch"
                        )
                    )
            )
    }

    override fun attachSideTabs(sideTabs: TabsWidget) {
        sideTabs.setMainTab(this)
        CombinedDirectionalFancyConfigurator.of(self(), self())?.let { sideTabs.attachSubTab(it) }
    }

    override fun handleDisplayClick(componentData: String, clickData: ClickData) {
        if (!clickData.isRemote) {
            if (componentData == "multiple_switch") {
                this.isMultiple = !this.isMultiple
            } else {
                this.config = Mth.clamp(this.config + (if (componentData == "ocAdd") 1 else -1), 1, 3)
            }
        }
    }

    override val multiRecipeTypes: Array<GTRecipeType> = arrayOf(TimeSpaceDistorterType)

    override fun getMaxParallel(): Int = GTLRecipeModifiers.getHatchParallel(this)

    class TimeSpaceDistorterRecipeLogic(val tsdMachine: TimeSpaceDistorter) : RecipeLogic(tsdMachine), IRecipeStatus {
        private var eut = 0L
        val recipeList = IntArrayList()
        private val failedRecipeIds = mutableSetOf<Int>()

        override fun findAndHandleRecipe() {
            this.lastRecipe = null
            this.lastOriginRecipe = null
            this.recipeStatus = null
            this.failedRecipeIds.clear()
            this.recipeList.clear()
            if (tsdMachine.isMultiple) {
                tsdMachine.getOverclockRecipe(::findAndModifyRecipe, maxThread = 16, minDuration = 1)?.let {
                    if (RecipeRunnerHelper.matchRecipeOutput(tsdMachine, it)) setupRecipe(it)
                }
            } else {
                lookup.find(machine, ::checkRecipe)?.let { recipe ->
                    this.modifyRecipe(recipe, tsdMachine.maxParallel.toLong())?.let {
                        lastOriginRecipe = recipe
                        setupRecipe(it)
                    }
                }
            }
        }

        private fun findAndModifyRecipe(parallel: Long): GTRecipe? {
            var recipe = lookup.find(machine, ::checkRecipe)
            while (recipe != null) {
                this.modifyRecipe(recipe, parallel)?.let { modifiedRecipe ->
                    this.recipeList.add(recipe.id.hashCode())
                    return modifiedRecipe
                }
                this.failedRecipeIds.add(recipe.id.hashCode())
                recipe = lookup.find(machine, ::checkRecipe)
            }
            return null
        }

        override fun setupRecipe(recipe: GTRecipe) {
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
            checkNotNull(lastRecipe)
            val energyMachine = tsdMachine as IEnergyMachine
            if (eut > 0 && eut <= energyMachine.energyContainerList.energyStored) {
                this.status = Status.WORKING
                energyMachine.energyContainerList.changeEnergy(-eut)
                ++this.progress
            } else {
                this.setWaiting(null)
            }
            if (this.status == Status.WAITING) this.doDamping()
        }

        override fun onRecipeFinish() {
            lastRecipe?.let { RecipeRunnerHelper.handleRecipeOutput(tsdMachine, it) }
            this.recipeList.clear()
            this.failedRecipeIds.clear()
            if (tsdMachine is ISuspendableMachine) {
                val ism = tsdMachine as ISuspendableMachine
                if (ism.`gtlcore$isSuspendAfterFinish`()) {
                    this.status = Status.SUSPEND
                    ism.`gtlcore$setSuspendAfterFinish`(false)
                } else {
                    if (tsdMachine.isMultiple) {
                        tsdMachine.getOverclockRecipe(::findAndModifyRecipe, maxThread = 16, minDuration = 1)?.let {
                            if (RecipeRunnerHelper.matchRecipeOutput(tsdMachine, it)) {
                                setupRecipe(it)
                                return
                            }
                        }
                    } else {
                        (lastOriginRecipe ?: lookup.find(machine, ::checkRecipe))?.let { recipe ->
                            this.modifyRecipe(recipe, tsdMachine.maxParallel.toLong())?.let {
                                lastOriginRecipe = recipe
                                setupRecipe(it)
                                return
                            }
                        }
                    }
                    this.status = Status.IDLE
                }
            }
            this.progress = 0
            this.duration = 0
        }

        override fun saveCustomPersistedData(tag: CompoundTag, forDrop: Boolean) {
            super.saveCustomPersistedData(tag, forDrop)
            tag.putLong("eut", eut)
        }

        override fun loadCustomPersistedData(tag: CompoundTag) {
            super.loadCustomPersistedData(tag)
            this.eut = tag.getLong("eut")
        }

        private fun checkRecipe(recipe: GTRecipe): Boolean = !this.recipeList.contains(recipe.id.hashCode()) &&
            RecipeRunnerHelper.matchRecipe(machine, recipe) && !this.failedRecipeIds.contains(recipe.id.hashCode()) &&
            IGTRecipe.of(recipe).euTier <= tsdMachine.tier && recipe.checkConditions(this).isSuccess

        private fun modifyRecipe(recipe: GTRecipe, parallel: Long): GTRecipe? {
            val parallels = when (recipe.recipeType) {
                DISTORT_RECIPES -> parallel / (recipe.data.getInt("ebf_temp") maxToLong 1L).pow(0.8)
                else -> parallel
            }
            (
                if (tsdMachine.isMultiple) {
                    FastRecipeModify.modify(
                        tsdMachine,
                        recipe,
                        parallels,
                        ocResult = FastRecipeModify.OverClockFactor(.25, 4.0)
                    ) { FastRecipeModify.ReduceResult(.1, tsdMachine.maintenance()) }
                } else {
                    FastRecipeModify.modify(
                        tsdMachine,
                        recipe,
                        parallels,
                        ocResult = FastRecipeModify.OverClockFactor(.5, 6.0)
                    ) { FastRecipeModify.ReduceResult(.1, tsdMachine.maintenance()) }
                }
                )?.let { recipe ->
                if (!beforeConsume(recipe.longParallel, tsdMachine)) return null
                val multiplier = if (tsdMachine.isMultiple) {
                    1.0
                } else {
                    when (this.tsdMachine.config) {
                        1 -> 1.0
                        2 -> 1.5
                        3 -> 3.0
                        else -> .0
                    }
                }
                if (multiplier > 1.0) {
                    val mdf = ContentModifier.multiplier(multiplier)
                    recipe.outputs.entries.forEach { (c, l) -> l.forEach { it.modify(c, mdf) } }
                }
                return recipe
            }
            return null
        }

        private fun beforeConsume(parallels: Long, machine: TimeSpaceDistorter): Boolean {
            if (machine.isMultiple) {
                return machine.inputItemStack(
                    ItemStack(QuantumAnomaly, (parallels / 27).safeToInt),
                    ItemStack(Hypercube, (parallels / 623).safeToInt)
                )
            }
            when (machine.config) {
                1 -> {
                    return machine.inputFluidStack(Infinity.getFluid(parallels / 70))
                }
                2 -> {
                    return machine.inputFluidStack(
                        Infinity.getFluid(parallels / 70),
                        Hypogen.getFluid(parallels / 180)
                    )
                }
                3 -> {
                    return machine.inputFluidStack(
                        Infinity.getFluid(parallels / 70),
                        Hypogen.getFluid(parallels / 180),
                        SpaceTime.getFluid(parallels / 340)
                    )
                }
                else -> return true
            }
        }
    }

    companion object {
        protected val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            TimeSpaceDistorter::class.java,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER
        )
        private val lookup: GTRecipeLookup by lazy { GTLAddRecipesTypes.TIME_SPACE_DISTORTER.lookup }
    }
}
