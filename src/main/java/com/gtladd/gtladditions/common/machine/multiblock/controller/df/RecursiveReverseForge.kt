package com.gtladd.gtladditions.common.machine.multiblock.controller.df

import org.gtlcore.gtlcore.api.machine.ISuspendableMachine
import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineHost
import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineModule
import org.gtlcore.gtlcore.api.machine.trait.IRecipeCapabilityMachine
import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus
import org.gtlcore.gtlcore.api.recipe.IGTRecipe
import org.gtlcore.gtlcore.api.recipe.RecipeMultiplierTracker
import org.gtlcore.gtlcore.api.recipe.RecipeResult
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.handleRecipeOutput
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.matchRecipe
import org.gtlcore.gtlcore.utils.datastructure.ModuleRenderInfo

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CombinedDirectionalFancyConfigurator
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.lookup.GTRecipeLookup
import com.gregtechceu.gtceu.utils.FormattingUtil

import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Component.translatable
import net.minecraft.network.chat.HoverEvent

import com.gtladd.gtladditions.api.machine.IEnergyMachine
import com.gtladd.gtladditions.api.machine.IMultipleRecipeTypeMachine
import com.gtladd.gtladditions.api.machine.gui.MultiblockDisplayText
import com.gtladd.gtladditions.api.recipe.FastRecipeModify
import com.gtladd.gtladditions.api.recipe.IWirelessGTRecipe
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.CATALYTIC_CASCADE_ARRAY
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.FRACTAL_MANIPULATOR
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.HYPERDIMENSIONAL_ENERGY_CONCETRATOR
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.MAGNETORHEOLOGICAL_CONVERGENCE_CORE
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.REVERSE_TIME_BOOSTING_ENGINE
import com.gtladd.gtladditions.common.recipe.GTLAddRecipesTypes
import com.gtladd.gtladditions.utils.ComponentUtil.literal
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import com.gtladd.gtladditions.utils.GTRecipeUtils.getEU
import com.gtladd.gtladditions.utils.GTRecipeUtils.setEU
import com.gtladd.gtladditions.utils.MathUtil.maxToLong
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

import java.math.BigDecimal
import java.math.BigInteger

