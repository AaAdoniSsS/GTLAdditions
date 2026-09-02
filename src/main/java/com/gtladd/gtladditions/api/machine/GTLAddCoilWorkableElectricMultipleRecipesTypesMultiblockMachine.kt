package com.gtladd.gtladditions.api.machine

import com.gregtechceu.gtceu.api.block.ICoilType
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.common.block.CoilBlock

import com.gtladd.gtladditions.utils.MathUtil.minToInt
import com.gtladd.gtladditions.utils.MathUtil.pow

open class GTLAddCoilWorkableElectricMultipleRecipesTypesMultiblockMachine(holder: IMachineBlockEntity) :
    GTLAddWorkableElectricMultipleRecipesTypesMachine(holder), ICoilMachine {
    override var coilType: ICoilType = CoilBlock.CoilType.CUPRONICKEL

    override fun getMaxParallel(): Int = Int.MAX_VALUE minToInt 2.pow(this.coilType.coilTemperature / 900)

    override fun onStructureFormed() {
        super.onStructureFormed()
        this.coilType = multiblockState.matchContext.get("CoilType")
    }

    fun getCoilTier(): Int = coilType.tier
}
