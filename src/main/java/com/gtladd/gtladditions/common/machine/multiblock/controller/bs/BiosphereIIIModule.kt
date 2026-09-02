package com.gtladd.gtladditions.common.machine.multiblock.controller.bs

import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineModule

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component

import com.gtladd.gtladditions.api.machine.GTLAddWorkableElectricMultipleRecipesMachine
import com.gtladd.gtladditions.api.machine.GTLAddWorkableElectricMultipleRecipesTypesMachine
import com.gtladd.gtladditions.common.machine.multiblock.controller.bs.BiosphereIIIPosHelper.calculatePossibleHostPositions
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import com.gtladd.gtladditions.utils.MathUtil.minToInt
import com.gtladd.gtladditions.utils.MathUtil.pow

abstract class BiosphereIIIModule(holder: IMachineBlockEntity) :
    GTLAddWorkableElectricMultipleRecipesTypesMachine(holder),
    IModularMachineModule<BiosphereIIIController, BiosphereIIIModule>,
    IMachineLife {

    companion object {
        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(BiosphereIIIModule::class.java, GTLAddWorkableElectricMultipleRecipesMachine.MANAGED_FIELD_HOLDER)
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

    @Persisted
    private var hostPosition: BlockPos? = null
    private var host: BiosphereIIIController? = null

    override fun getFieldHolder() = MANAGED_FIELD_HOLDER

    override fun getHost() = this.host

    override fun testBefore(obj: Any): Boolean {
        val circuit = host?.getCircuit()
        return (host?.tier ?: 0) >= GTValues.UEV && host?.isWorkingEnabled == true && circuit == 1
    }

    override fun addDisplayText(textList: MutableList<Component>) {
        super.addDisplayText(textList)
        if (isFormed) {
            textList.add(
                (
                    if (host == null) {
                        "gui.gtladditions.biosphere_iii_module_disconnect"
                    } else {
                        "gui.gtladditions.biosphere_iii_module_connect"
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

    override fun setHost(bsController: BiosphereIIIController?) {
        this.host = bsController
    }

    override fun getHostType() = BiosphereIIIController::class.java

    override fun getHostScanPositions(): Array<BlockPos> = calculatePossibleHostPositions(pos)

    override fun getThread() = 128 + 32 * ((host?.tier ?: 0) - GTValues.UEV)

    override fun getMaxParallel() = Int.MAX_VALUE minToInt 2.pow(host?.coilType?.coilTemperature?.div(1100) ?: 0)
}
