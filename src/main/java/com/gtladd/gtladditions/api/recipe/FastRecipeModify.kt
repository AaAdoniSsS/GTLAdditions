package com.gtladd.gtladditions.api.recipe

import org.gtlcore.gtlcore.api.recipe.IGTRecipe
import org.gtlcore.gtlcore.config.ConfigHolder
import org.gtlcore.gtlcore.utils.NumberUtils

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.recipe.GTRecipe

import com.gtladd.gtladditions.api.recipe.ContentList.Companion.getEUtList
import com.gtladd.gtladditions.utils.GTRecipeUtils.copy
import com.gtladd.gtladditions.utils.GTRecipeUtils.euTier
import com.gtladd.gtladditions.utils.GTRecipeUtils.getEU
import com.gtladd.gtladditions.utils.GTRecipeUtils.longParallel
import com.gtladd.gtladditions.utils.GTRecipeUtils.modify
import com.gtladd.gtladditions.utils.GTRecipeUtils.modifyNotTick
import com.gtladd.gtladditions.utils.MathUtil.maxToInt
import com.gtladd.gtladditions.utils.MathUtil.maxToLong
import com.gtladd.gtladditions.utils.MathUtil.minToInt
import com.gtladd.gtladditions.utils.MathUtil.minToLong

object FastRecipeModify {
    private val perfectOverClockFactor = OverClockFactor(.25, 4.0)
    private val noPerfectOverClockFactor = OverClockFactor(.5, 4.0)

    private val defaultReduceResult = ReduceResult(1.0, 1.0)

    private fun batchRecipe(machine: WorkableElectricMultiblockMachine, recipe: GTRecipe, pResult: ParallelResult): GTRecipe {
        if (recipe.duration <= (ConfigHolder.INSTANCE.batchProcessingTimeLimitTicks / 2)) {
            val t = (ConfigHolder.INSTANCE.batchProcessingTimeLimitTicks / recipe.duration) minToInt (pResult.maxParallel / recipe.longParallel)
            if (t <= 1) return recipe
            recipe.modifyNotTick(machine, t.toLong())
            (recipe as IGTRecipe).let {
                it.isBatchProcessed = true
                it.batchSize = t
                it.realParallels *= t
            }
        }
        return recipe
    }

    fun rrfModify(machine: WorkableElectricMultiblockMachine, recipe: GTRecipe, maxEU: Double, isWireless: Boolean = false, machineParallel: Long, ocResult: OverClockFactor, mdRecipe: (GTRecipe) -> GTRecipe?): GTRecipe? {
        if (maxEU <= 0) return null
        val mr = mdRecipe.invoke(recipe.copy) ?: return null
        val eut = mr.getEU
        val pr = getParallelResult(machine, mr, eut, maxEU, machineParallel)
        if (pr.actualParallel <= 0) return null
        val peu = eut.toDouble() * pr.actualParallel
        val stResult = subDoubleTickParallelOC(mr.duration, peu, maxEU, ocResult, pr)
        val t = stResult.parallel / pr.actualParallel
        val modify = mr.modify(machine, stResult.parallel)
        modify.duration = stResult.duration
        if (isWireless) {
            val wireless = IWirelessGTRecipe.of(modify)
            wireless.wirelessEUt = if (t > 1) stResult.parallelEUt else stResult.eut
            if (stResult.eut > 0L) {
                wireless.iO = IO.IN
            } else if (stResult.eut < 0L) {
                wireless.iO = IO.OUT
            }
        }
        return batchRecipe(machine, modify, pr)
    }

    fun modify(machine: WorkableElectricMultiblockMachine, recipe: GTRecipe, machineParallel: Long, isSub: Boolean = true, ocResult: OverClockFactor, reResult: (GTRecipe) -> ReduceResult): GTRecipe? {
        val mov = machine.overclockVoltage
        if (mov <= 0) return null
        val red = reResult.invoke(recipe)
        val eut = (1L maxToLong (recipe.getEU * red.reduceEUt))
        val d = recipe.duration.toDouble() * red.reduceDuration
        val pr = getParallelResult(machine, recipe, eut, mov.toDouble(), machineParallel)
        if (pr.actualParallel <= 0) return null

        val peu = eut * pr.actualParallel
        val rt = recipe.euTier
        var ocAmount = NumberUtils.getFakeVoltageTier(mov) - rt
        if (rt == 0) ocAmount--
        val stResult = if (ocAmount <= 0) {
            SubTickResult(peu.toDouble(), 1 maxToInt d, 0, peu.toDouble())
        } else {
            subTickParallelOC(d, peu.toDouble(), ocAmount, mov, isSub, ocResult, pr)
        }

        return useSubTickResult(machine, stResult, recipe, pr, false)
    }

