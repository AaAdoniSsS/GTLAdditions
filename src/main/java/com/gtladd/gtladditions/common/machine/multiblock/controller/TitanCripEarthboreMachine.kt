package com.gtladd.gtladditions.common.machine.multiblock.controller

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine

class TitanCripEarthboreMachine(holder: IMachineBlockEntity) :
    WorkableElectricMultiblockMachine(holder) {

    fun isBatchEnabled(): Boolean = false

    fun setBatchEnabled() {}

    fun supportsBatchProcessing(): Boolean = false

    fun canConfigureBatchProcessing(): Boolean = false
}
