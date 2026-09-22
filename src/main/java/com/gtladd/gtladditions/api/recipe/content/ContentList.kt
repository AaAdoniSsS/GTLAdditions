package com.gtladd.gtladditions.api.recipe.content

import com.gregtechceu.gtceu.api.recipe.content.Content
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient

import com.lowdragmc.lowdraglib.side.fluid.FluidStack

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient

import com.gtladd.gtladditions.utils.GTRecipeUtils.create
import it.unimi.dsi.fastutil.objects.ObjectArrayList

class ContentList : ObjectArrayList<Content> {

    constructor(capacity: Int) : super(capacity)
    constructor() : super()

    companion object {
        fun getEUtList(eut: Number): ContentList = ContentList(1).addEUt(eut.toLong())
        fun getItemStackList(vararg stack: ItemStack): ContentList {
            val list = ContentList(stack.size)
            list.addMaxChanceContent(*stack)
            return list
        }
        fun getFluidStackList(vararg stack: FluidStack): ContentList {
            val list = ContentList(stack.size)
            list.addMaxChanceContent(*stack)
            return list
        }
    }

    fun addContent(objects: Any, chance: Int, boostChance: Int): ContentList {
        this.add(Content(objects, chance, 10000, boostChance, null, null))
        return this
    }

    fun addMaxChanceContent(vararg objects: Any): ContentList {
        for (o in objects) {
            when (o) {
                is ItemStack -> this.add(MaxChanceContent(o.create()))
                is FluidStack -> this.add(MaxChanceContent(o.create()))
                is Ingredient -> this.add(MaxChanceContent(o))
                is FluidIngredient -> this.add(MaxChanceContent(o))
            }
        }
        return this
    }

    fun addNoCustomContent(vararg objects: Any): ContentList {
        for (o in objects) {
            when (o) {
                is ItemStack -> this.add(NoCustomContent(o.create()))
                is FluidStack -> this.add(NoCustomContent(o.create()))
                is Ingredient -> this.add(NoCustomContent(o))
                is FluidIngredient -> this.add(NoCustomContent(o))
            }
        }
        return this
    }

    fun addMaxChanceContent(objects: Collection<Any>): ContentList {
        for (o in objects) {
            when (o) {
                is ItemStack -> this.add(MaxChanceContent(o.create()))
                is FluidStack -> this.add(MaxChanceContent(o.create()))
                is Ingredient -> this.add(MaxChanceContent(o))
                is FluidIngredient -> this.add(MaxChanceContent(o))
            }
        }
        return this
    }

    fun addNoCustomContent(objects: Collection<Any>): ContentList {
        for (o in objects) {
            when (o) {
                is ItemStack -> this.add(NoCustomContent(o.create()))
                is FluidStack -> this.add(NoCustomContent(o.create()))
                is Ingredient -> this.add(NoCustomContent(o))
                is FluidIngredient -> this.add(NoCustomContent(o))
            }
        }
        return this
    }

    fun addChanceIngredient(ingredient: Ingredient, chance: Int, maxChance: Int = 10000, tierChanceBoost: Int = 0): ContentList {
        this.add(Content(ingredient, chance, maxChance, tierChanceBoost, null, null))
        return this
    }

    fun addChanceItemStack(itemStack: ItemStack, chance: Int, maxChance: Int = 10000, tierChanceBoost: Int = 0): ContentList {
        this.add(Content(itemStack.create(), chance, maxChance, tierChanceBoost, null, null))
        return this
    }

    fun addChanceFluidIngredient(fluidIngredient: FluidIngredient, chance: Int, maxChance: Int = 10000, tierChanceBoost: Int = 0): ContentList {
        this.add(Content(fluidIngredient, chance, maxChance, tierChanceBoost, null, null))
        return this
    }

    fun addChanceFluidStack(fluidStack: FluidStack, chance: Int, maxChance: Int = 10000, tierChanceBoost: Int = 0): ContentList {
        this.add(Content(fluidStack.create(), chance, maxChance, tierChanceBoost, null, null))
        return this
    }

    fun addEUt(eut: Long): ContentList {
        this.add(MaxChanceContent(eut))
        return this
    }

    fun addCWt(cwt: Int): ContentList {
        this.add(MaxChanceContent(cwt))
        return this
    }

    internal class MaxChanceContent(content: Any) : Content(content, 10000, 10000, 0, null, null)
    internal class NoCustomContent(content: Any) : Content(content, 0, 10000, 0, null, null)
}
