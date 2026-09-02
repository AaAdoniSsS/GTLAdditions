package com.gtladd.gtladditions.common.machine.multiblock.controller

import org.gtlcore.gtlcore.common.machine.multiblock.electric.HarmonyMachine

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.logic.OCParams
import com.gregtechceu.gtceu.api.recipe.logic.OCResult
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers
import com.gregtechceu.gtceu.utils.FormattingUtil

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.level.Level

import com.gtladd.gtladditions.api.manage.HarmonyManager
import com.gtladd.gtladditions.utils.ComponentUtil.literal
import com.gtladd.gtladditions.utils.MathUtil.ln

class ArcanicAstrograph(holder: IMachineBlockEntity) : HarmonyMachine(holder) {
    companion object {
        fun recipeModifier(machine: MetaMachine, recipe: GTRecipe, params: OCParams, result: OCResult): GTRecipe? {
            HarmonyMachine.recipeModifier(machine, recipe, params, result)?.let {
                val parallel = getMachineParallel(machine.level, machine as ArcanicAstrograph)
                return GTRecipeModifiers.accurateParallel(machine, it, parallel, false).getFirst()
            }
            return null
        }
        fun getMachineParallel(level: Level?, machine: ArcanicAstrograph): Int {
            if (level == null) return 0
            val machineCount = HarmonyManager.getMachineCount(level)
            return (924 * ln(machineCount.firstInt() + 15 * (machineCount.secondInt() + if (machine.recipeLogic.isWorking) 0 else 1))).toInt()
        }
    }

    override fun addDisplayText(textList: MutableList<Component>) {
        super.addDisplayText(textList)
        if (isFormed) {
            textList.add(
                Component.translatable(
                    "gtceu.multiblock.parallel",
                    FormattingUtil.formatNumbers(getMachineParallel(this.level, this)).literal
                        .withStyle(ChatFormatting.DARK_PURPLE)
                ).withStyle(ChatFormatting.GRAY)
            )
        }
    }
}
