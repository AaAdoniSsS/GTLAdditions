package com.gtladd.gtladditions.data.recipes

import org.gtlcore.gtlcore.common.data.GTLItems.*
import org.gtlcore.gtlcore.common.data.GTLMaterials.*
import org.gtlcore.gtlcore.common.data.GTLRecipeTypes.DECAY_HASTENER_RECIPES
import org.gtlcore.gtlcore.common.data.GTLRecipeTypes.DIMENSIONALLY_TRANSCENDENT_MIXER_RECIPES
import org.gtlcore.gtlcore.common.data.GTLRecipeTypes.GREENHOUSE_RECIPES
import org.gtlcore.gtlcore.common.data.GTLRecipeTypes.INCUBATOR_RECIPES
import org.gtlcore.gtlcore.common.data.GTLRecipeTypes.MAGIC_MANUFACTURER_RECIPES
import org.gtlcore.gtlcore.common.data.GTLRecipeTypes.WORLD_DATA_SCANNER_RECIPES
import org.gtlcore.gtlcore.common.recipe.condition.GravityCondition
import org.gtlcore.gtlcore.config.ConfigHolder

import com.gregtechceu.gtceu.api.GTValues.*
import com.gregtechceu.gtceu.api.data.tag.TagPrefix.*
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient
import com.gregtechceu.gtceu.common.data.GTItems.*
import com.gregtechceu.gtceu.common.data.GTMaterials.*
import com.gregtechceu.gtceu.common.data.GTMaterials.Oxygen
import com.gregtechceu.gtceu.common.data.GTRecipeTypes.ALLOY_SMELTER_RECIPES
import com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLY_LINE_RECIPES
import com.gregtechceu.gtceu.common.data.GTRecipeTypes.FORMING_PRESS_RECIPES
import com.gregtechceu.gtceu.common.data.GTRecipeTypes.LARGE_CHEMICAL_RECIPES
import com.gregtechceu.gtceu.common.data.GTRecipeTypes.MIXER_RECIPES
import com.gregtechceu.gtceu.data.recipe.CustomTags

import com.lowdragmc.lowdraglib.side.fluid.FluidStack

import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.world.item.Items

import appeng.core.definitions.AEItems
import com.gtladd.gtladditions.GTLAdditions.id
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine
import com.gtladd.gtladditions.common.register.GTLAddItems
import com.gtladd.gtladditions.common.register.GTLAddItems.PRIMARY_SOC_WAFER
import com.gtladd.gtladditions.utils.Registries.getFluid
import com.gtladd.gtladditions.utils.Registries.getItemStack
import dev.latvian.mods.kubejs.KubeJS

import java.util.function.Consumer
import kotlin.math.pow

object Misc {
    @JvmStatic
    fun init(provider: Consumer<FinishedRecipe>) {
        DECAY_HASTENER_RECIPES.recipeBuilder(id("titanium50"))
            .inputFluids(Titanium.getFluid(144))
            .outputFluids(Titanium50.getFluid(144))
            .EUt(VA[MAX].toLong()).duration(10).save(provider)

        FORMING_PRESS_RECIPES.recipeBuilder(id("guide"))
            .inputItems(AEItems.TABLET.stack()).outputItems(GTLAddItems.GUIDE_BOOK.asStack())
            .EUt(VA[LV].toLong()).duration(20).save(provider)

        ASSEMBLY_LINE_RECIPES.recipeBuilder(id("extreme_conversion_simulate_card"))
            .inputItems(FAST_CONVERSION_SIMULATE_CARD.asStack())
            .inputItems(EMITTER_OpV, 4)
            .inputItems(SENSOR_OpV, 4)
            .inputItems(FIELD_GENERATOR_OpV, 2)
            .inputItems("kubejs:supracausal_processor".getItemStack(2))
            .inputItems(foil, Radox, 16)
            .inputItems(wireFine, DraconiumAwakened, 32)
            .inputFluids(RawRadox.getFluid(2000))
            .inputFluids(AstralTitanium.getFluid(FluidStorageKeys.PLASMA, 5600))
            .inputFluids(CelestialTungsten.getFluid(FluidStorageKeys.PLASMA, 5600))
            .outputItems(GTLAddItems.ULTIMATE_CONVERSATION_CARD.asStack())
            .EUt(VA[OpV].toLong()).duration(300)
            .stationResearch {
                it.researchStack(FAST_CONVERSION_SIMULATE_CARD.asStack())
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[OpV]).CWUt(1024)
            }
            .save(provider)

