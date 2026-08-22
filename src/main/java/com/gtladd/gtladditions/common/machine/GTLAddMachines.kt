package com.gtladd.gtladditions.common.machine

import org.gtlcore.gtlcore.utils.TextUtil

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.client.renderer.machine.OverlaySteamMachineRenderer
import com.gregtechceu.gtceu.client.util.TooltipHelper
import com.gregtechceu.gtceu.common.data.GTCompassSections
import com.gregtechceu.gtceu.common.data.GTMachines
import com.gregtechceu.gtceu.utils.FormattingUtil

import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

import com.gtladd.gtladditions.api.registry.GTLAddRegistration.Companion.REGISTRATE
import com.gtladd.gtladditions.common.data.GTLAddCreativeModeTabs
import com.gtladd.gtladditions.common.data.MultiBlockModify
import com.gtladd.gtladditions.common.machine.hatch.*
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine
import com.gtladd.gtladditions.utils.ComponentUtil.literal
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import com.gtladd.gtladditions.utils.ComponentUtil.translatable
import com.hepdd.gtmthings.common.registry.GTMTRegistration
import com.hepdd.gtmthings.data.CreativeModeTabs
import com.hepdd.gtmthings.data.CustomMachines
import com.hepdd.gtmthings.data.WirelessMachines

import java.util.function.BiConsumer

object GTLAddMachines {
    val WIRELL_ENERGY_HIGH_TIERS: IntArray = GTValues.tiersBetween(5, 14)

    @JvmField
    val HUGE_STEAM_HATCH: MachineDefinition
    val SUPER_INPUT_DUAL_HATCH: MachineDefinition
    val ORE_PROCESSOR_HATCH: MachineDefinition
    val ME_BLOCK_CONVERSATION: MachineDefinition
    val VIENTIANE_TRANSCEIPTION_NODE: MachineDefinition
    val CLOUD_COMPUTATION_HATCH_TRANSMITTER: MachineDefinition
    val CLOUD_COMPUTATION_HATCH_RECEIVER: MachineDefinition
    val CLOUD_COMPUTATION_MONITOR: MachineDefinition
    val CLOUD_DATA_MACHINE: MachineDefinition
    val CLOUD_DATA_HATCH: MachineDefinition

    @JvmField
    val HUGE_OUTPUT_DUAL_HATCH: Array<MachineDefinition> = CustomMachines.registerTieredMachines(
        "huge_output_dual_hatch",
        { holder, tier -> HugeOutputDualHatch(holder, tier, IO.OUT) },
        { tier, builder ->
            builder.langValue(GTValues.VNF[tier] + " Huge Output Dual Hatch")
                .rotationState(RotationState.ALL)
                .overlayTieredHullRenderer("huge_dual_hatch.import")
                .abilities(*GTMachines.DUAL_OUTPUT_HATCH_ABILITIES)
                .compassNode("huge_dual_hatch")
                .tooltips(
                    "gtceu.machine.dual_hatch.export.tooltip".toComponent,
                    Component.translatable("gtceu.universal.tooltip.item_storage_capacity", (1 + tier) * 2 - 1),
                    Component.translatable(
                        "gtceu.universal.tooltip.fluid_storage_capacity_mult",
                        tier,
                        FormattingUtil.formatNumbers(0x7fffffff)
                    )
                )
                .tooltipBuilder(GTLAdd_ADD)
                .register()
        },
        *GTValues.tiersBetween(1, 13)
    )
    val LASER_INPUT_HATCH_16777216A: Array<MachineDefinition> = GTMachines.registerLaserHatch(IO.IN, 16777216, PartAbility.INPUT_LASER)
    val LASER_OUTPUT_HATCH_16777216A: Array<MachineDefinition> = GTMachines.registerLaserHatch(IO.OUT, 16777216, PartAbility.OUTPUT_LASER)
    val LASER_INPUT_HATCH_67108863A: Array<MachineDefinition> = GTMachines.registerLaserHatch(IO.IN, 67108863, PartAbility.INPUT_LASER)
    val LASER_OUTPUT_HATCH_67108863A: Array<MachineDefinition> = GTMachines.registerLaserHatch(IO.OUT, 67108863, PartAbility.OUTPUT_LASER)
    val WIRELESS_LASER_INPUT_HATCH_16777216A: Array<MachineDefinition>
    val WIRELESS_LASER_OUTPUT_HATCH_16777216A: Array<MachineDefinition>
    val WIRELESS_LASER_INPUT_HATCH_67108864A: Array<MachineDefinition>
    val WIRELESS_LASER_OUTPUT_HATCH_67108863A: Array<MachineDefinition>

