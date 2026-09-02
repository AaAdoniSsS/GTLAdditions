package com.gtladd.gtladditions.common.machine.multiblock.controller

import org.gtlcore.gtlcore.api.data.tag.GTLTagPrefix.nanoswarm
import org.gtlcore.gtlcore.api.pattern.util.IValueContainer
import org.gtlcore.gtlcore.api.recipe.IParallelLogic
import org.gtlcore.gtlcore.common.data.GTLMaterials.*
import org.gtlcore.gtlcore.common.machine.multiblock.electric.TierCasingMachine

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.block.ICoilType
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.logic.OCParams
import com.gregtechceu.gtceu.api.recipe.logic.OCResult
import com.gregtechceu.gtceu.common.block.CoilBlock
import com.gregtechceu.gtceu.common.data.GTMaterials

import com.lowdragmc.lowdraglib.gui.widget.SlotWidget
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import net.minecraft.ChatFormatting
import net.minecraft.MethodsReturnNonnullByDefault
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

import com.gtladd.gtladditions.api.recipe.ContentList
import com.gtladd.gtladditions.utils.ComponentUtil.literal
import com.gtladd.gtladditions.utils.GTRecipeUtils.copy
import com.gtladd.gtladditions.utils.GTRecipeUtils.setEU
import com.gtladd.gtladditions.utils.MathUtil.cbrt
import com.gtladd.gtladditions.utils.MathUtil.ln
import com.gtladd.gtladditions.utils.MathUtil.minToLong
import com.gtladd.gtladditions.utils.MathUtil.pow
import com.gtladd.gtladditions.utils.MathUtil.random
import com.gtladd.gtladditions.utils.MathUtil.sqrt
import com.hepdd.gtmthings.data.CreativeMachines
import it.unimi.dsi.fastutil.ints.IntArrayList

import java.text.DecimalFormat
import javax.annotation.ParametersAreNonnullByDefault
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.tanh
import kotlin.streams.toList

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
open class TaixuTurbidArray(holder: IMachineBlockEntity) : TierCasingMachine(holder, "SCTier"), IMachineModifyDrops {

    fun isBatchEnabled(): Boolean = false

    fun setBatchEnabled() {}

    fun supportsBatchProcessing(): Boolean = false

    fun canConfigureBatchProcessing(): Boolean = false

    @Persisted
    val machineStorage: NotifiableItemStackHandler = NotifiableItemStackHandler(this, 1, IO.NONE, IO.BOTH) { ItemStackTransfer(1) }.setFilter(::filter)
    private var coilType: ICoilType = CoilBlock.CoilType.CUPRONICKEL
    private var height = 0
    private var frameA = .0
    private var frameB = .0

    override fun createUIWidget(): Widget = (super.createUIWidget() as WidgetGroup).let {
        return it.addWidget(
            (
                SlotWidget(
                    this.machineStorage.storage,
                    0,
                    it.sizeWidth - 30,
                    it.sizeHeight - 30,
                    true,
                    true
                )
                )
                .setBackground(GuiTextures.SLOT)
        )
    }

    protected fun filter(itemStack: ItemStack) = itemStack.`is`(EnderiumNano) || itemStack.`is`(DraconiumNano) ||
        itemStack.`is`(SpacetimeNano) || itemStack.`is`(EternityNano) || itemStack.`is`(CREATE)

    override fun onDrops(drops: MutableList<ItemStack>) = this.clearInventory(this.machineStorage.storage)

    override fun onStructureFormed() {
        super.onStructureFormed()
        val context = this.multiblockState.matchContext
        this.coilType = context.get("CoilType")
        (context.getOrCreate("SpeedPipeValue") { IValueContainer.noop() }.getValue() as Int).let { this.height = it - 2 }
        this.frameA = 8.0 * (2.pow(this.casingTier) - 1) * sqrt(this.tier + 1)
        this.frameB = 3.8 * 1.3.pow(coil.indexOf(this.coilType.coilTemperature) + 1) * (this.coilType.coilTemperature.toDouble() / 36000.0).pow(0.7)
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        this.coilType = CoilBlock.CoilType.CUPRONICKEL
        this.height = 0
        this.frameA = .0
        this.frameB = .0
    }

    override fun beforeWorking(recipe: GTRecipe?): Boolean = true

