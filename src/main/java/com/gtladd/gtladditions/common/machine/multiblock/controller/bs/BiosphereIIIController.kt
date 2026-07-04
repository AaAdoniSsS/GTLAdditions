package com.gtladd.gtladditions.common.machine.multiblock.controller.bs

import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineHost
import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineModule
import org.gtlcore.gtlcore.api.machine.trait.ICheckPatternMachine
import org.gtlcore.gtlcore.utils.datastructure.ModuleRenderInfo

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CombinedDirectionalFancyConfigurator
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine

import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

import com.gtladd.gtladditions.api.machine.IEnergyMachine
import com.gtladd.gtladditions.api.machine.gui.MultiblockDisplayText
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine
import com.gtladd.gtladditions.common.machine.multiblock.controller.bs.BiosphereIIIPosHelper.calculateModulePositions
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

import java.util.function.Consumer

class BiosphereIIIController(holder: IMachineBlockEntity) :
    CoilWorkableElectricMultiblockMachine(holder),
    IModularMachineHost<BiosphereIIIController>,
    IMachineLife {
    val modulePos = ObjectOpenHashSet<IModularMachineModule<BiosphereIIIController, BiosphereIIIModule>>(8)
    private var itemBus: ItemBusPartMachine? = null
    fun getCircuit() = IntCircuitBehaviour.getCircuitConfiguration(itemBus?.circuitInventory?.getStackInSlot(0) ?: ItemStack.EMPTY)

    override fun createRecipeLogic(vararg args: Any) = BSRecipeLogic(this)

    override fun onStructureFormed() {
        super.onStructureFormed()
        safeClearModules()
        parts.forEach { if (it is ItemBusPartMachine) this.itemBus = it }
        scanAndConnectModules()
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        safeClearModules()
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
        ICheckPatternMachine.attachConfigurators(configuratorPanel, self())
    }

    override fun attachSideTabs(sideTabs: TabsWidget) {
        sideTabs.setMainTab(this)
        CombinedDirectionalFancyConfigurator.of(self(), self())?.let { sideTabs.attachSubTab(it) }
    }

    override fun createUIWidget(): Widget {
        val group = WidgetGroup(0, 0, 182 + 8, 117 + 8)
        group.addWidget(
            DraggableScrollableWidgetGroup(4, 4, 182, 117).setBackground(screenTexture)
                .addWidget(LabelWidget(4, 5, self().blockState.block.descriptionId))
                .addWidget(
                    ComponentPanelWidget(4, 17, ::addDisplayText)
                        .textSupplier(if (this.level!!.isClientSide) null else Consumer(::addDisplayText))
                        .setMaxWidthLimit(150)
                )
        )
        group.setBackground(GuiTextures.BACKGROUND_INVERSE)
        return group
    }

    override fun addDisplayText(textList: MutableList<Component>) {
        MultiblockDisplayText.builder(textList, isFormed)
            .setWorkingStatus(recipeLogic.isWorkingEnabled, true)
            .addEnergyTierLine(tier)
            .addWorkingStatusLine()
            .addComponent(Component.translatable("gtceu.machine.module", modulePos.size))
    }

    override fun onMachineRemoved() = safeClearModules()

    override fun getModuleSet() = this.modulePos

    override fun getModuleScanPositions(): Array<out BlockPos> = calculateModulePositions(pos, frontFacing)

    override fun isFormed() = this.isFormed

    override fun getMaxModuleCount() = 8

    override fun getModulesForRendering() = listOf(
        ModuleRenderInfo(
            BlockPos(-6, 7, 0),
            Direction.UP,
            Direction.NORTH,
            Direction.NORTH,
            Direction.UP,
            MultiBlockMachine.BIOSPHERE_III_MODULE
        ),
        ModuleRenderInfo(
            BlockPos(6, 7, 0),
            Direction.UP,
            Direction.NORTH,
            Direction.NORTH,
            Direction.UP,
            MultiBlockMachine.BIOSPHERE_III_MODULE
        ),
        ModuleRenderInfo(
            BlockPos(8, 9, 0),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.UP,
            MultiBlockMachine.BIOSPHERE_III_MODULE
        ),
        ModuleRenderInfo(
            BlockPos(-8, 9, 0),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.UP,
            MultiBlockMachine.BIOSPHERE_III_MODULE
        ),
        ModuleRenderInfo(
            BlockPos(8, 15, 0),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.UP,
            MultiBlockMachine.BIOSPHERE_III_MODULE
        ),
        ModuleRenderInfo(
            BlockPos(-8, 15, 0),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.UP,
            MultiBlockMachine.BIOSPHERE_III_MODULE
        ),
        ModuleRenderInfo(
            BlockPos(6, 17, 0),
            Direction.UP,
            Direction.NORTH,
            Direction.SOUTH,
            Direction.UP,
            MultiBlockMachine.BIOSPHERE_III_MODULE
        ),
        ModuleRenderInfo(
            BlockPos(6, 17, 0),
            Direction.UP,
            Direction.NORTH,
            Direction.SOUTH,
            Direction.UP,
            MultiBlockMachine.BIOSPHERE_III_MODULE
        ),
    )

    class BSRecipeLogic(val bsMachine: BiosphereIIIController) : RecipeLogic(bsMachine) {
        override fun serverTick() {
            if (!this.isSuspend) {
                if (this.progress < 20) this.handleRecipeWorking()
                if (this.progress >= 20) progress = 0
            } else if (this.subscription != null) {
                this.subscription.unsubscribe()
                this.subscription = null
            }
        }

        override fun handleRecipeWorking() {
            val ecList = (bsMachine as IEnergyMachine).energyContainerList
            if (bsMachine.maxVoltage > 0 && bsMachine.maxVoltage <= ecList.energyStored) {
                ecList.removeEnergy(bsMachine.maxVoltage)
                this.status = Status.WORKING
                ++this.progress
            } else {
                this.status = Status.SUSPEND
            }
        }

        override fun updateSound() = Unit
    }
}
