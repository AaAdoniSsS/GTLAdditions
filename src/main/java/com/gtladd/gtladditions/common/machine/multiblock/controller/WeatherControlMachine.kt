package com.gtladd.gtladditions.common.machine.multiblock.controller

import org.gtlcore.gtlcore.utils.MachineIO

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.TickableSubscription
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.gregtechceu.gtceu.common.data.GTItems
import com.gregtechceu.gtceu.common.data.GTMaterials

import com.lowdragmc.lowdraglib.gui.widget.SlotWidget
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.LightningRodBlock

open class WeatherControlMachine(holder: IMachineBlockEntity, vararg args: Any) :
    WorkableElectricMultiblockMachine(holder, *args) {

    @Persisted
    protected val fieldGeneratorSlot: NotifiableItemStackHandler =
        NotifiableItemStackHandler(this, 1, IO.NONE, IO.BOTH) { _ ->
            object : ItemStackTransfer(1) {
                override fun getSlotLimit(slot: Int): Int = 1
            }
        }

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

    override fun createUIWidget(): Widget {
        val group = super.createUIWidget() as WidgetGroup
        val slot = SlotWidget(
            fieldGeneratorSlot,
            0,
            group.sizeWidth - 30,
            group.sizeHeight - 30,
            true,
            true,
        )
        slot.setBackground(GuiTextures.SLOT)
        group.addWidget(slot)
        return group
    }

    override fun saveCustomPersistedData(tag: CompoundTag, forDrop: Boolean) {
        super.saveCustomPersistedData(tag, forDrop)
        val stack = fieldGeneratorSlot.getStackInSlot(0)
        if (!stack.isEmpty) tag.put("FieldGenerator", stack.save(CompoundTag()))
    }

    override fun loadCustomPersistedData(tag: CompoundTag) {
        super.loadCustomPersistedData(tag)
        if (tag.contains("FieldGenerator")) {
            fieldGeneratorSlot.setStackInSlot(0, ItemStack.of(tag.getCompound("FieldGenerator")))
        }
    }

    private fun fieldGeneratorCount(): Int {
        val stack: ItemStack = fieldGeneratorSlot.getStackInSlot(0)
        if (stack.isEmpty) return 0
        return when {
            stack.`is`(GTItems.FIELD_GENERATOR_LuV.asItem()) -> 16
            stack.`is`(GTItems.FIELD_GENERATOR_IV.asItem()) -> 8
            stack.`is`(GTItems.FIELD_GENERATOR_EV.asItem()) -> 4
            else -> 0
        }
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
        if (!MachineIO.notConsumableCircuit(this, 4)) return

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
        if (!MachineIO.inputFluid(this, GTMaterials.Radon.getFluid(radonPerTick))) return

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