        WORLD_DATA_SCANNER_RECIPES.recipeBuilder(id("barnarda_data"))
            .circuitMeta(1)
            .inputItems(TOOL_DATA_STICK.asStack(8))
            .inputItems("kubejs:barnarda_log".getItemStack(64))
            .inputFluids(PCBCoolant.getFluid(800))
            .inputFluids(BarnardaAir.getFluid(64000))
            .outputItems(GTLAddItems.BARNARDA_DATA.asStack(8))
            .EUt(2048).duration(4000)
            .dimension(KubeJS.id("barnarda"))
            .save(provider)

        if (ConfigHolder.INSTANCE.content.enableSkyBlokeMode) {
            WORLD_DATA_SCANNER_RECIPES.recipeBuilder(id("barnarda_data_sky"))
                .notConsumable(WORLD_FRAGMENTS_BARNARDA.asStack(1))
                .inputItems(TOOL_DATA_STICK.asStack(8))
                .inputItems("kubejs:barnarda_log".getItemStack(64))
                .inputFluids(PCBCoolant.getFluid(800))
                .inputFluids(BarnardaAir.getFluid(64000))
                .outputItems(GTLAddItems.BARNARDA_DATA.asStack(8))
                .EUt(2048).duration(4000)
                .dimension(KubeJS.id("barnarda"))
                .save(provider)
        }

        WORLD_DATA_SCANNER_RECIPES.recipeBuilder(id("creative_data"))
            .inputItems(TOOL_DATA_STICK.asStack(64))
            .inputItems(block, Magmatter, 64)
            .inputItems(PRIMARY_SOC_WAFER, 64)
            .inputFluids(FluidIngredient.of(FluidStack.create("kubejs:gelid_cryotheum".getFluid, 800)))
            .inputFluids(MagnetohydrodynamicallyConstrainedStarMatter.getFluid(64000))
            .outputItems(GTLAddItems.CREATE_DATA.asStack())
            .EUt(536870912).duration(4000)
            .dimension(KubeJS.id("create"))
            .save(provider)

        ALLOY_SMELTER_RECIPES.recipeBuilder(id("magmatter_nugget"))
            .inputItems(ingot, Magmatter)
            .notConsumable(SHAPE_MOLD_NUGGET)
            .outputItems(nugget, Magmatter, 9)
            .duration(2000).EUt(VA[MAX].toLong())
            .save(provider)

        DIMENSIONALLY_TRANSCENDENT_MIXER_RECIPES.recipeBuilder(id("miracle_adhesive"))
            .chancedInput(SUPER_GLUE.asStack(), 100, 0)
            .inputItems("kubejs:hyper_stable_self_healing_adhesive".getItemStack(100))
            .inputFluids(Miracle.getFluid(100))
            .outputFluids(MiracleAdhesive.getFluid(1000))
            .EUt(4L * VA[MAX]).duration(300)
            .addCondition(GravityCondition(false))
            .save(provider)

