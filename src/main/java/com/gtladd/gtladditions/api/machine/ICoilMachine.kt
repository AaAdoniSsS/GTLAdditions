package com.gtladd.gtladditions.api.machine

import com.gregtechceu.gtceu.api.block.ICoilType
import com.gregtechceu.gtceu.common.block.CoilBlock

interface ICoilMachine {
    val coilType: ICoilType get() = CoilBlock.CoilType.CUPRONICKEL
}
