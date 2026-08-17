package com.gtladd.gtladditions.common.machine.multiblock.controller

import org.gtlcore.gtlcore.api.recipe.IGTRecipe

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.recipe.GTRecipe

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

import com.gtladd.gtladditions.api.machine.GTLAddWorkableElectricMultipleRecipesMachine
import com.gtladd.gtladditions.api.machine.GTLAddWorkableElectricParallelHatchMultipleRecipesMachine
import com.gtladd.gtladditions.api.recipe.FastRecipeModify
import com.gtladd.gtladditions.common.machine.multiblock.controller.Resource.CreateUltimateBattery
import com.gtladd.gtladditions.common.machine.multiblock.controller.Resource.CryotheumDust
import com.gtladd.gtladditions.utils.MachineUtil.inputItemStack
import com.gtladd.gtladditions.utils.MathUtil.ln
import com.gtladd.gtladditions.utils.MathUtil.maxToInt
import com.gtladd.gtladditions.utils.MathUtil.pow
import com.gtladd.gtladditions.utils.MathUtil.safeToInt

class AntientropyCondensationCenter(holder: IMachineBlockEntity) : GTLAddWorkableElectricParallelHatchMultipleRecipesMachine(holder) {
    @Persisted
    private var isModify = false

    override fun modifyRecipe(recipe: GTRecipe): FastRecipeModify.ReduceResult {
        val multiplier = if (isModify) {
            FastRecipeModify.ReduceResult(0.5, 0.7)
        } else {
            super.modifyRecipe(recipe)
        }
        getRecipeLogic().setReduction(multiplier.reduceEUt, multiplier.reduceDuration)
        return multiplier
    }

    override fun onUse(state: BlockState, world: Level, pos: BlockPos, player: Player, hand: InteractionHand, hit: BlockHitResult): InteractionResult {
        if (!world.isClientSide && !this.isModify) {
            val stack = player.`kjs$getMainHandItem`()
            if (stack.`is`(CreateUltimateBattery)) {
                val a = stack.count - 1
                player.`kjs$setMainHandItem`(if (a == 0) ItemStack.EMPTY else ItemStack(CreateUltimateBattery, a))
                this.isModify = true
                getRecipeLogic().setReduction(0.5, 0.7)
            }
        }
        return super.onUse(state, world, pos, player, hand, hit)
    }

    override fun testBefore(obj: Object): Boolean {
        val l = (obj as? IGTRecipe)?.realParallels ?: (obj as Long)
        val count = 5 * (l / 2.pow(19) + 51.ln(l)) / 1.maxToInt(tier - 9)
        return this.inputItemStack(ItemStack(CryotheumDust, count.safeToInt))
    }

    override fun getFieldHolder() = MANAGED_FIELD_HOLDER

    companion object {
        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(AntientropyCondensationCenter::class.java, GTLAddWorkableElectricMultipleRecipesMachine.MANAGED_FIELD_HOLDER)
    }
}