class RecursiveReverseForge(holder: IMachineBlockEntity) :
    WorkableElectricMultiblockMachine(holder),
    IModularMachineHost<RecursiveReverseForge>,
    IMultipleRecipeTypeMachine,
    IMachineLife {
    var ccaModule: CatalyticCascadeArray? = null
    var hecModule: HyperdimensionalEnergyConcentrator? = null
    var mccModule: MagnetorheologicalConvergenceCore? = null
    var rtbeModule: ReverseTimeBoostingEngine? = null
    val modulePos = ObjectOpenHashSet<IModularMachineModule<RecursiveReverseForge, *>?>(8)

    override fun createRecipeLogic(vararg args: Any) = RRFRecipeLogic(this)

    override fun attachSideTabs(sideTabs: TabsWidget) {
        sideTabs.setMainTab(this)
        CombinedDirectionalFancyConfigurator.of(self(), self())?.let { sideTabs.attachSubTab(it) }
    }

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
        IRecipeCapabilityMachine.attachConfigurators(configuratorPanel, self() as WorkableElectricMultiblockMachine)
    }

    override fun addDisplayText(textList: MutableList<Component>) {
        val m1 = if (ccaModule != null) {
            if (ccaModule!!.isWorking) 2 else 1
        } else {
            0
        }
        val m2 = if (hecModule != null) {
            if (hecModule!!.isWorkingEnabled) 2 else 1
        } else {
            0
        }
        val m3 = if (mccModule != null) {
            if (mccModule!!.isWorking) 2 else 1
        } else {
            0
        }
        val m4 = if (rtbeModule != null) {
            if (rtbeModule!!.isWorking) 2 else 1
        } else {
            0
        }
        val effectiveTier = if (hecModule?.isWorkingEnabled == true &&
            hecModule?.hasDrone() == true
        ) {
            14
        } else {
            tier
        }
        val builder = MultiblockDisplayText.builder(textList, isFormed())
            .setWorkingStatus(recipeLogic.isWorkingEnabled, recipeLogic.isActive)
        if (hecModule?.uuid == null) {
            builder.addEnergyUsageLine(energyContainer)
        } else {
            builder.addComponent(
                translatable(
                    "gtceu.machine.hyperdimensional_energy_concentrator.gui.tooltip.0",
                    FormattingUtil.formatNumbers(hecModule!!.getEUt()).literal.withStyle(ChatFormatting.GRAY).withStyle { style ->
                        style.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, "gtceu.multiblock.max_energy_per_tick_hover".toComponent.withStyle(ChatFormatting.GRAY)))
                    }
                )
            )
        }
        builder.addEnergyTierLine(effectiveTier)
        builder.addMachineModeLine(multiRecipeType)
            .addParallelsLine(Int.MAX_VALUE)
            .addWorkingStatusLine()
            .addProgressLine(recipeLogic.progressPercent)
            .addRecipeStatus(recipeLogic as IRecipeStatus)
            .addComponent(
                translatable("gtceu.machine.recursive_reverse_forge.gui.module.$m1", translatable("block.gtladditions.catalytic_cascade_array")),
                translatable("gtceu.machine.recursive_reverse_forge.gui.module.$m2", translatable("block.gtladditions.hyperdimensional_energy_concentrator")),
                translatable("gtceu.machine.recursive_reverse_forge.gui.module.$m3", translatable("block.gtladditions.magnetorheological_convergence_core")),
                translatable("gtceu.machine.recursive_reverse_forge.gui.module.$m4", translatable("block.gtladditions.reverse_time_boosting_engine")),
                translatable("gtceu.machine.recursive_reverse_forge.gui.module.3", modulePos.count { it is FractalManipulator })
            )
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        safeClearModules()
    }

    override fun onMachineRemoved() = safeClearModules()

    override fun onStructureFormed() {
        super.onStructureFormed()
        safeClearModules()
        scanAndConnectModules()
    }

    override fun getModuleSet() = this.modulePos

    override fun getModuleScanPositions() = arrayOf(
        pos.offset(0, -7, 33),
        pos.offset(0, -7, -33),
        pos.offset(33, -7, 0),
        pos.offset(-33, -7, 0),
        pos.offset(9, -1, 9),
        pos.offset(-9, -1, 9),
        pos.offset(9, -1, -9),
        pos.offset(-9, -1, -9)
    )

    override fun <M : IModularMachineModule<RecursiveReverseForge, M>?> addModule(module: M & Any) {
        super.addModule(module)
        when (module) {
            is CatalyticCascadeArray -> this.ccaModule = module
            is HyperdimensionalEnergyConcentrator -> this.hecModule = module
            is MagnetorheologicalConvergenceCore -> this.mccModule = module
            is ReverseTimeBoostingEngine -> this.rtbeModule = module
        }
    }

    override fun <M : IModularMachineModule<RecursiveReverseForge, M>?> removeModule(module: M & Any) {
        super.removeModule(module)
        when (module) {
            is CatalyticCascadeArray -> this.ccaModule = null
            is HyperdimensionalEnergyConcentrator -> this.hecModule = null
            is MagnetorheologicalConvergenceCore -> this.mccModule = null
            is ReverseTimeBoostingEngine -> this.rtbeModule = null
        }
    }

    override fun getModulesForRendering() = listOf(
        ModuleRenderInfo(
            BlockPos(33, 0, 7),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.UP,
            CATALYTIC_CASCADE_ARRAY
        ),
        ModuleRenderInfo(
            BlockPos(0, 33, 7),
            Direction.UP,
            Direction.NORTH,
            Direction.UP,
            Direction.NORTH,
            HYPERDIMENSIONAL_ENERGY_CONCETRATOR
        ),
        ModuleRenderInfo(
            BlockPos(-33, 0, 7),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.UP,
            MAGNETORHEOLOGICAL_CONVERGENCE_CORE
        ),
        ModuleRenderInfo(
            BlockPos(0, -33, 7),
            Direction.UP,
            Direction.NORTH,
            Direction.DOWN,
            Direction.NORTH,
            REVERSE_TIME_BOOSTING_ENGINE
        ),
        ModuleRenderInfo(
            BlockPos(9, -9, 1),
            Direction.UP,
            Direction.NORTH,
            Direction.UP,
            Direction.SOUTH,
            FRACTAL_MANIPULATOR
        ),
        ModuleRenderInfo(
            BlockPos(9, 9, 1),
            Direction.UP,
            Direction.NORTH,
            Direction.UP,
            Direction.SOUTH,
            FRACTAL_MANIPULATOR
        ),
        ModuleRenderInfo(
            BlockPos(-9, 9, 1),
            Direction.UP,
            Direction.NORTH,
            Direction.UP,
            Direction.NORTH,
            FRACTAL_MANIPULATOR
        ),
        ModuleRenderInfo(
            BlockPos(-9, -9, 1),
            Direction.UP,
            Direction.NORTH,
            Direction.UP,
            Direction.NORTH,
            FRACTAL_MANIPULATOR
        )
    )

    override val multiRecipeTypes = arrayOf(GTLAddRecipesTypes.RecursiveReverseForgeType)

    class RRFRecipeLogic(val rrfMachine: RecursiveReverseForge) : RecipeLogic(rrfMachine), IRecipeStatus {
        private var eut = 0L
        private var bigEUt = BigInteger.ZERO

        override fun findAndHandleRecipe() {
            lastRecipe = null
            lastOriginRecipe = null
            recipeStatus = null
            modifySearchingRecipes(lookup.find(rrfMachine, this::checkRecipe))
        }

        override fun setupRecipe(recipe: GTRecipe) {
            if (this.handleRecipeIO(recipe, IO.IN)) {
                if (this.lastRecipe != null && recipe != this.lastRecipe) this.chanceCaches.clear()
                this.bigEUt = BigDecimal.valueOf(IWirelessGTRecipe.of(recipe).wirelessEUt).toBigInteger()
                this.eut = recipe.getEU
                this.lastRecipe = recipe
                this.status = Status.WORKING
                this.progress = 0
                this.duration = recipe.duration
            }
        }

        @Suppress("CAST_NEVER_SUCCEEDS")
        override fun handleRecipeWorking() {
            checkNotNull(this.lastRecipe)
            val energyMachine = rrfMachine as IEnergyMachine
            if (rrfMachine.hecModule?.isWorkingEnabled == true &&
                bigEUt.signum() > 0 && rrfMachine.hecModule!!.consumeWirelessEU(-bigEUt)
            ) {
                this.status = Status.WORKING
                ++this.progress
                rrfMachine.rtbeModule?.safePlusTemperature(65)
            } else if (eut > 0 && eut <= energyMachine.energyContainerList.energyStored) {
                this.status = Status.WORKING
                energyMachine.energyContainerList.changeEnergy(-eut)
                ++this.progress
                rrfMachine.rtbeModule?.safePlusTemperature(65)
            } else {
                this.setWaiting(null)
            }
            if (this.status == Status.WAITING) this.doDamping()
        }

        override fun onRecipeFinish() {
            lastRecipe?.let { handleRecipeOutput(rrfMachine, it) }
            if (rrfMachine is ISuspendableMachine) {
                val ism = rrfMachine as ISuspendableMachine
                if (ism.`gtlcore$isSuspendAfterFinish`()) {
                    this.status = Status.SUSPEND
                    ism.`gtlcore$setSuspendAfterFinish`(false)
                } else {
                    lastOriginRecipe?.let { if (modifySearchingRecipes(it)) return }
                    status = Status.IDLE
                }
            }
            progress = 0
            duration = 0
        }

        override fun saveCustomPersistedData(tag: CompoundTag, forDrop: Boolean) {
            super.saveCustomPersistedData(tag, forDrop)
            tag.putLong("eut", this.eut)
            tag.putByteArray("bigEUt", bigEUt.toByteArray())
        }

        override fun loadCustomPersistedData(tag: CompoundTag) {
            super.loadCustomPersistedData(tag)
            this.eut = tag.getLong("eut")
            this.bigEUt = BigInteger(tag.getByteArray("bigEUt"))
        }

        private fun modifySearchingRecipes(recipe: GTRecipe?): Boolean {
            recipe?.let {
                val isWireless = rrfMachine.hecModule?.isWorkingEnabled == true
                FastRecipeModify.rrfModify(
                    rrfMachine,
                    it,
                    (if (isWireless) rrfMachine.hecModule!!.getEUt() else rrfMachine.maxVoltage).toDouble(),
                    isWireless,
                    Int.MAX_VALUE.toLong(),
                    FastRecipeModify.getPerfectOverclock()
                ) { recipe ->
                    var gtRecipe: GTRecipe? = recipe
                    if (rrfMachine.rtbeModule != null) gtRecipe = rrfMachine.rtbeModule!!.modifyRecipe(gtRecipe!!)
                    if (gtRecipe != null && rrfMachine.mccModule != null) gtRecipe = rrfMachine.mccModule!!.modifyRecipe(gtRecipe)
                    if (gtRecipe != null && rrfMachine.ccaModule != null) gtRecipe = rrfMachine.ccaModule!!.modifyRecipe(gtRecipe)
                    gtRecipe?.setEU((gtRecipe.getEU * 0.8) maxToLong 1)
                    gtRecipe?.let { modified ->
                        val originalEUt = it.getEU.toDouble()
                        val energyMultiplier = if (originalEUt == 0.0) {
                            1.0
                        } else {
                            kotlin.math.abs(modified.getEU.toDouble() / originalEUt)
                        }
                        val durationMultiplier = if (it.duration <= 0) {
                            1.0
                        } else {
                            modified.duration.toDouble() / it.duration
                        }
                        RecipeMultiplierTracker.captureReduction(
                            rrfMachine,
                            it,
                            energyMultiplier,
                            durationMultiplier
                        )
                        return@rrfModify gtRecipe
                    }
                }?.let { modify ->
                    rrfMachine.rtbeModule?.setReturnContent(modify)
                    if (checkRecipe(modify)) {
                        lastOriginRecipe = it
                        setupRecipe(modify)
                        return true
                    }
                }
            }
            RecipeResult.of(rrfMachine, RecipeResult.FAIL_FIND)
            return false
        }

        private fun checkRecipe(recipe: GTRecipe): Boolean {
            val effectiveTier = if (rrfMachine.hecModule?.isWorkingEnabled == true &&
                rrfMachine.hecModule?.hasDrone() == true
            ) {
                14
            } else {
                rrfMachine.tier
            }
            return matchRecipe(this.machine, recipe) &&
                IGTRecipe.of(recipe).euTier <= effectiveTier
        }
    }

    companion object {
        private val lookup: GTRecipeLookup by lazy { GTLAddRecipesTypes.RECURSIVE_REVERSE_FORGE.lookup }
    }
}
