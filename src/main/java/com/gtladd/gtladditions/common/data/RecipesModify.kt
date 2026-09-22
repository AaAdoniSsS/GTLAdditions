package com.gtladd.gtladditions.common.data

import org.gtlcore.gtlcore.common.data.GTLMaterials
import org.gtlcore.gtlcore.common.data.GTLRecipeTypes.*

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability.CAP
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability
import com.gregtechceu.gtceu.api.data.tag.TagUtil
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys
import com.gregtechceu.gtceu.api.recipe.content.Content
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient
import com.gregtechceu.gtceu.common.data.GCyMRecipeTypes
import com.gregtechceu.gtceu.common.data.GTMaterials.Helium
import com.gregtechceu.gtceu.common.data.GTRecipeTypes.VACUUM_RECIPES
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder

import com.lowdragmc.lowdraglib.utils.LocalizationUtils

import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.material.Fluid

import com.gtladd.gtladditions.api.recipe.content.ContentList
import com.gtladd.gtladditions.common.machine.multiblock.controller.Resource.cellSet
import com.gtladd.gtladditions.common.machine.multiblock.controller.Resource.itemSet
import com.gtladd.gtladditions.common.recipe.GTLAddRecipesTypes
import com.gtladd.gtladditions.utils.GTRecipeUtils.amount
import com.gtladd.gtladditions.utils.GTRecipeUtils.stack
import com.gtladd.gtladditions.utils.GTRecipeUtils.test
import com.gtladd.gtladditions.utils.MathUtil.maxToInt
import com.gtladd.gtladditions.utils.MathUtil.safeToInt
import com.gtladd.gtladditions.utils.Registries.getFluid
import com.gtladd.gtladditions.utils.SingleStream
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectIntPair
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap

object RecipesModify {
    val RTBERecipeItemMap: Int2ObjectOpenHashMap<Array<ObjectIntPair<Item>>> = Int2ObjectOpenHashMap(64)
    val RTBERecipeFluidMap: Int2ObjectOpenHashMap<Array<ObjectIntPair<Fluid>>> = Int2ObjectOpenHashMap(64)