    fun copyModify(machine: WorkableElectricMultiblockMachine, recipe: GTRecipe, machineParallel: Long, isSub: Boolean = true, isOC: Boolean = true, ocResult: OverClockFactor, mdRecipe: (GTRecipe) -> GTRecipe): GTRecipe? {
        val mov = machine.overclockVoltage
        if (mov <= 0) return null
        val mr = mdRecipe.invoke(recipe.copy)
        val eut = mr.getEU
        val pr = getParallelResult(machine, mr, eut, mov.toDouble(), machineParallel)
        if (pr.actualParallel <= 0) return null
        if (!isOC) return mr.modify(machine, pr.actualParallel)

        val peu = eut * pr.actualParallel
        val rt = mr.euTier
        var ocAmount = NumberUtils.getFakeVoltageTier(mov) - rt
        if (rt == 0) ocAmount--
        val stResult = if (ocAmount <= 0) {
            SubTickResult(peu.toDouble(), mr.duration, 0, peu.toDouble())
        } else {
            subTickParallelOC(mr.duration.toDouble(), peu.toDouble(), ocAmount, mov, isSub, ocResult, pr)
        }

        return useSubTickResult(machine, stResult, mr, pr, true)
    }

    fun getParallelResult(machine: WorkableElectricMultiblockMachine, recipe: GTRecipe, recipeEUt: Long, machineEnergy: Double, machineParallel: Long): ParallelResult {
        val mp = ParallelCalculate.getParallel(machine, recipe, Long.MAX_VALUE)
        if (mp <= 0) return ParallelResult(0, 0)
        return ParallelResult(mp, (mp minToLong machineParallel) minToLong (machineEnergy / recipeEUt))
    }

    fun getDefaultReduce(): ReduceResult = defaultReduceResult

    fun getPerfectOverclock(): OverClockFactor = perfectOverClockFactor

    fun getNoPerfectOverclock(): OverClockFactor = noPerfectOverClockFactor

    private fun useSubTickResult(machine: WorkableElectricMultiblockMachine, stResult: SubTickResult, recipe: GTRecipe, pResult: ParallelResult, isCopy: Boolean): GTRecipe {
        var recipe = recipe
        val t = stResult.parallel / pResult.actualParallel
        val eut = if (t > 1) stResult.parallelEUt else stResult.eut
        recipe = if (isCopy) {
            recipe.modify(machine, stResult.parallel)
        } else {
            recipe.copy(machine, stResult.parallel, stResult.duration)
        }
        recipe.duration = stResult.duration

        if (stResult.eut > 0L) {
            recipe.tickInputs[EURecipeCapability.CAP] = getEUtList(eut)
        } else if (stResult.eut < 0L) {
            recipe.tickOutputs[EURecipeCapability.CAP] = getEUtList(-eut)
        }
        return batchRecipe(machine, recipe, pResult)
    }

    private fun subTickParallelOC(duration: Double, eut: Double, ocAmount: Int, maxVoltage: Long, isSub: Boolean, ocFactor: OverClockFactor, pResult: ParallelResult): SubTickResult {
        var duration = duration
        var eut = eut
        var ocAmount = ocAmount
        var parallel: Double
        var minParallel = pResult.actualParallel.toDouble()
        var sp = false
        var vfPowParallel = 1.0

        while (ocAmount-- > 0) {
            val pv = eut * ocFactor.voltageFactor
            if (pv > maxVoltage.toDouble()) break

            eut = pv
            if (sp) {
                parallel = minParallel / ocFactor.durationFactor
                if (parallel > pResult.maxParallel) break
                minParallel = parallel
                vfPowParallel *= ocFactor.voltageFactor
            } else {
                val pd = duration * ocFactor.durationFactor
                if (pd < 1.0) {
                    if (!isSub) break
                    parallel = minParallel / ocFactor.durationFactor
                    if (parallel > pResult.maxParallel) break
                    minParallel = parallel
                    vfPowParallel *= ocFactor.voltageFactor
                    sp = true
                } else {
                    duration = pd
                }
            }
        }

        return SubTickResult(eut / vfPowParallel, duration.toInt(), minParallel.toLong(), eut)
    }

    private fun subDoubleTickParallelOC(duration: Int, eut: Double, maxVoltage: Double, ocFactor: OverClockFactor, pResult: ParallelResult): SubTickResult {
        var duration = duration.toDouble()
        var eut = eut
        var sp = false
        var vfPowParallel = 1.0
        var p: Double
        var minParallel = pResult.actualParallel.toDouble()

        while (true) {
            val pv = eut * ocFactor.voltageFactor
            if (pv > maxVoltage) break

            eut = pv
            if (sp) {
                p = minParallel / ocFactor.durationFactor
                if (p > pResult.maxParallel) break
                minParallel = p
                vfPowParallel *= ocFactor.voltageFactor
            } else {
                val pd = duration * ocFactor.durationFactor
                if (pd < 1.0) {
                    p = minParallel / ocFactor.durationFactor
                    if (p > pResult.maxParallel) break
                    minParallel = p
                    vfPowParallel *= ocFactor.voltageFactor
                    sp = true
                } else {
                    duration = pd
                }
            }
        }

        return SubTickResult(eut / vfPowParallel, duration.toInt(), minParallel.toLong(), eut)
    }

    @JvmRecord
    data class ParallelResult(val maxParallel: Long, val actualParallel: Long)

    @JvmRecord
    data class ReduceResult(val reduceEUt: Double, val reduceDuration: Double)

    @JvmRecord
    data class OverClockFactor(val durationFactor: Double, val voltageFactor: Double)

    @JvmRecord
    private data class SubTickResult(val eut: Double, val duration: Int, val parallel: Long, val parallelEUt: Double)
}
