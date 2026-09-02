package com.gtladd.gtladditions.common.machine.multiblock.controller.fl

import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineModule

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife
import com.gregtechceu.gtceu.api.recipe.GTRecipe

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component

import com.gtladd.gtladditions.api.machine.GTLAddWorkableElectricMultipleRecipesMachine
import com.gtladd.gtladditions.api.machine.GTLAddWorkableElectricMultipleRecipesTypesMachine
import com.gtladd.gtladditions.api.machine.logic.GTLAddMultiRecipesTypesLogic
import com.gtladd.gtladditions.api.recipe.FastRecipeModify.ReduceResult
import com.gtladd.gtladditions.common.machine.multiblock.controller.fl.FloatingLightPosHelper.calculatePossibleHostPositions
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent

import kotlin.math.pow

abstract class FloatingLightModule(holder: IMachineBlockEntity) :
    GTLAddWorkableElectricMultipleRecipesTypesMachine(holder),
    IModularMachineModule<FloatingLightController, FloatingLightModule>,
    IMachineLife {

    companion object {
        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(FloatingLightModule::class.java, GTLAddWorkableElectricMultipleRecipesMachine.MANAGED_FIELD_HOLDER)
    }

    override fun createRecipeLogic(vararg args: Any) = FloatingLightModuleLogic(this)

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
    override fun getFieldHolder() = MANAGED_FIELD_HOLDER

    @Persisted
    private var hostPosition: BlockPos? = null
    private var host: FloatingLightController? = null

    override fun modifyRecipe(recipe: GTRecipe): ReduceResult {
        val modify = (host?.tier ?: 0) - GTValues.UIV
        val multiplier = if (modify > 0) {
            ReduceResult(0.9.pow(modify), 0.9.pow(modify))
        } else {
            super.modifyRecipe(recipe)
        }
        getRecipeLogic().setReduction(multiplier.reduceEUt, multiplier.reduceDuration)
        return multiplier
    }

    override fun getThread() = 128 * if (host?.isDouble == true) 2 else 1

    override fun addDisplayText(textList: MutableList<Component>) {
        super.addDisplayText(textList)
        if (isFormed) {
            textList.add(
                (
                    if (host == null) {
                        "gui.gtladditions.floating_light_deep_space_industrial_vessel_module_disconnect"
                    } else {
                        "gui.gtladditions.floating_light_deep_space_industrial_vessel_module_connect"
                    }
                    ).toComponent
            )
        }
    }

    override fun onMachineRemoved() = removeFromHost(this.host)

    override fun getHostPosition() = this.hostPosition

    override fun setHostPosition(pos1: BlockPos?) {
        this.hostPosition = pos1
    }

    override fun getHost() = this.host

    override fun setHost(flController: FloatingLightController?) {
        this.host = flController
    }

    override fun getHostType() = FloatingLightController::class.java

    override fun getHostScanPositions(): Array<BlockPos> = calculatePossibleHostPositions(pos)

    class FloatingLightModuleLogic(val flMachine: FloatingLightModule) : GTLAddMultiRecipesTypesLogic(flMachine) {
        override fun findAndHandleRecipe() {
            if ((flMachine.host?.tier ?: 0) >= GTValues.UEV && flMachine.host?.isWorkingEnabled == true && flMachine.host?.getCircuit() == 1) {
                super.findAndHandleRecipe()
            }
        }
    }
}
