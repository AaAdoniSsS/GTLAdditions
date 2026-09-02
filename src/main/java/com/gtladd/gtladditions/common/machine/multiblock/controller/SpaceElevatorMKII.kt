package com.gtladd.gtladditions.common.machine.multiblock.controller

import org.gtlcore.gtlcore.common.data.GTLBlocks
import org.gtlcore.gtlcore.common.data.machines.AdvancedMultiBlockMachine.*
import org.gtlcore.gtlcore.common.machine.multiblock.electric.SpaceElevatorMachine
import org.gtlcore.gtlcore.utils.MachineUtil
import org.gtlcore.gtlcore.utils.datastructure.ModuleRenderInfo

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
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
        return mutableListOf<ModuleRenderInfo?>(
            ModuleRenderInfo(BlockPos(4, 0, 14), Direction.NORTH, Direction.UP, Direction.NORTH, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(4, 0, -8), Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.UP, ASSEMBLER_MODULE),
            ModuleRenderInfo(BlockPos(-4, 0, 14), Direction.NORTH, Direction.UP, Direction.NORTH, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(-4, 0, -8), Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.UP, ASSEMBLER_MODULE),

            ModuleRenderInfo(BlockPos(11, 0, 7), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(-11, 0, 7), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, ASSEMBLER_MODULE),
            ModuleRenderInfo(BlockPos(11, 0, -1), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(-11, 0, -1), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, ASSEMBLER_MODULE),

            ModuleRenderInfo(BlockPos(18, 0, 11), Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(18, 0, -5), Direction.NORTH, Direction.UP, Direction.NORTH, Direction.UP, ASSEMBLER_MODULE),
            ModuleRenderInfo(BlockPos(-18, 0, 11), Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(-18, 0, -5), Direction.NORTH, Direction.UP, Direction.NORTH, Direction.UP, ASSEMBLER_MODULE),

            ModuleRenderInfo(BlockPos(8, 0, 21), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(8, 0, -15), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, ASSEMBLER_MODULE),
            ModuleRenderInfo(BlockPos(-8, 0, 21), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(-8, 0, -15), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, ASSEMBLER_MODULE),

            ModuleRenderInfo(BlockPos(1, 0, 24), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(1, 0, -18), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, ASSEMBLER_MODULE),
            ModuleRenderInfo(BlockPos(-1, 0, 24), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(-1, 0, -18), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, ASSEMBLER_MODULE),

            ModuleRenderInfo(BlockPos(21, 0, 4), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(21, 0, 2), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, ASSEMBLER_MODULE),
            ModuleRenderInfo(BlockPos(-21, 0, 4), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(-21, 0, 2), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, ASSEMBLER_MODULE),

            ModuleRenderInfo(BlockPos(7, 0, 25), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(7, 0, -19), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, ASSEMBLER_MODULE),
            ModuleRenderInfo(BlockPos(-7, 0, 25), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(-7, 0, -19), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, ASSEMBLER_MODULE),

            ModuleRenderInfo(BlockPos(22, 0, 10), Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(22, 0, -4), Direction.NORTH, Direction.UP, Direction.NORTH, Direction.UP, ASSEMBLER_MODULE),
            ModuleRenderInfo(BlockPos(-22, 0, 10), Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(-22, 0, -4), Direction.NORTH, Direction.UP, Direction.NORTH, Direction.UP, ASSEMBLER_MODULE),

            ModuleRenderInfo(BlockPos(26, 0, 9), Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(26, 0, -3), Direction.NORTH, Direction.UP, Direction.NORTH, Direction.UP, ASSEMBLER_MODULE),
            ModuleRenderInfo(BlockPos(-26, 0, 9), Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(-26, 0, -3), Direction.NORTH, Direction.UP, Direction.NORTH, Direction.UP, ASSEMBLER_MODULE),

            ModuleRenderInfo(BlockPos(6, 0, 29), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(6, 0, -23), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, ASSEMBLER_MODULE),
            ModuleRenderInfo(BlockPos(-6, 0, 29), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(-6, 0, -23), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, ASSEMBLER_MODULE),

            ModuleRenderInfo(BlockPos(16, 0, 22), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(16, 0, -16), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, ASSEMBLER_MODULE),
            ModuleRenderInfo(BlockPos(-16, 0, 22), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(-16, 0, -16), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, ASSEMBLER_MODULE),

            ModuleRenderInfo(BlockPos(19, 0, 19), Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(19, 0, -13), Direction.NORTH, Direction.UP, Direction.NORTH, Direction.UP, ASSEMBLER_MODULE),
            ModuleRenderInfo(BlockPos(-19, 0, 19), Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(-19, 0, -13), Direction.NORTH, Direction.UP, Direction.NORTH, Direction.UP, ASSEMBLER_MODULE),

            ModuleRenderInfo(BlockPos(16, 0, 26), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(16, 0, -20), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, ASSEMBLER_MODULE),
            ModuleRenderInfo(BlockPos(-16, 0, 26), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(-16, 0, -20), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, ASSEMBLER_MODULE),

            ModuleRenderInfo(BlockPos(23, 0, 19), Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(23, 0, -13), Direction.NORTH, Direction.UP, Direction.NORTH, Direction.UP, ASSEMBLER_MODULE),
            ModuleRenderInfo(BlockPos(-23, 0, 19), Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.UP, RESOURCE_COLLECTION),
            ModuleRenderInfo(BlockPos(-23, 0, -13), Direction.NORTH, Direction.UP, Direction.NORTH, Direction.UP, ASSEMBLER_MODULE)
        )
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
            if (level.getBlockState(blockPos).block == GTLBlocks.POWER_CORE.get()) {
                return blockPos
            }
        }

        return null
    }
}
