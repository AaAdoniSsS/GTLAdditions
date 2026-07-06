package com.gtladd.gtladditions.common.machine.multiblock

import org.gtlcore.gtlcore.GTLCore
import org.gtlcore.gtlcore.api.pattern.GTLPredicates
import org.gtlcore.gtlcore.api.pattern.GTLPredicates.diffAbilities
import org.gtlcore.gtlcore.common.block.BlockMap
import org.gtlcore.gtlcore.common.block.GTLFusionCasingBlock
import org.gtlcore.gtlcore.common.data.GTLBlocks.*
import org.gtlcore.gtlcore.common.data.GTLMachines
import org.gtlcore.gtlcore.common.data.GTLMaterials.*
import org.gtlcore.gtlcore.common.data.GTLRecipeTypes.*

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper
import com.gregtechceu.gtceu.api.data.tag.TagPrefix.block
import com.gregtechceu.gtceu.api.data.tag.TagPrefix.frameGt
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.*
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern
import com.gregtechceu.gtceu.api.pattern.Predicates.*
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.common.data.GCyMBlocks.*
import com.gregtechceu.gtceu.common.data.GCyMBlocks.HEAT_VENT
import com.gregtechceu.gtceu.common.data.GTBlocks.*
import com.gregtechceu.gtceu.common.data.GTMachines
import com.gregtechceu.gtceu.common.data.GTMaterials.*
import com.gregtechceu.gtceu.common.data.GTRecipeTypes.*

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.properties.SlabType

import appeng.core.definitions.AEBlocks
import com.gtladd.gtladditions.api.machine.*
import com.gtladd.gtladditions.api.recipe.FastRecipeModify
import com.gtladd.gtladditions.api.registry.GTLAddRegistration.Companion.REGISTRATE
import com.gtladd.gtladditions.client.render.machine.ArcanicAstrographRender
import com.gtladd.gtladditions.client.render.machine.SpaceElevatorMKIIRenderer
import com.gtladd.gtladditions.common.machine.GTLAddMachines
import com.gtladd.gtladditions.common.machine.GTLAddPredicates
import com.gtladd.gtladditions.common.machine.multiblock.controller.*
import com.gtladd.gtladditions.common.machine.multiblock.controller.bs.BiosphereIIIController
import com.gtladd.gtladditions.common.machine.multiblock.controller.bs.BiosphereIIIModule
import com.gtladd.gtladditions.common.machine.multiblock.controller.df.*
import com.gtladd.gtladditions.common.machine.multiblock.controller.fl.FloatingLightController
import com.gtladd.gtladditions.common.machine.multiblock.controller.fl.FloatingLightModule
import com.gtladd.gtladditions.common.machine.multiblock.structure.MultiBlockStructureA
import com.gtladd.gtladditions.common.machine.multiblock.structure.MultiBlockStructureB
import com.gtladd.gtladditions.common.recipe.GTLAddRecipesTypes
import com.gtladd.gtladditions.common.recipe.GTLAddRecipesTypes.BiosphereIIIType
import com.gtladd.gtladditions.utils.ComponentUtil.literal
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import com.gtladd.gtladditions.utils.MathUtil.pow
import com.gtladd.gtladditions.utils.Registries.getBlock
import com.gtladd.gtladditions.utils.Registries.getFluid
import com.hepdd.gtmthings.data.CustomMachines

import java.util.function.Function

