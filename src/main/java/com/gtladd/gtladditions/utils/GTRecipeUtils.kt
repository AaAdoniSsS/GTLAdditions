package com.gtladd.gtladditions.utils

import org.gtlcore.gtlcore.api.machine.multiblock.ParallelMachine
import org.gtlcore.gtlcore.api.recipe.IGTRecipe
import org.gtlcore.gtlcore.api.recipe.IParallelLogic
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.handleRecipeInput
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.matchRecipeInput
import org.gtlcore.gtlcore.api.recipe.chance.LongChanceLogic
import org.gtlcore.gtlcore.api.recipe.ingredient.LongIngredient

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.content.Content
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier.multiplier
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder

import com.lowdragmc.lowdraglib.side.fluid.FluidStack

import net.minecraft.tags.TagKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.material.Fluid

import com.gtladd.gtladditions.api.recipe.ContentList
import com.gtladd.gtladditions.api.recipe.FastRecipeModify
import com.gtladd.gtladditions.mixin.gtceu.api.FluidValueAccessor
import com.gtladd.gtladditions.utils.MachineUtil.maintenance
import com.gtladd.gtladditions.utils.MathUtil.maxToInt
import com.gtladd.gtladditions.utils.MathUtil.minToLong
import com.gtladd.gtladditions.utils.MathUtil.safePlus
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap

import java.util.function.Predicate

@Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
object GTRecipeUtils {

    fun WorkableElectricMultiblockMachine.getOverclockRecipe(getRecipe: (Long) -> GTRecipe?, testBefore: (Object) -> Boolean = { true }, maxThread: Int, minDuration: Int): GTRecipe? {
        if (!this.hasProxies()) return null
        val maxEUt = this.overclockVoltage
        if (maxEUt <= 0) return null
        val il = ContentList()
        val fl = ContentList()
        val p = (this as ParallelMachine).maxParallel.toLong()
        var totalEu = 0.0
        for (index in 1..maxThread) {
            val recipe = getRecipe.invoke(p) ?: break
            if (recipe.getEU > this.maxVoltage) break
            if (testBefore.invoke(recipe as Object)) {
                if (handleRecipeInput(this, recipe)) {
                    totalEu += recipe.duration * recipe.getEU.toDouble()
                    il.addAll(recipe.getOutputContents(ItemRecipeCapability.CAP))
                    fl.addAll(recipe.getOutputContents(FluidRecipeCapability.CAP))
                } else {
                    break
                }
            } else {
                break
            }
        }
        if (il.isEmpty && fl.isEmpty) return null
        val d = totalEu / maxEUt
        val o = GTRecipeBuilder.ofRaw().duration(minDuration maxToInt d)
        o.tickInput[EURecipeCapability.CAP] = ContentList.getEUtList(if (d > minDuration) maxEUt else (maxEUt * d / minDuration))
        if (!il.isEmpty) o.output[ItemRecipeCapability.CAP] = il
        if (!fl.isEmpty) o.output[FluidRecipeCapability.CAP] = fl
        return o.buildRawRecipe()
    }

    fun WorkableElectricMultiblockMachine.getFastMultipleRecipe(getRecipe: (Long) -> GTRecipe?, maxThread: Int, minDuration: Int): GTRecipe? {
        if (!this.hasProxies()) return null
        val maxEUt = this.overclockVoltage
        if (maxEUt <= 0) return null
        val il = ContentList()
        val fl = ContentList()
        var rp = (this as ParallelMachine).maxParallel.toLong() * maxThread
        var totalEu = 0.0
        while (rp > 0) {
            val recipe = getRecipe.invoke(rp) ?: break
            if (recipe.getEU > this.maxVoltage) break
            if (handleRecipeInput(this, recipe)) {
                rp -= recipe.longParallel
                totalEu += recipe.duration * recipe.getEU.toDouble()
                il.addAll(recipe.getOutputContents(ItemRecipeCapability.CAP))
                fl.addAll(recipe.getOutputContents(FluidRecipeCapability.CAP))
            } else {
                break
            }
            if (totalEu > maxEUt.toDouble() * 20 * 500) break
        }
        if (il.isEmpty && fl.isEmpty) return null
        val d = totalEu / maxEUt
        val o = GTRecipeBuilder.ofRaw().duration(minDuration maxToInt d)
        o.tickInput[EURecipeCapability.CAP] = ContentList.getEUtList(if (d > minDuration) maxEUt else (maxEUt * d / minDuration))
        if (!il.isEmpty) o.output[ItemRecipeCapability.CAP] = il
        if (!fl.isEmpty) o.output[FluidRecipeCapability.CAP] = fl
        return o.buildRawRecipe()
    }

