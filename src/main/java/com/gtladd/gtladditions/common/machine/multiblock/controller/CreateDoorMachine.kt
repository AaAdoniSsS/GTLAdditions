package com.gtladd.gtladditions.common.machine.multiblock.controller

import org.gtlcore.gtlcore.api.machine.trait.MEStock.IMETransfer
import org.gtlcore.gtlcore.api.recipe.RecipeResult
import org.gtlcore.gtlcore.api.recipe.RecipeResult.fail
import org.gtlcore.gtlcore.common.data.GTLItems
import org.gtlcore.gtlcore.utils.MachineUtil
import org.gtlcore.gtlcore.utils.MachineUtil.createItemEntity

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.Registries.DIMENSION
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB

import appeng.api.config.Actionable
import appeng.api.stacks.AEItemKey
import appeng.api.storage.cells.CellState
import appeng.me.cells.BasicCellInventory
import com.glodblock.github.extendedae.common.EPPItemAndBlock
import com.gtladd.gtladditions.api.machine.ConversationMachine
import com.gtladd.gtladditions.api.machine.IEnergyMachine
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import com.gtladd.gtladditions.utils.GTRecipeUtils.handleEUt
import com.gtladd.gtladditions.utils.GTRecipeUtils.matchEUt
import com.gtladd.gtladditions.utils.MathUtil.minToLong
import com.gtladd.gtladditions.utils.Registries.getItemStack

class CreateDoorMachine(holder: IMachineBlockEntity) : ConversationMachine(holder) {

    override fun onWorking(): Boolean {
        if (this.recipeLogic.progress == 5 && level is ServerLevel) {
            val sl = level as ServerLevel
            val pos = pos.offset(0, -13, 0)
            sl.sendParticles(ParticleTypes.DRAGON_BREATH, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), 1000, 4.0, 4.0, 4.0, 0.01)
            if (this.getCircuit() != 1) return true
            val entities = sl.getEntitiesOfClass(
                Entity::class.java,
                AABB(pos.x - 10.0, pos.y - 10.0, pos.z - 10.0, pos.x + 10.0, pos.y + 10.0, pos.z + 10.0)
            )
            for (e in entities) {
                if (e is ItemEntity) {
                    if (e.item.count <= 0) continue
                    when (e.item.item) {
                        MagnetohydrodynamicallyConstrainedStarMatterBlock.asItem() -> {
                            createItemEntity(sl, e.x, e.y, e.z, ItemStack(Blocks.COMMAND_BLOCK, e.item.count))
                            e.discard()
                        }
                        MagmatterIngot -> {
                            if (e.item.count % 64 == 0) {
                                createItemEntity(sl, e.x, e.y, e.z, ItemStack(MagmatterBlock, e.item.count / 64))
                                e.discard()
                            }
                        }
                        EPPItemAndBlock.FISHBIG.asItem() -> {
                            if (e.item.count % 64 == 0) {
                                createItemEntity(sl, e.x, e.y, e.z, GTLItems.ULTIMATE_TEA.asStack(e.item.count / 64))
                                e.discard()
                            }
                        }
                        GTLItems.ULTIMATE_TEA.asItem() -> {
                            if (e.item.count % 16 == 0) {
                                createItemEntity(sl, e.x, e.y, e.z, "kubejs:heartofthesmogus".getItemStack(e.item.count / 16))
                                e.discard()
                            }
                        }
                    }
                } else if (e is ServerPlayer) {
                    if (MachineUtil.hasFullArmorSet(e)) {
                        val createLevel = sl.server.getLevel(ResourceKey.create(DIMENSION, ResourceLocation("kubejs", "create")))
                        if (createLevel != null) e.teleportTo(createLevel, 0.0, 1.0, 0.0, e.xRot, e.yRot)
                    } else {
                        e.displayClientMessage("message.gtlcore.equipment_incompatible_dimension".toComponent, true)
                    }
                }
            }
        }
        return true
    }

    override fun afterWorking() {
        if (getCircuit() != 24) return
        bcHatch?.transformOther {
            if (!it.isCreate) {
                RecipeResult.ofWorking(this, fail("gtceu.machine.block_conversation.fail.2".toComponent))
                return@transformOther
            }
            val c = it.getCellInventory()
            if (c == null) {
                RecipeResult.ofWorking(this, fail("gtceu.machine.block_conversation.fail.0".toComponent))
                return@transformOther
            }
            if (c.status == CellState.FULL || c.status == CellState.TYPES_FULL) {
                RecipeResult.ofWorking(this, fail("gtceu.machine.block_conversation.fail.1".toComponent))
                return@transformOther
            }
            RecipeResult.ofWorking(this, null)
            for (s in it.aeItemHandler.inventory) {
                val i = s.getStackInSlot(0)
                if (!i.isEmpty) {
                    blockMap[i.item]?.let { b ->
                        val p = this.parallel minToLong ((c as? BasicCellInventory)?.remainingItemCount ?: Long.MAX_VALUE)
                        val g = (s as IMETransfer).extractGenericStack(p, false, true)
                        val a = if (i.item == MagmatterIngot) 64 else 1
                        it.insertCell(AEItemKey.of(b), g!!.amount / a, Actionable.MODULATE)
                    }
                }
            }
        }
    }

    override fun refreshSlot() {
        when (machineStorage.getStackInSlot(0).item) {
            CARD_3 -> {
                cardId = 3
                parallel = 65536
            }
            else -> {
                cardId = 0
                parallel = 0
            }
        }
    }

    override fun getStartRecipe() = dRecipe

    override fun beforeWork(): Boolean {
        val c = getCircuit()
        return (c == 1 || c == 24) && super.beforeWork()
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun tickConsume(): Boolean {
        if (EU.matchEUt(this as IEnergyMachine)) {
            EU.handleEUt(this)
            return true
        }
        return false
    }

    companion object {
        val dRecipe: GTRecipe by lazy { GTRecipeBuilder.ofRaw().duration(20).dimension(ResourceLocation("overworld")).buildRawRecipe() }
        const val EU: Long = 4L * Int.MAX_VALUE
    }
}
