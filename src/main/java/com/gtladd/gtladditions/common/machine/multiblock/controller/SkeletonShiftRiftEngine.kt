package com.gtladd.gtladditions.common.machine.multiblock.controller

import org.gtlcore.gtlcore.api.recipe.RecipeMultiplierTracker
import org.gtlcore.gtlcore.common.data.GTLRecipeTypes.DECAY_HASTENER_RECIPES

import com.gregtechceu.gtceu.api.block.ICoilType
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.common.block.CoilBlock

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style

import com.gtladd.gtladditions.api.machine.GTLAddWorkableElectricMultipleRecipeTypeMachine
import com.gtladd.gtladditions.api.recipe.FastRecipeModify
import com.gtladd.gtladditions.common.recipe.GTLAddRecipesTypes.SkeletonShiftRiftEngineType
import com.gtladd.gtladditions.utils.ComponentUtil.literal
import com.gtladd.gtladditions.utils.MathUtil.minToInt
import com.gtladd.gtladditions.utils.MathUtil.pow

class SkeletonShiftRiftEngine(holder: IMachineBlockEntity) : GTLAddWorkableElectricMultipleRecipeTypeMachine(holder) {
    fun isBatchEnabled(): Boolean = false

    fun setBatchEnabled() {}

    fun supportsBatchProcessing(): Boolean = false

    fun canConfigureBatchProcessing(): Boolean = false

    private var casingTier = 0
    private var coilType: ICoilType = CoilBlock.CoilType.CUPRONICKEL

    override fun onStructureFormed() {
        super.onStructureFormed()
        this.coilType = multiblockState.matchContext.get("CoilType")
        this.casingTier = multiblockState.matchContext.get("SCTier")
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        this.coilType = CoilBlock.CoilType.CUPRONICKEL
        this.casingTier = 0
    }

    override fun addDisplayText(textList: MutableList<Component>) {
        super.addDisplayText(textList)
        textList.add(
            Component.translatable(
                "gtceu.multiblock.blast_furnace.max_temperature",
                "${coilType.coilTemperature}K".literal.setStyle(Style.EMPTY.withColor(ChatFormatting.RED))
            )
        )
        textList.add(Component.translatable("gtceu.casings.tier", this.casingTier))
    }

    override val multiRecipeTypes: Array<GTRecipeType> = arrayOf(DECAY_HASTENER_RECIPES, SkeletonShiftRiftEngineType)

    override fun parallel() = Int.MAX_VALUE minToInt 2.pow(this.coilType.coilTemperature / 1200)

    override fun modifyRecipe(recipe: GTRecipe): GTRecipe? {
        val durationMultiplier = if (casingTier > 0) 1.0 / casingTier else 1.0
        RecipeMultiplierTracker.captureReduction(
            this,
            recipe,
            1.0,
            durationMultiplier
        )
        return FastRecipeModify.modify(
            this,
            recipe,
            parallel().toLong(),
            ocResult = FastRecipeModify.getPerfectOverclock()
        ) { FastRecipeModify.ReduceResult(1.0, durationMultiplier) }
    }
}