    fun WorkableElectricMultiblockMachine.getMultipleRecipe(getRecipeSet: MutableSet<GTRecipe>, testBefore: (Object) -> Boolean, modifyRecipe: (GTRecipe) -> FastRecipeModify.ReduceResult, maxThread: Int, minDuration: Int): GTRecipe? {
        if (!this.hasProxies()) return null
        val maxEUt = this.overclockVoltage
        if (maxEUt <= 0) return null
        val length = getRecipeSet.size
        if (length == 0) return null
        val mp = (this as ParallelMachine).maxParallel.toLong()
        val pa = LongArray(length)
        var i = 0
        var rp = mp * maxThread
        val q = ObjectArrayFIFOQueue<RecipeData>(length)
        val recipeList = ObjectArrayList<GTRecipe>(length)
        for (r in getRecipeSet) {
            if (r.getEU > this.maxVoltage) continue
            val p = IParallelLogic.getMaxParallel(this, r, mp * maxThread)
            if (p <= 0) continue
            recipeList.add(r)
            pa[i] = p minToLong (mp * maxThread / length)
            if (p > pa[i]) q.enqueue(RecipeData(i, p - pa[i]))
            rp -= pa[i++]
        }
        if (recipeList.isEmpty) return null
        while (rp > 0 && !q.isEmpty) {
            val d = q.dequeue()
            val g = rp / (q.size() + 1)
            if (g > 0) {
                val give = d.remainingWant minToLong g
                pa[d.index] += give
                rp -= give
                val nr = d.remainingWant - give
                if (nr > 0) q.enqueue(RecipeData(d.index, nr))
            } else {
                break
            }
        }
        i = 0
        if (!testBefore.invoke((mp * maxThread - rp) as Object)) return null
        val il = ContentList()
        val fl = ContentList()
        var totalEu = .0
        for (recipe in recipeList) {
            val c = recipe.copy(this, if (pa[i] > 1) pa[i] else 1, recipe.duration)
            i++
            if (matchRecipeInput(this, c) && handleRecipeInput(this, c)) {
                val red = modifyRecipe.invoke(c)
                totalEu += c.getEU.toDouble() * c.duration * this.maintenance() * red.reduceEUt * red.reduceDuration
                il.addAll(c.getOutputContents(ItemRecipeCapability.CAP))
                fl.addAll(c.getOutputContents(FluidRecipeCapability.CAP))
            }
            if (totalEu / maxEUt > 20 * 500) break
        }
        if (il.isEmpty && fl.isEmpty) return null
        val d = totalEu / maxEUt
        val o = GTRecipeBuilder.ofRaw().duration(minDuration maxToInt d)
        o.tickInput[EURecipeCapability.CAP] = ContentList.getEUtList(if (d > minDuration) maxEUt else (maxEUt * d / minDuration))
        if (!il.isEmpty) o.output[ItemRecipeCapability.CAP] = il
        if (!fl.isEmpty) o.output[FluidRecipeCapability.CAP] = fl
        return o.buildRawRecipe()
    }

    val GTRecipe.copy: GTRecipe get() = GTRecipe(
        this.recipeType, this.id,
        copyMapContents(this.inputs, null), copyMapContents(this.outputs, null),
        copyMapContents(this.tickInputs, null), copyMapContents(this.tickOutputs, null),
        this.inputChanceLogics, this.outputChanceLogics,
        this.tickInputChanceLogics, this.tickOutputChanceLogics,
        this.conditions, this.ingredientActions, this.data, this.duration, this.isFuel
    )