    @JvmStatic
    fun init() {
        MultiBlockMachine.init()
        MultiBlockModify.init()
    }

    val GTLAdd_ADD = BiConsumer { _: ItemStack, components: MutableList<Component> ->
        components.add(TextUtil.full_color("gui.gtladditions.add".translatable).literal.withStyle { it.withColor(TooltipHelper.RAINBOW.current) })
    }

    init {
        GTMTRegistration.GTMTHINGS_REGISTRATE.creativeModeTab { CreativeModeTabs.WIRELESS_TAB }
        WIRELESS_LASER_INPUT_HATCH_16777216A = WirelessMachines.registerWirelessLaserHatch(
            IO.IN,
            16777216,
            PartAbility.INPUT_LASER,
            WIRELL_ENERGY_HIGH_TIERS
        )
        WIRELESS_LASER_OUTPUT_HATCH_16777216A = WirelessMachines.registerWirelessLaserHatch(
            IO.OUT,
            16777216,
            PartAbility.OUTPUT_LASER,
            WIRELL_ENERGY_HIGH_TIERS
        )
        WIRELESS_LASER_INPUT_HATCH_67108864A = WirelessMachines.registerWirelessLaserHatch(
            IO.IN,
            67108864,
            PartAbility.INPUT_LASER,
            WIRELL_ENERGY_HIGH_TIERS
        )
        WIRELESS_LASER_OUTPUT_HATCH_67108863A = WirelessMachines.registerWirelessLaserHatch(
            IO.OUT,
            67108863,
            PartAbility.OUTPUT_LASER,
            WIRELL_ENERGY_HIGH_TIERS
        )
        REGISTRATE.creativeModeTab { GTLAddCreativeModeTabs.GTLADD_MACHINE }
        HUGE_STEAM_HATCH = REGISTRATE.machine("huge_steam_input_hatch", ::HugeSteamHatchPartMachine)
            .rotationState(RotationState.ALL).abilities(PartAbility.STEAM)
            .tooltips(
                "gtceu.multiblock.steam_oc_hv".toComponent,
                "gtceu.multiblock.steam_duraiton".toComponent,
                Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", 0x7fffffff),
                "gtceu.machine.steam.steam_hatch.tooltip".toComponent
            )
            .tooltipBuilder(GTLAdd_ADD)
            .compassSections(GTCompassSections.STEAM)
            .compassNode("steam_hatch")
            .renderer { OverlaySteamMachineRenderer(ResourceLocation("gtceu", "block/machine/part/steam_hatch")) }
            .register()

        SUPER_INPUT_DUAL_HATCH = REGISTRATE.machine("super_input_dual_hatch", ::SuperDualHatchPartMachine)
            .rotationState(RotationState.ALL)
            .abilities(*GTMachines.DUAL_INPUT_HATCH_ABILITIES)
            .langValue("Super Input Dual Hatch")
            .overlayTieredHullRenderer("super_input_dual_hatch.import")
            .tooltips(
                Component.translatable("gtceu.universal.tooltip.item_storage_capacity", 37),
                Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity_mult", 24, Long.MAX_VALUE shr 12)
            )
            .tooltipBuilder(GTLAdd_ADD)
            .tier(14)
            .register()

        ORE_PROCESSOR_HATCH = REGISTRATE.machine("spectral_analysis_hatch", ::OreProcessorHatch)
            .rotationState(RotationState.ALL)
            .langValue("Ore Processor Hatch")
            .overlayTieredHullRenderer("op_hatch")
            .tooltips(
                "gtceu.universal.disabled".toComponent,
                "gtceu.machine.hold_g.tooltip.1".toComponent,
                "gtceu.machine.hold_g.tooltip.2".toComponent
            )
            .tooltipBuilder(GTLAdd_ADD)
            .tier(8)
            .register()

        ME_BLOCK_CONVERSATION = REGISTRATE.machine("me_block_conservation", ::MEBlockConversationHatch)
            .rotationState(RotationState.ALL)
            .overlayTieredHullRenderer("me_block_conservation")
            .tooltips(
                "gtceu.universal.disabled".toComponent,
                "gtceu.machine.me.item_import.tooltip".toComponent,
                "gtceu.machine.block_conversation.tooltip.0".toComponent,
                "gtceu.machine.block_conversation.tooltip.1".toComponent,
                "gtceu.machine.hold_g.tooltip.1".toComponent,
                "gtceu.machine.hold_g.tooltip.2".toComponent
            )
            .langValue("Transmutation Bus Hatch")
            .tooltipBuilder(GTLAdd_ADD)
            .tier(11)
            .register()

        VIENTIANE_TRANSCEIPTION_NODE = REGISTRATE.machine("vientiane_transcription_node", ::VientianeTranscriptionNode)
            .rotationState(RotationState.ALL)
            .overlayTieredHullRenderer("vientiane_transcription_node")
            .langValue("Vientiane Transcription Node")
            .tooltipBuilder(GTLAdd_ADD)
            .tier(14)
            .register()

        CLOUD_COMPUTATION_MONITOR = REGISTRATE.machine("cloud_computation_monitor", ::CloudOpticalComputationMonitorMachine)
            .rotationState(RotationState.ALL)
            .overlayTieredHullRenderer("cloud_computation_monitor")
            .langValue("Cloud Computation Monitor")
            .tooltipBuilder(GTLAdd_ADD)
            .tier(11)
            .register()

        CLOUD_COMPUTATION_HATCH_TRANSMITTER = REGISTRATE.machine("cloud_computation_transmitter_hatch") { holder ->
            CloudOpticalComputationHatchMachine(holder, true)
        }
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.COMPUTATION_DATA_TRANSMISSION)
            .overlayTieredHullRenderer("cloud_computation_transmitter_hatch")
            .langValue("Cloud Computation Transmitter Hatch")
            .tooltipBuilder(GTLAdd_ADD)
            .tier(11)
            .register()

        CLOUD_COMPUTATION_HATCH_RECEIVER = REGISTRATE.machine("cloud_computation_receiver_hatch") { holder ->
            CloudOpticalComputationHatchMachine(holder, false)
        }
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.COMPUTATION_DATA_RECEPTION)
            .overlayTieredHullRenderer("cloud_computation_receiver_hatch")
            .langValue("Cloud Computation Receiver Hatch")
            .tooltipBuilder(GTLAdd_ADD)
            .tier(11)
            .register()

        CLOUD_DATA_MACHINE = REGISTRATE.machine("cloud_data_machine", ::CloudOpticalDataMachine)
            .rotationState(RotationState.ALL)
            .overlayTieredHullRenderer("cloud_data_machine")
            .langValue("Cloud Data Machine")
            .tooltipBuilder(GTLAdd_ADD)
            .tier(11)
            .register()

        CLOUD_DATA_HATCH = REGISTRATE.machine("cloud_data_hatch", ::CloudOpticalDataHatchMachine)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.OPTICAL_DATA_RECEPTION)
            .overlayTieredHullRenderer("cloud_data_hatch")
            .langValue("Cloud Data Hatch")
            .tooltipBuilder(GTLAdd_ADD)
            .tier(11)
            .register()
    }
}