    override fun addDisplayText(textList: MutableList<Component>) {
        super.addDisplayText(textList)
        if (this.isFormed) {
            textList.add(Component.translatable("gtceu.machine.taixu.gui.tooltip.0", this.height))
            textList.add(Component.translatable("gtceu.machine.taixu.gui.tooltip.1", this.getMaxParallel()))
            textList.add(
                Component.translatable(
                    "gtceu.multiblock.blast_furnace.max_temperature",
                    "${coilType.coilTemperature}K".literal.setStyle(Style.EMPTY.withColor(ChatFormatting.RED))
                )
            )
            if (this.tier > GTValues.UIV) {
                val df = DecimalFormat(".00'%'")
                textList.add(Component.translatable("gtceu.machine.taixu.gui.tooltip.2", df.format(this.successRateA())))
                textList.add(Component.translatable("gtceu.machine.taixu.gui.tooltip.3", this.baseOutputFluid1()))
                if (this.tier > GTValues.OpV) {
                    textList.add(Component.translatable("gtceu.machine.taixu.gui.tooltip.4", df.format(this.successRateB())))
                    textList.add(Component.translatable("gtceu.machine.taixu.gui.tooltip.5", this.baseOutputFluid2()))
                }
            }
        }
    }

    private fun successRateA(): Double = if (machineStorage.getStackInSlot(0).`is`(CREATE)) {
        100.0
    } else {
        (100 / (1 + exp(-0.1 * (this.frameA / 50 + this.frameB / 100 + this.height / 3))) + this.slotAdd)
    }

    private fun successRateB(): Double = if (machineStorage.getStackInSlot(0).`is`(CREATE)) {
        100.0
    } else {
        (100 * (1 - exp(-0.02 * ((this.frameA + this.frameB) / 20 + cbrt(this.height) * this.tier / 3))) + this.slotAdd)
    }

    private fun baseOutputFluid1(): Int = (40960 * tanh(0.007 * (this.frameA * this.height / 9 + sqrt(this.frameB) * ln(this.tier + 2)))).toInt()

    private fun baseOutputFluid2(): Int = (22500 * tanh(sqrt(this.frameA * this.frameB) * (this.height + this.tier) * 0.045 / 200)).toInt()

    fun getMaxParallel(): Int = if (machineStorage.getStackInSlot(0).`is`(CREATE)) {
        3.pow(16)
    } else {
        (4096 * 1.621.pow(this.coilType.coilTemperature.toDouble() / 6400)).toInt()
    }

    private val slotAdd: Double
        get() {
            val item = this.machineStorage.storage.getStackInSlot(0).item
            val amount = this.machineStorage.storage.getStackInSlot(0).count
            return when (item) {
                EnderiumNano -> .01 * amount
                DraconiumNano -> .05 * amount
                SpacetimeNano -> .1 * amount
                EternityNano -> .2 * amount
                else -> .0
            }
        }

    override fun getFieldHolder(): ManagedFieldHolder = MANAGED_FIELD_HOLDER

    companion object {
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder =
            ManagedFieldHolder(TaixuTurbidArray::class.java, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER)
        private val coil: IntArrayList by lazy { IntArrayList(GTCEuAPI.HEATING_COILS.keys.stream().mapToInt { it.coilTemperature }.sorted().toList()) }
        private val EnderiumNano: Item by lazy { ChemicalHelper.get(nanoswarm, Enderium).item }
        private val DraconiumNano: Item by lazy { ChemicalHelper.get(nanoswarm, Draconium).item }
        private val SpacetimeNano: Item by lazy { ChemicalHelper.get(nanoswarm, SpaceTime).item }
        private val EternityNano: Item by lazy { ChemicalHelper.get(nanoswarm, Eternity).item }
        private val CREATE: Item by lazy { CreativeMachines.CREATIVE_ENERGY_INPUT_HATCH.asStack().item }

        fun recipeModifier(machine: MetaMachine, recipe: GTRecipe, params: OCParams, result: OCResult): GTRecipe? {
            (machine as TaixuTurbidArray).let {
                val maxParallel = IParallelLogic.getMaxParallel(it, recipe, it.getMaxParallel().toLong())
                if (maxParallel <= 0) return null
                val fluidList = ContentList(2)
                if (100.random() <= it.successRateA().toInt() && it.tier >= GTValues.UXV) {
                    fluidList.addMaxChanceContent(UuAmplifier.getFluid(it.baseOutputFluid1().toLong()))
                }
                if (100.random() <= it.successRateB().toInt() && it.tier >= GTValues.MAX) {
                    fluidList.addMaxChanceContent(GTMaterials.UUMatter.getFluid(it.baseOutputFluid2().toLong()))
                }
                if (!fluidList.isEmpty) recipe.outputs[FluidRecipeCapability.CAP] = fluidList
                val minParallel = IParallelLogic.getMinParallel(it, recipe, maxParallel)
                val copy = recipe.copy(it, (maxParallel minToLong minParallel), 100)
                copy.setEU(524288L * GTValues.V[it.tier])
                return copy
            }
        }
    }
}
