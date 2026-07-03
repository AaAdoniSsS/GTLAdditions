package com.gtladd.gtladditions.common.machine.multiblock.controller

import org.gtlcore.gtlcore.api.machine.trait.MEStock.IMETransfer
import org.gtlcore.gtlcore.api.recipe.RecipeResult
import org.gtlcore.gtlcore.api.recipe.RecipeResult.fail
import org.gtlcore.gtlcore.common.data.GTLItems

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.common.data.GTMachines
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks

import appeng.api.config.Actionable
import appeng.api.stacks.AEItemKey
import appeng.api.storage.cells.CellState
import com.glodblock.github.extendedae.common.EPPItemAndBlock
import com.gtladd.gtladditions.api.machine.ConversationMachine
import com.gtladd.gtladditions.api.machine.IEnergyMachine
import com.gtladd.gtladditions.utils.AEUtil.getItem
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import com.gtladd.gtladditions.utils.MachineUtil.inputItemStack
import com.gtladd.gtladditions.utils.MathUtil.minToLong
import com.gtladd.gtladditions.utils.Registries.getBlock
import com.gtladd.gtladditions.utils.Registries.getItemStack
import dev.latvian.mods.kubejs.KubeJS

class CreateAggregationMachine(holder: IMachineBlockEntity) : ConversationMachine(holder) {

    override fun onWorking(): Boolean {
        recipeLogic.progress.takeIf { it == 19 && level is ServerLevel && getCircuit() == 1 }?.let {
            val pos = pos.offset(0, -16, 0)
            when (level!!.getBlockState(pos).block) {
                CommandBlockBroken -> if (this.inputItemStack(ChainCommandBlockCore)) {
                    level!!.setBlockAndUpdate(pos, Blocks.CHAIN_COMMAND_BLOCK.defaultBlockState())
                }
                ChainCommandBlockBroken -> if (this.inputItemStack(RepeatingCommandBlockCore)) {
                    level!!.setBlockAndUpdate(pos, Blocks.REPEATING_COMMAND_BLOCK.defaultBlockState())
                }
                EPPItemAndBlock.FISHBIG -> if (this.inputItemStack(UltimateTea)) {
                    level!!.setBlockAndUpdate(pos, GTMachines.CREATIVE_FLUID.defaultBlockState())
                } else if (this.inputItemStack(Heartofthesmogus)) {
                    level!!.setBlockAndUpdate(pos, GTMachines.CREATIVE_ITEM.defaultBlockState())
                }
            }
        }
        return true
    }

    override fun afterWorking() {
        if (getCircuit() != 24) return
        bcHatch?.transformOther {
            if (!it.isCreate) {
                RecipeResult.ofWorking(this, fail("gtceu.machine.block_conversation.fail.2".toComponent))
                return@transformOther
            }
            val c = it.getCellInventory()
            if (c == null) {
                RecipeResult.ofWorking(this, fail("gtceu.machine.block_conversation.fail.0".toComponent))
                return@transformOther
            }
            if (c.status == CellState.FULL || c.status == CellState.TYPES_FULL) {
                RecipeResult.ofWorking(this, fail("gtceu.machine.block_conversation.fail.1".toComponent))
                return@transformOther
            }
            RecipeResult.ofWorking(this, null)
            val m = it.aeItemHandler.inventory.map { slot -> slot.stock?.getItem ?: Items.AIR }.withIndex().associate { (i, v) -> v to i }
            itemPair.map { ip ->
                val f = m[ip.first]
                val s = m[ip.second]
                if (f != null && s != null) f to s else -1 to -1
            }.filter { pair -> pair.first != -1 }
                .forEach { pair ->
                    val s1 = it.aeItemHandler.inventory[pair.first]
                    val s2 = it.aeItemHandler.inventory[pair.second]
                    val pa = this.parallel minToLong s1.stock!!.amount minToLong s2.stock!!.amount
                    val g1 = (s1 as IMETransfer).extractGenericStack(pa, false, true)
                    (s2 as IMETransfer).extractGenericStack(pa, false, true)
                    val key = when (g1!!.getItem) {
                        CommandBlockBroken.asItem(), ChainCommandBlockCore.item -> AEItemKey.of(Blocks.CHAIN_COMMAND_BLOCK.asItem())
                        ChainCommandBlockBroken.asItem(), RepeatingCommandBlockCore.item -> AEItemKey.of(Blocks.REPEATING_COMMAND_BLOCK.asItem())
                        else -> null
                    }
                    it.insertCell(key!!, pa, Actionable.MODULATE)
                }
        }
    }

    override fun refreshSlot() {
        when (machineStorage.getStackInSlot(0).item) {
            CARD_3 -> {
                cardId = 3
                parallel = 8192
            }
            else -> {
                cardId = 0
                parallel = 0
            }
        }
    }

    override fun isWork(): Boolean {
        val c = getCircuit()
        return c == 1 || c == 24
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun tickConsume(): Boolean {
        val ecList = (this as IEnergyMachine).energyContainerList
        if (EU <= ecList.energyStored && dRecipe.matchTickRecipe(this).isSuccess) {
            dRecipe.handleTickRecipeIO(IO.IN, this, this.recipeLogic.chanceCaches)
            ecList.changeEnergy(-EU)
            return true
        }
        return false
    }

    override fun getStartRecipe() = dRecipe

    companion object {
        val dRecipe: GTRecipe by lazy { GTRecipeBuilder.ofRaw().CWUt(GTValues.VA[14]).duration(20).dimension(KubeJS.id("create")).buildRawRecipe() }
        val CommandBlockBroken = "kubejs:command_block_broken".getBlock
        val ChainCommandBlockBroken = "kubejs:chain_command_block_broken".getBlock
        val ChainCommandBlockCore = "kubejs:chain_command_block_core".getItemStack()
        val RepeatingCommandBlockCore = "kubejs:repeating_command_block_core".getItemStack()
        val UltimateTea: ItemStack = GTLItems.ULTIMATE_TEA.asStack(8)
        val itemPair: List<Pair<Item, Item>> by lazy { listOf(CommandBlockBroken.asItem() to ChainCommandBlockCore.item, ChainCommandBlockBroken.asItem() to RepeatingCommandBlockCore.item) }
        const val EU: Long = 16L * Int.MAX_VALUE
    }
}
