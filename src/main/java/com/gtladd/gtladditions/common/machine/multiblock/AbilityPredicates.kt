package com.gtladd.gtladditions.common.machine.multiblock

import com.gregtechceu.gtceu.api.block.IMachineBlock
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.api.pattern.Predicates
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate

import net.minecraft.world.level.block.Block

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

fun abilitiesExcept(excluded: IMachineBlock, vararg abilities: PartAbility): TraceabilityPredicate {
    val excludedBlock = excluded.self()
    val blocks = ObjectOpenHashSet<Block>()
    for (ability in abilities) {
        for (block in ability.allBlocks) {
            if (block !== excludedBlock) blocks.add(block)
        }
    }
    return Predicates.blocks(*blocks.toTypedArray())
}
