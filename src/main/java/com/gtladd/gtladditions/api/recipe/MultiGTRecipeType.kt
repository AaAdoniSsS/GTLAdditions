package com.gtladd.gtladditions.api.recipe

import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.api.recipe.lookup.GTRecipeLookup
import com.gregtechceu.gtceu.api.registry.GTRegistries

import net.minecraft.resources.ResourceLocation

import com.gtladd.gtladditions.api.recipe.lookup.MultiGTRecipeLookup

class MultiGTRecipeType : GTRecipeType {
    private val typeList: Array<GTRecipeType>
    private val lookup: MultiGTRecipeLookup

    constructor(types: Array<GTRecipeType>, registryName: ResourceLocation, group: String) : super(registryName, group) {
        setXEIVisible(false)
        setRecipeBuilder(null)
        recipeUI = null
        this.typeList = types
        this.lookup = MultiGTRecipeLookup(*types)
    }

    override fun getLookup(): GTRecipeLookup = this.lookup

    fun getTypeList(): Array<GTRecipeType> = this.typeList

    companion object {
        fun registry(registryName: ResourceLocation, group: String, vararg types: GTRecipeType): GTRecipeType {
            val type = MultiGTRecipeType(Array(types.size) { types[it] }, registryName, group)
            GTRegistries.RECIPE_TYPES.register(type.registryName, type)
            return type
        }
    }
}