    @JvmStatic
    fun init() {
        INTEGRATED_ORE_PROCESSOR.addDataInfo { if (it.contains("handle")) LocalizationUtils.format("gtceu.integrated_ore_processor.advanced") else "" }
        val filterLiquid = { content: Content ->
            (content.content as? FluidIngredient)?.let {
                return@let it.test(TagUtil.createFluidTag("liquid_helium")) || it.test(Helium.getFluid(FluidStorageKeys.LIQUID, 1))
            } == true
        }
        val filterGas = { content: Content ->
            (content.content as? FluidIngredient)?.let {
                return@let it.test(Helium.fluid) || it.test(Helium.getFluid(FluidStorageKeys.GAS, 1))
            } == true
        }
        VACUUM_RECIPES.onRecipeBuild { recipeBuilder, provider ->
            val builder = GTLAddRecipesTypes.ANTIENTROPY_CONDENSATION.copyFrom(recipeBuilder)
            builder.input[CAP]?.removeIf(filterLiquid)
            builder.output[CAP]?.removeIf(filterGas)
            builder.save(provider)
        }
        PLASMA_CONDENSER_RECIPES.onRecipeBuild { recipeBuilder, provider ->
            val builder = GTLAddRecipesTypes.ANTIENTROPY_CONDENSATION.copyFrom(recipeBuilder)
            builder.input[CAP]?.removeIf(filterLiquid)
            if (!builder.id.path.equals("helium_condenser")) builder.output[CAP]?.removeIf(filterGas)
            builder.save(provider)
        }
        DISTORT_RECIPES.onRecipeBuild { recipeBuilder, provider ->
            val builder = GTLAddRecipesTypes.TIME_SPACE_DISTORTER.copyFrom(recipeBuilder)
            val (i, o) = modify(builder)
            builder.input.replaceAll { t, _ -> i[t] }
            builder.output.replaceAll { t, _ -> o[t] }
            builder.save(provider)
        }
        QFT_RECIPES.onRecipeBuild { recipeBuilder, provider ->
            val builder = GTLAddRecipesTypes.TIME_SPACE_DISTORTER.copyFrom(recipeBuilder)
            if (builder.id.path.equals("make_miracle_crystal")) {
                builder.save(provider)
                return@onRecipeBuild
            }
            val (i, o) = modify(builder)
            builder.input.replaceAll { t, _ -> i[t] }
            builder.output.replaceAll { t, _ -> o[t] }
            builder.save(provider)
        }
        DIMENSIONALLY_TRANSCENDENT_PLASMA_FORGE_RECIPES.onRecipeBuild { recipeBuilder, provider ->
            val builder = GTLAddRecipesTypes.RECURSIVE_REVERSE_FORGE.copyFrom(recipeBuilder)
            builder.id(recipeBuilder.id.withSuffix("_1"))
            builder.input.forEach { (t, u) ->
                if (t == ItemRecipeCapability.CAP) {
                    u.forEach {
                        val item = (it.content as Ingredient).`kjs$getFirst`().item
                        when (item) {
                            in cellSet -> it.slotName = "c"
                            in itemSet -> it.slotName = "i"
                        }
                    }
                }
            }
            builder.output.forEach { (t, u) ->
                if (t == ItemRecipeCapability.CAP) {
                    val a = ObjectArrayList<ObjectIntPair<Item>>()
                    u.forEach {
                        when (val item = (it.content as Ingredient).`kjs$getFirst`().item) {
                            in cellSet -> it.slotName = "c"
                            in itemSet -> it.slotName = "i"
                            else -> a.add(ObjectIntPair.of(item, it.amount(t).safeToInt))
                        }
                    }
                    if (!a.isEmpty) RTBERecipeItemMap.put(builder.id.withPrefix(GTLAddRecipesTypes.RECURSIVE_REVERSE_FORGE.registryName.path + "/").hashCode(), a.toTypedArray())
                } else if (t == CAP) {
                    val l =
                        u
                            .map { it.content as FluidIngredient }
                            .filter {
                                it.stack.fluid !=
                                    GTLMaterials.DimensionallyTranscendentResidue.fluid
                            }.map { ObjectIntPair.of(it.stack.fluid, it.amount.safeToInt) }
                            .toTypedArray()
                    if (!l.isEmpty()) RTBERecipeFluidMap.put(builder.id.withPrefix(GTLAddRecipesTypes.RECURSIVE_REVERSE_FORGE.registryName.path + "/").hashCode(), l)
                }
            }
            builder.save(provider)
        }
        STELLAR_FORGE_RECIPES.onRecipeBuild { recipeBuilder, provider ->
            val builder = GTLAddRecipesTypes.RECURSIVE_REVERSE_FORGE.copyFrom(recipeBuilder)
            builder.id(recipeBuilder.id.withSuffix("_2"))
            builder.input.forEach { (t, u) ->
                if (t == ItemRecipeCapability.CAP) {
                    u.forEach {
                        val item = (it.content as Ingredient).`kjs$getFirst`().item
                        when (item) {
                            in cellSet -> it.slotName = "c"
                            in itemSet -> it.slotName = "i"
                        }
                    }
                }
            }
            builder.output.forEach { (t, u) ->
                if (t == ItemRecipeCapability.CAP) {
                    val a = ObjectArrayList<ObjectIntPair<Item>>()
                    u.forEach {
                        when (val item = (it.content as Ingredient).`kjs$getFirst`().item) {
                            in cellSet -> it.slotName = "c"
                            in itemSet -> it.slotName = "i"
                            else -> a.add(ObjectIntPair.of(item, it.amount(t).safeToInt))
                        }
                    }
                    if (!a.isEmpty) RTBERecipeItemMap.put(builder.id.withPrefix(GTLAddRecipesTypes.RECURSIVE_REVERSE_FORGE.registryName.path + "/").hashCode(), a.toTypedArray())
                } else if (t == CAP) {
                    val l =
                        u
                            .map { it.content as FluidIngredient }
                            .filter {
                                it.stack.fluid !=
                                    GTLMaterials.DimensionallyTranscendentResidue.fluid
                            }.map { ObjectIntPair.of(it.stack.fluid, it.amount.safeToInt) }
                            .toTypedArray()
                    if (!l.isEmpty()) RTBERecipeFluidMap.put(builder.id.withPrefix(GTLAddRecipesTypes.RECURSIVE_REVERSE_FORGE.registryName.path + "/").hashCode(), l)
                }
            }
            builder.save(provider)
        }
        GCyMRecipeTypes.ALLOY_BLAST_RECIPES.onRecipeBuild { recipeBuilder, provider ->
            val builder = GTLAddRecipesTypes.CHAOTIC_ALCHEMY.copyFrom(recipeBuilder)
            val content = builder.output[CAP]!![0]
            val fluidIngredient = content.content as FluidIngredient
            val id = (fluidIngredient.stack.fluid as Fluid).builtInRegistryHolder().key().location().toString()
            if (id.contains("molten")) {
                builder.output[CAP] = listOf(Content(FluidIngredient.fromValues(SingleStream.createSingle(FluidIngredient.FluidValue(id.replace("molten_", "").getFluid)), fluidIngredient.amount, fluidIngredient.nbt), content.chance, content.maxChance, content.tierChanceBoost, content.slotName, content.uiName))
            }
            builder.duration((builder.duration * 0.8) maxToInt 1).save(provider)
        }
    }

    private fun modify(builder: GTRecipeBuilder): Pair<Map<RecipeCapability<*>, MutableList<Content>>, Map<RecipeCapability<*>, MutableList<Content>>> {
        val ci = Reference2ObjectOpenHashMap<RecipeCapability<*>, MutableList<Content>>(builder.input.size)
        builder.input.entries.forEach { (c, l) ->
            if (!l.isEmpty()) {
                val cl = ContentList(l.size)
                for (t in l) {
                    if (t.chance >= t.maxChance) {
                        cl.addMaxChanceContent(t.content)
                    } else {
                        if (c == ItemRecipeCapability.CAP) {
                            val item = (t.content as Ingredient).`kjs$getFirst`()
                            if (item.`kjs$getId`().contains("nanoswarm")) {
                                cl.addNoCustomContent(t.content)
                            } else {
                                cl.addChanceIngredient(t.content as Ingredient, t.chance, t.maxChance * 10)
                            }
                        } else {
                            cl.addNoCustomContent(t.content)
                        }
                    }
                }
                ci[c] = cl
            }
        }
        val co = Reference2ObjectOpenHashMap<RecipeCapability<*>, MutableList<Content>>(builder.output.size)
        builder.output.entries.forEach { (c, l) ->
            if (!l.isEmpty()) {
                val cl = ContentList(l.size)
                l.forEach { cl.addMaxChanceContent(it.content) }
                co[c] = cl
            }
        }
        return Pair(ci, co)
    }
}