    fun GTRecipe.modify(holder: IRecipeLogicMachine, parallel: Long): GTRecipe {
        (this as IGTRecipe).realParallels = parallel
        this.inputs.contentModify(parallel)
        this.tickInputs.contentModify(parallel)
        this.tickOutputs.contentModify(parallel)
        val o = copyContentChances(holder, this, parallel)
        this.outputs.replaceAll { t, _ -> o[t] }
        return this
    }

    fun GTRecipe.copy(holder: IRecipeLogicMachine, parallel: Long, reductionDuration: Double, reductionEUt: Double): GTRecipe {
        val modifier = multiplier(parallel.toDouble())
        val modifierTick = multiplier(parallel.toDouble() * reductionEUt)
        val copy = GTRecipe(
            this.recipeType, this.id,
            copyMapContents(this.inputs, modifier), copyContentChances(holder, this, parallel),
            copyMapContents(this.tickInputs, modifierTick),
            copyMapContents(this.tickOutputs, modifierTick),
            this.inputChanceLogics, this.outputChanceLogics,
            this.tickInputChanceLogics, this.tickOutputChanceLogics,
            this.conditions, this.ingredientActions,
            this.data, 1.coerceAtLeast((this.duration * reductionDuration).toInt()), this.isFuel
        )
        (copy as IGTRecipe).realParallels = parallel
        return copy
    }

    fun GTRecipe.copy(holder: IRecipeLogicMachine, parallel: Long, duration: Int): GTRecipe {
        val modifier = multiplier(parallel.toDouble())
        val copy = GTRecipe(
            this.recipeType, this.id,
            copyMapContents(this.inputs, modifier), copyContentChances(holder, this, parallel),
            copyMapContents(this.tickInputs, modifier), copyMapContents(this.tickOutputs, modifier),
            this.inputChanceLogics, this.outputChanceLogics,
            this.tickInputChanceLogics, this.tickOutputChanceLogics,
            this.conditions, this.ingredientActions,
            this.data, duration, this.isFuel
        )
        (copy as IGTRecipe).realParallels = parallel
        return copy
    }

    private fun copyMapContents(contents: MutableMap<RecipeCapability<*>, MutableList<Content>>, modifier: ContentModifier?): MutableMap<RecipeCapability<*>, MutableList<Content>> {
        val map = Reference2ObjectOpenHashMap<RecipeCapability<*>, MutableList<Content>>(contents.size)
        contents.entries.forEach { (c, l) ->
            if (!l.isEmpty()) {
                val cl = ContentList()
                l.forEach { cl.add(it.copy(c, modifier)) }
                map[c] = cl
            }
        }
        return map
    }

    private fun copyContentChances(holder: IRecipeLogicMachine, recipe: GTRecipe, parallel: Long): MutableMap<RecipeCapability<*>, MutableList<Content>> {
        val mdf = multiplier(parallel.toDouble())
        val rc = Reference2ObjectOpenHashMap<RecipeCapability<*>, MutableList<Content>>(recipe.outputs.size)
        for ((cap, list) in recipe.outputs) {
            var ccl: MutableList<Content>? = ObjectArrayList()
            val cl = rc.computeIfAbsent(cap) { ObjectArrayList() }
            for (cont in list) {
                if (cont.chance >= cont.maxChance) {
                    cl.add(cont.copy(cap, mdf))
                } else {
                    ccl!!.add(cont.copy(cap, mdf))
                }
            }
            if (!ccl!!.isEmpty()) {
                ccl = LongChanceLogic.OR.roll(
                    ccl,
                    recipe.recipeType.chanceFunction,
                    recipe.euTier,
                    holder.chanceTier,
                    holder.recipeLogic.getChanceCaches()[cap],
                    parallel,
                    cap
                )
                ccl?.let { it.forEach { c -> cl.add(ContentList.MaxChanceContent(c.content)) } }
            }
            if (cl.isEmpty()) rc.remove(cap)
        }
        return rc
    }

