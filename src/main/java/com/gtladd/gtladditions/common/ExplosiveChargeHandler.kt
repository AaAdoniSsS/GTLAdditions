package com.gtladd.gtladditions.common

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

import com.gtladd.gtladditions.api.async.StagedBlastManager

object ExplosiveChargeHandler {

    private class Charge(val ignitionItem: ResourceLocation, val radius: Int)

    private val CHARGES: Map<ResourceLocation, Charge> = mapOf(
        ResourceLocation("kubejs", "naquadria_charge") to
            Charge(ResourceLocation("gtceu", "quantum_star"), 125),
        ResourceLocation("kubejs", "leptonic_charge") to
            Charge(ResourceLocation("gtceu", "gravi_star"), 250),
        ResourceLocation("kubejs", "quantum_chromodynamic_charge") to
            Charge(ResourceLocation("kubejs", "unstable_star"), 500)
    )

    @SubscribeEvent
    @JvmStatic
    fun onRightClickBlock(event: PlayerInteractEvent.RightClickBlock) {
        if (!event.side.isServer) return
        if (event.isCanceled) return

        val level: Level = event.level
        if (level !is ServerLevel) return

        val blockId = BuiltInRegistries.BLOCK.getKey(level.getBlockState(event.pos).block) ?: return
        val charge = CHARGES[blockId] ?: return
        if (!matches(event.itemStack, charge.ignitionItem)) return

        if (!event.entity.isCreative) {
            event.itemStack.shrink(1)
        }

        StagedBlastManager.spawn(level, event.pos, charge.radius)
    }

    private fun matches(stack: ItemStack, id: ResourceLocation): Boolean {
        if (stack.isEmpty) return false
        return BuiltInRegistries.ITEM.getKey(stack.item) == id
    }
}
