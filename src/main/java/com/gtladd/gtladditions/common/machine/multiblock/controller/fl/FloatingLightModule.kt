package com.gtladd.gtladditions.common.machine.multiblock.controller.fl

import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineModule

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic
import com.gregtechceu.gtceu.api.recipe.GTRecipe

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component

import com.gtladd.gtladditions.api.machine.MultipleRecipesMachine
import com.gtladd.gtladditions.api.machine.MultipleRecipesTypesMachine
import com.gtladd.gtladditions.api.recipe.FastRecipeModify.ReduceResult
import com.gtladd.gtladditions.common.machine.multiblock.controller.fl.FloatingLightPosHelper.calculatePossibleHostPositions
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import com.gtladd.gtladditions.utils.ModuleConnectionTicker

import kotlin.math.pow

abstract class FloatingLightModule(holder: IMachineBlockEntity) :
    MultipleRecipesTypesMachine(holder),
    IModularMachineModule<FloatingLightController, FloatingLightModule>,
    IMachineLife {

    companion object {
        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(FloatingLightModule::class.java, MultipleRecipesMachine.MANAGED_FIELD_HOLDER)
    }

    override fun onStructureFormed() {
        super.onStructureFormed()
        if (!findAndConnectToHost()) removeFromHost(this.host)
        ModuleConnectionTicker.attach(this, this)
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        removeFromHost(this.host)
        ModuleConnectionTicker.stop(this)
    }

    override fun onPartUnload() {
        super.onPartUnload()
        removeFromHost(this.host)
        ModuleConnectionTicker.stop(this)
    }

    override fun onConnected(host: FloatingLightController) {
        super.onConnected(host)
        getRecipeLogic().markLastRecipeDirty()
        getRecipeLogic().status = RecipeLogic.Status.IDLE
    }

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

    override fun testBefore(obj: Any): Boolean {
        val circuit = host?.getCircuit()
        return (host?.tier ?: 0) >= GTValues.UIV && host?.isWorkingEnabled == true && (circuit == 1 || circuit == 2)
    }

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
}
