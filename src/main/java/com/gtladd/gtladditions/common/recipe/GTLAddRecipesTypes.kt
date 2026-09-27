package com.gtladd.gtladditions.common.recipe

import org.gtlcore.gtlcore.common.data.GTLRecipeTypes.*

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.block.ICoilType
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.common.data.GTRecipeTypes.*
import com.gregtechceu.gtceu.common.data.GTSoundEntries
import com.gregtechceu.gtceu.utils.FormattingUtil

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler
import com.lowdragmc.lowdraglib.utils.LocalizationUtils

import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike

import com.gtladd.gtladditions.GTLAdditions.id
import com.gtladd.gtladditions.api.recipe.MultiGTRecipeType.Companion.registry
import com.gtladd.gtladditions.common.data.RecipesModify
import com.gtladd.gtladditions.utils.Registries.getItem
import com.gtladd.gtladditions.utils.Registries.getItemStack

import java.util.function.Function

object GTLAddRecipesTypes {
    @JvmField
    val PHOTON_MATRIX_ETCH: GTRecipeType =
        register("photon_matrix_etch", MULTIBLOCK)
            .setEUIO(IO.IN).setMaxIOSize(3, 1, 1, 0)
            .setMaxTooltips(4)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC)

    @JvmField
    val TRANSMUTATION_BLOCK_CONVERSION: GTRecipeType =
        register("transmutation_block_conversion", MULTIBLOCK).setXEIVisible(false)
            .setEUIO(IO.IN).setMaxIOSize(2, 1, 0, 0)

    @JvmField
    val TECTONIC_FAULT_GENERATOR: GTRecipeType =
        register("tectonic_fault_generator", MULTIBLOCK)
            .setEUIO(IO.IN).setMaxIOSize(2, 1, 0, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC)

    @JvmField
    val BIOLOGICAL_SIMULATION: GTRecipeType =
        register("biological_simulation", MULTIBLOCK)
            .setEUIO(IO.IN).setMaxIOSize(3, 7, 2, 0)
            .addDataInfo { if (it.contains("infinity")) LocalizationUtils.format("gtceu.biological_simulation.infinity.1") else "" }
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC)

    @JvmField
    val VOIDFLUX_REACTION: GTRecipeType =
        register("voidflux_reaction", MULTIBLOCK)
            .setEUIO(IO.IN).setMaxIOSize(3, 0, 0, 1)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC)

    @JvmField
    val STELLAR_LGNITION: GTRecipeType =
        register("stellar_lgnition", MULTIBLOCK)
            .setEUIO(IO.IN).setMaxIOSize(1, 0, 1, 1)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARC_FURNACE, FillDirection.LEFT_TO_RIGHT)
            .addDataInfo {
                val temp = it.getInt("ebf_temp")
                LocalizationUtils.format("gtceu.recipe.temperature", FormattingUtil.formatNumbers(temp))
            }.addDataInfo(
                Function {
                    val temp = it.getInt("ebf_temp")
                    ICoilType.getMinRequiredType(temp)?.let { requiredCoil ->
                        return@Function LocalizationUtils.format(
                            "gtceu.recipe.coil.tier",
                            I18n.get(requiredCoil.material!!.unlocalizedName)
                        )
                    }
                    ""
                }
            ).setUiBuilder { recipe, widgetGroup ->
                val temp = recipe.data.getInt("ebf_temp")
                val items = ArrayList<MutableList<ItemStack>>()
                items.add(
                    GTCEuAPI.HEATING_COILS.entries.stream()
                        .filter { it.key.coilTemperature >= temp }
                        .map { ItemStack(it.value.get()) }.toList()
                )
                widgetGroup.addWidget(
                    SlotWidget(
                        CycleItemStackHandler(items),
                        0,
                        widgetGroup.size.width - 50,
                        widgetGroup.size.height - 40,
                        false,
                        false
                    )
                )
            }.setSound(GTSoundEntries.ARC)

    @JvmField
    val CHAOTIC_ALCHEMY: GTRecipeType =
        register("chaotic_alchemy", MULTIBLOCK)
            .setEUIO(IO.IN).setMaxIOSize(9, 0, 3, 1)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
            .setSlotOverlay(false, false, false, GuiTextures.FURNACE_OVERLAY_1)
            .setSlotOverlay(false, false, true, GuiTextures.FURNACE_OVERLAY_1)
            .setSlotOverlay(false, true, false, GuiTextures.FURNACE_OVERLAY_2)
            .setSlotOverlay(false, true, true, GuiTextures.FURNACE_OVERLAY_2)
            .setSlotOverlay(true, true, false, GuiTextures.FURNACE_OVERLAY_2)
            .setSlotOverlay(true, true, true, GuiTextures.FURNACE_OVERLAY_2)
            .addDataInfo {
                val temp = it.getInt("ebf_temp")
                LocalizationUtils.format("gtceu.recipe.temperature", FormattingUtil.formatNumbers(temp))
            }.addDataInfo {
                val temp = it.getInt("ebf_temp")
                ICoilType.getMinRequiredType(temp)?.let { requiredCoil ->
                    return@addDataInfo LocalizationUtils.format(
                        "gtceu.recipe.coil.tier",
                        I18n.get(requiredCoil.material!!.unlocalizedName)
                    )
                }
                ""
            }.setMaxTooltips(4).setUiBuilder { recipe, widgetGroup ->
                val temp = recipe.data.getInt("ebf_temp")
                val items = ArrayList<MutableList<ItemStack>>()
                items.add(
                    GTCEuAPI.HEATING_COILS.entries.stream()
                        .filter { it.key.coilTemperature >= temp }
                        .map { ItemStack(it.value.get() as ItemLike) }.toList()
                )
                widgetGroup.addWidget(
                    SlotWidget(
                        CycleItemStackHandler(items),
                        0,
                        widgetGroup.size.width - 25,
                        widgetGroup.size.height - 40,
                        false,
                        false
                    )
                )
            }.setSound(GTSoundEntries.ARC)

    @JvmField
    val ANTIENTROPY_CONDENSATION: GTRecipeType =
        register("antientropy_condensation", MULTIBLOCK)
            .setEUIO(IO.IN).setMaxIOSize(2, 2, 2, 1)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC)

    @JvmField
    val MOLECULAR_DECONSTRUCTION: GTRecipeType =
        register("molecular_deconstruction", MULTIBLOCK)
            .setEUIO(IO.IN).setMaxIOSize(1, 0, 0, 1)
            .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC)

    @JvmField
    val CHAOS_WEAVE: GTRecipeType =
        register("chaos_weave", MULTIBLOCK)
            .setEUIO(IO.IN).setMaxIOSize(1, 1, 0, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC)

    @JvmField
    val FRACTAL_RECONSTRUCTION: GTRecipeType =
        register("fractal_reconstruction", MULTIBLOCK)
            .setEUIO(IO.IN).setMaxIOSize(3, 1, 3, 1)
            .addDataInfo {
                val id = it.getString("accelerant").getItem.`kjs$getIdLocation`()
                var string = I18n.get(id.toLanguageKey("item"))
                if (string.equals(id.toLanguageKey("item"))) string = I18n.get(id.toLanguageKey("block"))
                return@addDataInfo LocalizationUtils.format("gtceu.recipe.accelerant", string)
            }.setMaxTooltips(4).setUiBuilder { recipe, widgetGroup ->
                widgetGroup.addWidget(
                    SlotWidget(
                        ItemStackTransfer(recipe.data.getString("accelerant").getItemStack(64)),
                        0,
                        widgetGroup.size.width - 126,
                        widgetGroup.size.height - 107,
                        false,
                        false
                    )
                        .setBackgroundTexture(ResourceTexture("gtladditions:textures/gui/slot.png"))
                        .setHoverTooltips(Component.translatable("gtceu.recipe.accelerant.hover"))
                )
            }.setSound(GTSoundEntries.ARC)

    @JvmField
    val EvolutionOfPrimordial: GTRecipeType =
        register("evolution_of_primordial", MULTIBLOCK)
            .setEUIO(IO.IN).setMaxIOSize(1, 1, 2, 16)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL)

    @JvmField
    val TIME_SPACE_DISTORTER: GTRecipeType =
        register("time_space_distorter", MULTIBLOCK).setXEIVisible(false)
            .setEUIO(IO.IN).setMaxIOSize(18, 9, 9, 9)

    @JvmField
    val RECURSIVE_REVERSE_FORGE: GTRecipeType =
        register("recursive_reverse_forge", MULTIBLOCK).setXEIVisible(false)
            .setEUIO(IO.IN).setMaxIOSize(3, 2, 9, 2)

    @JvmField
    val ONTOKINETIC: GTRecipeType =
        register("ontokinetic", MULTIBLOCK)
            .setEUIO(IO.IN).setMaxIOSize(15, 1, 4, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ARC)

    val InfernoCleftSmeltingVaultType =
        registry(id("inferno_cleft_smelting_vault_type"), MULTIBLOCK, PYROLYSE_RECIPES, CRACKING_RECIPES)

    val SkeletonShiftRiftEngineType =
        registry(id("skeleton_shift_rift_engine_type"), MULTIBLOCK, FUSION_RECIPES, SUPER_PARTICLE_COLLIDER_RECIPES)

    val LucidEtchdreamerType =
        registry(id("lucid_etchdreamer_type"), MULTIBLOCK, PHOTON_MATRIX_ETCH, LASER_ENGRAVER_RECIPES)

    val TimeSpaceDistorterType =
        registry(id("time_space_distorter_type"), MULTIBLOCK, QFT_RECIPES, DISTORT_RECIPES)

    val RecursiveReverseForgeType =
        registry(id("recursive_reverse_forge_type"), MULTIBLOCK, DIMENSIONALLY_TRANSCENDENT_PLASMA_FORGE_RECIPES, STELLAR_FORGE_RECIPES)

    val SuperFactoryMk1Type_1 =
        registry(id("super_factory_mk1_1_type"), MULTIBLOCK, EXTRUDER_RECIPES, CUTTER_RECIPES, MIXER_RECIPES, FORMING_PRESS_RECIPES)

    val SuperFactoryMk2Type_1 =
        registry(id("super_factory_mk2_1_type"), MULTIBLOCK, CENTRIFUGE_RECIPES, THERMAL_CENTRIFUGE_RECIPES)

    val SuperFactoryMk2Type_2 =
        registry(id("super_factory_mk2_2_type"), MULTIBLOCK, ELECTROLYZER_RECIPES, ELECTROMAGNETIC_SEPARATOR_RECIPES)

    val SuperFactoryMk2Type_3 =
        registry(id("super_factory_mk2_3_type"), MULTIBLOCK, SIFTER_RECIPES, DEHYDRATOR_RECIPES)

    val SuperFactoryMk3Type_1 =
        registry(id("super_factory_mk3_1_type"), MULTIBLOCK, FLUID_SOLIDFICATION_RECIPES, EXTRACTOR_RECIPES)

    val SuperFactoryMk4Type_1 =
        registry(id("super_factory_mk4_1_type"), MULTIBLOCK, PRECISION_ASSEMBLER_RECIPES, CIRCUIT_ASSEMBLER_RECIPES)

    val SuperFactoryMk4Type_2 =
        registry(id("super_factory_mk4_2_type"), MULTIBLOCK, ARC_FURNACE_RECIPES, CANNER_RECIPES, LIGHTNING_PROCESSOR_RECIPES)

    val BiosphereIIIType =
        registry(id("biosphere_iii_type"), MULTIBLOCK, GREENHOUSE_RECIPES, FISHING_GROUND_RECIPES)

    @JvmStatic
    fun init() {
        RecipesModify.init()
    }
}
