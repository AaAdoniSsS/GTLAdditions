package com.gtladd.gtladditions.common.machine.multiblock.controller.df

import org.gtlcore.gtlcore.common.data.GTLMaterials
import org.gtlcore.gtlcore.common.machine.multiblock.part.HugeFluidHatchPartMachine

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.common.data.GTMaterials

import com.lowdragmc.lowdraglib.side.fluid.FluidStack
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.material.Fluids

import com.gtladd.gtladditions.api.machine.gui.MultiblockDisplayText
import com.gtladd.gtladditions.api.recipe.content.ContentList.MaxChanceContent
import com.gtladd.gtladditions.common.data.RecipesModify
import com.gtladd.gtladditions.common.machine.hatch.SuperDualHatchPartMachine
import com.gtladd.gtladditions.common.machine.hatch.VientianeTranscriptionNode
import com.gtladd.gtladditions.common.machine.multiblock.controller.Resource
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import com.gtladd.gtladditions.utils.GTRecipeUtils.create
import com.gtladd.gtladditions.utils.MathUtil.maxToInt
import com.gtladd.gtladditions.utils.MathUtil.minToDouble
import com.gtladd.gtladditions.utils.MathUtil.minToInt
import com.gtladd.gtladditions.utils.MathUtil.minToLong
import com.gtladd.gtladditions.utils.MathUtil.pow
import it.unimi.dsi.fastutil.objects.ObjectArrayList

import kotlin.math.exp
import kotlin.math.pow

class ReverseTimeBoostingEngine(holder: IMachineBlockEntity) : RRFModuleMachine(holder) {

    private var fluidHatch: HugeFluidHatchPartMachine? = null
    private var returnHatch: SuperDualHatchPartMachine? = null
    private var hatch: VientianeTranscriptionNode? = null

    @Persisted
    var superHeat = false

    @Persisted
    private var temperature = 48000
    private var returnItem: ItemStack? = null
    private var returnFluid: FluidStack? = null

    init {
        isWorking = true
    }

    fun modifyRecipe(recipe: GTRecipe): GTRecipe? {
        returnItem = null
        returnFluid = null
        if (superHeat) return null
        if (isWorking) {
            val div = findDiv(recipe)
            if (div > 0.1) recipe.duration = (recipe.duration * (0.8 minToDouble (0.05 + 0.8 * exp(-3.2 * div.pow(1.9))))) maxToInt 1
        }
        return recipe
    }

    fun setReturnContent(recipe: GTRecipe) {
        if (returnItem != null) {
            val list = recipe.outputs.computeIfAbsent(ItemRecipeCapability.CAP) { ObjectArrayList() }
            list.add(MaxChanceContent(returnItem!!.create()))
            returnItem = null
            return
        }
        if (returnFluid != null) {
            val list = recipe.outputs.computeIfAbsent(FluidRecipeCapability.CAP) { ObjectArrayList() }
            list.add(MaxChanceContent(returnFluid!!.create()))
            returnFluid = null
            return
        }
    }

    private fun findDiv(recipe: GTRecipe): Double {
        returnHatch?.inventory?.let {
            RecipesModify.RTBERecipeItemMap[recipe.id.hashCode()]?.let { t ->
                for (i in 0..<it.slots) {
                    val item = it.getStackInSlot(i)
                    if (!item.isEmpty) {
                        for (il in t) if (il.first() == item.item) {
                            val ma = (temperature * il.rightInt()) / 2
                            val ri = it.extractItemInternal(i, item.count minToInt ma, false)
                            returnItem = item.copyWithCount((getReturnDiv() * ri.count).toInt())
                            return (ri.count / ma) minToDouble 1
                        }
                    }
                }
            }
        }
        returnHatch?.tank?.let {
            RecipesModify.RTBERecipeFluidMap[recipe.id.hashCode()]?.let { t ->
                for (i in 0..<it.tanks) {
                    val fluid = it.getFluidInTank(i)
                    if (!fluid.isEmpty) {
                        for (fl in t) if (fl.first() == fluid.fluid) {
                            val ma = (temperature * fl.rightInt()) / 2
                            val rf = it.drainInternal(fluid.copy(fluid.amount minToLong ma), false)
                            returnFluid = fluid.copy((getReturnDiv() * rf.amount).toLong())
                            return (rf.amount / ma) minToDouble 1
                        }
                    }
                }
            }
        }
        return 0.0
    }

