package com.gtladd.gtladditions.common.machine.multiblock.controller.df

import org.gtlcore.gtlcore.api.machine.ISuspendableMachine
import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineModule
import org.gtlcore.gtlcore.api.machine.trait.ICheckPatternMachine
import org.gtlcore.gtlcore.api.machine.trait.IRecipeCapabilityMachine
import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus
import org.gtlcore.gtlcore.api.recipe.RecipeResult
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.handleRecipeOutput
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.matchRecipe

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.utils.FormattingUtil

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Component.translatable
import net.minecraft.network.chat.HoverEvent

import com.gtladd.gtladditions.api.machine.gui.MultiblockDisplayText
import com.gtladd.gtladditions.api.recipe.ContentList
import com.gtladd.gtladditions.api.recipe.FastRecipeModify
import com.gtladd.gtladditions.api.recipe.IWirelessGTRecipe
import com.gtladd.gtladditions.utils.ComponentUtil.literal
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import com.gtladd.gtladditions.utils.GTRecipeUtils.create
import com.gtladd.gtladditions.utils.GTRecipeUtils.euTier
import com.gtladd.gtladditions.utils.GTRecipeUtils.getEU
import com.gtladd.gtladditions.utils.MachineUtil.inputItemStack
import com.gtladd.gtladditions.utils.Registries.getItemStack

import java.math.BigDecimal
import java.math.BigInteger

