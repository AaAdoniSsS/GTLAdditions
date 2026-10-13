package com.gtladd.gtladditions.data.recipes

import org.gtlcore.gtlcore.common.data.GTLMaterials.Jasper
import org.gtlcore.gtlcore.common.data.GTLMaterials.RawTengam
import org.gtlcore.gtlcore.common.data.GTLRecipeTypes.INTEGRATED_ORE_PROCESSOR
import org.gtlcore.gtlcore.config.ConfigHolder

import com.gregtechceu.gtceu.api.data.tag.TagPrefix.*
import com.gregtechceu.gtceu.common.data.GTMaterials.*

import net.minecraft.data.recipes.FinishedRecipe

import com.gtladd.gtladditions.GTLAdditions.id

import java.util.function.Consumer

object IntegratedtedOreProcessor {
    val oreNumber: Int = ConfigHolder.INSTANCE.content.oreMultiplier
    val oreFluid: Int = 100 * oreNumber

    @JvmStatic
    fun init(provider: Consumer<FinishedRecipe>) {
        INTEGRATED_ORE_PROCESSOR.recipeBuilder(id("jasper_ore_processed"))
            .circuitMeta(24)
            .inputItems(ore, Jasper)
            .inputFluids(DistilledWater.getFluid(2L * oreFluid))
            .outputItems(dust, Jasper, 2 * oreNumber)
            .chancedOutput(dust, Talc, 1400, 850)
            .chancedOutput(dust, Talc, 2 * oreNumber, 3300, 0)
            .outputItems(dust, Stone, 2 * oreNumber)
            .chancedOutput(dust, Boron, 2 * oreNumber, 1400, 850)
            .chancedOutput(dust, RawTengam, 2 * oreNumber, 1000, 0)
            .chancedOutput(dust, RawTengam, 2 * oreNumber, 500, 0)
            .addData("handle", 1)
            .EUt(30).duration(26 + 800 * 2 * oreNumber).save(provider)
        INTEGRATED_ORE_PROCESSOR.recipeBuilder(id("jasper_raw_ore_processed"))
            .circuitMeta(24)
            .inputItems(rawOre, Jasper)
            .inputFluids(DistilledWater.getFluid(oreFluid.toLong()))
            .outputItems(dust, Jasper, oreNumber)
            .chancedOutput(dust, Talc, 1400, 850)
            .chancedOutput(dust, Talc, oreNumber, 3300, 0)
            .outputItems(dust, Stone, oreNumber)
            .chancedOutput(dust, Boron, oreNumber, 1400, 850)
            .chancedOutput(dust, RawTengam, oreNumber, 1000, 0)
            .chancedOutput(dust, RawTengam, oreNumber, 500, 0)
            .addData("handle", 1)
            .EUt(30).duration(26 + 800 * oreNumber).save(provider)
        val arrays = arrayOf(
            arrayOf(Cooperite, Nickel, Palladium, Mercury),
            arrayOf(Bornite, Pyrite, Gold, Mercury),
            arrayOf(Tetrahedrite, Antimony, Cadmium, SodiumPersulfate),
            arrayOf(Chalcocite, Sulfur)
        )
        for (pure in arrays) {
            INTEGRATED_ORE_PROCESSOR.recipeBuilder(id("purified_" + pure[0].name + "_ore_8"))
                .circuitMeta(8)
                .inputItems(ore, pure[0])
                .inputFluids(DistilledWater.getFluid(2L * oreFluid))
                .outputItems(crushedPurified, pure[0], 2 * oreNumber)
                .chancedOutput(dust, pure[1], 1400, 850)
                .chancedOutput(dust, pure[1], 2 * oreNumber, 3300, 0)
                .outputItems(dust, Stone, 2 * oreNumber)
                .EUt(30).duration(26 + 200 * 2 * oreNumber).save(provider)
            INTEGRATED_ORE_PROCESSOR.recipeBuilder(id("purified_" + pure[0].name + "_raw_ore_8"))
                .circuitMeta(8)
                .inputItems(rawOre, pure[0])
                .inputFluids(DistilledWater.getFluid(oreFluid.toLong()))
                .outputItems(crushedPurified, pure[0], oreNumber)
                .chancedOutput(dust, pure[1], 1400, 850)
                .chancedOutput(dust, pure[1], oreNumber, 3300, 0)
                .outputItems(dust, Stone, oreNumber)
                .EUt(30).duration(26 + 200 * oreNumber).save(provider)
            if (pure[0] == Chalcocite) return
            INTEGRATED_ORE_PROCESSOR.recipeBuilder(id("purified_" + pure[0].name + "_ore_9"))
                .circuitMeta(9)
                .inputItems(ore, pure[0])
                .inputFluids(pure[3].getFluid(2L * oreFluid))
                .outputItems(crushedPurified, pure[0], 2 * oreNumber)
                .chancedOutput(dust, pure[1], 1400, 850)
                .chancedOutput(dust, pure[2], 2 * oreNumber, 7000, 580)
                .outputItems(dust, Stone, 2 * oreNumber)
                .EUt(30).duration(26 + 200 * 2 * oreNumber).save(provider)
            INTEGRATED_ORE_PROCESSOR.recipeBuilder(id("purified_" + pure[0].name + "_raw_ore_9"))
                .circuitMeta(9)
                .inputItems(rawOre, pure[0])
                .inputFluids(pure[3].getFluid(oreFluid.toLong()))
                .outputItems(crushedPurified, pure[0], oreNumber)
                .chancedOutput(dust, pure[1], 1400, 850)
                .chancedOutput(dust, pure[2], oreNumber, 7000, 580)
                .outputItems(dust, Stone, oreNumber)
                .EUt(30).duration(26 + 200 * oreNumber).save(provider)
        }
    }
}
