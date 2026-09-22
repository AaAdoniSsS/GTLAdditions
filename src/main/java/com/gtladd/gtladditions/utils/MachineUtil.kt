package com.gtladd.gtladditions.utils

import org.gtlcore.gtlcore.api.machine.trait.IRecipeCapabilityMachine
import org.gtlcore.gtlcore.api.recipe.RecipeResult.fail
import org.gtlcore.gtlcore.api.recipe.RecipeResult.of
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour.getCircuitConfiguration
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder
import com.gregtechceu.gtceu.utils.FluidStackHashStrategy
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy

import com.lowdragmc.lowdraglib.side.fluid.FluidStack

import net.minecraft.network.chat.Component.translatable
import net.minecraft.world.item.ItemStack

import com.gtladd.gtladditions.api.recipe.content.ContentList
import com.gtladd.gtladditions.utils.MathUtil.safeToInt
import com.hepdd.gtmthings.common.block.machine.multiblock.part.HugeBusPartMachine
import it.unimi.dsi.fastutil.objects.Object2LongMaps
import it.unimi.dsi.fastutil.objects.ObjectArrayList

object MachineUtil {

    fun WorkableMultiblockMachine.inputItemStack(vararg stack: ItemStack): Boolean {
        if (stack.isEmpty()) return true
        val rcm = this as IRecipeCapabilityMachine
        val contentMap = O2LHashMap(ItemStackHashStrategy.comparingAllButCount())
        rcm.getNormalRecipeHandlePart(IO.IN).forEach { handle ->
            Object2LongMaps.fastForEach(handle.getSelfContent(ItemRecipeCapability.CAP, true)) { entry ->
                contentMap.addTo(entry.key, entry.longValue)
            }
        }
        rcm.sharedRecipeHandlePart?.let {
            Object2LongMaps.fastForEach(it.getSelfContent(ItemRecipeCapability.CAP, true)) { entry ->
                contentMap.addTo(entry.key, entry.longValue)
            }
        }
        val list = ObjectArrayList<ItemStack>(stack.size)
        stack.forEach {
            val count = contentMap.getLong(it)
            if (count < it.count) list.add(it.copyWithCount((it.count - count).safeToInt))
        }
        if (list.isEmpty) {
            val builder = GTRecipeBuilder.ofRaw()
            builder.input[ItemRecipeCapability.CAP] = ContentList.getItemStackList(*stack)
            val recipe = builder.buildRawRecipe()
            return RecipeRunnerHelper.handleRecipeInput(this, recipe)
        } else {
            of(this, fail(translatable("gtceu.recipe.fail.no.input.item", list.joinToString(", ") { it.displayName.string + "x${it.count}" })))
            return false
        }
    }

    fun WorkableMultiblockMachine.inputFluidStack(vararg stack: FluidStack): Boolean {
        if (stack.isEmpty()) return true
        val rcm = this as IRecipeCapabilityMachine
        val contentMap = O2LHashMap(FluidStackHashStrategy.comparingAllButAmount())
        rcm.getNormalRecipeHandlePart(IO.IN).forEach { handle ->
            Object2LongMaps.fastForEach(handle.getSelfContent(FluidRecipeCapability.CAP, true)) { entry ->
                contentMap.addTo(entry.key, entry.longValue)
            }
        }
        rcm.sharedRecipeHandlePart?.let {
            Object2LongMaps.fastForEach(it.getSelfContent(FluidRecipeCapability.CAP, true)) { entry ->
                contentMap.addTo(entry.key, entry.longValue)
            }
        }
        val list = ObjectArrayList<FluidStack>(stack.size)
        stack.forEach {
            val amount = contentMap.getLong(it)
            if (amount < it.amount) list.add(it.copy(it.amount - amount))
        }
        if (list.isEmpty) {
            val builder = GTRecipeBuilder.ofRaw()
            builder.input[FluidRecipeCapability.CAP] = ContentList.getFluidStackList(*stack)
            val recipe = builder.buildRawRecipe()
            return RecipeRunnerHelper.handleRecipeInput(this, recipe)
        } else {
            of(this, fail(translatable("gtceu.recipe.fail.no.input.fluid", list.joinToString(", ") { it.displayName.string + "x${it.amount}mB" })))
            return false
        }
    }

    fun MultiblockControllerMachine.getCircuit(): Int {
        for (p in this.parts) {
            if (p is ItemBusPartMachine) {
                val i = p.circuitInventory.getStackInSlot(0)
                if (i.isEmpty) continue
                return getCircuitConfiguration(i)
            } else if (p is HugeBusPartMachine) {
                val i = p.circuitInventory.getStackInSlot(0)
                if (i.isEmpty) continue
                return getCircuitConfiguration(i)
            }
        }
        return 0
    }

    fun WorkableMultiblockMachine.maintenance(): Double = (this as? IRecipeCapabilityMachine)?.maintenanceMachine?.durationMultiplier?.toDouble() ?: 1.0
}
