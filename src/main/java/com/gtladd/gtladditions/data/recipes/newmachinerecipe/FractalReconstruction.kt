package com.gtladd.gtladditions.data.recipes.newmachinerecipe

import org.gtlcore.gtlcore.api.data.tag.GTLTagPrefix
import org.gtlcore.gtlcore.common.data.GTLItems
import org.gtlcore.gtlcore.common.data.GTLMaterials.*
import org.gtlcore.gtlcore.config.ConfigHolder

import com.gregtechceu.gtceu.api.GTValues.*
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust
import com.gregtechceu.gtceu.api.data.tag.TagPrefix.ingot
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient
import com.gregtechceu.gtceu.common.data.GTItems
import com.gregtechceu.gtceu.common.data.GTMaterials.*

import com.lowdragmc.lowdraglib.side.fluid.FluidStack

import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks

import com.gtladd.gtladditions.GTLAdditions.id
import com.gtladd.gtladditions.common.recipe.GTLAddRecipesTypes.FRACTAL_RECONSTRUCTION
import com.gtladd.gtladditions.common.register.GTLAddMaterial
import com.gtladd.gtladditions.utils.Registries.getItemStack
import earth.terrarium.adastra.common.registry.ModFluids
import earth.terrarium.adastra.common.registry.ModItems

import java.util.function.Consumer

