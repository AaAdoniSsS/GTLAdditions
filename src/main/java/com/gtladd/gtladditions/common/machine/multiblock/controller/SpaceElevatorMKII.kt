package com.gtladd.gtladditions.common.machine.multiblock.controller

import org.gtlcore.gtlcore.common.machine.multiblock.electric.SpaceElevatorMachine
import org.gtlcore.gtlcore.utils.MachineUtil
import org.gtlcore.gtlcore.utils.Registries
import org.gtlcore.gtlcore.utils.datastructure.ModuleRenderInfo

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

open class SpaceElevatorMKII(holder: IMachineBlockEntity) : SpaceElevatorMachine(holder) {
    override fun getModuleScanPositions(): Array<out BlockPos>? {
        val level = this.level
        val powerCore = this.getPowerCore(this.pos, level!!)
        return if (powerCore != null) {
            arrayOf<BlockPos>(
                powerCore.offset(4, 2, 11),
                powerCore.offset(4, 2, -11),
                powerCore.offset(-4, 2, 11),
                powerCore.offset(-4, 2, -11),
                powerCore.offset(11, 2, 4),
                powerCore.offset(-11, 2, 4),
                powerCore.offset(11, 2, -4),
                powerCore.offset(-11, 2, -4),
                powerCore.offset(18, 2, 8),
                powerCore.offset(18, 2, -8),
                powerCore.offset(-18, 2, 8),
                powerCore.offset(-18, 2, -8),
                powerCore.offset(8, 2, 18),
                powerCore.offset(8, 2, -18),
                powerCore.offset(-8, 2, 18),
                powerCore.offset(-8, 2, -18),
                powerCore.offset(1, 2, 21),
                powerCore.offset(1, 2, -21),
                powerCore.offset(-1, 2, 21),
                powerCore.offset(-1, 2, -21),
                powerCore.offset(21, 2, 1),
                powerCore.offset(21, 2, -1),
                powerCore.offset(-21, 2, 1),
                powerCore.offset(-21, 2, -1),
                powerCore.offset(7, 2, 22),
                powerCore.offset(7, 2, -22),
                powerCore.offset(-7, 2, 22),
                powerCore.offset(-7, 2, -22),
                powerCore.offset(22, 2, 7),
                powerCore.offset(22, 2, -7),
                powerCore.offset(-22, 2, 7),
                powerCore.offset(-22, 2, -7),
                powerCore.offset(26, 2, 6),
                powerCore.offset(26, 2, -6),
                powerCore.offset(-26, 2, 6),
                powerCore.offset(-26, 2, -6),
                powerCore.offset(6, 2, 26),
                powerCore.offset(6, 2, -26),
                powerCore.offset(-6, 2, 26),
                powerCore.offset(-6, 2, -26),
                powerCore.offset(16, 2, 19),
                powerCore.offset(16, 2, -19),
                powerCore.offset(-16, 2, 19),
                powerCore.offset(-16, 2, -19),
                powerCore.offset(19, 2, 16),
                powerCore.offset(19, 2, -16),
                powerCore.offset(-19, 2, 16),
                powerCore.offset(-19, 2, -16),
                powerCore.offset(16, 2, 23),
                powerCore.offset(16, 2, -23),
                powerCore.offset(-16, 2, 23),
                powerCore.offset(-16, 2, -23),
                powerCore.offset(23, 2, 16),
                powerCore.offset(23, 2, -16),
                powerCore.offset(-23, 2, 16),
                powerCore.offset(-23, 2, -16),
            )
        } else {
            MachineUtil.EMPTY_POS_ARRAY
        }
    }

    override fun getModulesForRendering(): List<ModuleRenderInfo?> {
        return mutableListOf<ModuleRenderInfo?>()
    }

    private fun getPowerCore(pos: BlockPos, level: Level): BlockPos? {
        val coordinates =
            arrayOf<BlockPos>(
                pos.offset(3, -2, 0),
                pos.offset(-3, -2, 0),
                pos.offset(0, -2, 3),
                pos.offset(0, -2, -3)
            )

        for (blockPos in coordinates) {
            if (Registries.getBlockId(level.getBlockState(blockPos).block) == "gtlcore:power_core") {
                return blockPos
            }
        }

        return null
    }
}
