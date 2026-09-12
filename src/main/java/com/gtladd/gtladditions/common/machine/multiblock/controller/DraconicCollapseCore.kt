package com.gtladd.gtladditions.common.machine.multiblock.controller

import org.gtlcore.gtlcore.common.machine.multiblock.electric.StorageMachine

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.logic.OCParams
import com.gregtechceu.gtceu.api.recipe.logic.OCResult

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack

import com.gtladd.gtladditions.api.recipe.FastRecipeModify
import com.gtladd.gtladditions.common.register.GTLAddItems
import com.gtladd.gtladditions.utils.ComponentUtil.literal
import com.gtladd.gtladditions.utils.MathUtil.pow

class DraconicCollapseCore(holder: IMachineBlockEntity) : StorageMachine(holder, 1) {

    @Persisted
    var isSuper = false

    init {
        machineStorage.addChangedListener {
            isSuper = GTLAddItems.HARMONIZING_CORE.isIn(machineStorageItem)
            if (isFormed) {
                (level as? ServerLevel)?.server?.execute {
                    patternLock.lock()
                    try {
                        this.onStructureInvalid()
                        val mwsd = MultiblockWorldSavedData.getOrCreate(level as ServerLevel)
                        mwsd.removeMapping(this.multiblockState)
                        mwsd.addAsyncLogic(this)
                    } finally {
                        patternLock.unlock()
                    }
                }
            }
        }
    }

    override fun filter(itemStack: ItemStack) = GTLAddItems.HARMONIZING_CORE.isIn(itemStack)

    override fun addDisplayText(textList: MutableList<Component>) {
        super.addDisplayText(textList)
        if (isFormed) {
            textList.add(
                Component.translatable(
                    "gtceu.multiblock.parallel",
                    (if (isSuper) 12 else 8).pow(this.getTier() - 10).literal
                        .withStyle(ChatFormatting.DARK_PURPLE)
                ).withStyle(ChatFormatting.GRAY)
            )
        }
    }

    override fun getFieldHolder() = MANAGED_FIELD_HOLDER

    companion object {
        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(DraconicCollapseCore::class.java, StorageMachine.MANAGED_FIELD_HOLDER)
        fun recipeModify(machine: MetaMachine, recipe: GTRecipe, ocParams: OCParams, ocResult: OCResult): GTRecipe? {
            if (machine is DraconicCollapseCore) {
                return FastRecipeModify.modify(
                    machine,
                    recipe,
                    (if (machine.isSuper) 12L else 8L).pow(machine.tier - 10),
                    ocResult = FastRecipeModify.getPerfectOverclock()
                ) { FastRecipeModify.getDefaultReduce() }
            }
            return null
        }
    }
}