class FractalManipulator(holder: IMachineBlockEntity) :
    WorkableElectricMultiblockMachine(holder),
    IModularMachineModule<RecursiveReverseForge, FractalManipulator>,
    IMachineLife {

    @Persisted
    private var hostPosition: BlockPos? = null
    private var host: RecursiveReverseForge? = null

    override fun createRecipeLogic(vararg args: Any?) = FMRecipeLogic(this)

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
        ICheckPatternMachine.attachConfigurators(configuratorPanel, self())
        IRecipeCapabilityMachine.attachConfigurators(configuratorPanel, self() as WorkableElectricMultiblockMachine)
    }

    override fun addDisplayText(textList: MutableList<Component>) {
        val m1 = if (this.host?.ccaModule != null) {
            if (this.host?.ccaModule!!.isWorking) 2 else 1
        } else {
            0
        }
        val m2 = if (this.host?.hecModule != null) {
            if (this.host?.hecModule!!.isWorkingEnabled) 2 else 1
        } else {
            0
        }
        val effectiveTier = if (this.host?.hecModule?.isWorkingEnabled == true &&
            this.host?.hecModule?.hasDrone() == true
        ) {
            14
        } else {
            tier
        }
        val builder = MultiblockDisplayText.builder(textList, isFormed())
            .setWorkingStatus(recipeLogic.isWorkingEnabled, recipeLogic.isActive)
        if (this.host?.hecModule?.uuid == null) {
            builder.addEnergyUsageLine(energyContainer)
        } else {
            builder.addComponent(
                translatable(
                    "gtceu.machine.hyperdimensional_energy_concentrator.gui.tooltip.0",
                    FormattingUtil.formatNumbers(this.host!!.hecModule!!.getEUt()).literal.withStyle(ChatFormatting.GRAY).withStyle { style ->
                        style.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, "gtceu.multiblock.max_energy_per_tick_hover".toComponent.withStyle(ChatFormatting.GRAY)))
                    }
                )
            )
        }
        builder.addEnergyTierLine(effectiveTier)
            .addMachineModeLine(recipeType)
            .addParallelsLine(Int.MAX_VALUE)
            .addWorkingStatusLine()
            .addProgressLine(recipeLogic.progressPercent)
            .addRecipeStatus(recipeLogic as IRecipeStatus)
            .addComponent(translatable("gtceu.machine.recursive_reverse_forge.gui.module.4", if (host == null) "×" else "✓"),)
        builder.addComponent(
            translatable("gtceu.machine.fractal_manipulator.gui.module.$m1", translatable("block.gtladditions.catalytic_cascade_array")),
            translatable("gtceu.machine.fractal_manipulator.gui.module.$m2", translatable("block.gtladditions.hyperdimensional_energy_concentrator"))
        )
    }

    override fun onStructureFormed() {
        super.onStructureFormed()
        if (!findAndConnectToHost()) removeFromHost(this.host)
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        removeFromHost(this.host)
    }

    override fun onPartUnload() {
        super.onPartUnload()
        removeFromHost(this.host)
    }

    override fun onMachineRemoved() = removeFromHost(this.host)

    override fun getFieldHolder(): ManagedFieldHolder = MANAGED_FIELD_HOLDER

    override fun getHostPosition() = this.hostPosition

    override fun setHostPosition(pos1: BlockPos?) {
        this.hostPosition = pos1
    }

    override fun getHost() = this.host

    override fun setHost(reverseForge: RecursiveReverseForge?) {
        this.host = reverseForge
    }

    override fun getHostType() = RecursiveReverseForge::class.java

    override fun getHostScanPositions() = arrayOf(
        pos.offset(9, 1, 9),
        pos.offset(-9, 1, 9),
        pos.offset(9, 1, -9),
        pos.offset(-9, 1, -9)
    )

    override fun onConnected(host: RecursiveReverseForge) = recipeLogic.updateTickSubscription()

    companion object {
        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(FractalManipulator::class.java, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER)
    }

    class FMRecipeLogic(val fmMachine: FractalManipulator) : RecipeLogic(fmMachine), IRecipeStatus {
        private var eut: Long = 0
        private var bigEUt = BigInteger.ZERO

        fun handleGTRecipe(): Boolean {
            if (fmMachine.host?.rtbeModule?.isWorking == true) {
                val hec = fmMachine.host?.hecModule
                val effectiveTier =
                    if (hec != null && hec.isWorkingEnabled && hec.hasDrone()
                    ) {
                        14
                    } else {
                        fmMachine.tier
                    }
                (
                    lastOriginRecipe ?: fmMachine.recipeType.lookup.find(fmMachine) {
                        matchRecipe(fmMachine, it) && it.euTier <= effectiveTier
                    }
                    )?.let { recipe ->
                    val isWireless = fmMachine.host?.hecModule?.isWorkingEnabled == true
                    FastRecipeModify.rrfModify(
                        fmMachine,
                        recipe,
                        (if (isWireless) fmMachine.host?.hecModule?.getEUt()!! else fmMachine.maxVoltage).toDouble(),
                        isWireless,
                        Int.MAX_VALUE.toLong(),
                        FastRecipeModify.getPerfectOverclock()
                    ) { fmMachine.host?.ccaModule?.modifyRecipe(it) ?: it }?.let { modify ->
                        val item = recipe.data.getString("accelerant").getItemStack(64)
                        if (fmMachine.inputItemStack(item)) {
                            modify.outputs.computeIfAbsent(ItemRecipeCapability.CAP) { ContentList(1) }
                                .add(ContentList.MaxChanceContent(item.create()))
                            if (matchRecipe(fmMachine, modify)) {
                                lastOriginRecipe = recipe
                                setupRecipe(modify)
                                return true
                            }
                        } else {
                            recipeStatus = RecipeResult.fail("gtceu.recipe.fail.accelerant".toComponent)
                        }
                    }
                }
            }
            return false
        }

        override fun findAndHandleRecipe() {
            lastRecipe = null
            lastOriginRecipe = null
            recipeStatus = null
            handleGTRecipe()
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

        override fun handleRecipeWorking() {
            checkNotNull(this.lastRecipe)

            if (eut > 0 && eut <= this.fmMachine.energyContainer.energyStored) {
                this.status = Status.WORKING
                this.fmMachine.energyContainer.changeEnergy(-eut)
                ++this.progress
            } else if (fmMachine.host?.hecModule?.isWorkingEnabled == true &&
                bigEUt.signum() > 0 && fmMachine.host?.hecModule!!.consumeWirelessEU(-bigEUt)
            ) {
                this.status = Status.WORKING
                ++this.progress
            } else {
                this.setWaiting(null)
            }

            if (this.status == Status.WAITING) this.doDamping()
        }

        override fun onRecipeFinish() {
            lastRecipe?.let { handleRecipeOutput(fmMachine, it) }
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
            tag.putByteArray("bigEUt", bigEUt.toByteArray())
        }

        override fun loadCustomPersistedData(tag: CompoundTag) {
            super.loadCustomPersistedData(tag)
            eut = tag.getLong("eut")
            this.bigEUt = BigInteger(tag.getByteArray("bigEUt"))
        }
    }
}
