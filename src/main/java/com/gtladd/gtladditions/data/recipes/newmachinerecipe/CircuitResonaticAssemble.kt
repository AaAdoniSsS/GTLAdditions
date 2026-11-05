package com.gtladd.gtladditions.data.recipes.newmachinerecipe

import org.gtlcore.gtlcore.common.data.GTLMaterials.*

import com.gregtechceu.gtceu.common.data.GTMaterials.*
import com.gregtechceu.gtceu.utils.GTUtil

import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.world.item.ItemStack

import com.gtladd.gtladditions.common.recipe.GTLAddRecipesTypes.CIRCUIT_RESONATIC_ASSEMBLE_RECIPES
import com.gtladd.gtladditions.utils.Registries.getItemStack

import java.util.function.Consumer
import kotlin.math.max

object CircuitResonaticAssemble {
    @JvmStatic
    fun init(provider: Consumer<FinishedRecipe>) {
        fun circuit(name: String, eut: Long, duration: Int, vararg inputs: ItemStack) {
            val builder = CIRCUIT_RESONATIC_ASSEMBLE_RECIPES.recipeBuilder("circuit_resonatic_$name")
                .inputItems(*inputs)
                .outputItems("kubejs:circuit_resonatic_$name".getItemStack(5))
                .EUt(eut)
                .duration(duration)

            if (eut < 160000L) {
                builder.inputFluids(SolderingAlloy.getFluid(36))
            } else {
                val am = GTUtil.getFloorTierByVoltage(eut) - 6
                builder.inputFluids(SuperMutatedLivingSolder.getFluid(max(1, 36 * am).toLong()))
            }
            builder.save(provider)
        }

        circuit(
            "ulv", 30L, 75,
            "gtceu:tantalum_capacitor".getItemStack(3),
            "gtceu:resistor".getItemStack(3),
            "gtceu:inductor".getItemStack(3),
            "kubejs:imprinted_resonatic_circuit_board".getItemStack(),
            "gtceu:magneto_resonatic_gem".getItemStack(),
            "gtceu:vacuum_tube".getItemStack()
        )
        circuit(
            "lv", 120L, 135,
            "gtceu:diode".getItemStack(3),
            "gtceu:capacitor".getItemStack(3),
            "gtceu:transistor".getItemStack(3),
            "kubejs:imprinted_resonatic_circuit_board".getItemStack(),
            "gtceu:magneto_resonatic_gem".getItemStack(),
            "kubejs:circuit_resonatic_ulv".getItemStack()
        )
        circuit(
            "mv", 480L, 225,
            "gtceu:diode".getItemStack(6),
            "gtceu:capacitor".getItemStack(6),
            "gtceu:transistor".getItemStack(6),
            "kubejs:imprinted_resonatic_circuit_board".getItemStack(),
            "gtceu:magneto_resonatic_gem".getItemStack(),
            "kubejs:circuit_resonatic_lv".getItemStack()
        )
        circuit(
            "hv", 1920L, 345,
            "gtceu:smd_diode".getItemStack(3),
            "gtceu:smd_capacitor".getItemStack(3),
            "gtceu:smd_transistor".getItemStack(3),
            "kubejs:imprinted_resonatic_circuit_board".getItemStack(2),
            "gtceu:flawless_magneto_resonatic_gem".getItemStack(),
            "kubejs:circuit_resonatic_mv".getItemStack()
        )
        circuit(
            "ev", 7680L, 495,
            "gtceu:smd_diode".getItemStack(6),
            "gtceu:smd_capacitor".getItemStack(6),
            "gtceu:smd_transistor".getItemStack(6),
            "kubejs:imprinted_resonatic_circuit_board".getItemStack(4),
            "gtceu:flawless_magneto_resonatic_gem".getItemStack(),
            "kubejs:circuit_resonatic_hv".getItemStack()
        )
        circuit(
            "iv", 30720L, 675,
            "gtceu:advanced_smd_diode".getItemStack(3),
            "gtceu:advanced_smd_capacitor".getItemStack(3),
            "gtceu:advanced_smd_transistor".getItemStack(3),
            "kubejs:imprinted_resonatic_circuit_board".getItemStack(4),
            "gtceu:flawless_magneto_resonatic_gem".getItemStack(),
            "kubejs:circuit_resonatic_ev".getItemStack()
        )
        circuit(
            "luv", 122880L, 855,
            "gtceu:advanced_smd_diode".getItemStack(6),
            "gtceu:advanced_smd_capacitor".getItemStack(6),
            "gtceu:advanced_smd_transistor".getItemStack(6),
            "kubejs:imprinted_resonatic_circuit_board".getItemStack(4),
            "gtceu:flawless_magneto_resonatic_gem".getItemStack(),
            "kubejs:circuit_resonatic_iv".getItemStack()
        )
        circuit(
            "zpm", 491520L, 1065,
            "kubejs:smd_diode_bioware".getItemStack(12),
            "kubejs:smd_capacitor_bioware".getItemStack(12),
            "kubejs:smd_transistor_bioware".getItemStack(12),
            "kubejs:imprinted_resonatic_circuit_board".getItemStack(8),
            "gtceu:exquisite_magneto_resonatic_gem".getItemStack(),
            "kubejs:circuit_resonatic_luv".getItemStack()
        )
        circuit(
            "uv", 1966080L, 1095,
            "kubejs:smd_diode_optical".getItemStack(12),
            "kubejs:smd_capacitor_optical".getItemStack(12),
            "kubejs:smd_transistor_optical".getItemStack(12),
            "kubejs:imprinted_resonatic_circuit_board".getItemStack(8),
            "gtceu:exquisite_magneto_resonatic_gem".getItemStack(),
            "kubejs:circuit_resonatic_zpm".getItemStack()
        )
        circuit(
            "uhv", 7864320L, 1125,
            "kubejs:smd_diode_exotic".getItemStack(12),
            "kubejs:smd_capacitor_exotic".getItemStack(12),
            "kubejs:smd_transistor_exotic".getItemStack(12),
            "kubejs:imprinted_resonatic_circuit_board".getItemStack(8),
            "gtceu:exquisite_magneto_resonatic_gem".getItemStack(),
            "kubejs:circuit_resonatic_uv".getItemStack()
        )
        circuit(
            "uev", 31457280L, 1155,
            "kubejs:smd_diode_cosmic".getItemStack(12),
            "kubejs:smd_capacitor_cosmic".getItemStack(12),
            "kubejs:smd_transistor_cosmic".getItemStack(12),
            "kubejs:imprinted_resonatic_circuit_board".getItemStack(8),
            "gtceu:exquisite_magneto_resonatic_gem".getItemStack(),
            "kubejs:circuit_resonatic_uhv".getItemStack()
        )
        circuit(
            "uiv", 125829120L, 1185,
            "kubejs:smd_diode_supracausal".getItemStack(12),
            "kubejs:smd_capacitor_supracausal".getItemStack(12),
            "kubejs:smd_transistor_supracausal".getItemStack(12),
            "kubejs:imprinted_resonatic_circuit_board".getItemStack(8),
            "gtceu:exquisite_magneto_resonatic_gem".getItemStack(),
            "kubejs:circuit_resonatic_uev".getItemStack()
        )
    }
}
