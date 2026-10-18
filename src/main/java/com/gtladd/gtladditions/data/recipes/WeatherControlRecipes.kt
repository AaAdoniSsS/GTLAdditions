package com.gtladd.gtladditions.data.recipes

import org.gtlcore.gtlcore.common.data.GTLRecipeTypes.WEATHER_CONTROL_RECIPES

import com.gregtechceu.gtceu.api.GTValues.EV
import com.gregtechceu.gtceu.api.GTValues.VA

import net.minecraft.data.recipes.FinishedRecipe

import java.util.function.Consumer

object WeatherControlRecipes {

    @JvmStatic
    fun init(provider: Consumer<FinishedRecipe>) {
        WEATHER_CONTROL_RECIPES.recipeBuilder("weather_control_4")
            .circuitMeta(4)
            .duration(200)
            .EUt(VA[EV].toLong())
            .save(provider)
    }
}
