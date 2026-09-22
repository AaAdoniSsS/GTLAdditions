package com.gtladd.gtladditions.common.machine.multiblock.controller.df

import org.gtlcore.gtlcore.common.data.GTLMaterials
import org.gtlcore.gtlcore.common.machine.multiblock.part.HugeFluidHatchPartMachine

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.content.Content
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine

import com.lowdragmc.lowdraglib.side.fluid.FluidStack
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

import com.gtladd.gtladditions.api.machine.ConversationMachine.Companion.MagmatterBlock
import com.gtladd.gtladditions.api.machine.gui.MultiblockDisplayText
import com.gtladd.gtladditions.api.recipe.content.ContentList
import com.gtladd.gtladditions.common.machine.multiblock.controller.Resource.BlackBodyNaquadriaSupersolid
import com.gtladd.gtladditions.common.machine.multiblock.controller.Resource.HyperStableSelfHealingAdhesive
import com.gtladd.gtladditions.common.machine.multiblock.controller.Resource.QuantumAnomaly
import com.gtladd.gtladditions.utils.CollectionUtil.allNull
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import com.gtladd.gtladditions.utils.GTRecipeUtils.amount
import com.gtladd.gtladditions.utils.GTRecipeUtils.setAmount
import com.gtladd.gtladditions.utils.MathUtil.random
import com.hepdd.gtmthings.common.block.machine.multiblock.part.HugeBusPartMachine
import it.unimi.dsi.fastutil.objects.ObjectIntPair

class MagnetorheologicalConvergenceCore(holder: IMachineBlockEntity) : RRFModuleMachine(holder) {

    private var itemHatch1: HugeBusPartMachine? = null
    private var itemHatch2: HugeBusPartMachine? = null
    private var fluidHatch: HugeFluidHatchPartMachine? = null
    private var consumeItemHatch: ItemBusPartMachine? = null

    private var fluidStack: FluidStack? = null
    private val itemArray = arrayOfNulls<ObjectIntPair<Item>>(2)

    @Persisted
    private var failItem1: Byte = 0

    @Persisted
    private var failItem2: Byte = 0

    @Persisted
    private var failFluid: Byte = 3

    @Persisted
    private var isModify = false

    @Persisted
    private var tick = 0

    fun modifyRecipe(recipe: GTRecipe): GTRecipe {
        if (isModify && isWorking) {
            recipe.outputs.forEach { (c, l) ->
                if (l.size > 1) {
                    val lc = ContentList(1)
                    val amount = l.sumOf { it.amount(c) }
                    var cn: Content? = null
                    for (t in l) {
                        if (!("c" == t.slotName || "i" == t.slotName)) {
                            cn = t
                            break
                        }
                    }
                    if (cn != null) {
                        lc.add(cn.setAmount(amount))
                        recipe.outputs[c] = lc
                    }
                }
            }
        }
        return recipe
    }

    override fun startupUpdate() {
        if (tick >= 2880) tick = 0
        if (tick % 100 == 0) findAndConnectToHost()
        if (tick % 240 == 0) {
            failItem1 = 0
            failItem2 = 0
            failFluid = 3
            initArray()
            itemHatch1!!.let {
                val item1 = it.inventory.getStackInSlot(0)
                if (!item1.isEmpty) {
                    if (item1.item == itemArray[0]!!.first()) {
                        failItem1 = -1
                        if (item1.count > itemArray[0]!!.rightInt()) {
                            failItem1 = 1
                        } else if (item1.count < itemArray[0]!!.rightInt()) {
                            failItem1 = 2
                        }
                    } else if (item1.item == itemArray[1]!!.first()) {
                        failItem2 = -1
                        if (item1.count > itemArray[1]!!.rightInt()) {
                            failItem2 = 1
                        } else if (item1.count < itemArray[1]!!.rightInt()) {
                            failItem2 = 2
                        }
                    }
                    it.inventory.extractItemInternal(0, item1.count, false)
                }
            }
            itemHatch2!!.let {
                val item2 = it.inventory.getStackInSlot(0)
                if (!item2.isEmpty) {
                    if (failItem1 == 0.toByte() && item2.item == itemArray[0]!!.first()) {
                        failItem1 = -1
                        if (item2.count > itemArray[0]!!.rightInt()) {
                            failItem1 = 1
                        } else if (item2.count < itemArray[0]!!.rightInt()) {
                            failItem1 = 2
                        }
                    } else if (failItem2 == 0.toByte() && item2.item == itemArray[1]!!.first()) {
                        failItem2 = -1
                        if (item2.count > itemArray[1]!!.rightInt()) {
                            failItem2 = 1
                        } else if (item2.count < itemArray[1]!!.rightInt()) {
                            failItem2 = 2
                        }
                    }
                    it.inventory.extractItemInternal(0, item2.count, false)
                }
            }
            fluidHatch?.let {
                val tf = getFluid()
                val fs = it.tank.storages[0].fluid
                if (!fs.isEmpty) {
                    if (fs.fluid == tf.fluid) {
                        failFluid = -1
                        if (fs.amount > tf.amount) {
                            failFluid = 4
                        } else if (fs.amount < tf.amount) {
                            failFluid = 5
                        }
                    }
                    it.tank.drainInternal(tf, false)
                }
            }
            isModify = failItem1 < 0 && failItem2 < 0 && failFluid < 0
        }
        if (tick % 20 == 0) {
            consumeItemHatch?.let {
                for (i in 0..<it.inventory.size) {
                    val item = it.inventory.getStackInSlot(i)
                    if (item.item == MagmatterBlock && item.count >= 2) {
                        it.inventory.extractItemInternal(i, 2, false)
                        return@let
                    }
                }
                isModify = false
            }
        }
        tick++
    }

