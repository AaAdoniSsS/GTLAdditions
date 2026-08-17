package com.gtladd.gtladditions.common.machine.multiblock.controller

import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineModule
import org.gtlcore.gtlcore.common.data.GTLBlocks
import org.gtlcore.gtlcore.common.data.GTLRecipeTypes.ASSEMBLER_MODULE_RECIPES
import org.gtlcore.gtlcore.common.machine.multiblock.electric.SpaceElevatorMachine
import org.gtlcore.gtlcore.utils.MachineUtil

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife
import com.gregtechceu.gtceu.api.recipe.GTRecipe

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel

import com.gtladd.gtladditions.api.machine.GTLAddWorkableElectricMultipleRecipesMachine
import com.gtladd.gtladditions.api.machine.logic.GTLAddMultipleRecipesLogic
import com.gtladd.gtladditions.api.recipe.FastRecipeModify
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import com.gtladd.gtladditions.utils.MathUtil.format
import com.gtladd.gtladditions.utils.MathUtil.pow

import kotlin.math.pow

class AdvancedSpaceElevatorModuleMachine(holder: IMachineBlockEntity) :
    GTLAddWorkableElectricMultipleRecipesMachine(holder),
    IModularMachineModule<SpaceElevatorMachine, AdvancedSpaceElevatorModuleMachine>,
    IMachineLife {

    @DescSynced
    private var spaceElevatorTier = 0
    private var moduleTier = 0

    @Persisted
    private var hostPosition: BlockPos? = null
    private var host: SpaceElevatorMachine? = null

    companion object {
        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(
            AdvancedSpaceElevatorModuleMachine::class.java,
            GTLAddWorkableElectricMultipleRecipesMachine.MANAGED_FIELD_HOLDER
        )
    }

    override fun createRecipeLogic(vararg args: Any) = AdvancedSpaceElevatorModuleMachineRecipeLogic(this)

    override fun getHost(): SpaceElevatorMachine? = host

    override fun setHost(host: SpaceElevatorMachine?) {
        this.host = host
    }

    override fun getHostType(): Class<SpaceElevatorMachine> = SpaceElevatorMachine::class.java

    override fun getHostPosition(): BlockPos? = hostPosition

    override fun setHostPosition(pos: BlockPos?) {
        this.hostPosition = pos
    }

    override fun getFieldHolder() = MANAGED_FIELD_HOLDER

    override fun onConnected(host: SpaceElevatorMachine) {
        getSpaceElevatorTier()
        recipeLogic.updateTickSubscription()
    }

    override fun getHostScanPositions(): Array<out BlockPos> {
        level.takeIf { it is ServerLevel }?.let {
            val pos = getPos()
            val coordinates = arrayOf(
                pos.offset(8, -2, 3),
                pos.offset(8, -2, -3),
                pos.offset(-8, -2, 3),
                pos.offset(-8, -2, -3),
                pos.offset(3, -2, 8),
                pos.offset(3, -2, -8),
                pos.offset(-3, -2, 8),
                pos.offset(-3, -2, -8),
                pos.offset(4, -2, 11),
                pos.offset(4, -2, -11),
                pos.offset(-4, -2, 11),
                pos.offset(-4, -2, -11),
                pos.offset(11, -2, 4),
                pos.offset(-11, -2, 4),
                pos.offset(11, -2, -4),
                pos.offset(-11, -2, -4),
                pos.offset(18, -2, 8),
                pos.offset(18, -2, -8),
                pos.offset(-18, -2, 8),
                pos.offset(-18, -2, -8),
                pos.offset(8, -2, 18),
                pos.offset(8, -2, -18),
                pos.offset(-8, -2, 18),
                pos.offset(-8, -2, -18),
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
            )

            for (i in coordinates) {
                if (it.getBlockState(i).block == GTLBlocks.POWER_CORE.get()) {
                    return arrayOf(
                        i.offset(3, 2, 0),
                        i.offset(-3, 2, 0),
                        i.offset(0, 2, 3),
                        i.offset(0, 2, -3)
                    )
                }
            }
        }
        return MachineUtil.EMPTY_POS_ARRAY
    }

    private fun getSpaceElevatorTier() {
        host?.let {
            val logic = it.recipeLogic
            if (logic.isWorking && logic.progress > 80) {
                spaceElevatorTier = it.tier - 7
                moduleTier = it.casingTier
            } else {
                spaceElevatorTier = 0
                moduleTier = 0
            }
            return
        }
        spaceElevatorTier = 0
        moduleTier = 0
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

    override fun onMachineRemoved() = removeFromHost(this.host)

    override fun onWorking(): Boolean {
        val value = super.onWorking()
        if (this.offsetTimer % 10L == 0L) {
            this.getSpaceElevatorTier()
            if (this.spaceElevatorTier < 1) recipeLogic.progress -= 2
        }
        return value
    }

    override fun addDisplayText(textList: MutableList<Component>) {
        super.addDisplayText(textList)
        this.takeIf { it.isFormed }?.let {
            offsetTimer.takeIf { it % 10L == 0L }?.let { this.getSpaceElevatorTier() }
            textList.add(("tooltip.gtlcore.space_elevator" + (if (spaceElevatorTier < 1) "_not" else "") + "_connected").toComponent)
            textList.add(Component.translatable("gtceu.machine.duration_multiplier.tooltip", .8.pow(spaceElevatorTier - 1).format(2)))
        }
    }

    override fun getOverClock(): FastRecipeModify.OverClockFactor = if (
        this.recipeType == ASSEMBLER_MODULE_RECIPES
    ) {
        FastRecipeModify.OverClockFactor(0.25, 4.0)
    } else {
        FastRecipeModify.OverClockFactor(0.5, 4.0)
    }

    override fun modifyRecipe(recipe: GTRecipe): FastRecipeModify.ReduceResult {
        val multiplier = if (spaceElevatorTier < 1) {
            FastRecipeModify.getDefaultReduce()
        } else {
            FastRecipeModify.ReduceResult(1.0, .8.pow(spaceElevatorTier - 1))
        }
        getRecipeLogic().setReduction(multiplier.reduceEUt, multiplier.reduceDuration)
        return multiplier
    }
    override fun getMaxParallel(): Int = if (host is SpaceElevatorMKII) {
        12.pow(this.moduleTier - 1)
    } else {
        8.pow(this.moduleTier - 1)
    }

    class AdvancedSpaceElevatorModuleMachineRecipeLogic(val asemMachine: AdvancedSpaceElevatorModuleMachine) :
        GTLAddMultipleRecipesLogic(asemMachine) {

        override fun checkRecipe(recipe: GTRecipe): Boolean {
            if (asemMachine.spaceElevatorTier < 1) asemMachine.getSpaceElevatorTier()
            return asemMachine.spaceElevatorTier >= 1 && super.checkRecipe(recipe)
        }
    }
}