    private fun getReturnDiv() = if (temperature > 97000) {
        1.0 - 0.85 * ((temperature - 97000) / 13000).pow(0.42)
    } else if (temperature >= 93000) {
        1.0
    } else if (temperature >= 48000) {
        0.5 + 0.5 * (temperature - 48000) / 45000
    } else {
        0.0
    }

    override fun startupUpdate() {
        if (offsetTimer % 20 == 0L) {
            if (host?.recipeLogic?.isWorking != true) safeMinusTemperature(900)
            fluidHatch?.tank?.let {
                for (i in 0..<it.tanks) {
                    val fluidStack = it.getFluidInTank(i)
                    if (fluidStack.isEmpty) continue
                    if (fluidStack.amount >= 100000) {
                        when (fluidStack.fluid) {
                            LAVA.fluid -> if (!superHeat) {
                                safePlusTemperature(2500)
                                it.drainInternal(LAVA, false)
                            }
                            BLAZE.fluid -> if (!superHeat) {
                                safePlusTemperature(4600)
                                it.drainInternal(BLAZE, false)
                            }
                            RAW_STAR_MATTER_PLASMA.fluid -> if (!superHeat) {
                                safePlusTemperature(14000)
                                it.drainInternal(RAW_STAR_MATTER_PLASMA, false)
                            }
                            ICE.fluid -> {
                                safeMinusTemperature(1900 + if (superHeat) 7125 else 0)
                                it.drainInternal(ICE, false)
                            }
                            HELIUM.fluid -> {
                                safeMinusTemperature(3400 + if (superHeat) 7125 else 0)
                                it.drainInternal(HELIUM, false)
                            }
                            CRYOTHEUM.fluid -> {
                                safeMinusTemperature(6700 + if (superHeat) 7125 else 0)
                                it.drainInternal(CRYOTHEUM, false)
                            }
                        }
                    }
                }
            }
        }
    }

    fun safePlusTemperature(plus: Int) {
        temperature += plus
        hatch?.update(temperature)
        if (temperature > 105000) superHeat = true
    }

    fun safeMinusTemperature(minus: Int) {
        temperature -= minus
        if (temperature < 48000) {
            temperature = 48000
            if (superHeat) superHeat = false
        }
        hatch?.update(temperature)
    }

    override fun addDisplayText(textList: MutableList<Component>) {
        MultiblockDisplayText.builder(textList, isFormed())
            .addComponent(
                Component.translatable("gtceu.machine.recursive_reverse_forge.gui.module.4", if (host == null) "×" else "✓"),
                Component.translatable("gtceu.machine.reverse_time_boosting_engine.gui.tooltip.0", this.temperature),
                Component.translatable("gtceu.machine.reverse_time_boosting_engine.gui.tooltip.1", if (superHeat) "Yes" else "No"),
                if (isWorking) "gtceu.multiblock.running".toComponent else "gtceu.multiblock.work_paused".toComponent
            )
    }

    override fun partTest(part: IMultiPart) {
        when (part) {
            is VientianeTranscriptionNode -> {
                this.hatch = part
                this.hatch!!.controlMachine = true
            }
            is HugeFluidHatchPartMachine -> this.fluidHatch = part
            is SuperDualHatchPartMachine -> this.returnHatch = part
        }
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        this.hatch!!.controlMachine = false
        this.hatch = null
    }

    override fun getFieldHolder() = MANAGED_FIELD_HOLDER

    companion object {
        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(ReverseTimeBoostingEngine::class.java, RRFModuleMachine.MANAGED_FIELD_HOLDER)
        private val LAVA = FluidStack.create(Fluids.LAVA, 100000)
        private val BLAZE = GTMaterials.Blaze.getFluid(100000)
        private val RAW_STAR_MATTER_PLASMA = GTLMaterials.RawStarMatter.getFluid(FluidStorageKeys.PLASMA, 100000)
        private val ICE = GTMaterials.Ice.getFluid(100000)
        private val HELIUM = GTMaterials.Helium.getFluid(FluidStorageKeys.LIQUID, 100000)
        private val CRYOTHEUM = FluidStack.create(Resource.Cryotheum, 100000)
    }
}