    private fun initArray() {
        if (itemArray.allNull()) {
            val list = (0..2).shuffled().take(2).sorted()
            itemArray[0] = ObjectIntPair.of(getItem(0, list), 65536.random())
            itemArray[1] = ObjectIntPair.of(getItem(1, list), 65536.random())
        }
    }

    private fun getItem(n: Int, list: List<Int>) = when (list[n]) {
        0 -> BlackBodyNaquadriaSupersolid
        1 -> QuantumAnomaly
        2 -> HyperStableSelfHealingAdhesive
        else -> Items.AIR
    }

    private fun getFluid(): FluidStack {
        if (fluidStack == null) {
            fluidStack = when (1.random()) {
                0 -> GTLMaterials.ExcitedDtec.getFluid(6553600.random().toLong())
                1 -> GTLMaterials.ExcitedDtsc.getFluid(6553600.random().toLong())
                else -> FluidStack.empty()
            }
        }
        return fluidStack!!
    }

    override fun setWorkable(isWorking: Boolean) {
        super.setWorkable(isWorking)
        this.tick = 0
    }

    override fun partTest(part: IMultiPart) {
        when (part) {
            is HugeBusPartMachine ->
                if (this.itemHatch1 == null) {
                    this.itemHatch1 = part
                } else if (this.itemHatch2 == null) {
                    this.itemHatch2 = part
                }
            is ItemBusPartMachine -> this.consumeItemHatch = part
            is HugeFluidHatchPartMachine -> this.fluidHatch = part
        }
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        this.itemHatch1 = null
        this.itemHatch2 = null
        this.fluidHatch = null
        this.consumeItemHatch = null
    }

    override fun addDisplayText(textList: MutableList<Component>) {
        val builder = MultiblockDisplayText.builder(textList, isFormed())
        builder.addComponent(
            Component.translatable("gtceu.machine.recursive_reverse_forge.gui.module.4", (if (host == null) "×" else "✓")),
            Component.translatable("gtceu.machine.catalytic_cascade_array.gui.tooltip.2", ((tick % 240) / 20)),
            "gtceu.machine.magnetorheological_convergence_core.gui.tooltip.6".toComponent
        )
        if (isModify) {
            builder.addComponent("gtceu.machine.magnetorheological_convergence_core.gui.tooltip.7".toComponent)
            return
        }
        if (failItem1 > -1) builder.addComponent(Component.translatable("gtceu.machine.magnetorheological_convergence_core.gui.tooltip.$failItem1", 1))
        if (failItem2 > -1) builder.addComponent(Component.translatable("gtceu.machine.magnetorheological_convergence_core.gui.tooltip.$failItem2", 2))
        if (failFluid > -1) builder.addComponent("gtceu.machine.magnetorheological_convergence_core.gui.tooltip.$failFluid".toComponent)
    }

    override fun saveCustomPersistedData(tag: CompoundTag, forDrop: Boolean) {
        super.saveCustomPersistedData(tag, forDrop)
        fluidStack?.saveToTag(tag)
        val tags = ListTag()
        itemArray.forEach {
            if (it != null) {
                val mt = CompoundTag()
                mt.putIntArray("itemCount", listOf(Item.getId(it.first()), it.rightInt()))
                tags.add(mt)
            }
        }
        if (tags.isNotEmpty()) tag.put("list", tags)
    }

    override fun loadCustomPersistedData(tag: CompoundTag) {
        super.loadCustomPersistedData(tag)
        fluidStack = FluidStack.loadFromTag(tag)
        if (tag.contains("list")) {
            val lt = tag.get("list") as ListTag
            for (i in 0..1) {
                val c = (lt[i] as CompoundTag).getIntArray("itemCount")
                itemArray[i] = ObjectIntPair.of(Item.byId(c[0]), c[1])
            }
        }
    }

    override fun getFieldHolder() = MANAGED_FIELD_HOLDER

    companion object {
        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(MagnetorheologicalConvergenceCore::class.java, RRFModuleMachine.MANAGED_FIELD_HOLDER)
    }
}