        ASSEMBLY_LINE_RECIPES.recipeBuilder(id("harmonizing_core"))
            .inputItems(MultiBlockMachine.DRACONIC_COLLAPSE_CORE)
            .inputItems(CustomTags.MAX_CIRCUITS, 4)
            .inputItems(Items.ENCHANTED_GOLDEN_APPLE, 16)
            .inputItems(Items.ENCHANTED_GOLDEN_APPLE, 16)
            .inputItems(
                "kubejs:draconic_core".getItemStack(8),
                "kubejs:wyvern_core".getItemStack(8),
                "kubejs:awakened_core".getItemStack(8),
                "kubejs:chaotic_core".getItemStack(8),
                EMITTER_MAX.asStack(64),
                SENSOR_MAX.asStack(64)
            )
            .inputItems(dustSmall, Magmatter, 48)
            .inputItems(dustSmall, Magmatter, 48)
            .inputFluids(
                CosmicComputingMixture.getFluid(20000),
                RawRadox.getFluid(4800),
                DilutedXenoxene.getFluid(20000),
                PurifiedXenoxene.getFluid(20000)
            )
            .outputItems(GTLAddItems.HARMONIZING_CORE)
            .duration(6600).EUt(VA[MAX].toLong())
            .stationResearch {
                it.researchStack("kubejs:draconic_core".getItemStack())
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[MAX]).CWUt(8192)
            }
            .save(provider)

        GREENHOUSE_RECIPES.recipeBuilder(id("apple"))
            .notConsumable(Items.OAK_SAPLING)
            .circuitMeta(3)
            .inputFluids(Water.getFluid(1000))
            .outputItems(Items.OAK_LOG, 32)
            .outputItems(Items.APPLE, 4)
            .outputItems(Items.OAK_SAPLING, 3)
            .duration(900).EUt(VA[LV].toLong())
            .save(provider)

        GREENHOUSE_RECIPES.recipeBuilder(id("apple_fertiliser"))
            .notConsumable(Items.OAK_SAPLING)
            .inputItems(FERTILIZER.asStack(4))
            .circuitMeta(4)
            .inputFluids(Water.getFluid(1000))
            .outputItems(Items.OAK_LOG, 64)
            .outputItems(Items.APPLE, 8)
            .outputItems(Items.OAK_SAPLING, 6)
            .duration(300).EUt(VA[MV].toLong())
            .save(provider)

        INCUBATOR_RECIPES.recipeBuilder(id("cake"))
            .inputItems(dust, Wheat, 3)
            .inputItems(Items.SUGAR, 2)
            .inputItems(Items.EGG)
            .inputFluids(Milk.getFluid(3000))
            .outputItems(Items.CAKE)
            .duration(100).EUt(VA[MV].toLong())
            .save(provider)

        INCUBATOR_RECIPES.recipeBuilder(id("bee_spawn_egg"))
            .inputItems(Items.BONE, 4)
            .inputItems(Items.HONEYCOMB, 4)
            .inputItems(Items.HONEY_BOTTLE, 4)
            .inputFluids(Biomass.getFluid(1000))
            .inputFluids(Milk.getFluid(1000))
            .outputItems(Items.BEE_SPAWN_EGG)
            .duration(1200).EUt(VA[HV].toLong())
            .save(provider)

        INCUBATOR_RECIPES.recipeBuilder(id("honey_bottle"))
            .notConsumable(Items.BEE_SPAWN_EGG)
            .notConsumable(Items.BEEHIVE)
            .inputItems(Items.GLASS_BOTTLE)
            .inputFluids(Biomass.getFluid(1000))
            .outputItems(Items.HONEY_BOTTLE)
            .duration(600).EUt(VA[EV].toLong())
            .save(provider)

        MIXER_RECIPES.recipeBuilder(id("warped_ender_pearl"))
            .circuitMeta(9)
            .inputItems(Items.BONE_MEAL, 4)
            .inputItems(Items.BLAZE_POWDER, 4)
            .inputItems(dust, EnderPearl)
            .outputItems("kubejs:warped_ender_pearl".getItemStack())
            .duration(400).EUt(VA[MV].toLong())
            .save(provider)

        for (i in 1..<5) {
            MAGIC_MANUFACTURER_RECIPES.recipeBuilder(id(String.format("mana_%d", i - 1)))
                .notConsumable(FIELD_GENERATOR_MAX.asStack(4.0.pow(i - 1).toInt()))
                .circuitMeta(i)
                .outputFluids(Mana.getFluid(10000000L * 4.0.pow(i - 1).toInt()))
                .duration(20 * i).EUt(VA[MAX].toLong())
                .save(provider)
        }

        LARGE_CHEMICAL_RECIPES.recipeBuilder(id("isopropyl_alcohol_to_hydrogen_peroxide"))
            .inputFluids(IsopropylAlcohol.getFluid(2000))
            .inputFluids(Oxygen.getFluid(1000))
            .outputFluids(Acetone.getFluid(2000))
            .outputFluids(HydrogenPeroxide.getFluid(2000))
            .duration(600).EUt(VA[HV].toLong())
            .save(provider)
    }
}
