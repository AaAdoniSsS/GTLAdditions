package com.gtladd.gtladditions.api.machine

import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus
import org.gtlcore.gtlcore.common.data.GTLItems.CONVERSION_SIMULATE_CARD
import org.gtlcore.gtlcore.common.data.GTLItems.FAST_CONVERSION_SIMULATE_CARD
import org.gtlcore.gtlcore.common.data.GTLMaterials

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper.get
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper.getBlock
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.api.data.tag.TagPrefix.block
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder

import com.lowdragmc.lowdraglib.gui.widget.SlotWidget
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks.*

import com.gtladd.gtladditions.api.machine.gui.MultiblockDisplayText
import com.gtladd.gtladditions.common.machine.hatch.MEBlockConversationHatch
import com.gtladd.gtladditions.common.register.GTLAddItems
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import com.gtladd.gtladditions.utils.MathUtil.exp
import com.gtladd.gtladditions.utils.MathUtil.pow
import com.gtladd.gtladditions.utils.Registries.getBlock
import com.gtladd.gtladditions.utils.Registries.getItemStack
import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap

import kotlin.math.pow

open class ConversationMachine(holder: IMachineBlockEntity) :
    WorkableElectricMultiblockMachine(holder),
    IMachineModifyDrops {

    protected var bcHatch: MEBlockConversationHatch? = null
    private var busHatch: ItemBusPartMachine? = null

    @Persisted
    protected val machineStorage: NotifiableItemStackHandler = NotifiableItemStackHandler(this, 1, IO.NONE, IO.BOTH) {
        object : ItemStackTransfer(1) {
            override fun getSlotLimit(slot: Int): Int = 1
        }
    }.setFilter(::filter).also { it.addChangedListener(::refreshSlot) }

    @Persisted
    var cardId = 0

    @Persisted
    var parallel = 0L

    private fun filter(stack: ItemStack) = stack.`is`(CARD_1) || stack.`is`(CARD_2) || stack.`is`(CARD_3)

    protected fun getCircuit() = IntCircuitBehaviour.getCircuitConfiguration(busHatch?.circuitInventory?.getStackInSlot(0) ?: ItemStack.EMPTY)

    open fun refreshSlot() {
        when (machineStorage.getStackInSlot(0).item) {
            CARD_3 -> {
                cardId = 3
                parallel = 5.7.pow(tier).toLong()
            }
            CARD_2 -> {
                cardId = 2
                parallel = 4L.pow(tier)
            }
            CARD_1 -> {
                cardId = 1
                parallel = exp(tier)
            }
            else -> {
                cardId = 0
                parallel = 0
            }
        }
    }

    open fun getStartRecipe(): GTRecipe = recipe

    open fun tickConsume(): Boolean {
        val ecList = (this as IEnergyMachine).energyContainerList
        if (this.maxVoltage > 0 && this.maxVoltage <= ecList.energyStored) {
            ecList.changeEnergy(-this.maxVoltage)
            return true
        }
        return false
    }

    open fun isWork(): Boolean = true

    override fun onStructureFormed() {
        super.onStructureFormed()
        parts.forEach {
            when (it) {
                is MEBlockConversationHatch -> this.bcHatch = it
                is ItemBusPartMachine -> this.busHatch = it
            }
        }
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        this.bcHatch = null
        this.busHatch = null
    }

    override fun addDisplayText(textList: MutableList<Component>) {
        MultiblockDisplayText.builder(textList, isFormed())
            .setWorkingStatus(recipeLogic.isWorkingEnabled, recipeLogic.isActive)
            .addEnergyUsageLine(energyContainer)
            .addEnergyTierLine(tier)
            .addParallelsLine(parallel)
            .addWorkingStatusLine()
            .addProgressLine(recipeLogic.progressPercent)
    }

    override fun attachConfigurators(configuratorPanel: ConfiguratorPanel) {
        configuratorPanel.attachConfigurators(
            IFancyConfiguratorButton.Toggle(
                GuiTextures.BUTTON_POWER.getSubTexture(0.0, 0.0, 1.0, 0.5),
                GuiTextures.BUTTON_POWER.getSubTexture(0.0, 0.5, 1.0, 0.5),
                { this.isWorkingEnabled },
                { _, pressed -> this.isWorkingEnabled = pressed }
            )
                .setTooltipsSupplier { listOf(if (it) "behaviour.soft_hammer.enabled".toComponent else "behaviour.soft_hammer.disabled".toComponent) }
        )
    }

    override fun createUIWidget(): Widget = (super.createUIWidget() as WidgetGroup).let {
        return it.addWidget(
            SlotWidget(
                machineStorage.storage,
                0,
                it.sizeWidth - 30,
                it.sizeHeight - 30,
                true,
                true
            ).setBackground(GuiTextures.SLOT)
        )
    }

    override fun afterWorking() {
        bcHatch?.afterWorking(this)
    }

    override fun getRecipeLogic() = super.getRecipeLogic() as ConversationRecipeLogic

    override fun createRecipeLogic(vararg args: Any) = ConversationRecipeLogic(this)

    override fun getFieldHolder() = MANAGED_FIELD_HOLDER

    override fun onDrops(drops: MutableList<ItemStack>) = this.clearInventory(this.machineStorage.storage)

    class ConversationRecipeLogic(val cMachine: ConversationMachine) : RecipeLogic(cMachine), IRecipeStatus {

        override fun findAndHandleRecipe() {
            lastRecipe = null
            if (!cMachine.isWork()) return
            setupRecipe(cMachine.getStartRecipe())
        }

        override fun setupRecipe(recipe: GTRecipe) {
            if (recipe.checkConditions(this).isSuccess) {
                this.lastRecipe = recipe
                this.status = Status.WORKING
                this.progress = 0
                this.duration = if (cMachine.cardId == 3) 20 else recipe.duration
            }
        }

        override fun handleRecipeWorking() {
            if (cMachine.tickConsume()) {
                this.status = Status.WORKING
                cMachine.onWorking()
                ++this.progress
            } else {
                this.setWaiting(null)
            }
            if (this.status == Status.WAITING) this.doDamping()
        }

        override fun onRecipeFinish() {
            cMachine.afterWorking()
            if (!cMachine.isWork()) {
                this.status = Status.IDLE
                this.progress = 0
                this.duration = 0
                return
            }
            setupRecipe(cMachine.getStartRecipe())
        }
    }

    companion object {
        val recipe: GTRecipe by lazy { GTRecipeBuilder.ofRaw().buildRawRecipe() }
        val CARD_1: Item by lazy { CONVERSION_SIMULATE_CARD.asItem() }
        val CARD_2: Item by lazy { FAST_CONVERSION_SIMULATE_CARD.asItem() }
        val CARD_3: Item by lazy { GTLAddItems.ULTIMATE_CONVERSATION_CARD.asItem() }
        val MagmatterIngot: Item = get(TagPrefix.ingot, GTLMaterials.Magmatter).item
        val MagmatterBlock: Item = getBlock(block, GTLMaterials.Magmatter).asItem()
        val MagnetohydrodynamicallyConstrainedStarMatterBlock: Block = getBlock(block, GTLMaterials.MagnetohydrodynamicallyConstrainedStarMatter)
        val Heartofthesmogus: ItemStack = "kubejs:heartofthesmogus".getItemStack(64)
        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(ConversationMachine::class.java, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER)
        val HASH_STRATEGY = object : Hash.Strategy<Item> {
            override fun hashCode(o: Item?) = o?.hashCode() ?: 0
            override fun equals(a: Item?, b: Item?) = a?.hashCode() == b?.hashCode()
        }
        val blockMap: Object2ObjectOpenCustomHashMap<Item, Item> by lazy {
            val map = Object2ObjectOpenCustomHashMap<Item, Item>(HASH_STRATEGY)
            map[BONE_BLOCK.asItem()] = "kubejs:essence_block".getBlock.asItem()
            map[OAK_LOG.asItem()] = CRIMSON_STEM.asItem()
            map[BIRCH_LOG.asItem()] = WARPED_STEM.asItem()
            map[getBlock(block, GTMaterials.Calcium).asItem()] = BONE_BLOCK.asItem()
            map[MOSS_BLOCK.asItem()] = SCULK.asItem()
            map[GRASS_BLOCK.asItem()] = MOSS_BLOCK.asItem()
            map["kubejs:infused_obsidian".getBlock.asItem()] = "kubejs:draconium_block_charged".getBlock.asItem()
            map[MagnetohydrodynamicallyConstrainedStarMatterBlock.asItem()] = COMMAND_BLOCK.asItem()
            map[MagmatterIngot] = MagmatterBlock
            return@lazy map
        }
    }
}
