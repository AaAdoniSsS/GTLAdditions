package com.gtladd.gtladditions.data.recipes

import org.gtlcore.gtlcore.api.data.tag.GTLTagPrefix.nanoswarm
import org.gtlcore.gtlcore.common.data.GTLBlocks.*
import org.gtlcore.gtlcore.common.data.GTLItems.*
import org.gtlcore.gtlcore.common.data.GTLItems.REALLY_ULTIMATE_BATTERY
import org.gtlcore.gtlcore.common.data.GTLMachines
import org.gtlcore.gtlcore.common.data.GTLMachines.WIRELESS_DATA_HATCH_RECEIVER
import org.gtlcore.gtlcore.common.data.GTLMachines.WIRELESS_DATA_HATCH_TRANSMITTER
import org.gtlcore.gtlcore.common.data.GTLMaterials.*
import org.gtlcore.gtlcore.common.data.GTLRecipeTypes.SUPRACHRONAL_ASSEMBLY_LINE_RECIPES
import org.gtlcore.gtlcore.common.data.machines.AdvancedMultiBlockMachineA.*
import org.gtlcore.gtlcore.common.data.machines.AdvancedMultiBlockMachineB.*
import org.gtlcore.gtlcore.common.data.machines.GeneratorMachine.DYSON_SPHERE
import org.gtlcore.gtlcore.common.data.machines.MultiBlockMachineA.*
import org.gtlcore.gtlcore.common.data.machines.MultiBlockMachineB.WOOD_DISTILLATION

import com.gregtechceu.gtceu.api.GTValues.*
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper
import com.gregtechceu.gtceu.api.data.tag.TagPrefix.*
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys.PLASMA
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient
import com.gregtechceu.gtceu.common.data.GTItems.*
import com.gregtechceu.gtceu.common.data.GTMachines
import com.gregtechceu.gtceu.common.data.GTMaterials.*
import com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLY_LINE_RECIPES
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines.ADVANCED_DATA_ACCESS_HATCH
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines.DATA_BANK
import com.gregtechceu.gtceu.data.recipe.CustomTags
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper

import com.lowdragmc.lowdraglib.side.fluid.FluidStack

import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.world.level.block.Blocks

import com.gtladd.gtladditions.GTLAdditions.id
import com.gtladd.gtladditions.common.machine.GTLAddMachines
import com.gtladd.gtladditions.common.machine.GTLAddMachines.CLOUD_COMPUTATION_HATCH_RECEIVER
import com.gtladd.gtladditions.common.machine.GTLAddMachines.CLOUD_COMPUTATION_HATCH_TRANSMITTER
import com.gtladd.gtladditions.common.machine.GTLAddMachines.CLOUD_COMPUTATION_MONITOR
import com.gtladd.gtladditions.common.machine.GTLAddMachines.CLOUD_DATA_HATCH
import com.gtladd.gtladditions.common.machine.GTLAddMachines.CLOUD_DATA_MACHINE
import com.gtladd.gtladditions.common.machine.GTLAddMachines.VIENTIANE_TRANSCEIPTION_NODE
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.BIOSPHERE_III
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.BIOSPHERE_III_MODULE
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.CATALYTIC_CASCADE_ARRAY
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_1
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_2
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_3
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_4
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_5
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.FRACTAL_MANIPULATOR
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.FUXI_BAGUA_HEAVEN_FORGING_FURNACE
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.HYPERDIMENSIONAL_ENERGY_CONCETRATOR
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.MAGNETORHEOLOGICAL_CONVERGENCE_CORE
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.NEBULA_REAPER
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.PLANETARY_IONISATION_CONVERGENCE_TOWER
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.PRIMORDIAL_EVOLUTION_NEXUS
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.RECURSIVE_REVERSE_FORGE
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.REVERSE_TIME_BOOSTING_ENGINE
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.SKELETON_SHIFT_RIFT_ENGINE
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.SPACE_ELEVATOR_MKII
import com.gtladd.gtladditions.common.register.GTLAddItems
import com.gtladd.gtladditions.common.register.GTLAddItems.OUTSTANDING_SOC_WAFER
import com.gtladd.gtladditions.utils.Registries.getFluid
import com.gtladd.gtladditions.utils.Registries.getItemStack
import com.hepdd.gtmthings.data.WirelessMachines
import com.hepdd.gtmthings.data.WirelessMachines.WIRELESS_COMPUTATION_HATCH_RECEIVER
import com.hepdd.gtmthings.data.WirelessMachines.WIRELESS_COMPUTATION_HATCH_TRANSMITTER
import com.hepdd.gtmthings.data.WirelessMachines.WIRELESS_ENERGY_MONITOR

