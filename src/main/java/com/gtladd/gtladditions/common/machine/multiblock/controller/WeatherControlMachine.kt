package com.gtladd.gtladditions.common.machine.multiblock.controller

import org.gtlcore.gtlcore.common.machine.multiblock.electric.StorageMachine

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.TickableSubscription
import com.gregtechceu.gtceu.common.data.GTItems
import com.gregtechceu.gtceu.common.data.GTMaterials

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.LightningRodBlock

import com.gtladd.gtladditions.utils.MachineUtil.getCircuit
import com.gtladd.gtladditions.utils.MachineUtil.inputFluidStack

open class WeatherControlMachine(holder: IMachineBlockEntity) :
    StorageMachine(holder, 1) {

    private var tickSubs: TickableSubscription? = null

    private var rodCache: List<BlockPos> = emptyList()
    private var lastScanTick = Long.MIN_VALUE

    override fun onStructureFormed() {
        super.onStructureFormed()
        tickSubs = subscribeServerTick(tickSubs, this::onTick)
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        tickSubs?.unsubscribe()
        tickSubs = null
    }

    override fun filter(itemStack: ItemStack): Boolean = when {
        itemStack.`is`(GTItems.FIELD_GENERATOR_LuV.asItem()) -> true
        itemStack.`is`(GTItems.FIELD_GENERATOR_IV.asItem()) -> true
        itemStack.`is`(GTItems.FIELD_GENERATOR_EV.asItem()) -> true
        else -> false
    }

    private fun fieldGeneratorCount(): Int = when {
        machineStorageItem.`is`(GTItems.FIELD_GENERATOR_LuV.asItem()) -> 16
        machineStorageItem.`is`(GTItems.FIELD_GENERATOR_IV.asItem()) -> 8
        machineStorageItem.`is`(GTItems.FIELD_GENERATOR_EV.asItem()) -> 4
        else -> 0
    }

    private fun isStructureBlock(p: BlockPos): Boolean {
        val origin = self().pos
        val dx = p.x - origin.x
        val dy = p.y - origin.y
        val dz = p.z - origin.z
        return dx in -1..1 && dz in -1..1 && dy in 0..6
    }

    private fun onTick() {
        if (isRemote) return
        val level = self().level as? ServerLevel ?: return
        if (getCircuit() != 4) return

        if (offsetTimer % 20L == 0L) {
            level.setWeatherParameters(0, 200, true, true)
        }

        if (!recipeLogic.isWorking) return

        val count = fieldGeneratorCount()
        if (count <= 0) return

        val radonPerTick = when (count) {
            16 -> 4000L
            8 -> 2000L
            else -> 1000L
        }
        if (!inputFluidStack(GTMaterials.Radon.getFluid(radonPerTick))) return

        val interval = when (count) {
            16 -> 10L
            8 -> 15L
            else -> 20L
        }
        if (offsetTimer % interval != 0L) return

        val center = self().pos.relative(frontFacing, -1)
        val range = 4
        val now = offsetTimer
        if (now - lastScanTick >= 200L || rodCache.size < count) {
            lastScanTick = now
            val found = ArrayList<BlockPos>()
            for (p in BlockPos.betweenClosed(
                center.offset(-range, -range, -range),
                center.offset(range, range, range),
            )) {
                if (level.getBlockState(p).block is LightningRodBlock && !isStructureBlock(p)) {
                    found.add(p.immutable())
                    if (found.size >= 16) break
                }
            }
            rodCache = found
        }
        for (p in rodCache.take(count)) {
            EntityType.LIGHTNING_BOLT.spawn(level, p.above(), MobSpawnType.TRIGGERED)
        }
    }
}
