package com.gtladd.gtladditions.api.async

import com.lowdragmc.lowdraglib.Platform
import com.lowdragmc.lowdraglib.async.AsyncThreadData
import com.lowdragmc.lowdraglib.async.IAsyncLogic

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.material.Fluid

import com.gtladd.gtladditions.utils.LevelUtil.throwItemEntity
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap

class AsyncFluidTransform(val level: ServerLevel, val pos: BlockPos, val itemStack: ItemStack, val expectedFluid: Fluid) : IAsyncLogic {
    private var tick = 0
    private var finished = false

    override fun asyncTick(periodID: Long) {
        if (isClientPaused()) return
        if (++tick < 600 || finished) return
        finished = true
        level.server.execute { complete() }
    }

    fun cancel() {
        if (finished) return
        finished = true
        AsyncThreadData.getOrCreate(level).removeAsyncLogic(this)
    }

    private fun complete() {
        AsyncThreadData.getOrCreate(level).removeAsyncLogic(this)
        if (getTask(level, pos) !== this) return
        removeTask(level, pos)
        if (level.isLoaded(pos)) {
            val fluidState = level.getFluidState(pos)
            if (fluidState.isSource && fluidState.`is`(expectedFluid)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3)
                if (!itemStack.isEmpty) level.throwItemEntity(pos, itemStack)
            }
        }
        tick++
    }

    private fun isClientPaused(): Boolean {
        if (!Platform.isClient()) return false
        val server = Platform.getMinecraftServer() ?: return false
        if (!server.isSingleplayer) return false
        return Minecraft.getInstance().isPaused
    }

    companion object {

        private val TASKS = Object2ObjectOpenHashMap<ResourceLocation, Long2ObjectOpenHashMap<AsyncFluidTransform>>()

        @JvmStatic
        fun register(level: ServerLevel, pos: BlockPos, task: AsyncFluidTransform) {
            TASKS.computeIfAbsent(level.dimension().location()) { Long2ObjectOpenHashMap() }
                .put(pos.asLong(), task)?.cancel()
            AsyncThreadData.getOrCreate(level).addAsyncLogic(task)
        }

        @JvmStatic
        fun unregister(level: ServerLevel, pos: BlockPos) {
            TASKS[level.dimension().location()]?.remove(pos.asLong())?.cancel()
        }

        @JvmStatic
        fun onLevelUnload(level: Level) {
            if (level !is ServerLevel) return
            TASKS.remove(level.dimension().location())?.values?.forEach { it.cancel() }
        }

        private fun getTask(level: ServerLevel, pos: BlockPos): AsyncFluidTransform? = TASKS[level.dimension().location()]?.get(pos.asLong())

        private fun removeTask(level: ServerLevel, pos: BlockPos) {
            TASKS[level.dimension().location()]?.remove(pos.asLong())
        }
    }
}