    private fun MutableMap<RecipeCapability<*>, MutableList<Content>>.contentModify(parallel: Long) {
        if (parallel == 1L) return
        this.entries.forEach { (cap, list) ->
            if (!list.isEmpty()) list.forEach { if (it.chance > 0) it.modify(cap, parallel) }
        }
    }

    fun GTRecipe.setEU(eu: Long) = this.tickInputs.put(EURecipeCapability.CAP, ContentList.getEUtList(eu))

    val GTRecipe.getEU: Long get() = (if (!tickInputs.isEmpty()) tickInputs[EURecipeCapability.CAP]!![0].content else tickOutputs[EURecipeCapability.CAP]!![0].content ?: 0) as Long

    val GTRecipe.euTier: Int get() = (this as IGTRecipe).euTier

    val GTRecipe.longParallel: Long get() = (this as IGTRecipe).realParallels

    fun Content.modify(cap: RecipeCapability<*>, modifier: ContentModifier) {
        when (cap) {
            ItemRecipeCapability.CAP -> (content as? LongIngredient)?.let { it.actualAmount = modifier.apply(it.actualAmount).toLong() }
            FluidRecipeCapability.CAP -> (content as FluidIngredient).let { it.amount = modifier.apply(it.amount).toLong() }
            EURecipeCapability.CAP -> (content as Long).let { content = modifier.apply(it).toLong() }
        }
    }

    fun Content.modify(cap: RecipeCapability<*>, modifier: Number) {
        when (cap) {
            ItemRecipeCapability.CAP -> (content as? LongIngredient)?.let { it.actualAmount = it.actualAmount * modifier.toLong() }
            FluidRecipeCapability.CAP -> (content as FluidIngredient).let { it.amount = it.amount * modifier.toLong() }
            EURecipeCapability.CAP -> (content as Long).let { content = it * modifier.toLong() }
        }
    }

    fun Content.plus(x: Number): Content {
        val o = content
        when (o) {
            is LongIngredient -> o.actualAmount = o.actualAmount safePlus x.toLong()
            is FluidIngredient -> o.amount = o.amount safePlus x.toLong()
            is Long -> content = o safePlus x.toLong()
        }
        return this
    }

    fun Content.setAmount(x: Number): Content {
        val o = content
        return when (o) {
            is LongIngredient -> {
                o.actualAmount = x.toLong()
                this
            }
            is FluidIngredient -> {
                o.amount = x.toLong()
                this
            }
            is Long -> {
                this.content = x.toLong()
                this
            }
            else -> this
        }
    }

    fun Content.amount(cap: RecipeCapability<*>): Long = when (cap) {
        ItemRecipeCapability.CAP -> (content as Ingredient).amount
        FluidRecipeCapability.CAP -> (content as FluidIngredient).amount
        EURecipeCapability.CAP -> (content as Long)
        else -> 1L
    }

    fun <K> Content.test(k: K): Boolean {
        return (content as? Predicate<K>)?.test(k) == true
    }

    val Ingredient.amount: Long get() =
        when (this) {
            is LongIngredient -> this.actualAmount
            is SizedIngredient -> this.amount.toLong()
            else -> 1L
        }

    val FluidIngredient.stack: FluidStack get() {
        getStacks().forEach { if (it != null && !it.isEmpty) return it }
        return FluidStack.empty()
    }

    fun FluidIngredient.test(fluid: Fluid): Boolean {
        this.values.forEach { (it as? FluidIngredient.FluidValue)?.let { value -> return (value as FluidValueAccessor).fluid == fluid } }
        return false
    }

    fun FluidIngredient.test(key: TagKey<Fluid>): Boolean {
        this.values.forEach { (it as? FluidIngredient.TagValue)?.let { value -> return value.tag == key } }
        return false
    }

    fun FluidStack.create() = FluidIngredient(SingleStream.createSingle(FluidIngredient.FluidValue(this.fluid)), this.amount, this.tag)

    fun ItemStack.create(amount: Long = this.count.toLong()): LongIngredient = LongIngredient.create(Ingredient.fromValues(SingleStream.createSingle(Ingredient.ItemValue(this))), amount)

    @JvmRecord
    private data class RecipeData(val index: Int, val remainingWant: Long)
}