object FractalReconstruction {
    @JvmStatic
    fun init(provider: Consumer<FinishedRecipe>) {
        FRACTAL_RECONSTRUCTION.recipeBuilder(id("cosmicneutroniume"))
            .notConsumable("kubejs:spacetime_catalyst".getItemStack(4))
            .inputItems("kubejs:quantum_chromodynamic_charge".getItemStack(5))
            .inputFluids(HeavyQuarkDegenerateMatter.getFluid(10000))
            .inputFluids(Periodicium.getFluid(1000))
            .inputFluids(UUMatter.getFluid(10000000))
            .outputFluids(CosmicNeutronium.getFluid(50000))
            .addData("accelerant", "kubejs:extremely_durable_plasma_cell")
            .EUt(64L * VA[MAX]).duration(6000)
            .save(provider)
        FRACTAL_RECONSTRUCTION.recipeBuilder(id("transcendentmetal"))
            .notConsumable(TagPrefix.block, Periodicium, 32)
            .inputItems("kubejs:quantum_anomaly".getItemStack(8))
            .inputItems(TagPrefix.block, Tennessine)
            .inputFluids(SpaceTime.getFluid(8800))
            .inputFluids(ExcitedDtsc.getFluid(4000))
            .inputFluids(ExcitedDtec.getFluid(8000))
            .outputFluids(TranscendentMetal.getFluid(1152))
            .addData("accelerant", "kubejs:spacetime_catalyst")
            .EUt(64L * VA[MAX]).duration(5600)
            .save(provider)
        FRACTAL_RECONSTRUCTION.recipeBuilder(id("high_energy_quark_gluon_plasma"))
            .notConsumable("kubejs:eternity_catalyst".getItemStack(4))
            .inputItems(TagPrefix.plateDouble, SuperheavyHAlloy, 2)
            .inputItems(TagPrefix.plateDouble, SuperheavyLAlloy, 2)
            .inputFluids(HeavyQuarkDegenerateMatter.getFluid(3160))
            .inputFluids(Antimatter.getFluid(5600))
            .inputFluids(Starmetal.getFluid(1440))
            .outputFluids(HighEnergyQuarkGluon.getFluid(FluidStorageKeys.PLASMA, 1152))
            .addData("accelerant", "kubejs:quantum_chromodynamic_charge")
            .EUt(16L * VA[MAX]).duration(3200)
            .save(provider)
        FRACTAL_RECONSTRUCTION.recipeBuilder(id("cosmic_singularity_shirabon"))
            .notConsumable("avaritia:eternal_singularity".getItemStack(32))
            .inputItems(GTLTagPrefix.nanoswarm, Eternity, 1)
            .inputFluids(RawStarMatter.getFluid(FluidStorageKeys.PLASMA, 400000))
            .inputFluids(SpaceTime.getFluid(5375))
            .inputFluids(ExcitedDtsc.getFluid(90000))
            .outputItems("kubejs:cosmic_singularity".getItemStack(43))
            .outputFluids(Shirabon.getFluid(396288))
            .addData("accelerant", "kubejs:eternity_catalyst")
            .EUt(256L * VA[MAX]).duration(9600)
            .save(provider)
        FRACTAL_RECONSTRUCTION.recipeBuilder(id("draconium_dust"))
            .notConsumable(ItemStack(Items.DRAGON_EGG, 48))
            .inputItems(Blocks.OBSIDIAN.asItem())
            .inputFluids(Glowstone.getFluid(576))
            .inputFluids(VibrantAlloy.getFluid(144))
            .inputFluids(UuAmplifier.getFluid(864))
            .outputItems("kubejs:draconium_dust".getItemStack(9))
            .addData("accelerant", "kubejs:draconium_block_charged")
            .EUt(VA[UXV].toLong()).duration(2000)
            .save(provider)
        FRACTAL_RECONSTRUCTION.recipeBuilder(id("dragon_blood"))
            .notConsumable(ItemStack(Items.DRAGON_BREATH, 56))
            .inputItems(GTItems.STEM_CELLS, 256)
            .inputItems(dust, Naquadria, 32)
            .inputFluids(Mutagen.getFluid(10000))
            .inputFluids(Mana.getFluid(64000))
            .inputFluids(XpJuice.getFluid(12800))
            .outputFluids(DragonBlood.getFluid(64000))
            .addData("accelerant", "kubejs:dragon_cells")
            .EUt(VA[UXV].toLong()).duration(4000)
            .save(provider)
        FRACTAL_RECONSTRUCTION.recipeBuilder(id("echo_shard"))
            .notConsumable(ItemStack(Items.SCULK_SENSOR, 64))
            .notConsumable(ItemStack(Items.SCULK_SHRIEKER, 64))
            .inputItems(Items.VINE, 64)
            .inputFluids(UnknowWater.getFluid(1000))
            .inputFluids(Mana.getFluid(51200))
            .inputFluids(BarnardaAir.getFluid(144000))
            .outputFluids(EchoShard.getFluid(10000))
            .addData("accelerant", Items.SCULK.`kjs$getIdLocation`().toString())
            .EUt(64L * VA[MAX]).duration(3200)
            .save(provider)
        FRACTAL_RECONSTRUCTION.recipeBuilder(id("glacio_spirit"))
            .notConsumable("kubejs:magic_core".getItemStack(16))
            .inputItems(dust, Celestine, 16)
            .inputItems("kubejs:essence".getItemStack())
            .inputFluids(FluidIngredient.of(FluidStack.create(ModFluids.CRYO_FUEL.get(), 100)))
            .inputFluids(Ice.getFluid(12564), Mana.getFluid(32767))
            .outputItems("kubejs:glacio_spirit".getItemStack(64))
            .addData("accelerant", ModItems.ICE_SHARD.id.toString())
            .EUt(VA[UV].toLong()).duration(1000)
            .save(provider)
        if (ConfigHolder.INSTANCE.content.enableSkyBlokeMode) {
            FRACTAL_RECONSTRUCTION.recipeBuilder(id("mining_crystal"))
                .inputItems(TagPrefix.nugget, Magmatter, 9)
                .inputItems(GTLTagPrefix.nanoswarm, TranscendentMetal, 48)
                .inputItems("kubejs:quantum_anomaly".getItemStack(9216))
                .notConsumableFluid(GTLAddMaterial.MINING_ESSENCE.getFluid(64000))
                .inputFluids(MiracleAdhesive.getFluid(2880))
                .inputFluids(ExcitedDtsc.getFluid(9423))
                .outputItems(GTLItems.MINING_CRYSTAL.asStack(512))
                .addData("accelerant", GTLItems.SUPER_GLUE.asItem().`kjs$getId`())
                .EUt(16L * VA[MAX]).duration(500)
                .save(provider)
            FRACTAL_RECONSTRUCTION.recipeBuilder(id("treasures_crystal"))
                .inputItems(TagPrefix.nugget, Magmatter, 9)
                .inputItems("kubejs:cosmic_singularity".getItemStack(12))
                .inputItems("kubejs:quantum_anomaly".getItemStack(9216))
                .notConsumableFluid(GTLAddMaterial.TREASURES_ESSENCE.getFluid(64000))
                .inputFluids(MiracleAdhesive.getFluid(2880))
                .inputFluids(PrimordialMatter.getFluid(9423))
                .outputItems(GTLItems.TREASURES_CRYSTAL.asStack(512))
                .addData("accelerant", GTLItems.SUPER_GLUE.asItem().`kjs$getId`())
                .EUt(16L * VA[MAX]).duration(500)
                .save(provider)
        }
        FRACTAL_RECONSTRUCTION.recipeBuilder(id("hypercube"))
            .inputItems("kubejs:quantum_anomaly".getItemStack())
            .inputItems("kubejs:quantum_chromodynamic_charge".getItemStack(4))
            .inputItems(ingot, TranscendentMetal, 16)
            .notConsumableFluid(TemporalFluid.getFluid(16000))
            .inputFluids(CosmicNeutronium.getFluid(288), SpaceTime.getFluid(1000))
            .addData("accelerant", "gtceu:magmatter_block")
            .outputItems("kubejs:hypercube".getItemStack(64))
            .EUt(48L * VA[MAX]).duration(4800)
            .save(provider)
    }
}
