package com.gtladd.gtladditions.mixin.gtlcore.machine;

import org.gtlcore.gtlcore.common.data.GTLBlocks;
import org.gtlcore.gtlcore.common.machine.multiblock.electric.SpaceElevatorModuleMachine;
import org.gtlcore.gtlcore.utils.MachineUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SpaceElevatorModuleMachine.class)
public abstract class SpaceElevatorModuleMachineMixin {

    /**
     * @author GNSW
     * @reason 新增更多的模块相对坐标
     */
    @Overwrite(remap = false)
    public BlockPos[] getHostScanPositions() {
        BlockPos pos = ((SpaceElevatorModuleMachine) (Object) this).getPos();
        BlockPos[] powerCorePositions = new BlockPos[] {
                pos.offset(8, -2, 3),
                pos.offset(8, -2, -3),
                pos.offset(-8, -2, 3),
                pos.offset(-8, -2, -3),
                pos.offset(3, -2, 8),
                pos.offset(-3, -2, 8),
                pos.offset(3, -2, -8),
                pos.offset(-3, -2, -8),
                pos.offset(4, -2, 9),
                pos.offset(4, -2, -9),
                pos.offset(-4, -2, 9),
                pos.offset(-4, -2, -9),
                pos.offset(9, -2, 4),
                pos.offset(-9, -2, 4),
                pos.offset(9, -2, -4),
                pos.offset(-9, -2, -4),
                pos.offset(18, -2, 8),
                pos.offset(18, -2, -8),
                pos.offset(-18, -2, 8),
                pos.offset(-18, -2, -8),
                pos.offset(9, -2, 18),
                pos.offset(9, -2, -18),
                pos.offset(-9, -2, 18),
                pos.offset(-9, -2, -18),
                pos.offset(1, -2, 21),
                pos.offset(1, -2, -21),
                pos.offset(-1, -2, 21),
                pos.offset(-1, -2, -21),
                pos.offset(21, -2, 1),
                pos.offset(21, -2, -1),
                pos.offset(-21, -2, 1),
                pos.offset(-21, -2, -1),
                pos.offset(7, -2, 22),
                pos.offset(7, -2, -22),
                pos.offset(-7, -2, 22),
                pos.offset(-7, -2, -22),
                pos.offset(22, -2, 7),
                pos.offset(22, -2, -7),
                pos.offset(-22, -2, 7),
                pos.offset(-22, -2, -7),
                pos.offset(26, -2, 6),
                pos.offset(26, -2, -6),
                pos.offset(-26, -2, 6),
                pos.offset(-26, -2, -6),
                pos.offset(6, -2, 26),
                pos.offset(6, -2, -26),
                pos.offset(-6, -2, 26),
                pos.offset(-6, -2, -26),
                pos.offset(16, -2, 19),
                pos.offset(16, -2, -19),
                pos.offset(-16, -2, 19),
                pos.offset(-16, -2, -19),
                pos.offset(19, -2, 16),
                pos.offset(19, -2, -16),
                pos.offset(-19, -2, 16),
                pos.offset(-19, -2, -16),
                pos.offset(16, -2, 23),
                pos.offset(16, -2, -23),
                pos.offset(-16, -2, 23),
                pos.offset(-16, -2, -23),
                pos.offset(23, -2, 16),
                pos.offset(23, -2, -16),
                pos.offset(-23, -2, 16),
                pos.offset(-23, -2, -16),
        };
        Level var4 = ((SpaceElevatorModuleMachine) (Object) this).getLevel();
        if (var4 instanceof ServerLevel serverLevel) {
            for (BlockPos i : powerCorePositions) {
                if (serverLevel.getBlockState(i).getBlock() == GTLBlocks.POWER_CORE.get()) {
                    return new BlockPos[] {
                            i.offset(3, 2, 0),
                            i.offset(-3, 2, 0),
                            i.offset(0, 2, 3),
                            i.offset(0, 2, -3)
                    };
                }
            }
        }

        return MachineUtil.EMPTY_POS_ARRAY;
    }
}
