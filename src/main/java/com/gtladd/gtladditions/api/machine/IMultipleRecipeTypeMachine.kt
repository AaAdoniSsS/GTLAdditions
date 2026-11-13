package com.gtladd.gtladditions.api.machine

import org.gtlcore.gtlcore.integration.monitor.IMonitorRecipeType

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine
import com.gregtechceu.gtceu.api.recipe.GTRecipeType

import net.minecraft.network.chat.Component

import com.gtladd.gtladditions.api.recipe.MultiGTRecipeType
import com.gtladd.gtladditions.utils.ComponentUtil.literal
import com.gtladd.gtladditions.utils.ComponentUtil.translatable

interface IMultipleRecipeTypeMachine : IRecipeLogicMachine, IMonitorRecipeType {

    val multiRecipeTypes: Array<GTRecipeType>

    val multiRecipeType: GTRecipeType get() = this.multiRecipeTypes[activeRecipeType]

    override fun monitorRecipeTypeName(): Component = (
        (multiRecipeType as? MultiGTRecipeType)?.getTypeList()?.joinToString(", ") { it.registryName.toLanguageKey().translatable }
            ?: multiRecipeType.registryName.toLanguageKey().translatable
        ).literal
}