import java.util.function.Consumer

object NewMachineRecipe2 {
    @JvmStatic
    fun init(provider: Consumer<FinishedRecipe>) {
        VanillaRecipeHelper.addShapedRecipe(
            provider, true, id("vientiane_transcription_node"),
            VIENTIANE_TRANSCEIPTION_NODE.asStack(),
            "ADA", "CBC", "EFE",
            'A', CustomTags.UHV_CIRCUITS,
            'B', "gtceu:advanced_fluid_detector_cover".getItemStack(),
            'C', ChemicalHelper.get(wireFine, Enderite),
            'D', GTMachines.HULL[14].asStack(),
            'E', EMITTER_UV.asStack(),
            'F', GTLMachines.HEAT_SENSOR.asStack()
        )
        SUPRACHRONAL_ASSEMBLY_LINE_RECIPES.recipeBuilder(id("recursive_reverse_forge"))
            .inputItems(
                "kubejs:supracausal_mainframe".getItemStack(64),
                "kubejs:suprachronal_max".getItemStack(64)
            )
            .inputItems(FUXI_BAGUA_HEAVEN_FORGING_FURNACE, 16)
            .inputItems(SKELETON_SHIFT_RIFT_ENGINE, 32)
            .inputItems(CREATE_COMPUTATION, 32)
            .inputItems(DIMENSIONALLY_TRANSCENDENT_MIXER, 64)
            .inputItems(block, Magmatter, 64)
            .inputItems(
                CREATE_CASING.asStack(64),
                "kubejs:chaotic_energy_core".getItemStack(8),
                GTLAddItems.PRIMARY_SOC.asStack(64),
                GTLAddItems.PRIMARY_SOC.asStack(32),
                "kubejs:hyperdimensional_drone".getItemStack(48),
                "kubejs:create_aggregatione_core".getItemStack(56)
            )
            .inputItems(plateDouble, MagnetohydrodynamicallyConstrainedStarMatter, 64)
            .inputItems(plateDouble, MagnetohydrodynamicallyConstrainedStarMatter, 64)
            .inputItems("avaritia:singularity".getItemStack(64))
            .inputFluids(
                Miracle.getFluid(100000),
                RawStarMatter.getFluid(PLASMA, 16384000),
                Shirabon.getFluid(144000),
                CosmicElement.getFluid(14400000)
            )
            .outputItems(RECURSIVE_REVERSE_FORGE)
            .EUt(8192L * VA[MAX])
            .duration(112000)
            .stationResearch {
                it.researchStack(FUXI_BAGUA_HEAVEN_FORGING_FURNACE.asStack())
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[MAX]).CWUt(65536)
            }
            .save(provider)
        SUPRACHRONAL_ASSEMBLY_LINE_RECIPES.recipeBuilder(id("reverse_time_boosting_engine"))
            .inputItems(
                "kubejs:supracausal_mainframe".getItemStack(4),
                "kubejs:suprachronal_max".getItemStack(4)
            )
            .inputItems(SKELETON_SHIFT_RIFT_ENGINE, 64)
            .inputItems(SKELETON_SHIFT_RIFT_ENGINE, 48)
            .inputItems("kubejs:dimension_creation_casing".getItemStack(4))
            .inputItems(GTLMachines.NEUTRON_ACCELERATOR[14], 16)
            .inputItems(
                "kubejs:spacetime_catalyst".getItemStack(64),
                FIELD_GENERATOR_MAX.asStack(32),
                CREATE_CASING.asStack(56)
            )
            .inputItems(wireGtHex, SpaceTime, 32)
            .inputItems(plateDouble, Shirabon, 48)
            .inputItems(foil, Cosmic, 64)
            .inputFluids(
                TemporalFluid.getFluid(48000),
                SpatialFluid.getFluid(48000)
            )
            .outputItems(REVERSE_TIME_BOOSTING_ENGINE)
            .EUt(1024L * VA[MAX])
            .duration(23000)
            .stationResearch {
                it.researchStack(SKELETON_SHIFT_RIFT_ENGINE.asStack())
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[MAX]).CWUt(16384)
            }
            .save(provider)
        SUPRACHRONAL_ASSEMBLY_LINE_RECIPES.recipeBuilder(id("magnetorheological_convergence_core"))
            .inputItems(
                "kubejs:supracausal_mainframe".getItemStack(4),
                "kubejs:suprachronal_max".getItemStack(4)
            )
            .inputItems(block, Magmatter, 64)
            .inputItems(dust, Magmatter, 48)
            .inputItems("kubejs:recursively_folded_negative_space".getItemStack(48))
            .inputItems(GTLAddItems.SPACETIME_LENS, 64)
            .inputItems(GTLAddItems.PRIMARY_SOC, 24)
            .inputItems(
                FIELD_GENERATOR_MAX.asStack(32),
                CREATE_CASING.asStack(56)
            )
            .inputItems(wireGtHex, SpaceTime, 32)
            .inputItems(plateDouble, Shirabon, 48)
            .inputItems(foil, Cosmic, 64)
            .inputFluids(
                Eternity.getFluid(48000),
                Chaos.getFluid(48000)
            )
            .outputItems(MAGNETORHEOLOGICAL_CONVERGENCE_CORE)
            .EUt(1024L * VA[MAX])
            .duration(23000)
            .stationResearch {
                it.researchStack(ChemicalHelper.get(block, Magmatter, 64))
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[MAX]).CWUt(16384)
            }
            .save(provider)
        SUPRACHRONAL_ASSEMBLY_LINE_RECIPES.recipeBuilder(id("catalytic_cascade_array"))
            .inputItems(
                "kubejs:supracausal_mainframe".getItemStack(4),
                "kubejs:suprachronal_max".getItemStack(4)
            )
            .inputItems(DIMENSIONALLY_TRANSCENDENT_MIXER, 64)
            .inputItems(
                "kubejs:create_aggregatione_core".getItemStack(48),
                "avaritia:singularity".getItemStack(64)
            )
            .inputItems(plateDouble, MagnetohydrodynamicallyConstrainedStarMatter, 48)
            .inputItems(
                "kubejs:nuclear_star".getItemStack(48),
                FIELD_GENERATOR_MAX.asStack(32),
                CREATE_CASING.asStack(56)
            )
            .inputItems(wireGtHex, SpaceTime, 32)
            .inputItems(plateDouble, Shirabon, 48)
            .inputItems(foil, Cosmic, 64)
            .inputFluids(
                DimensionallyTranscendentProsaicCatalyst.getFluid(32000),
                DimensionallyTranscendentResplendentCatalyst.getFluid(32000),
                DimensionallyTranscendentExoticCatalyst.getFluid(32000),
                DimensionallyTranscendentStellarCatalyst.getFluid(32000)
            )
            .outputItems(CATALYTIC_CASCADE_ARRAY)
            .EUt(1024L * VA[MAX])
            .duration(23000)
            .stationResearch {
                it.researchStack("kubejs:nuclear_star".getItemStack())
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[MAX]).CWUt(16384)
            }
            .save(provider)
        SUPRACHRONAL_ASSEMBLY_LINE_RECIPES.recipeBuilder(id("hyperdimensional_energy_concentrator"))
            .inputItems(
                "kubejs:supracausal_mainframe".getItemStack(4),
                "kubejs:suprachronal_max".getItemStack(4),
                "kubejs:create_ultimate_battery".getItemStack(48),
                "kubejs:chaotic_energy_core".getItemStack(56)
            )
            .inputItems(CREATE_COMPUTATION, 64)
            .inputItems("kubejs:hyperdimensional_drone".getItemStack(8))
            .inputItems(GTLAddMachines.WIRELESS_LASER_INPUT_HATCH_67108864A[14].asStack(64))
            .inputItems(WirelessMachines.WIRELESS_ENERGY_MONITOR.asStack(64))
            .inputItems(CREATE_CASING.asStack(56))
            .inputItems(wireGtHex, SpaceTime, 32)
            .inputItems(plateDouble, Shirabon, 48)
            .inputItems(foil, Cosmic, 64)
            .inputFluids(TranscendentMetal.getFluid(96000))
            .inputFluids(FluidIngredient.of(FluidStack.create("kubejs:gelid_cryotheum".getFluid, 16384000)))
            .outputItems(HYPERDIMENSIONAL_ENERGY_CONCETRATOR)
            .EUt(1024L * VA[MAX])
            .duration(23000)
            .stationResearch {
                it.researchStack(CREATE_COMPUTATION.asStack())
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[MAX]).CWUt(16384)
            }
            .save(provider)
        SUPRACHRONAL_ASSEMBLY_LINE_RECIPES.recipeBuilder(id("fractal_manipulator"))
            .inputItems(
                "kubejs:supracausal_mainframe".getItemStack(4),
                "kubejs:suprachronal_max".getItemStack(4)
            )
            .inputItems(QFT, 64)
            .inputItems(CHEMICAL_DISTORT, 64)
            .inputItems(
                "kubejs:eternity_catalyst".getItemStack(32),
                "avaritia:infinity_catalyst".getItemStack(32),
                "kubejs:chain_command_block_core".getItemStack(56),
                "kubejs:repeating_command_block_core".getItemStack(56)
            )
            .inputItems(CREATE_CASING.asStack(56))
            .inputItems(wireGtHex, SpaceTime, 32)
            .inputItems(plateDouble, Shirabon, 48)
            .inputItems(foil, Cosmic, 64)
            .inputFluids(
                MagnetohydrodynamicallyConstrainedStarMatter.getFluid(48000),
                PrimordialMatter.getFluid(144000)
            )
            .outputItems(FRACTAL_MANIPULATOR)
            .EUt(1024L * VA[MAX])
            .duration(23000)
            .stationResearch {
                it.researchStack(Blocks.COMMAND_BLOCK.asItem().defaultInstance)
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[MAX]).CWUt(16384)
            }
            .save(provider)

        ASSEMBLY_LINE_RECIPES.recipeBuilder(id("planetary_ionisation_convergence_tower"))
            .inputItems(
                "minecraft:lightning_rod".getItemStack(64)
            )
            .inputItems(
                GTLMachines.LIGHTNING_ROD[4].asStack(64),
                GTLMachines.LIGHTNING_ROD[5].asStack(64),
                GTLMachines.LIGHTNING_ROD[6].asStack(64)
            )
            .inputItems(CustomTags.UEV_CIRCUITS, 16)
            .inputItems(SPS_CASING, 16)
            .inputItems("kubejs:space_drone_mk3".getItemStack(16))
            .inputItems(nanoswarm, Neutronium, 64)
            .inputItems(
                "gtceu:uhv_ultimate_battery".getItemStack(56),
                "gtceu:uhv_ultimate_battery".getItemStack(56)
            )
            .inputItems("kubejs:nm_chip".getItemStack(48))
            .inputItems("gtceu:highly_advanced_soc".getItemStack(48))
            .inputItems(FIELD_GENERATOR_UEV, 64)
            .inputItems(plateDouble, Quantum, 32)
            .inputItems(plateDouble, Dalisenite, 32)
            .inputItems("kubejs:special_ceramics".getItemStack(64))
            .inputFluids(Promethium.getFluid(8192))
            .inputFluids(Rhenium.getFluid(8192))
            .inputFluids(MutatedLivingSolder.getFluid(16384))
            .inputFluids(HastelloyX78.getFluid(9216))
            .outputItems(PLANETARY_IONISATION_CONVERGENCE_TOWER)
            .EUt(VA[UIV].toLong()).duration(6000)
            .stationResearch {
                it.researchStack(GTLMachines.LIGHTNING_ROD[6].asStack())
                    .dataStack(TOOL_DATA_MODULE.asStack()).dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[UIV]).CWUt(256)
            }
            .save(provider)

        SUPRACHRONAL_ASSEMBLY_LINE_RECIPES.recipeBuilder(id("floating_light_deep_space_industrial_vessel"))
            .inputItems(DYSON_SPHERE, 16)
            .inputItems(SPACE_ELEVATOR, 16)
            .inputItems(SPACE_PROBE_SURFACE_RECEPTION, 16)
            .inputItems(ASSEMBLER_MODULE, 32)
            .inputItems(RESOURCE_COLLECTION, 32)
            .inputItems(POWER_MODULE_5, 48)
            .inputItems(CustomTags.UXV_CIRCUITS, 64)
            .inputItems(EXTREMELY_ULTIMATE_BATTERY, 64)
            .inputItems(
                "kubejs:space_drone_mk5".getItemStack(56),
                "kubejs:space_probe_mk1".getItemStack(56),
                "kubejs:precision_circuit_assembly_robot_mk4".getItemStack(56),
                "kubejs:proto_matter".getItemStack(56)
            )
            .inputItems(COVER_SOLAR_PANEL_EV, 64)
            .inputItems(COVER_SOLAR_PANEL_EV, 64)
            .inputItems(
                "kubejs:wyvern_core".getItemStack(64),
                "kubejs:wyvern_core".getItemStack(64)
            )
            .inputFluids(SuperMutatedLivingSolder.getFluid(480000))
            .inputFluids(UUMatter.getFluid(560000))
            .inputFluids(Ytterbium178.getFluid(8192))
            .inputFluids(CosmicComputingMixture.getFluid(320000))
            .outputItems(FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL)
            .EUt(VA[UXV].toLong()).duration(7200)
            .stationResearch {
                it.researchStack(DYSON_SPHERE.asStack())
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[UXV]).CWUt(2048)
            }
            .save(provider)

        SUPRACHRONAL_ASSEMBLY_LINE_RECIPES.recipeBuilder(id("floating_light_deep_space_industrial_vessel_module_5"))
            .inputItems("avaritia:extreme_crafting_table".getItemStack(64))
            .inputItems(FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_1, 4)
            .inputItems(FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_2, 4)
            .inputItems(FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_3, 4)
            .inputItems(FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_4, 4)
            .inputItems(CustomTags.MAX_CIRCUITS, 64)
            .inputItems(MEGA_ULTIMATE_BATTERY)
            .inputItems("kubejs:hypercube".getItemStack(64))
            .inputItems(plateDouble, DraconiumAwakened, 32)
            .inputItems(plateDouble, CosmicNeutronium, 32)
            .inputItems(plateDouble, Crystalmatrix, 32)
            .inputItems(plateDouble, Starmetal, 32)
            .inputFluids(SpatialFluid.getFluid(3200))
            .inputFluids(TemporalFluid.getFluid(3200))
            .inputFluids(TranscendentMetal.getFluid(3200))
            .outputItems(FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_5)
            .EUt(4L * VA[MAX].toLong()).duration(7200)
            .stationResearch {
                it.researchStack("avaritia:extreme_crafting_table".getItemStack())
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[MAX]).CWUt(4096)
            }
            .save(provider)

        ASSEMBLY_LINE_RECIPES.recipeBuilder(id("primordial_evolution_nexus"))
            .inputItems(PETROCHEMICAL_PLANT, 16)
            .inputItems(WOOD_DISTILLATION, 16)
            .inputItems(CustomTags.UIV_CIRCUITS, 8)
            .inputItems(REALLY_ULTIMATE_BATTERY)
            .inputItems(FIELD_GENERATOR_UEV, 16)
            .inputItems("kubejs:x_ray_laser".getItemStack(4))
            .inputItems(nanoswarm, Iridium, 64)
            .inputItems(nanoswarm, Osmium, 64)
            .inputItems(plateDouble, Enderite, 8)
            .inputItems(plateDouble, HastelloyX78, 8)
            .inputItems("kubejs:hui_circuit_5".getItemStack(32))
            .inputItems("kubejs:special_ceramics".getItemStack(32))
            .inputFluids(Zylon.getFluid(5760))
            .inputFluids(DegenerateRhenium.getFluid(PLASMA, 20000))
            .inputFluids(Dubnium.getFluid(3200))
            .inputFluids(Seaborgium.getFluid(3200))
            .outputItems(PRIMORDIAL_EVOLUTION_NEXUS)
            .EUt(VA[UEV].toLong()).duration(7200)
            .stationResearch {
                it.researchStack(WOOD_DISTILLATION.asStack())
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[UIV]).CWUt(512)
            }
            .save(provider)

        ASSEMBLY_LINE_RECIPES.recipeBuilder(id("biosphere_iii"))
            .inputItems(LARGE_GREENHOUSE, 16)
            .inputItems(COVER_SOLAR_PANEL_HV, 4)
            .inputItems(FIELD_GENERATOR_UEV, 8)
            .inputItems(CustomTags.UIV_CIRCUITS, 16)
            .inputItems("kubejs:biological_cells".getItemStack(64))
            .inputItems("kubejs:biological_cells".getItemStack(64))
            .inputItems("kubejs:essence_seed".getItemStack(64))
            .inputItems("kubejs:essence_seed".getItemStack(64))
            .inputItems(plateDouble, Seaborgium, 16)
            .inputItems(plateDouble, Dubnium, 16)
            .inputItems(rodLong, Neptunium, 8)
            .inputItems(rodLong, Neptunium, 8)
            .inputItems(wireFine, RutheniumTriniumAmericiumNeutronate, 48)
            .inputItems(wireFine, RutheniumTriniumAmericiumNeutronate, 48)
            .inputFluids(RawRadox.getFluid(1000))
            .inputFluids(AbsoluteEthanol.getFluid(10000))
            .inputFluids(MutatedLivingSolder.getFluid(5760))
            .inputFluids(Polyetheretherketone.getFluid(7776))
            .outputItems(BIOSPHERE_III)
            .EUt(VA[UHV].toLong()).duration(7200)
            .stationResearch {
                it.researchStack(LARGE_GREENHOUSE.asStack())
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[UV]).CWUt(512)
            }
            .save(provider)

        ASSEMBLY_LINE_RECIPES.recipeBuilder(id("garden_of_hermes"))
            .inputItems(LARGE_GREENHOUSE, 8)
            .inputItems(COVER_SOLAR_PANEL_HV, 2)
            .inputItems(FIELD_GENERATOR_UEV, 4)
            .inputItems(CustomTags.UIV_CIRCUITS, 8)
            .inputItems(OUTSTANDING_SOC_WAFER, 64)
            .inputItems(OUTSTANDING_SOC_WAFER, 64)
            .inputItems(
                "kubejs:draconium_dust".getItemStack(64),
                "kubejs:draconium_dust".getItemStack(64)
            )
            .inputItems(plateDouble, Quantanium, 16)
            .inputItems(plateDouble, Highurabilityompoundteel, 16)
            .inputItems(wireFine, TitanSteel, 48)
            .inputItems(wireFine, TitanSteel, 48)
            .inputFluids(SuperMutatedLivingSolder.getFluid(20000))
            .inputFluids(CaliforniumCyclopentadienide.getFluid(100000))
            .inputFluids(Zylon.getFluid(14400))
            .inputFluids(MoltenCalciumSalts.getFluid(12000))
            .outputItems(BIOSPHERE_III_MODULE)
            .EUt(VA[UEV].toLong()).duration(3600)
            .stationResearch {
                it.researchStack(BIOSPHERE_III.asStack())
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[UEV]).CWUt(512)
            }
            .save(provider)

        ASSEMBLY_LINE_RECIPES.recipeBuilder(id("mega_space_elevator"))
            .inputItems(SPACE_ELEVATOR, 64)
            .inputItems(SPACE_ELEVATOR, 64)
            .inputItems(POWER_MODULE_5, 64)
            .inputItems("kubejs:module_connector".getItemStack(48))
            .inputItems(CustomTags.OpV_CIRCUITS, 64)
            .inputItems(INSANELY_ULTIMATE_BATTERY, 8)
            .inputItems(FIELD_GENERATOR_UXV, 56)
            .inputItems(wireGtHex, DraconiumAwakened, 32)
            .inputItems(
                "kubejs:nuclear_clock".getItemStack(32),
                "kubejs:charged_lepton_trap_crystal".getItemStack(32),
                "kubejs:eigenfolded_kerr_manifold".getItemStack(64),
                "kubejs:eigenfolded_kerr_manifold".getItemStack(64)
            )
            .inputItems(nanoswarm, BlackDwarfMatter, 64)
            .inputItems(nanoswarm, WhiteDwarfMatter, 64)
            .inputItems(
                "kubejs:wyvern_core".getItemStack(64),
                "kubejs:wyvern_core".getItemStack(64)
            )
            .inputFluids(Hikarium.getFluid(1440000))
            .inputFluids(SuperMutatedLivingSolder.getFluid(2880000))
            .inputFluids(Radox.getFluid(560000))
            .inputFluids(GammaRaysPhotoresist.getFluid(1000000))
            .outputItems(SPACE_ELEVATOR_MKII)
            .EUt(VA[OpV].toLong()).duration(7200)
            .stationResearch {
                it.researchStack(NEBULA_REAPER.asStack())
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[UXV]).CWUt(1024)
            }
            .save(provider)

        ASSEMBLY_LINE_RECIPES.recipeBuilder(id("cloud_computation_monitor"))
            .inputItems(GTMachines.HULL[UIV])
            .inputItems(
                "gtceu:computer_monitor_cover".getItemStack(16),
                "kubejs:warped_ender_pearl".getItemStack(64)
            )
            .inputItems(ULTIMATE_TERMINAL)
            .inputItems(CustomTags.UIV_CIRCUITS, 8)
            .inputItems(foil, Neutronium, 32)
            .inputItems(foil, Neutronium, 32)
            .inputFluids(MutatedLivingSolder.getFluid(5760))
            .inputFluids(FluidIngredient.of(FluidStack.create("kubejs:gelid_cryotheum".getFluid, 2880)))
            .outputItems(CLOUD_COMPUTATION_MONITOR)
            .EUt(VA[UEV].toLong()).duration(600)
            .stationResearch {
                it.researchStack(WIRELESS_ENERGY_MONITOR.asStack())
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[UV]).CWUt(256)
            }
            .save(provider)

        ASSEMBLY_LINE_RECIPES.recipeBuilder(id("cloud_computation_transmitter_hatch"))
            .inputItems(GTMachines.HULL[UIV])
            .inputItems(WIRELESS_COMPUTATION_HATCH_TRANSMITTER)
            .inputItems(CustomTags.UEV_CIRCUITS, 2)
            .inputItems(SENSOR_UEV, 4)
            .inputItems(HIGHLY_INSULATING_FOIL, 8)
            .inputFluids(MutatedLivingSolder.getFluid(5760))
            .inputFluids(Zylon.getFluid(1008))
            .inputFluids(CaliforniumCyclopentadienide.getFluid(4000))
            .outputItems(CLOUD_COMPUTATION_HATCH_TRANSMITTER)
            .EUt(VA[UHV].toLong()).duration(300)
            .stationResearch {
                it.researchStack(WIRELESS_COMPUTATION_HATCH_TRANSMITTER.asStack())
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[UV]).CWUt(256)
            }
            .save(provider)

        ASSEMBLY_LINE_RECIPES.recipeBuilder(id("cloud_computation_receiver_hatch"))
            .inputItems(GTMachines.HULL[UIV])
            .inputItems(WIRELESS_COMPUTATION_HATCH_RECEIVER)
            .inputItems(CustomTags.UEV_CIRCUITS, 2)
            .inputItems(EMITTER_UEV, 4)
            .inputItems(HIGHLY_INSULATING_FOIL, 8)
            .inputFluids(MutatedLivingSolder.getFluid(5760))
            .inputFluids(Zylon.getFluid(1008))
            .inputFluids(CaliforniumCyclopentadienide.getFluid(4000))
            .outputItems(CLOUD_COMPUTATION_HATCH_RECEIVER)
            .EUt(VA[UHV].toLong()).duration(300)
            .stationResearch {
                it.researchStack(WIRELESS_COMPUTATION_HATCH_RECEIVER.asStack())
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[UV]).CWUt(256)
            }
            .save(provider)

        ASSEMBLY_LINE_RECIPES.recipeBuilder(id("cloud_data_hatch"))
            .inputItems(GTMachines.HULL[UIV])
            .inputItems(WIRELESS_DATA_HATCH_RECEIVER)
            .inputItems(CustomTags.UEV_CIRCUITS, 2)
            .inputItems(EMITTER_UEV, 4)
            .inputItems(HIGHLY_INSULATING_FOIL, 8)
            .inputFluids(MutatedLivingSolder.getFluid(5760))
            .inputFluids(Zylon.getFluid(1008))
            .inputFluids(CaliforniumCyclopentadienide.getFluid(4000))
            .outputItems(CLOUD_DATA_HATCH)
            .EUt(VA[UHV].toLong()).duration(300)
            .stationResearch {
                it.researchStack(WIRELESS_DATA_HATCH_RECEIVER.asStack())
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[UV]).CWUt(256)
            }
            .save(provider)

        ASSEMBLY_LINE_RECIPES.recipeBuilder(id("cloud_data_machine"))
            .inputItems(GTMachines.HULL[UIV])
            .inputItems(DATA_BANK)
            .inputItems(WIRELESS_DATA_HATCH_TRANSMITTER)
            .inputItems(ADVANCED_DATA_ACCESS_HATCH, 5)
            .inputItems(TOOL_DATA_MODULE)
            .inputItems(CustomTags.UEV_CIRCUITS, 4)
            .inputItems(HIGHLY_INSULATING_FOIL, 64)
            .inputItems(foil, Ruridit, 64)
            .inputItems(wireGtDouble, RutheniumTriniumAmericiumNeutronate, 64)
            .inputFluids(MutatedLivingSolder.getFluid(5760))
            .inputFluids(Zylon.getFluid(4032))
            .inputFluids(FluidIngredient.of(FluidStack.create("kubejs:gelid_cryotheum".getFluid, 2880)))
            .outputItems(CLOUD_DATA_MACHINE)
            .EUt(VA[UEV].toLong()).duration(600)
            .stationResearch {
                it.researchStack(ADVANCED_DATA_ACCESS_HATCH.asStack())
                    .dataStack(TOOL_DATA_MODULE.asStack())
                    .EUt(VA[UV]).CWUt(256)
            }
            .save(provider)
    }
}
