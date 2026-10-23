package com.gtladd.gtladditions.common.machine.multiblock.controller.df

import org.gtlcore.gtlcore.common.data.GTLMaterials
import org.gtlcore.gtlcore.common.machine.multiblock.part.HugeFluidHatchPartMachine

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier

import com.lowdragmc.lowdraglib.side.fluid.FluidStack
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component

import com.gtladd.gtladditions.api.machine.gui.MultiblockDisplayText
import com.gtladd.gtladditions.common.machine.hatch.VientianeTranscriptionNode
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import com.gtladd.gtladditions.utils.GTRecipeUtils.modify

import kotlin.random.Random

class CatalyticCascadeArray(holder: IMachineBlockEntity) : RRFModuleMachine(holder) {

    @Persisted
    private var isModify = false

    @Persisted
    private var isEnergy = false

    @Persisted
    private var isFail = false
    private var fluid: FluidStack = FluidStack.empty()

    @Persisted
    private var tick: Byte = 0
    private var hatch: VientianeTranscriptionNode? = null
    private var fluidHatch: HugeFluidHatchPartMachine? = null

    fun modifyRecipe(recipe: GTRecipe): GTRecipe {
        if (isWorking) {
            if (isModify) recipe.outputs.forEach { (c, l) -> l.forEach { if (!("c" == it.slotName || "i" == it.slotName)) it.modify(c, 2L) } }
            if (isEnergy) recipe.tickInputs[EURecipeCapability.CAP]?.let { it[0].modify(EURecipeCapability.CAP, modify) }
        }
        return recipe
    }

    override fun startupUpdate() {
        if (offsetTimer % 20 == 0L) {
            if (tick == 0.toByte()) {
                val signal = Random.nextInt(1, 16)
                hatch?.setRedStoneSignal(signal)
                fluid = when (signal) {
                    in 1..3 -> f1
                    in 4..6 -> f2
                    in 7..9 -> f3
                    in 10..12 -> f4
                    in 13..15 -> f5
                    else -> FluidStack.empty()
                }
            }
            if (tick >= 5) {
                val f = fluidHatch?.tank?.storages[0]?.fluid ?: FluidStack.empty()
                if (f.isEmpty) {
                    isModify = false
                    isEnergy = false
                    isFail = true
                } else if (!isFail && f.fluid == fluid.fluid && f.amount >= fluid.amount) {
                    fluidHatch!!.tank.drainInternal(fluid, false)
                    isModify = true
                    isEnergy = true
                } else {
                    fluidHatch!!.tank.drainInternal(f, false)
                    isModify = false
                    isEnergy = true
                    isFail = true
                }
            }
            tick++
            if (tick >= 30) {
                isFail = false
                tick = 0
                fluid = FluidStack.empty()
            }
        }
    }

    override fun addDisplayText(textList: MutableList<Component>) {
        MultiblockDisplayText.builder(textList, isFormed())
            .addComponent(
                Component.translatable("gtceu.machine.recursive_reverse_forge.gui.module.4", if (host == null) "×" else "✓"),
                "gtceu.machine.catalytic_cascade_array.gui.tooltip.${if (isModify) 0 else 1}".toComponent,
                Component.translatable("gtceu.machine.catalytic_cascade_array.gui.tooltip.2", tick)
            )
    }

    override fun saveCustomPersistedData(tag: CompoundTag, forDrop: Boolean) {
        super.saveCustomPersistedData(tag, forDrop)
        fluid.saveToTag(tag)
    }

    override fun loadCustomPersistedData(tag: CompoundTag) {
        super.loadCustomPersistedData(tag)
        fluid = FluidStack.loadFromTag(tag)
    }

    override fun partTest(part: IMultiPart) {
        if (part is VientianeTranscriptionNode) {
            this.hatch = part
        } else if (part is HugeFluidHatchPartMachine) {
            this.fluidHatch = part
        }
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        this.hatch = null
        this.fluidHatch = null
    }

    override fun getFieldHolder() = MANAGED_FIELD_HOLDER

    companion object {
        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(CatalyticCascadeArray::class.java, RRFModuleMachine.MANAGED_FIELD_HOLDER)
        private val modify = ContentModifier.multiplier(0.15)
        private val f1 = GTLMaterials.DimensionallyTranscendentCrudeCatalyst.getFluid(40000)
        private val f2 = GTLMaterials.DimensionallyTranscendentProsaicCatalyst.getFluid(40000)
        private val f3 = GTLMaterials.DimensionallyTranscendentResplendentCatalyst.getFluid(40000)
        private val f4 = GTLMaterials.DimensionallyTranscendentExoticCatalyst.getFluid(40000)
        private val f5 = GTLMaterials.DimensionallyTranscendentStellarCatalyst.getFluid(40000)
    }
}