object MultiBlockMachine {
    @JvmField
    val LUCID_ETCHDREAMER: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "lucid_etchdreamer",
        Function {
            return@Function object : GTLAddCoilWorkableElectricMultipleRecipesTypesMultiblockMachine(it) {
                override val multiRecipeTypes: Array<GTRecipeType> = arrayOf(GTLAddRecipesTypes.LucidEtchdreamerType)
            }
        }
    )
        .nonYAxisRotation()
        .tooltipTextCoilParallel()
        .tooltipTextLaser()
        .tooltipTextMultiRecipes()
        .tooltipTextMultiRecipeTypes()
        .tooltipTextMultiRecipeType(GTLAddRecipesTypes.PHOTON_MATRIX_ETCH, LASER_ENGRAVER_RECIPES)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeTypes(GTLAddRecipesTypes.PHOTON_MATRIX_ETCH, LASER_ENGRAVER_RECIPES)
        .appearanceBlock(IRIDIUM_CASING)
        .pattern {
            MultiBlockStructureA.LUCID_ETCHDREAMER_STRUCTURE
                .where("I", controller(blocks(it.get())))
                .where(
                    "A",
                    blocks(IRIDIUM_CASING.get())
                        .or(autoAbilities(*it.recipeTypes))
                        .or(abilities(MAINTENANCE).setExactLimit(1))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(1))
                )
                .where("D", heatingCoils())
                .where("E", blocks("kubejs:neutronium_pipe_casing".getBlock))
                .where("B", blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                .where("C", blocks(DIMENSION_INJECTION_CASING.get()))
                .where("G", blocks(GRAVITON_FIELD_CONSTRAINT_CASING.get()))
                .where("F", blocks(CLEANROOM_GLASS.get()))
                .where("H", blocks("kubejs:annihilate_core".getBlock))
                .where(" ", any())
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/iridium_casing"),
            GTCEu.id("block/multiblock/gcym/large_engraving_laser")
        )
        .register()

    @JvmField
    val ATOMIC_TRANSMUTATION_CORE: MultiblockMachineDefinition = REGISTRATE.multiblock("atomic_transmutation_core", ::ConversationMachine)
        .noneRotation()
        .tooltipTextKey(
            "gtceu.multiblock.atomic_transmutation_core.tooltip.0".toComponent,
            "gtceu.machine.hold_g.tooltip.1".toComponent,
            "gtceu.machine.hold_g.tooltip.2".toComponent
        )
        .tooltipTextRecipeTypes(GTLAddRecipesTypes.TRANSMUTATION_BLOCK_CONVERSION)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(GTLAddRecipesTypes.TRANSMUTATION_BLOCK_CONVERSION)
        .appearanceBlock(LAFIUM_MECHANICAL_CASING)
        .pattern {
            MultiBlockStructureB.ATOMIC_TRANSMUTATION_CORE_STRUCTURE
                .where("~", controller(blocks(it.get())))
                .where(
                    "d",
                    blocks(LAFIUM_MECHANICAL_CASING.get())
                        .or(blocks(GTLAddMachines.ME_BLOCK_CONVERSATION.get()).setMaxGlobalLimited(1))
                        .or(abilities(INPUT_ENERGY).setMaxGlobalLimited(1))
                )
                .where("E", blocks(DRAGON_STRENGTH_TRITANIUM_CASING.get()))
                .where("B", blocks(ECHO_CASING.get()))
                .where("L", blocks("kubejs:dyson_deployment_magnet".getBlock))
                .where("D", blocks(LAFIUM_MECHANICAL_CASING.get()))
                .where("F", blocks(RHENIUM_REINFORCED_ENERGY_GLASS.get()))
                .where("A", blocks(SPS_CASING.get()))
                .where("K", blocks("kubejs:force_field_glass".getBlock))
                .where("I", blocks(DIMENSION_INJECTION_CASING.get()))
                .where("H", blocks("kubejs:containment_field_generator".getBlock))
                .where("C", blocks(ChemicalHelper.getBlock(frameGt, Adamantium)))
                .where("G", blocks("kubejs:titansteel_coil_block".getBlock))
                .where("J", blocks("kubejs:restraint_device".getBlock))
                .where(" ", any())
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/lafium_mechanical_casing"),
            GTCEu.id("block/multiblock/cleanroom")
        )
        .register()

    @JvmField
    val ASTRAL_CONVERGENCE_NEXUS: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "astral_convergence_nexus",
        ::AdvancedSpaceElevatorModuleMachine
    )
        .nonYAxisRotation()
        .tooltipTextMaxParallels("gtceu.multiblock.max_parallel.space_elevator_module".toComponent)
        .tooltipTextKey("gtceu.multiblock.reduce_time.space_elevator_module".toComponent)
        .tooltipTextLaser()
        .tooltipTextMultiRecipes()
        .tooltipTextRecipeTypes(ASSEMBLER_MODULE_RECIPES)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(ASSEMBLER_MODULE_RECIPES)
        .appearanceBlock(SPACE_ELEVATOR_MECHANICAL_CASING)
        .pattern {
            FactoryBlockPattern.start()
                .aisle("aaa", "bcb", "bbb", "bbb", "bbb")
                .aisle("aaa", "bbb", "bbb", "bbb", "bbb")
                .aisle("aaa", "bbb", "bbb", "b~b", "bbb")
                .where("~", controller(blocks(it.get())))
                .where(
                    "b",
                    blocks(SPACE_ELEVATOR_MECHANICAL_CASING.get())
                        .or(autoAbilities(*it.recipeTypes))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(1))
                )
                .where("a", blocks("kubejs:module_base".getBlock))
                .where("c", blocks("kubejs:module_connector".getBlock))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/space_elevator_mechanical_casing"),
            GTCEu.id("block/multiblock/gcym/large_assembler")
        )
        .register()

    @JvmField
    val NEBULA_REAPER: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "nebula_reaper",
        ::AdvancedSpaceElevatorModuleMachine
    )
        .nonYAxisRotation()
        .tooltipTextMaxParallels("gtceu.multiblock.max_parallel.space_elevator_module".toComponent)
        .tooltipTextKey("gtceu.multiblock.reduce_time.space_elevator_module".toComponent)
        .tooltipTextLaser()
        .tooltipTextMultiRecipes()
        .tooltipTextRecipeTypes(MINER_MODULE_RECIPES, DRILLING_MODULE_RECIPES)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(MINER_MODULE_RECIPES)
        .recipeType(DRILLING_MODULE_RECIPES)
        .appearanceBlock(SPACE_ELEVATOR_MECHANICAL_CASING)
        .pattern {
            FactoryBlockPattern.start()
                .aisle("aaa", "bcb", "bbb", "bbb", "bbb")
                .aisle("aaa", "bbb", "bbb", "bbb", "bbb")
                .aisle("aaa", "bbb", "bbb", "b~b", "bbb")
                .where("~", controller(blocks(it.get())))
                .where(
                    "b",
                    blocks(SPACE_ELEVATOR_MECHANICAL_CASING.get())
                        .or(autoAbilities(*it.recipeTypes))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(1))
                )
                .where("a", blocks("kubejs:module_base".getBlock))
                .where("c", blocks("kubejs:module_connector".getBlock))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/space_elevator_mechanical_casing"),
            GTCEu.id("block/multiblock/gcym/large_assembler")
        )
        .register()

    @JvmField
    val ARCANIC_ASTROGRAPH: MultiblockMachineDefinition = REGISTRATE.multiblock("arcanic_astrograph", ::ArcanicAstrograph)
        .nonYAxisRotation()
        .recipeType(COSMOS_SIMULATION_RECIPES)
        .recipeModifier(ArcanicAstrograph::recipeModifier)
        .tooltips(
            *arrayOf<Component>(
                "gtceu.machine.hold_g.tooltip.1".toComponent,
                "gtceu.machine.eye_of_harmony.tooltip.0".toComponent,
                "gtceu.machine.eye_of_harmony.tooltip.1".toComponent,
                "gtceu.machine.eye_of_harmony.tooltip.2".toComponent,
                "gtceu.machine.eye_of_harmony.tooltip.3".toComponent,
                "gtceu.machine.eye_of_harmony.tooltip.4".toComponent,
                "gtceu.machine.eye_of_harmony.tooltip.5".toComponent,
                "gtceu.machine.eye_of_harmony.tooltip.6".toComponent,
                Component.translatable("gtceu.machine.available_recipe_map_1.tooltip", "gtceu.cosmos_simulation".toComponent)
            )
        )
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .appearanceBlock(CREATE_CASING)
        .pattern {
            MultiBlockStructureA.EYE_OF_HARMONY_STRUCTURE
                .where('~', controller(blocks(it.get())))
                .where(
                    'B',
                    blocks(HIGH_POWER_CASING.get())
                        .or(abilities(EXPORT_ITEMS).setMaxGlobalLimited(1))
                        .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(1))
                        .or(abilities(EXPORT_FLUIDS).setMaxGlobalLimited(1))
                        .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(1))
                )
                .where('A', blocks(CREATE_CASING.get()))
                .where('D', blocks(DIMENSION_INJECTION_CASING.get()))
                .where('E', blocks("kubejs:dimension_creation_casing".getBlock))
                .where('F', blocks("kubejs:spacetime_compression_field_generator".getBlock))
                .where('G', blocks("kubejs:dimensional_stability_casing".getBlock))
                .where(" ", any())
                .build()
        }
        .renderer(::ArcanicAstrographRender)
        .hasTESR(true)
        .register()

    @JvmField
    val ARCANE_CACHE_VAULT: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "arcane_cache_vault",
        ::GTLAddCoilWorkableElectricMultipleRecipesMultiblockMachine
    )
        .allRotation()
        .tooltipTextCoilParallel()
        .tooltipTextLaser()
        .tooltipTextMultiRecipes()
        .tooltipTextRecipeTypes(PACKER_RECIPES)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(PACKER_RECIPES)
        .appearanceBlock(OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING)
        .pattern {
            MultiBlockStructureB.ARCANE_CACHE_VAULT_STRUCTURE
                .where("W", controller(blocks(it.get())))
                .where(
                    "C",
                    blocks(OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING.get())
                        .or(autoAbilities(*it.recipeTypes))
                        .or(abilities(MAINTENANCE).setExactLimit(1))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(1))
                )
                .where("R", blocks("kubejs:force_field_glass".getBlock))
                .where("S", blocks(PIKYONIUM_MACHINE_CASING.get()))
                .where("T", heatingCoils())
                .where("N", blocks(ChemicalHelper.getBlock(frameGt, NaquadahAlloy)))
                .where("G", blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                .where("M", blocks("kubejs:molecular_coil".getBlock))
                .where("K", blocks(FUSION_GLASS.get()))
                .where("D", blocks(NAQUADAH_ALLOY_CASING.get()))
                .where("B", blocks(IRIDIUM_CASING.get()))
                .where("P", blocks("kubejs:neutronium_gearbox".getBlock))
                .where("L", blocks(FILTER_CASING_STERILE.get()))
                .where("H", blocks(CASING_ATOMIC.get()))
                .where("Q", blocks(ANTIFREEZE_HEATPROOF_MACHINE_CASING.get()))
                .where("J", blocks(HERMETIC_CASING_UHV.get()))
                .where("I", blocks(MOLECULAR_CASING.get()))
                .where("V", blocks("kubejs:hollow_casing".getBlock))
                .where("E", blocks(DIMENSION_INJECTION_CASING.get()))
                .where("U", blocks("kubejs:neutronium_pipe_casing".getBlock))
                .where("F", blocks(ChemicalHelper.getBlock(frameGt, Quantanium)))
                .where("O", blocks(ADVANCED_COMPUTER_CASING.get()))
                .where(" ", any())
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/oxidation_resistant_hastelloy_n_mechanical_casing"),
            GTCEu.id("block/multiblock/gcym/large_packer")
        )
        .register()

    @JvmField
    val DRACONIC_COLLAPSE_CORE: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "draconic_collapse_core",
        ::DraconicCollapseCore
    )
        .nonYAxisRotation()
        .tooltipTextKey(
            "gtceu.multiblock.draconic_collapse_core.tooltip.0".toComponent,
            "gtceu.multiblock.draconic_collapse_core.tooltip.1".toComponent,
            "gtceu.multiblock.draconic_collapse_core.tooltip.2".toComponent,
            "gtceu.multiblock.draconic_collapse_core.tooltip.3".toComponent
        )
        .tooltipOnlyTextLaser()
        .tooltipTextPerfectOverclock()
        .tooltipTextRecipeTypes(AGGREGATION_DEVICE_RECIPES)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(AGGREGATION_DEVICE_RECIPES)
        .recipeModifier(DraconicCollapseCore::recipeModify)
        .appearanceBlock(FUSION_CASING_MK5)
        .pattern {
            MultiBlockStructureA.DRACONIC_COLLAPSE_CORE_STRUCTURE
                .where("E", controller(blocks(it.get())))
                .where(
                    "D",
                    blocks(GTLFusionCasingBlock.getCasingState(10))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(2)).setMinGlobalLimited(1)
                )
                .where(
                    "L",
                    blocks(GTLFusionCasingBlock.getCasingState(10))
                        .or(blocks(GTMachines.ITEM_IMPORT_BUS[0].get()))
                        .or(blocks(CustomMachines.HUGE_ITEM_IMPORT_BUS[0].get()))
                )
                .where(
                    "O",
                    blocks(GTLFusionCasingBlock.getCasingState(10))
                        .or(GTLAddPredicates.dccBlocks())
                )
                .where("I", blocks(MOLECULAR_CASING.get()))
                .where("K", blocks("kubejs:annihilate_core".getBlock))
                .where("J", blocks("kubejs:aggregatione_core".getBlock))
                .where("F", blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                .where("B", blocks(ChemicalHelper.getBlock(frameGt, Neutronium)))
                .where("A", blocks(DIMENSION_INJECTION_CASING.get()))
                .where("C", blocks(GTLFusionCasingBlock.getCasingState(10)))
                .where("H", blocks("kubejs:hollow_casing".getBlock))
                .where("G", blocks(GTLFusionCasingBlock.getCompressedCoilState(10)))
                .where(" ", any())
                .build()
        }
        .workableCasingRenderer(
            GTLFusionCasingBlock.getCasingType(10).texture,
            GTCEu.id("block/multiblock/fusion_reactor")
        )
        .register()

    @JvmField
    val TITAN_CRIP_EARTHBORE: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "titan_crip_earthbore",
        ::WorkableElectricMultiblockMachine
    )
        .noneRotation()
        .tooltipTextKey(
            "gtceu.multiblock.titan_crip_earthbore.tooltip.0".toComponent,
            "gtceu.multiblock.titan_crip_earthbore.tooltip.1".toComponent
        )
        .tooltipTextRecipeTypes(GTLAddRecipesTypes.TECTONIC_FAULT_GENERATOR)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(GTLAddRecipesTypes.TECTONIC_FAULT_GENERATOR)
        .recipeModifier { machine, recipe, _, _ ->
            (machine as WorkableElectricMultiblockMachine).let {
                return@recipeModifier FastRecipeModify.modify(
                    it,
                    recipe,
                    2L.pow(it.tier - 6),
                    ocResult = FastRecipeModify.getNoPerfectOverclock()
                ) { FastRecipeModify.getDefaultReduce() }
            }
        }
        .appearanceBlock(ECHO_CASING)
        .pattern {
            MultiBlockStructureA.TITAN_CRIP_EARTHBORE_STRUCTURE
                .where("~", controller(blocks(it.get())))
                .where(
                    "E",
                    blocks(ECHO_CASING.get())
                        .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(1))
                        .or(abilities(EXPORT_ITEMS).setMaxGlobalLimited(1))
                        .or(abilities(INPUT_ENERGY).setMaxGlobalLimited(2))
                        .or(ability(INPUT_LASER, 13, 14).setMaxGlobalLimited(1))
                        .or(abilities(MAINTENANCE).setExactLimit(1))
                )
                .where("I", blocks("kubejs:neutronium_gearbox".getBlock))
                .where("H", blocks("kubejs:neutronium_pipe_casing".getBlock))
                .where("G", blocks("kubejs:machine_casing_grinding_head".getBlock))
                .where("B", blocks(ChemicalHelper.getBlock(frameGt, Neutronium)))
                .where("C", blocks(ECHO_CASING.get()))
                .where("A", blocks(MOLECULAR_CASING.get()))
                .where("F", blocks(Blocks.BEDROCK))
                .where("D", blocks("kubejs:molecular_coil".getBlock))
                .build()
        }
        .additionalDisplay { controller, components ->
            (controller as WorkableElectricMultiblockMachine).takeIf { it.isFormed }?.let {
                components.add(
                    Component.translatable(
                        "gtceu.multiblock.parallel",
                        2.pow(it.getTier() - 6).literal
                            .withStyle(ChatFormatting.DARK_PURPLE)
                    ).withStyle(ChatFormatting.GRAY)
                )
            }
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/echo_casing"),
            GTCEu.id("block/multiblock/cleanroom")
        )
        .register()

    @JvmField
    val BIOLOGICAL_SIMULATION_LABORATORY: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "biological_simulation_laboratory",
        ::BiologicalSimulationLaboratory
    )
        .allRotation()
        .tooltipTextKey(
            "gtceu.multiblock.biological_simulation_laboratory.tooltip.0".toComponent,
            "gtceu.machine.hold_g.tooltip.1".toComponent,
            "gtceu.machine.hold_g.tooltip.2".toComponent,
            "gtceu.multiblock.biological_simulation_laboratory.tooltip.1".toComponent
        )
        .tooltipTextPerfectOverclock()
        .tooltipTextRecipeTypes(GTLAddRecipesTypes.BIOLOGICAL_SIMULATION)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(GTLAddRecipesTypes.BIOLOGICAL_SIMULATION)
        .appearanceBlock(NAQUADAH_ALLOY_CASING)
        .pattern {
            MultiBlockStructureA.BIOLOGICAL_SIMULATION_LABORATORY_STRUCTURE
                .where("~", controller(blocks(it.get())))
                .where(
                    "A",
                    blocks(NAQUADAH_ALLOY_CASING.get())
                        .or(autoAbilities(*it.recipeTypes))
                        .or(blocks(*INPUT_LASER.getBlockRange(12, 14).toTypedArray()).setMaxGlobalLimited(1))
                )
                .where("B", blocks(ChemicalHelper.getBlock(frameGt, NaquadahAlloy)))
                .where("C", blocks(HERMETIC_CASING_LuV.get()))
                .where("E", blocks(FUSION_GLASS.get()))
                .where("G", blocks(COMPUTER_HEAT_VENT.get()))
                .where("D", blocks(ADVANCED_COMPUTER_CASING.get()))
                .where("H", blocks(FILTER_CASING_STERILE.get()))
                .where("F", blocks(HERMETIC_CASING_ZPM.get()))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/hyper_mechanical_casing"),
            GTCEu.id("block/multiblock/fusion_reactor")
        )
        .register()

    @JvmField
    val DIMENSIONALLY_TRANSCENDENT_CHEMICAL_PLANT: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "dimensionally_transcendent_chemical_plant",
        Function {
            return@Function object : GTLAddCoilWorkableElectricMultipleRecipesMultiblockMachine(it) {
                override fun modifyRecipe(recipe: GTRecipe): FastRecipeModify.ReduceResult {
                    val reduce = 1 - this.getCoilTier() * 0.05
                    return FastRecipeModify.ReduceResult(reduce * 0.6, reduce * 0.4)
                }
            }
        }
    )
        .nonYAxisRotation()
        .tooltipTextKey(
            "gtceu.multiblock.dimensionally_transcendent_chemical_plant".toComponent,
            "gtceu.machine.chemical_plant.tooltip.0".toComponent,
            "gtceu.machine.hold_g.tooltip.3".toComponent
        )
        .tooltipTextCoilParallel()
        .tooltipTextLaser()
        .tooltipTextMultiRecipes()
        .tooltipTextRecipeTypes(LARGE_CHEMICAL_RECIPES)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(LARGE_CHEMICAL_RECIPES)
        .appearanceBlock(CASING_PTFE_INERT)
        .pattern {
            GTLMachines.DTPF
                .where("a", controller(blocks(it.get())))
                .where(
                    "e",
                    blocks(CASING_PTFE_INERT.get())
                        .or(abilities(MAINTENANCE).setExactLimit(1))
                        .or(abilities(EXPORT_ITEMS).setPreviewCount(1))
                        .or(abilities(IMPORT_ITEMS).setPreviewCount(1))
                        .or(abilities(EXPORT_FLUIDS).setPreviewCount(1))
                        .or(abilities(IMPORT_FLUIDS).setPreviewCount(1))
                        .or(abilities(INPUT_ENERGY).setMaxGlobalLimited(2))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(1))
                )
                .where("b", blocks(HIGH_POWER_CASING.get()))
                .where("C", heatingCoils())
                .where("d", blocks(CASING_PTFE_INERT.get()))
                .where("s", blocks("gtceu:ptfe_pipe_casing".getBlock))
                .where(" ", any())
                .build()
        }
        .workableCasingRenderer(
            GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"),
            GTCEu.id("block/machines/chemical_reactor")
        )
        .register()

    @JvmField
    val QUANTUM_SYPHON_MATRIX: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "quantum_syphon_matrix",
        ::GTLAddWorkableElectricParallelHatchMultipleRecipesMachine
    )
        .noneRotation()
        .tooltipTextParallelHatch()
        .tooltipTextLaser()
        .tooltipTextMultiRecipes()
        .tooltipTextRecipeTypes(GTLAddRecipesTypes.VOIDFLUX_REACTION)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(GTLAddRecipesTypes.VOIDFLUX_REACTION)
        .appearanceBlock(HIGH_POWER_CASING)
        .pattern {
            MultiBlockStructureA.QUANTUM_SYPHON_MATRIX_STRUCTURE
                .where("~", controller(blocks(it.get())))
                .where(
                    "F",
                    blocks(HIGH_POWER_CASING.get())
                        .or(autoAbilities(*it.recipeTypes))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(1))
                        .or(abilities(PARALLEL_HATCH).setMaxGlobalLimited(1))
                )
                .where("C", blocks(ChemicalHelper.getBlock(frameGt, Neutronium)))
                .where("G", blocks("kubejs:accelerated_pipeline".getBlock))
                .where("D", blocks(MOLECULAR_CASING.get()))
                .where("H", blocks("kubejs:neutronium_gearbox".getBlock))
                .where("J", blocks("kubejs:neutronium_pipe_casing".getBlock))
                .where("A", blocks(NAQUADAH_ALLOY_CASING.get()))
                .where("B", blocks("gtceu:assembly_line_grating".getBlock))
                .where("I", blocks(HERMETIC_CASING_UHV.get()))
                .where("E", blocks("kubejs:hollow_casing".getBlock))
                .where(" ", any())
                .build()
        }
        .workableCasingRenderer(
            GTCEu.id("block/casings/hpca/high_power_casing"),
            GTCEu.id("block/machines/gas_collector")
        )
        .register()

    @JvmField
    val FUXI_BAGUA_HEAVEN_FORGING_FURNACE: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "fuxi_bagua_heaven_forging_furnace",
        Function {
            return@Function object : GTLAddCoilWorkableElectricParallelHatchMultipleRecipesMachine(it) {
                override fun modifyRecipe(recipe: GTRecipe): FastRecipeModify.ReduceResult {
                    return FastRecipeModify.ReduceResult(0.6, 0.8)
                }
            }
        }
    )
        .allRotation()
        .tooltipTextKey(
            "gtceu.multiblock.fuxi_bagua_heaven_forging_furnace.tooltip.0".toComponent,
            "gtceu.multiblock.fuxi_bagua_heaven_forging_furnace.tooltip.1".toComponent,
            "gtceu.multiblock.fuxi_bagua_heaven_forging_furnace.tooltip.2".toComponent
        )
        .tooltipTextParallelHatch()
        .tooltipOnlyTextLaser()
        .tooltipTextMultiRecipes()
        .tooltipTextRecipeTypes(
            GTLAddRecipesTypes.STELLAR_LGNITION,
            GTLAddRecipesTypes.CHAOTIC_ALCHEMY,
            GTLAddRecipesTypes.MOLECULAR_DECONSTRUCTION,
            ULTIMATE_MATERIAL_FORGE_RECIPES
        )
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeTypes(
            GTLAddRecipesTypes.STELLAR_LGNITION,
            GTLAddRecipesTypes.CHAOTIC_ALCHEMY,
            GTLAddRecipesTypes.MOLECULAR_DECONSTRUCTION,
            ULTIMATE_MATERIAL_FORGE_RECIPES
        )
        .appearanceBlock(DIMENSION_INJECTION_CASING)
        .pattern {
            MultiBlockStructureA.FUXI_BAGUA_HEAVEN_FORGING_FURNACE_STRUCTURE
                .where("D", controller(blocks(it.get())))
                .where(
                    "C",
                    blocks(DIMENSION_INJECTION_CASING.get())
                        .or(abilities(EXPORT_ITEMS).setPreviewCount(1))
                        .or(abilities(IMPORT_ITEMS).setPreviewCount(1))
                        .or(abilities(EXPORT_FLUIDS).setPreviewCount(1))
                        .or(abilities(IMPORT_FLUIDS).setPreviewCount(1))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(2))
                        .or(abilities(PARALLEL_HATCH).setMaxGlobalLimited(1))
                )
                .where("K", blocks("kubejs:neutronium_pipe_casing".getBlock))
                .where("X", heatingCoils())
                .where("J", blocks("kubejs:dimensional_bridge_casing".getBlock))
                .where("F", blocks(GRAVITON_FIELD_CONSTRAINT_CASING.get()))
                .where("I", blocks("kubejs:molecular_coil".getBlock))
                .where("A", blocks("gtceu:atomic_casing".getBlock))
                .where("G", blocks(DEGENERATE_RHENIUM_CONSTRAINED_CASING.get()))
                .where("N", blocks(ULTIMATE_STELLAR_CONTAINMENT_CASING.get()))
                .where("B", blocks(DIMENSION_INJECTION_CASING.get()))
                .where("E", blocks("kubejs:dimension_creation_casing".getBlock))
                .where("H", blocks("kubejs:spacetime_compression_field_generator".getBlock))
                .where("L", blocks(COMPRESSED_FUSION_COIL_MK2_PROTOTYPE.get()))
                .where("M", blocks("kubejs:dimensional_stability_casing".getBlock))
                .where("O", blocks("kubejs:restraint_device".getBlock))
                .build()
        }
        .additionalDisplay { controller, components ->
            (controller as GTLAddCoilWorkableElectricParallelHatchMultipleRecipesMachine).takeIf { it.isFormed }?.let {
                components.add(
                    Component.translatable(
                        "gtceu.multiblock.blast_furnace.max_temperature",
                        "${it.coilType.coilTemperature}K".literal.setStyle(Style.EMPTY.withColor(ChatFormatting.RED))
                    )
                )
            }
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/dimension_injection_casing"),
            GTCEu.id("block/multiblock/fusion_reactor")
        )
        .register()

    @JvmField
    val ANTIENTROPY_CONDENSATION_CENTER: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "antientropy_condensation_center",
        ::AntientropyCondensationCenter
    )
        .allRotation()
        .tooltipTextKey(
            "gtceu.multiblock.antientropy_condensation_center.0".toComponent,
            "gtceu.multiblock.antientropy_condensation_center.1".toComponent,
            "gtceu.machine.hold_g.tooltip.3".toComponent
        )
        .tooltipTextParallelHatch()
        .tooltipOnlyTextLaser()
        .tooltipTextMultiRecipes()
        .tooltipTextRecipeTypes(GTLAddRecipesTypes.ANTIENTROPY_CONDENSATION)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(GTLAddRecipesTypes.ANTIENTROPY_CONDENSATION)
        .appearanceBlock(ANTIFREEZE_HEATPROOF_MACHINE_CASING)
        .pattern {
            MultiBlockStructureA.ANTIENTROPY_CONDENSATION_CENTER_STRUCTURE
                .where("B", controller(blocks(it.get())))
                .where(
                    "X",
                    blocks(ANTIFREEZE_HEATPROOF_MACHINE_CASING.get())
                        .or(abilities(EXPORT_ITEMS).setPreviewCount(1))
                        .or(abilities(IMPORT_ITEMS).setPreviewCount(1))
                        .or(abilities(EXPORT_FLUIDS).setPreviewCount(1))
                        .or(abilities(IMPORT_FLUIDS).setPreviewCount(1))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(2))
                        .or(abilities(PARALLEL_HATCH).setMaxGlobalLimited(1))
                )
                .where("C", blocks(MOLECULAR_CASING.get()))
                .where("K", blocks(ChemicalHelper.getBlock(frameGt, Mithril)))
                .where("D", blocks(HERMETIC_CASING_UXV.get()))
                .where("M", blocks("kubejs:containment_field_generator".getBlock))
                .where("J", blocks("kubejs:force_field_glass".getBlock))
                .where("I", blocks("kubejs:dimensional_bridge_casing".getBlock))
                .where("A", blocks(ANTIFREEZE_HEATPROOF_MACHINE_CASING.get()))
                .where("F", blocks(COMPRESSED_FUSION_COIL_MK2.get()))
                .where("G", blocks(FILTER_CASING_LAW.get()))
                .where("H", blocks("kubejs:hollow_casing".getBlock))
                .where("E", blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                .where("L", blocks(DIMENSION_INJECTION_CASING.get()))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/antifreeze_heatproof_machine_casing"),
            GTCEu.id("block/multiblock/vacuum_freezer")
        )
        .register()

    @JvmField
    val TAIXU_TURBID_ARRAY: MultiblockMachineDefinition = REGISTRATE.multiblock("taixu_turbid_array", ::TaixuTurbidArray)
        .noneRotation()
        .tooltipTextKey(
            "gtceu.machine.hold_g.tooltip.0".toComponent,
            "gtceu.machine.hold_g.tooltip.1".toComponent,
            "gtceu.machine.hold_g.tooltip.2".toComponent,
            "gtceu.machine.taixu_turbid_array.tooltip.1".toComponent,
            "gtceu.machine.taixu_turbid_array.tooltip.2".toComponent
        )
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(GTLAddRecipesTypes.CHAOS_WEAVE)
        .recipeModifier(TaixuTurbidArray::recipeModifier)
        .appearanceBlock(MACHINE_CASING_UHV)
        .pattern {
            MultiBlockStructureA.TAIXU_TURBID_ARRAY_STRUCTURE
                .where("T", controller(blocks(it.get())))
                .where(
                    "K",
                    blocks(MACHINE_CASING_UHV.get())
                        .or(abilities(INPUT_LASER).setExactLimit(1))
                        .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(1, 1))
                        .or(abilities(EXPORT_ITEMS).setMaxGlobalLimited(2))
                        .or(abilities(EXPORT_FLUIDS).setMaxGlobalLimited(2))
                )
                .where("H", blocks(MACHINE_CASING_UHV.get()))
                .where("E", blocks("gtceu:woods_glass_block".getBlock))
                .where("J", blocks(DIMENSION_INJECTION_CASING.get()))
                .where("B", blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                .where("R", blocks("kubejs:force_field_glass".getBlock))
                .where("S", GTLPredicates.countBlock("SpeedPipe", "kubejs:speeding_pipe".getBlock))
                .where("G", blocks("kubejs:hollow_casing".getBlock))
                .where("F", blocks(ChemicalHelper.getBlock(frameGt, NaquadahAlloy)))
                .where("N", blocks(FUSION_CASING_MK5.get()))
                .where("I", blocks(SPS_CASING.get()))
                .where("P", blocks(FUSION_GLASS.get()))
                .where("M", GTLPredicates.tierCasings(BlockMap.scMap, "SCTier"))
                .where("A", blocks(IRIDIUM_CASING.get()))
                .where("L", blocks("kubejs:containment_field_generator".getBlock))
                .where("Q", blocks("kubejs:dimensional_bridge_casing".getBlock))
                .where("C", blocks("gtceu:atomic_casing".getBlock))
                .where("D", blocks(ChemicalHelper.getBlock(frameGt, Mithril)))
                .where("O", heatingCoils())
                .build()
        }
        .workableCasingRenderer(
            GTCEu.id("block/casings/voltage/uhv/side"),
            GTCEu.id("block/multiblock/top/fusion_reactor")
        )
        .register()

    @JvmField
    val INFERNO_CLEFT_SMELTING_VAULT: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "inferno_cleft_smelting_vault",
        Function {
            return@Function object : GTLAddCoilWorkableElectricMultipleRecipesTypesMultiblockMachine(it) {
                override val multiRecipeTypes = arrayOf(GTLAddRecipesTypes.InfernoCleftSmeltingVaultType)
            }
        }
    )
        .nonYAxisRotation()
        .tooltipTextCoilParallel()
        .tooltipTextLaser()
        .tooltipTextMultiRecipes()
        .tooltipTextMultiRecipeTypes()
        .tooltipTextMultiRecipeType(PYROLYSE_RECIPES, CRACKING_RECIPES)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeTypes(PYROLYSE_RECIPES, CRACKING_RECIPES)
        .appearanceBlock(IRIDIUM_CASING)
        .pattern {
            MultiBlockStructureA.INFERNO_CLEFT_SMELTING_VAULT
                .where("L", controller(blocks(it.get())))
                .where(
                    "I",
                    blocks(IRIDIUM_CASING.get())
                        .or(abilities(EXPORT_ITEMS).setPreviewCount(1))
                        .or(abilities(IMPORT_ITEMS).setPreviewCount(1))
                        .or(abilities(EXPORT_FLUIDS).setPreviewCount(1))
                        .or(abilities(IMPORT_FLUIDS).setPreviewCount(1))
                        .or(abilities(INPUT_ENERGY).setMaxGlobalLimited(2))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(1))
                )
                .where("M", blocks(GTMachines.MUFFLER_HATCH[8].get()))
                .where("G", heatingCoils())
                .where("H", heatingCoils())
                .where("B", blocks(IRIDIUM_CASING.get()))
                .where("A", blocks(MOLECULAR_CASING.get()))
                .where("J", blocks(HERMETIC_CASING_LuV.get()))
                .where("C", blocks(HYPER_MECHANICAL_CASING.get()))
                .where("E", blocks("kubejs:neutronium_pipe_casing".getBlock))
                .where("K", blocks(HYPER_CORE.get()))
                .where("D", blocks("gtceu:high_temperature_smelting_casing".getBlock))
                .where("F", blocks(FUSION_GLASS.get()))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/iridium_casing"),
            GTCEu.id("block/multiblock/pyrolyse_oven")
        )
        .register()

    @JvmField
    val SKELETON_SHIFT_RIFT_ENGINE: MultiblockMachineDefinition = REGISTRATE.multiblock("skeleton_shift_rift_engine", ::SkeletonShiftRiftEngine)
        .nonYAxisRotation()
        .tooltipTextKey(
            "gtceu.multiblock.skeleton_shift_rift_engine.0".toComponent,
            "gtceu.multiblock.skeleton_shift_rift_engine.1".toComponent
        )
        .tooltipTextLaser()
        .tooltipTextMultiRecipeTypes()
        .tooltipTextPerfectOverclock()
        .tooltipTextRecipeTypes(DECAY_HASTENER_RECIPES)
        .tooltipTextMultiRecipeType(FUSION_RECIPES, SUPER_PARTICLE_COLLIDER_RECIPES)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeTypes(DECAY_HASTENER_RECIPES, FUSION_RECIPES, SUPER_PARTICLE_COLLIDER_RECIPES)
        .appearanceBlock(HYPER_MECHANICAL_CASING)
        .pattern {
            MultiBlockStructureA.SKELETON_SHIFT_RIFT_ENGINE
                .where("Q", controller(blocks(it.get())))
                .where(
                    "h",
                    blocks(HYPER_MECHANICAL_CASING.get())
                        .or(abilities(EXPORT_ITEMS).setPreviewCount(1))
                        .or(abilities(IMPORT_ITEMS).setPreviewCount(1))
                        .or(abilities(EXPORT_FLUIDS).setPreviewCount(1))
                        .or(abilities(IMPORT_FLUIDS).setPreviewCount(1))
                        .or(abilities(INPUT_ENERGY).setMaxGlobalLimited(2))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(1))
                )
                .where("P", GTLPredicates.tierCasings(BlockMap.scMap, "SCTier"))
                .where("E", blocks(ChemicalHelper.getBlock(frameGt, BlackSteel)))
                .where("B", blocks(HIGH_POWER_CASING.get()))
                .where("D", blocks(SPS_CASING.get()))
                .where("J", blocks("gtceu:steel_pipe_casing".getBlock))
                .where("A", blocks(IRIDIUM_CASING.get()))
                .where("M", blocks("gtceu:tungstensteel_gearbox".getBlock))
                .where("H", blocks(HYPER_MECHANICAL_CASING.get()))
                .where("O", blocks(DEGENERATE_RHENIUM_CONSTRAINED_CASING.get()))
                .where("F", blocks("kubejs:neutronium_pipe_casing".getBlock))
                .where("I", blocks("kubejs:dimensional_bridge_casing".getBlock))
                .where("N", heatingCoils())
                .where("G", blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                .where("C", blocks(ENHANCE_HYPER_MECHANICAL_CASING.get()))
                .where("K", blocks(HYPER_CORE.get()))
                .where("L", blocks(FUSION_GLASS.get()))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/hyper_mechanical_casing"),
            GTCEu.id("block/multiblock/fusion_reactor")
        )
        .register()

    val TIME_SPACE_DISTORTER: MultiblockMachineDefinition = REGISTRATE.multiblock("time_space_distorter", ::TimeSpaceDistorter)
        .nonYAxisRotation()
        .tooltipTextKey(
            "gtceu.multiblock.time_space_distorter.tooltip.0".toComponent,
            "gtceu.multiblock.time_space_distorter.tooltip.1".toComponent,
            "gtceu.multiblock.time_space_distorter.tooltip.2".toComponent,
            "gtceu.multiblock.time_space_distorter.tooltip.3".toComponent,
            "gtceu.multiblock.time_space_distorter.tooltip.4".toComponent,
            "gtceu.multiblock.time_space_distorter.tooltip.5".toComponent,
            "gtceu.machine.hold_g.tooltip.0".toComponent,
            "gtceu.machine.hold_g.tooltip.1".toComponent
        )
        .tooltipTextParallelHatch()
        .tooltipTextLaser()
        .tooltipTextMultiRecipeTypes()
        .tooltipTextMultiRecipeType(QFT_RECIPES, DISTORT_RECIPES)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeTypes(QFT_RECIPES, DISTORT_RECIPES)
        .appearanceBlock(DIMENSION_INJECTION_CASING)
        .pattern {
            MultiBlockStructureB.TIME_SPACE_DISTORTER_STRUCTURE
                .where("~", controller(blocks(it.get())))
                .where(
                    "B",
                    blocks(DIMENSION_INJECTION_CASING.get())
                        .or(autoAbilities(*it.recipeTypes))
                        .or(abilities(MAINTENANCE).setExactLimit(1))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(1))
                        .or(abilities(PARALLEL_HATCH).setMaxGlobalLimited(1))
                )
                .where("F", blocks(RHENIUM_REINFORCED_ENERGY_GLASS.get()))
                .where("C", blocks(DIMENSION_INJECTION_CASING.get()))
                .where("U", blocks("kubejs:dyson_deployment_casing".getBlock))
                .where("N", blocks("kubejs:dimensional_bridge_casing".getBlock))
                .where("V", blocks(MANIPULATOR.get()))
                .where("G", blocks(ChemicalHelper.getBlock(frameGt, MagnetohydrodynamicallyConstrainedStarMatter)))
                .where("E", blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                .where("I", blocks("kubejs:force_field_glass".getBlock))
                .where("X", blocks("kubejs:dimensional_stability_casing".getBlock))
                .where("A", blocks(CREATE_CASING.get()))
                .where("S", blocks(Blocks.BEACON))
                .where("H", blocks(ChemicalHelper.getBlock(frameGt, QuantumChromodynamicallyConfinedMatter)))
                .where("Z", blocks("kubejs:dimension_creation_casing".getBlock))
                .where("W", blocks(HYPER_CORE.get()))
                .where("M", blocks("gtceu:atomic_casing".getBlock))
                .where("Y", blocks("kubejs:annihilate_core".getBlock))
                .where("Q", blocks(Blocks.DIAMOND_BLOCK))
                .where("P", blocks(ChemicalHelper.getBlock(frameGt, Infinity)))
                .where("R", blocks(INFINITY_GLASS.get()))
                .where("T", blocks("kubejs:dyson_control_casing".getBlock))
                .where("[", blocks("kubejs:create_aggregatione_core".getBlock))
                .where("L", blocks("kubejs:eternity_coil_block".getBlock))
                .where("O", blocks(ChemicalHelper.getBlock(frameGt, Eternity)))
                .where("K", blocks("kubejs:molecular_coil".getBlock))
                .where("J", blocks(QFT_COIL.get()))
                .where("D", blocks("kubejs:uruium_coil_block".getBlock))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/dimension_injection_casing"),
            GTCEu.id("block/multiblock/fusion_reactor")
        )
        .register()

    val RECURSIVE_REVERSE_FORGE: MultiblockMachineDefinition = REGISTRATE.multiblock("recursive_reverse_forge", ::RecursiveReverseForge)
        .noneRotation()
        .tooltipTextKey("gtceu.machine.hold_g.tooltip.0".toComponent, "gtceu.machine.hold_g.tooltip.1".toComponent)
        .tooltipTextMultiRecipeTypes()
        .tooltipTextMultiRecipeType(DIMENSIONALLY_TRANSCENDENT_PLASMA_FORGE_RECIPES, STELLAR_FORGE_RECIPES)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeTypes(DIMENSIONALLY_TRANSCENDENT_PLASMA_FORGE_RECIPES, STELLAR_FORGE_RECIPES)
        .appearanceBlock(CREATE_CASING)
        .pattern {
            MultiBlockStructureB.RECURSIVE_REVERSE_FORGE_STRUCTURE
                .where("V", controller(blocks(it.get())))
                .where(
                    "B",
                    blocks(DIMENSION_INJECTION_CASING.get())
                        .or(abilities(EXPORT_ITEMS).setMaxGlobalLimited(2))
                        .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(4))
                        .or(abilities(EXPORT_FLUIDS).setMaxGlobalLimited(2))
                        .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(4))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(1))
                )
                .where("D", blocks(DIMENSION_INJECTION_CASING.get()))
                .where("Q", blocks(HYPER_CORE.get()))
                .where("A", blocks(CREATE_CASING.get()))
                .where("L", blocks("kubejs:dimension_creation_casing".getBlock))
                .where("U", blocks(FUSION_GLASS.get()))
                .where("G", blocks("kubejs:annihilate_core".getBlock))
                .where("C", blocks(SPS_CASING.get()))
                .where("E", blocks(ADVANCED_FUSION_COIL.get()))
                .where("J", blocks(FUSION_CASING_MK4.get()))
                .where("O", blocks(GRAVITON_FIELD_CONSTRAINT_CASING.get()))
                .where("M", blocks(DRAGON_STRENGTH_TRITANIUM_CASING.get()))
                .where("P", blocks(IMPROVED_SUPERCONDUCTOR_COIL.get()))
                .where("K", blocks(DEGENERATE_RHENIUM_CONSTRAINED_CASING.get()))
                .where("F", blocks(ChemicalHelper.getBlock(frameGt, Infinity)))
                .where("N", blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                .where("S", blocks("kubejs:magic_core".getBlock))
                .where("T", blocks(Blocks.CRYING_OBSIDIAN))
                .where("H", blocks(DIMENSION_CONNECTION_CASING.get()))
                .where("I", blocks(HIGH_POWER_CASING.get()))
                .where("R", blocks(FUSION_CASING_MK5.get()))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/create_casing"),
            GTCEu.id("block/multiblock/top/cosmos_simulation")
        )
        .register()

    val CATALYTIC_CASCADE_ARRAY: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "catalytic_cascade_array",
        ::CatalyticCascadeArray
    )
        .nonYAxisRotation()
        .tooltipTextKey("tooltip.gtladditions.recursive_reverse_forge_module_0".toComponent)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .appearanceBlock(DIMENSION_INJECTION_CASING)
        .pattern {
            MultiBlockStructureB.RECURSIVE_REVERSE_FORGE_MODULE_1_STRUCTURE
                .where("F", controller(blocks(it.get())))
                .where(
                    "B",
                    blocks(DIMENSION_INJECTION_CASING.get())
                        .or(blocks(GTLMachines.HUGE_FLUID_IMPORT_HATCH[1].get()).setExactLimit(1))
                        .or(blocks(GTLAddMachines.VIENTIANE_TRANSCEIPTION_NODE.get()).setExactLimit(1))
                )
                .where("C", blocks(DEGENERATE_RHENIUM_CONSTRAINED_CASING.get()))
                .where("E", blocks(GRAVITON_FIELD_CONSTRAINT_CASING.get()))
                .where("D", blocks("gtceu:attuned_tengam_block".getBlock))
                .where("A", blocks(FUSION_CASING_MK3.get()))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/dimension_injection_casing"),
            GTCEu.id("block/multiblock/fusion_reactor")
        )
        .register()

    val HYPERDIMENSIONAL_ENERGY_CONCETRATOR: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "hyperdimensional_energy_concentrator",
        ::HyperdimensionalEnergyConcentrator
    )
        .nonYAxisRotation()
        .tooltipTextKey("tooltip.gtladditions.recursive_reverse_forge_module_0".toComponent)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(DUMMY_RECIPES)
        .appearanceBlock(DIMENSION_INJECTION_CASING)
        .pattern {
            MultiBlockStructureB.RECURSIVE_REVERSE_FORGE_MODULE_1_STRUCTURE
                .where("F", controller(blocks(it.get())))
                .where(
                    "B",
                    blocks(DIMENSION_INJECTION_CASING.get())
                        .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(1))
                        .or(abilities(INPUT_ENERGY).setExactLimit(1))
                        .or(abilities(COMPUTATION_DATA_RECEPTION).setExactLimit(1))
                )
                .where("C", blocks(DEGENERATE_RHENIUM_CONSTRAINED_CASING.get()))
                .where("E", blocks(GRAVITON_FIELD_CONSTRAINT_CASING.get()))
                .where("D", blocks("gtceu:attuned_tengam_block".getBlock))
                .where("A", blocks(FUSION_CASING_MK3.get()))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/dimension_injection_casing"),
            GTCEu.id("block/multiblock/fusion_reactor")
        )
        .register()

    val MAGNETORHEOLOGICAL_CONVERGENCE_CORE: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "magnetorheological_convergence_core",
        ::MagnetorheologicalConvergenceCore
    )
        .nonYAxisRotation()
        .tooltipTextKey("tooltip.gtladditions.recursive_reverse_forge_module_0".toComponent)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .appearanceBlock(DIMENSION_INJECTION_CASING)
        .pattern {
            MultiBlockStructureB.RECURSIVE_REVERSE_FORGE_MODULE_1_STRUCTURE
                .where("F", controller(blocks(it.get())))
                .where(
                    "B",
                    blocks(DIMENSION_INJECTION_CASING.get())
                        .or(blocks(CustomMachines.HUGE_ITEM_IMPORT_BUS[0].get()).setExactLimit(2))
                        .or(blocks(GTLMachines.HUGE_FLUID_IMPORT_HATCH[1].get()).setExactLimit(1))
                        .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(3))
                )
                .where("C", blocks(DEGENERATE_RHENIUM_CONSTRAINED_CASING.get()))
                .where("E", blocks(GRAVITON_FIELD_CONSTRAINT_CASING.get()))
                .where("D", blocks("gtceu:attuned_tengam_block".getBlock))
                .where("A", blocks(FUSION_CASING_MK3.get()))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/dimension_injection_casing"),
            GTCEu.id("block/multiblock/fusion_reactor")
        )
        .register()

    val REVERSE_TIME_BOOSTING_ENGINE: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "reverse_time_boosting_engine",
        ::ReverseTimeBoostingEngine
    )
        .nonYAxisRotation()
        .tooltipTextKey("tooltip.gtladditions.recursive_reverse_forge_module_0".toComponent)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .appearanceBlock(DIMENSION_INJECTION_CASING)
        .pattern {
            MultiBlockStructureB.RECURSIVE_REVERSE_FORGE_MODULE_1_STRUCTURE
                .where("F", controller(blocks(it.get())))
                .where(
                    "B",
                    blocks(DIMENSION_INJECTION_CASING.get())
                        .or(blocks(GTLAddMachines.SUPER_INPUT_DUAL_HATCH.get()).setMaxGlobalLimited(1))
                        .or(blocks(*GTLMachines.HUGE_FLUID_IMPORT_HATCH.mapNotNull { hatch: MachineDefinition? -> hatch?.get() }.toTypedArray()).setMaxGlobalLimited(1))
                        .or(blocks(GTLAddMachines.VIENTIANE_TRANSCEIPTION_NODE.get()).setExactLimit(1))
                )
                .where("C", blocks(DEGENERATE_RHENIUM_CONSTRAINED_CASING.get()))
                .where("E", blocks(GRAVITON_FIELD_CONSTRAINT_CASING.get()))
                .where("D", blocks("gtceu:attuned_tengam_block".getBlock))
                .where("A", blocks(FUSION_CASING_MK3.get()))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/dimension_injection_casing"),
            GTCEu.id("block/multiblock/fusion_reactor")
        )
        .register()

    val FRACTAL_MANIPULATOR: MultiblockMachineDefinition = REGISTRATE.multiblock("fractal_manipulator", ::FractalManipulator)
        .noneRotation()
        .tooltipTextKey("tooltip.gtladditions.recursive_reverse_forge_module_1".toComponent)
        .tooltipOnlyTextLaser()
        .tooltipTextRecipeTypes(GTLAddRecipesTypes.FRACTAL_RECONSTRUCTION)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(GTLAddRecipesTypes.FRACTAL_RECONSTRUCTION)
        .appearanceBlock(DIMENSION_INJECTION_CASING)
        .pattern {
            MultiBlockStructureB.RECURSIVE_REVERSE_FORGE_MODULE_2_STRUCTURE
                .where("I", controller(blocks(it.get())))
                .where(
                    "C",
                    blocks(DIMENSION_INJECTION_CASING.get())
                        .or(abilities(EXPORT_ITEMS).setMaxGlobalLimited(2))
                        .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(2))
                        .or(abilities(EXPORT_FLUIDS).setMaxGlobalLimited(2))
                        .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(2))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(1))
                )
                .where("F", blocks("kubejs:dimension_creation_casing".getBlock))
                .where("B", blocks(RHENIUM_REINFORCED_ENERGY_GLASS.get()))
                .where("H", blocks("kubejs:annihilate_core".getBlock))
                .where("E", blocks(SPACETIMECONTINUUMRIPPER.get()))
                .where("A", blocks(SPS_CASING.get()))
                .where("D", blocks(GRAVITON_FIELD_CONSTRAINT_CASING.get()))
                .where("G", blocks("kubejs:magic_core".getBlock))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/dimension_injection_casing"),
            GTCEu.id("block/multiblock/top/fusion_reactor")
        )
        .register()

    val PLANETARY_IONISATION_CONVERGENCE_TOWER: MultiblockMachineDefinition = REGISTRATE.multiblock("planetary_ionisation_convergence_tower", ::PlanetaryIonisationConvergenceTower)
        .nonYAxisRotation()
        .tooltipTextKey("gtceu.machine.hold_g.tooltip.0".toComponent, "gtceu.machine.hold_g.tooltip.1".toComponent)
        .tooltipOnlyTextLaser()
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(DUMMY_RECIPES)
        .generator(true)
        .appearanceBlock(SPS_CASING)
        .pattern {
            MultiBlockStructureB.PLANETARY_IONISATION_CONVERGENCE_TOWER_STRUCTURE
                .where("Z", controller(blocks(it.get())))
                .where(
                    "I",
                    blocks(SPS_CASING.get())
                        .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(1))
                        .or(abilities(OUTPUT_LASER).setExactLimit(1))
                )
                .where("U", blocks(PLASTCRETE.get()))
                .where("G", blocks(ELECTROLYTIC_CELL.get()))
                .where("N", blocks(RHENIUM_REINFORCED_ENERGY_GLASS.get()))
                .where("H", GTLAddPredicates.slabBlock(SlabType.BOTTOM, Blocks.POLISHED_DEEPSLATE_SLAB))
                .where("O", blocks(Blocks.POLISHED_DEEPSLATE_WALL))
                .where("P", blocks(CASING_EXTREME_ENGINE_INTAKE.get()))
                .where("R", blocks(FUSION_GLASS.get()))
                .where("X", blocks(SUPERCONDUCTING_COIL.get()))
                .where("M", GTLPredicates.tierCasings(BlockMap.scMap, "SCTier"))
                .where("B", blocks(ENHANCE_HYPER_MECHANICAL_CASING.get()))
                .where("W", blocks(ChemicalHelper.getBlock(frameGt, NaquadahAlloy)))
                .where("C", blocks(HIGH_POWER_CASING.get()))
                .where("Q", GTLAddPredicates.heatingCoils(14400))
                .where("T", blocks(AEBlocks.QUARTZ_VIBRANT_GLASS.block()))
                .where("D", blocks(HYPER_MECHANICAL_CASING.get()))
                .where("F", blocks(ChemicalHelper.getBlock(frameGt, Mithril)))
                .where("J", blocks(OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING.get()))
                .where("L", blocks(Blocks.IRON_TRAPDOOR))
                .where("K", blocks(HEAT_VENT.get()))
                .where("E", blocks(MOLECULAR_CASING.get()))
                .where("V", blocks(CASING_PTFE_INERT.get()))
                .where("S", blocks(HSSS_REINFORCED_BOROSILICATE_GLASS.get()))
                .where("[", blocks(Blocks.BEACON))
                .where("a", blocks(CLEANROOM_GLASS.get()))
                .where("Y", blocks(Blocks.IRON_BLOCK))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/sps_casing"),
            GTCEu.id("block/multiblock/gcym/large_assembler")
        )
        .register()

    val FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL: MultiblockMachineDefinition = REGISTRATE.multiblock("floating_light_deep_space_industrial_vessel", ::FloatingLightController)
        .nonYAxisRotation()
        .tooltipTextKey("gtceu.machine.hold_g.tooltip.1".toComponent)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .appearanceBlock(DIMENSIONALLY_TRANSCENDENT_CASING)
        .pattern {
            MultiBlockStructureB.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_STRUCTURE
                .where("Y", controller(blocks(it.get())))
                .where(
                    "X",
                    blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get())
                        .or(diffAbilities(listOf(IMPORT_FLUIDS), listOf(IMPORT_ITEMS)).setMaxGlobalLimited(1))
                        .or(abilities(INPUT_ENERGY).setExactLimit(1))
                )
                .where("]", blocks("kubejs:neutronium_gearbox".getBlock))
                .where("D", blocks(Blocks.SEA_LANTERN))
                .where("F", blocks(HEAT_VENT.get()))
                .where("R", fluids("kubejs:gelid_cryotheum".getFluid))
                .where("K", blocks(ChemicalHelper.getBlock(frameGt, Vibranium)))
                .where("c", blocks(Blocks.BLUE_STAINED_GLASS))
                .where("e", blocks("kubejs:neutronium_pipe_casing".getBlock))
                .where("T", blocks(Blocks.POLISHED_DEEPSLATE_WALL))
                .where("U", GTLAddPredicates.slabBlock(SlabType.BOTTOM, Blocks.POLISHED_DEEPSLATE_SLAB))
                .where("H", blocks("kubejs:dimensional_bridge_casing".getBlock))
                .where("Z", blocks(Blocks.LODESTONE))
                .where("b", blocks(SUPER_COOLER_COMPONENT.get()))
                .where("E", blocks(FUSION_GLASS.get()))
                .where("M", blocks("kubejs:accelerated_pipeline".getBlock))
                .where("^", blocks(BATTERY_ULTIMATE_UHV.get()))
                .where("C", blocks(MOLECULAR_CASING.get()))
                .where("W", blocks(ChemicalHelper.getBlock(frameGt, Neutronium)))
                .where("P", blocks(LARGE_METAL_SHEETS[DyeColor.LIGHT_BLUE]!!.get()))
                .where("`", fluids("minecraft:lava".getFluid))
                .where("_", blocks(ULTIMATE_STELLAR_CONTAINMENT_CASING.get()))
                .where("J", blocks(HYPER_CORE.get()))
                .where("N", blocks(ChemicalHelper.getBlock(frameGt, Mithril)))
                .where("G", blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                .where("Q", blocks(AEBlocks.QUARTZ_VIBRANT_GLASS.block()))
                .where("[", blocks(Blocks.LECTERN))
                .where("L", blocks(HIGH_POWER_CASING.get()))
                .where("S", blocks(FILTER_CASING_LAW.get()))
                .where("I", blocks(SPS_CASING.get()))
                .where("O", blocks(ENHANCE_HYPER_MECHANICAL_CASING.get()))
                .where("V", blocks(IRIDIUM_CASING.get()))
                .where("A", blocks("minecraft:cyan_stained_glass".getBlock))
                .where("a", blocks(OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING.get()))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/dimensionally_transcendent_casing"),
            GTCEu.id("block/multiblock/cosmos_simulation")
        )
        .register()

    val FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_1: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "floating_light_deep_space_industrial_vessel_module_1",
        Function {
            return@Function object : FloatingLightModule(it) {
                override val multiRecipeTypes: Array<GTRecipeType> =
                    arrayOf(
                        LATHE_RECIPES,
                        BENDER_RECIPES,
                        COMPRESSOR_RECIPES,
                        FORGE_HAMMER_RECIPES,
                        WIREMILL_RECIPES,
                        POLARIZER_RECIPES,
                        GTLAddRecipesTypes.SuperFactoryMk1Type_1
                    )
            }
        }
    )
        .allRotation()
        .tooltipTextKey("tooltip.gtladditions.floating_light_deep_space_industrial_vessel_module".toComponent)
        .tooltipTextMaxParallels(Int.MAX_VALUE)
        .tooltipTextLaser()
        .tooltipTextMultiRecipes()
        .tooltipTextMultiRecipeTypes()
        .tooltipTextRecipeTypes(LATHE_RECIPES, BENDER_RECIPES, COMPRESSOR_RECIPES, FORGE_HAMMER_RECIPES, WIREMILL_RECIPES, POLARIZER_RECIPES)
        .tooltipTextMultiRecipeType(EXTRUDER_RECIPES, CUTTER_RECIPES, MIXER_RECIPES, FORMING_PRESS_RECIPES)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(LATHE_RECIPES)
        .recipeType(BENDER_RECIPES)
        .recipeType(COMPRESSOR_RECIPES)
        .recipeType(FORGE_HAMMER_RECIPES)
        .recipeType(CUTTER_RECIPES)
        .recipeType(EXTRUDER_RECIPES)
        .recipeType(MIXER_RECIPES)
        .recipeType(WIREMILL_RECIPES)
        .recipeType(FORMING_PRESS_RECIPES)
        .recipeType(POLARIZER_RECIPES)
        .appearanceBlock(DIMENSIONALLY_TRANSCENDENT_CASING)
        .pattern {
            MultiBlockStructureB.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_STRUCTURE
                .where("D", controller(blocks(it.get())))
                .where(
                    "E",
                    blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get())
                        .or(abilities(MAINTENANCE).setExactLimit(1))
                        .or(autoAbilities(*it.recipeTypes))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(1))
                )
                .where("C", blocks(ChemicalHelper.getBlock(frameGt, Vibranium)))
                .where("B", blocks(MOLECULAR_CASING.get()))
                .where("A", blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/dimensionally_transcendent_casing"),
            GTCEu.id("block/multiblock/fusion_reactor")
        )
        .register()

    val FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_2: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "floating_light_deep_space_industrial_vessel_module_2",
        Function {
            return@Function object : FloatingLightModule(it) {
                override val multiRecipeTypes: Array<GTRecipeType> =
                    arrayOf(
                        ROCK_BREAKER_RECIPES,
                        ORE_WASHER_RECIPES,
                        MACERATOR_RECIPES,
                        GTLAddRecipesTypes.SuperFactoryMk2Type_1,
                        GTLAddRecipesTypes.SuperFactoryMk2Type_2,
                        GTLAddRecipesTypes.SuperFactoryMk2Type_3
                    )
            }
        }
    )
        .allRotation()
        .tooltipTextKey("tooltip.gtladditions.floating_light_deep_space_industrial_vessel_module".toComponent)
        .tooltipTextMaxParallels(Int.MAX_VALUE)
        .tooltipTextLaser()
        .tooltipTextMultiRecipes()
        .tooltipTextMultiRecipeTypes()
        .tooltipTextRecipeTypes(ROCK_BREAKER_RECIPES, ORE_WASHER_RECIPES, MACERATOR_RECIPES)
        .tooltipTextMultiRecipeType(CENTRIFUGE_RECIPES, THERMAL_CENTRIFUGE_RECIPES)
        .tooltipTextMultiRecipeType(ELECTROLYZER_RECIPES, ELECTROMAGNETIC_SEPARATOR_RECIPES)
        .tooltipTextMultiRecipeType(SIFTER_RECIPES, DEHYDRATOR_RECIPES)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(ROCK_BREAKER_RECIPES)
        .recipeType(ORE_WASHER_RECIPES)
        .recipeType(CENTRIFUGE_RECIPES)
        .recipeType(ELECTROLYZER_RECIPES)
        .recipeType(SIFTER_RECIPES)
        .recipeType(MACERATOR_RECIPES)
        .recipeType(DEHYDRATOR_RECIPES)
        .recipeType(THERMAL_CENTRIFUGE_RECIPES)
        .recipeType(ELECTROMAGNETIC_SEPARATOR_RECIPES)
        .appearanceBlock(DIMENSIONALLY_TRANSCENDENT_CASING)
        .pattern {
            MultiBlockStructureB.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_STRUCTURE
                .where("D", controller(blocks(it.get())))
                .where(
                    "E",
                    blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get())
                        .or(abilities(MAINTENANCE).setExactLimit(1))
                        .or(autoAbilities(*it.recipeTypes))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(1))
                )
                .where("C", blocks(ChemicalHelper.getBlock(frameGt, Vibranium)))
                .where("B", blocks(MOLECULAR_CASING.get()))
                .where("A", blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/dimensionally_transcendent_casing"),
            GTCEu.id("block/multiblock/fusion_reactor")
        )
        .register()

    val FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_3: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "floating_light_deep_space_industrial_vessel_module_3",
        Function {
            return@Function object : FloatingLightModule(it) {
                override val multiRecipeTypes: Array<GTRecipeType> =
                    arrayOf(
                        EVAPORATION_RECIPES, AUTOCLAVE_RECIPES, BREWING_RECIPES, FERMENTING_RECIPES, DISTILLERY_RECIPES, DISTILLATION_RECIPES,
                        FLUID_HEATER_RECIPES, CHEMICAL_BATH_RECIPES, GTLAddRecipesTypes.SuperFactoryMk3Type_1
                    )
            }
        }
    )
        .allRotation()
        .tooltipTextKey("tooltip.gtladditions.floating_light_deep_space_industrial_vessel_module".toComponent)
        .tooltipTextMaxParallels(Int.MAX_VALUE)
        .tooltipTextLaser()
        .tooltipTextMultiRecipes()
        .tooltipTextMultiRecipeTypes()
        .tooltipTextRecipeTypes(
            EVAPORATION_RECIPES,
            AUTOCLAVE_RECIPES,
            BREWING_RECIPES,
            FERMENTING_RECIPES,
            DISTILLERY_RECIPES,
            DISTILLATION_RECIPES,
            FLUID_HEATER_RECIPES,
            CHEMICAL_BATH_RECIPES
        )
        .tooltipTextMultiRecipeType(FLUID_SOLIDFICATION_RECIPES, EXTRACTOR_RECIPES)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(EVAPORATION_RECIPES)
        .recipeType(AUTOCLAVE_RECIPES)
        .recipeType(EXTRACTOR_RECIPES)
        .recipeType(BREWING_RECIPES)
        .recipeType(FERMENTING_RECIPES)
        .recipeType(DISTILLERY_RECIPES)
        .recipeType(DISTILLATION_RECIPES)
        .recipeType(FLUID_HEATER_RECIPES)
        .recipeType(FLUID_SOLIDFICATION_RECIPES)
        .recipeType(CHEMICAL_BATH_RECIPES)
        .appearanceBlock(DIMENSIONALLY_TRANSCENDENT_CASING)
        .pattern {
            MultiBlockStructureB.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_STRUCTURE
                .where("D", controller(blocks(it.get())))
                .where(
                    "E",
                    blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get())
                        .or(abilities(MAINTENANCE).setExactLimit(1))
                        .or(autoAbilities(*it.recipeTypes))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(1))
                )
                .where("C", blocks(ChemicalHelper.getBlock(frameGt, Vibranium)))
                .where("B", blocks(MOLECULAR_CASING.get()))
                .where("A", blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/dimensionally_transcendent_casing"),
            GTCEu.id("block/multiblock/fusion_reactor")
        )
        .register()

    val FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_4: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "floating_light_deep_space_industrial_vessel_module_4",
        Function {
            return@Function object : FloatingLightModule(it) {
                override val multiRecipeTypes: Array<GTRecipeType> =
                    arrayOf(ASSEMBLER_RECIPES, GTLAddRecipesTypes.SuperFactoryMk4Type_1, GTLAddRecipesTypes.SuperFactoryMk4Type_2)
            }
        }
    )
        .allRotation()
        .tooltipTextKey("tooltip.gtladditions.floating_light_deep_space_industrial_vessel_module".toComponent)
        .tooltipTextMaxParallels(Int.MAX_VALUE)
        .tooltipTextLaser()
        .tooltipTextMultiRecipes()
        .tooltipTextMultiRecipeTypes()
        .tooltipTextRecipeTypes(ASSEMBLER_RECIPES)
        .tooltipTextMultiRecipeType(PRECISION_ASSEMBLER_RECIPES, CIRCUIT_ASSEMBLER_RECIPES)
        .tooltipTextMultiRecipeType(ARC_FURNACE_RECIPES, CANNER_RECIPES, LIGHTNING_PROCESSOR_RECIPES)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(CANNER_RECIPES)
        .recipeType(ARC_FURNACE_RECIPES)
        .recipeType(LIGHTNING_PROCESSOR_RECIPES)
        .recipeType(ASSEMBLER_RECIPES)
        .recipeType(PRECISION_ASSEMBLER_RECIPES)
        .recipeType(CIRCUIT_ASSEMBLER_RECIPES)
        .appearanceBlock(DIMENSIONALLY_TRANSCENDENT_CASING)
        .pattern {
            MultiBlockStructureB.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_STRUCTURE
                .where("D", controller(blocks(it.get())))
                .where(
                    "E",
                    blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get())
                        .or(abilities(MAINTENANCE).setExactLimit(1))
                        .or(autoAbilities(*it.recipeTypes))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(1))
                )
                .where("C", blocks(ChemicalHelper.getBlock(frameGt, Vibranium)))
                .where("B", blocks(MOLECULAR_CASING.get()))
                .where("A", blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/dimensionally_transcendent_casing"),
            GTCEu.id("block/multiblock/fusion_reactor")
        )
        .register()

    val FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_5: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "floating_light_deep_space_industrial_vessel_module_5",
        Function {
            return@Function object : FloatingLightModule(it) {
                override val multiRecipeTypes: Array<GTRecipeType> =
                    arrayOf(GTLAddRecipesTypes.ONTOKINETIC)
            }
        }
    )
        .allRotation()
        .tooltipTextKey("tooltip.gtladditions.floating_light_deep_space_industrial_vessel_module".toComponent)
        .tooltipTextMaxParallels(Int.MAX_VALUE)
        .tooltipTextLaser()
        .tooltipTextMultiRecipes()
        .tooltipTextRecipeTypes(GTLAddRecipesTypes.ONTOKINETIC)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(GTLAddRecipesTypes.ONTOKINETIC)
        .appearanceBlock(DIMENSIONALLY_TRANSCENDENT_CASING)
        .pattern {
            MultiBlockStructureB.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_STRUCTURE
                .where("D", controller(blocks(it.get())))
                .where(
                    "E",
                    blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get())
                        .or(abilities(MAINTENANCE).setExactLimit(1))
                        .or(autoAbilities(*it.recipeTypes))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(1))
                )
                .where("C", blocks(ChemicalHelper.getBlock(frameGt, Vibranium)))
                .where("B", blocks(MOLECULAR_CASING.get()))
                .where("A", blocks(DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/dimensionally_transcendent_casing"),
            GTCEu.id("block/multiblock/fusion_reactor")
        )
        .register()

    val PRIMORDIAL_EVOLUTION_NEXUS: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "primordial_evolution_nexus",
        Function {
            return@Function object : GTLAddCoilWorkableElectricMultipleRecipesTypesMultiblockMachine(it) {
                override val multiRecipeTypes = arrayOf(GTLAddRecipesTypes.EvolutionOfPrimordial)
            }
        }
    )
        .nonYAxisRotation()
        .tooltipTextCoilParallel()
        .tooltipTextLaser()
        .tooltipTextMultiRecipes()
        .tooltipTextRecipeTypes(GTLAddRecipesTypes.EvolutionOfPrimordial)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(GTLAddRecipesTypes.EvolutionOfPrimordial)
        .appearanceBlock(HIGH_POWER_CASING)
        .pattern {
            MultiBlockStructureB.PRIMORDIAL_EVOLUTION_NEXUS_STRUCTURE
                .where("H", controller(blocks(it.get())))
                .where("J", blocks(CASING_ALUMINIUM_FROSTPROOF.get()))
                .where("M", blocks("kubejs:laser_cooling_casing".getBlock))
                .where("P", blocks(CLEANROOM_GLASS.get()))
                .where("[", blocks(ChemicalHelper.getBlock(frameGt, StainlessSteel)))
                .where(
                    "F",
                    blocks(HIGH_POWER_CASING.get()).or(abilities(EXPORT_ITEMS).setPreviewCount(1))
                        .or(abilities(IMPORT_ITEMS).setPreviewCount(1))
                        .or(abilities(EXPORT_FLUIDS).setPreviewCount(1))
                        .or(abilities(IMPORT_FLUIDS).setPreviewCount(1))
                        .or(abilities(INPUT_ENERGY).setMaxGlobalLimited(2))
                        .or(abilities(INPUT_LASER).setMaxGlobalLimited(1))
                )
                .where("A", blocks("kubejs:high_strength_concrete".getBlock))
                .where("G", blocks(CASING_STAINLESS_CLEAN.get()))
                .where("`", blocks(CASING_ALUMINIUM_FROSTPROOF.get()))
                .where("L", blocks("kubejs:gelid_cryotheum".getBlock))
                .where("+", blocks(CRUSHING_WHEELS.get()))
                .where("_", blocks(ChemicalHelper.getBlock(frameGt, Tungsten)))
                .where("B", blocks(MOLECULAR_CASING.get()))
                .where("W", blocks("gtceu:ptfe_pipe_casing".getBlock))
                .where("Q", blocks(ChemicalHelper.getBlock(block, RedAlloy)))
                .where("U", blocks(FUSION_GLASS.get()))
                .where("V", heatingCoils())
                .where("T", blocks("kubejs:red_steel_casing".getBlock))
                .where("R", blocks(ChemicalHelper.getBlock(block, BlueAlloy)))
                .where("]", blocks(CASING_WATERTIGHT.get()))
                .where("S", blocks(CASING_TUNGSTENSTEEL_PIPE.get()))
                .where("K", blocks(HEAT_VENT.get()))
                .where("I", blocks("kubejs:neutronium_pipe_casing".getBlock))
                .where("Z", blocks(CASING_SECURE_MACERATION.get()))
                .where("D", blocks(CASING_HIGH_TEMPERATURE_SMELTING.get()))
                .where("^", blocks(CASING_CORROSION_PROOF.get()))
                .where("N", blocks(GTMachines.MUFFLER_HATCH[10].get()))
                .where("O", blocks(CASING_VIBRATION_SAFE.get()))
                .where("Y", blocks(FILTER_CASING_STERILE.get()))
                .where("C", blocks(ANTIFREEZE_HEATPROOF_MACHINE_CASING.get()))
                .where("a", blocks(CASING_INVAR_HEATPROOF.get()))
                .where("E", blocks(CASING_EXTREME_ENGINE_INTAKE.get()))
                .where("c", blocks(CASING_STAINLESS_EVAPORATION.get()))
                .where("X", blocks(CASING_PTFE_INERT.get()))
                .where("b", blocks(ChemicalHelper.getBlock(frameGt, Ruridit)))
                .build()
        }
        .workableCasingRenderer(
            GTCEu.id("block/casings/hpca/high_power_casing"),
            GTCEu.id("block/multiblock/gcym/large_assembler")
        )
        .register()

    val BIOSPHERE_III: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "biosphere_iii",
        ::BiosphereIIIController
    )
        .nonYAxisRotation()
        .tooltipTextKey("gtceu.machine.hold_g.tooltip.1".toComponent)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .appearanceBlock(SPS_CASING)
        .pattern {
            MultiBlockStructureB.BIOSPHERE_III_STRUCTURE
                .where("|", controller(blocks(it.get())))
                .where(
                    "{",
                    blocks(SPS_CASING.get())
                        .or(abilities(IMPORT_ITEMS).setExactLimit(1))
                        .or(abilities(INPUT_ENERGY).setExactLimit(1))
                )
                .where("b", blocks("gtceu:computer_casing".getBlock))
                .where("g", blocks("kubejs:neutronium_pipe_casing".getBlock))
                .where("r", blocks("kubejs:molecular_coil".getBlock))
                .where("G", blocks("minecraft:mud".getBlock))
                .where("B", blocks("gtceu:naquadria_frame".getBlock))
                .where("*", blocks("gtceu:sterilizing_filter_casing".getBlock))
                .where("<", blocks("minecraft:diorite".getBlock))
                .where("K", blocks("minecraft:gravel".getBlock))
                .where("m", heatingCoils())
                .where("E", blocks("gtlcore:rhenium_reinforced_energy_glass".getBlock))
                .where("M", blocks("minecraft:mangrove_leaves".getBlock))
                .where("T", blocks("minecraft:oak_leaves".getBlock))
                .where("i", blocks("gtlcore:super_computation_component".getBlock))
                .where("l", blocks("gtlcore:antifreeze_heatproof_machine_casing".getBlock))
                .where("~", blocks("minecraft:water".getBlock).or(blocks("minecraft:bubble_column".getBlock)))
                .where("L", blocks("minecraft:water".getBlock))
                .where("R", blocks("minecraft:packed_mud".getBlock))
                .where("S", blocks("kubejs:magic_core".getBlock))
                .where("y", blocks("minecraft:red_stained_glass_pane".getBlock))
                .where("#", blocks("minecraft:coarse_dirt".getBlock))
                .where(";", blocks("minecraft:deepslate".getBlock))
                .where("H", blocks("minecraft:dirt".getBlock))
                .where("P", blocks("minecraft:mangrove_log".getBlock))
                .where("Y", blocks("minecraft:acacia_log".getBlock))
                .where("t", blocks("gtlcore:hyper_core".getBlock))
                .where("`", blocks("gtceu:high_power_casing".getBlock))
                .where("W", blocks("minecraft:sea_lantern".getBlock))
                .where("x", blocks("minecraft:green_stained_glass_pane".getBlock))
                .where("&", blocks("gtceu:cleanroom_glass".getBlock))
                .where("!", blocks("kubejs:speeding_pipe".getBlock))
                .where("f", blocks("gtceu:advanced_computer_casing".getBlock))
                .where("c", blocks("gtlcore:oxidation_resistant_hastelloy_n_mechanical_casing".getBlock))
                .where("=", blocks("gtlcore:iridium_casing".getBlock))
                .where("j", blocks("gtlcore:enhance_hyper_mechanical_casing".getBlock))
                .where(">", blocks("minecraft:granite".getBlock))
                .where("k", blocks("kubejs:dimensional_bridge_casing".getBlock))
                .where("o", blocks("gtlcore:dimensionally_transcendent_casing".getBlock))
                .where("O", blocks("minecraft:sweet_berry_bush".getBlock))
                .where("@", blocks("minecraft:yellow_stained_glass_pane".getBlock))
                .where(".", blocks("gtlcore:degenerate_rhenium_constrained_casing".getBlock))
                .where("z", blocks("kubejs:containment_field_generator".getBlock))
                .where("}", blocks("minecraft:soul_sand".getBlock))
                .where("^", blocks("minecraft:spruce_leaves".getBlock))
                .where("X", blocks("minecraft:oak_log".getBlock))
                .where("J", blocks("minecraft:sand".getBlock))
                .where("N", blocks("minecraft:mangrove_roots".getBlock))
                .where("-", blocks("minecraft:sponge".getBlock))
                .where("e", blocks("kubejs:gelid_cryotheum".getBlock))
                .where("Q", blocks("minecraft:grass_block".getBlock))
                .where("D", blocks("gtceu:fusion_glass".getBlock))
                .where("V", blocks("kubejs:hollow_casing".getBlock))
                .where("C", blocks("kubejs:high_strength_concrete".getBlock))
                .where("h", blocks("gtlcore:sps_casing".getBlock))
                .where("a", blocks("gtceu:computer_heat_vent".getBlock))
                .where("d", blocks("gtlcore:super_cooler_component".getBlock))
                .where("n", blocks("gtlcore:molecular_casing".getBlock))
                .where("]", blocks("minecraft:cactus".getBlock))
                .where("%", blocks("gtceu:filter_casing".getBlock))
                .where("I", blocks("gtlcore:law_filter_casing".getBlock))
                .where("v", blocks("gtceu:uhv_ultimate_battery".getBlock))
                .where("_", blocks("minecraft:spruce_log".getBlock))
                .where("[", blocks("minecraft:clay".getBlock))
                .where("?", blocks("minecraft:lava".getBlock))
                .where("u", blocks("kubejs:force_field_glass".getBlock))
                .where("F", blocks("gtceu:inert_machine_casing".getBlock))
                .where("q", blocks("kubejs:restraint_device".getBlock))
                .where("A", blocks("gtceu:plascrete".getBlock))
                .where("U", blocks("minecraft:acacia_leaves".getBlock))
                .where(":", blocks("minecraft:andesite".getBlock))
                .where("+", blocks("gtlcore:hyper_mechanical_casing".getBlock))
                .where("p", blocks("gtceu:atomic_casing".getBlock))
                .where("s", blocks("kubejs:aggregatione_core".getBlock))
                .where("w", blocks("minecraft:blue_stained_glass_pane".getBlock))
                .build()
        }
        .workableCasingRenderer(
            GTLCore.id("block/casings/sps_casing"),
            GTCEu.id("block/multiblock/implosion_compressor")
        )
        .register()

    val BIOSPHERE_III_MODULE: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "garden_of_hermes",
        Function {
            return@Function object : BiosphereIIIModule(it) {
                override val multiRecipeTypes: Array<GTRecipeType> = arrayOf(BiosphereIIIType)
            }
        }
    )
        .nonYAxisRotation()
        .tooltipTextKey(
            "gtceu.multiblock.biosphereiii.1".toComponent,
            "gtceu.multiblock.biosphereiii.0".toComponent
        )
        .tooltipTextMultiRecipes()
        .tooltipTextMultiRecipeTypes()
        .tooltipTextMultiRecipeType(GREENHOUSE_RECIPES, FISHING_GROUND_RECIPES)
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .recipeType(GREENHOUSE_RECIPES)
        .recipeType(FISHING_GROUND_RECIPES)
        .appearanceBlock(HIGH_POWER_CASING)
        .pattern {
            MultiBlockStructureB.BIOSPHERE_III_MODULE_STRUCTURE
                .where(
                    "A",
                    blocks(HIGH_POWER_CASING.get()).or(abilities(EXPORT_ITEMS).setPreviewCount(1))
                        .or(abilities(IMPORT_ITEMS).setPreviewCount(1))
                        .or(abilities(EXPORT_FLUIDS).setPreviewCount(1))
                        .or(abilities(IMPORT_FLUIDS).setPreviewCount(1))
                        .or(abilities(INPUT_ENERGY).setMaxGlobalLimited(2))
                        .or(ability(INPUT_LASER, 11, 12, 13, 14).setMaxGlobalLimited(1))
                        .or(abilities(MAINTENANCE).setExactLimit(1))
                )
                .where("B", controller(blocks(it.get())))
                .where("C", blocks("kubejs:high_strength_concrete".getBlock))
                .build()
        }
        .workableCasingRenderer(
            GTCEu.id("block/casings/hpca/high_power_casing"),
            GTCEu.id("block/multiblock/implosion_compressor")
        )
        .register()

    val SPACE_ELEVATOR_MKII: MultiblockMachineDefinition = REGISTRATE.multiblock(
        "space_elevator_mkii",
        ::SpaceElevatorMKII
    )
        .nonYAxisRotation()
        .allowExtendedFacing(false)
        .recipeType(SPACE_ELEVATOR_RECIPES)
        .tooltips(Component.translatable("gtceu.multiblock.space_elevator_mkii.0"))
        .tooltips(Component.translatable("gtceu.machine.space_elevator.tooltip.1"))
        .tooltips(Component.translatable("gtceu.multiblock.space_elevator_mkii.1"))
        .tooltips(Component.translatable("gtceu.machine.space_elevator.tooltip.2"))
        .tooltips(
            Component.translatable(
                "gtceu.machine.available_recipe_map_1.tooltip",
                Component.translatable("gtceu.space_elevator")
            )
        )
        .tooltipBuilder(GTLAddMachines.GTLAdd_ADD)
        .appearanceBlock(SPACE_ELEVATOR_MECHANICAL_CASING)
        .recipeModifier { machine, recipe, _, _ ->
            (machine as WorkableElectricMultiblockMachine).let {
                return@recipeModifier FastRecipeModify.modify(
                    it,
                    recipe,
                    1,
                    ocResult = FastRecipeModify.OverClockFactor(1.0, 4.0)
                ) { FastRecipeModify.getDefaultReduce() }
            }
        }
        .pattern {
            MultiBlockStructureB.SPACE_ELEVATOR_MKII_STRUCTURE
                .where("P", controller(blocks(it.get())))
                .where(
                    "L",
                    blocks(SPACE_ELEVATOR_MECHANICAL_CASING.get())
                        .or(abilities(INPUT_ENERGY).setExactLimit(1))
                        .or(abilities(IMPORT_ITEMS).setExactLimit(1))
                        .or(abilities(COMPUTATION_DATA_RECEPTION).setExactLimit(1))
                        .or(abilities(MAINTENANCE).setExactLimit(1))
                )
                .where("A", blocks(SPACE_ELEVATOR_SUPPORT.get()))
                .where("G", blocks(ChemicalHelper.getBlock(frameGt, Neutronium)))
                .where("E", blocks("kubejs:space_elevator_internal_support".getBlock))
                .where("M", GTLPredicates.tierCasings(BlockMap.sepmMap, "SEPMTier"))
                .where("B", blocks("kubejs:high_strength_concrete".getBlock))
                .where("D", blocks(SPACE_ELEVATOR_MECHANICAL_CASING.get()))
                .where("N", blocks(POWER_CORE.get()))
                .where("I", blocks("kubejs:module_base".getBlock))
                .where(
                    "J",
                    any()
                        .or(blocks("kubejs:module_connector".getBlock)).setPreviewCount(1)
                )
                .where("K", blocks("ad_astra:glowing_iron_pillar".getBlock))
                .where("C", blocks("ad_astra:aeronos_cap".getBlock))
                .where("H", blocks("ad_astra:marked_iron_pillar".getBlock))
                .where("O", air())
                .build()
        }
        .renderer(::SpaceElevatorMKIIRenderer)
        .hasTESR(true)
        .register()

    @JvmStatic
    fun init() {}
}
