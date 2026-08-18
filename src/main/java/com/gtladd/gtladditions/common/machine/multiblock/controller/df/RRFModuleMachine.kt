package com.gtladd.gtladditions.common.machine.multiblock.controller.df

import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineModule

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CombinedDirectionalFancyConfigurator
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine
import com.gregtechceu.gtceu.api.pattern.MultiblockState
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData

import com.lowdragmc.lowdraglib.gui.modular.ModularUI
import com.lowdragmc.lowdraglib.gui.widget.*
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player

import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

import java.util.function.Consumer

abstract class RRFModuleMachine(holder: IMachineBlockEntity) :
    MultiblockControllerMachine(holder),
    IModularMachineModule<RecursiveReverseForge, RRFModuleMachine>,
    IFancyUIMachine,
    IDisplayUIMachine,
    IMachineLife {

    companion object {
        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(RRFModuleMachine::class.java, MultiblockControllerMachine.MANAGED_FIELD_HOLDER)
    }

    override fun getFieldHolder() = MANAGED_FIELD_HOLDER

    private val startupSubs = ConditionalSubscriptionHandler(this, this::startupUpdate) { this.isFormed && this.isWorking }

    @Persisted
    var isWorking = false

    @Persisted
    private var hostPosition: BlockPos? = null
    private var host: RecursiveReverseForge? = null

    open fun partTest(part: IMultiPart) = Unit

    open fun setWorkable(isWorking: Boolean) {
        this.isWorking = isWorking
        startupSubs.updateSubscription()
    }

    open fun startupUpdate() = Unit

    override fun onStructureFormed() {
        isFormed = true
        this.parts.clear()
        val set = multiblockState.matchContext.getOrCreate<MutableSet<IMultiPart>>("parts") { ObjectOpenHashSet() }
        for (part in set) {
            if (shouldAddPartToController(part)) {
                this.parts.add(part)
                part.addedToController(this)
                partTest(part)
            }
        }
        updatePartPositions()
        if (!findAndConnectToHost()) removeFromHost(this.host)
        startupSubs.initialize(level)
    }

    override fun onStructureInvalid() {
        isFormed = false
        parts.forEach { it.removedFromController(this) }
        parts.clear()
        updatePartPositions()
        removeFromHost(this.host)
    }

    override fun onPartUnload() {
        parts.removeIf { it.self().isInValid }
        multiblockState.setError(MultiblockState.UNLOAD_ERROR)
        (level as ServerLevel).let { MultiblockWorldSavedData.getOrCreate(it).addAsyncLogic(this) }
        updatePartPositions()
        removeFromHost(this.host)
    }

    override fun attachSideTabs(sideTabs: TabsWidget) {
        sideTabs.setMainTab(this)
        CombinedDirectionalFancyConfigurator.of(self(), self())?.let { sideTabs.attachSubTab(it) }
    }

    override fun attachConfigurators(configuratorPanel: ConfiguratorPanel) {
        configuratorPanel.attachConfigurators(
            IFancyConfiguratorButton.Toggle(
                GuiTextures.BUTTON_POWER.getSubTexture(0.0, 0.0, 1.0, 0.5),
                GuiTextures.BUTTON_POWER.getSubTexture(0.0, 0.5, 1.0, 0.5),
                this::isWorking,
                { _, pressed -> this.setWorkable(pressed) }
            )
                .setTooltipsSupplier { listOf((if (it) "behaviour.soft_hammer.enabled" else "behaviour.soft_hammer.disabled").toComponent) }
        )
    }

    override fun createUIWidget(): Widget {
        val group = WidgetGroup(0, 0, 182 + 8, 117 + 8)
        group.addWidget(
            DraggableScrollableWidgetGroup(4, 4, 182, 117).setBackground(screenTexture)
                .addWidget(LabelWidget(4, 5, self().blockState.block.descriptionId))
                .addWidget(
                    ComponentPanelWidget(4, 17) { this.addDisplayText(it) }
                        .textSupplier(if (this.level!!.isClientSide) null else Consumer { this.addDisplayText(it) })
                        .setMaxWidthLimit(150)
                        .clickHandler { componentData, clickData -> this.handleDisplayClick(componentData, clickData) }
                )
        )
        group.setBackground(GuiTextures.BACKGROUND_INVERSE)
        return group
    }

    override fun createUI(entityPlayer: Player): ModularUI = ModularUI(198, 208, this, entityPlayer).widget(FancyMachineUIWidget(this, 198, 208))

    override fun isRemote() = super<MultiblockControllerMachine>.isRemote

    override fun onMachineRemoved() = removeFromHost(this.host)

    override fun getHostPosition() = this.hostPosition

    override fun setHostPosition(pos1: BlockPos?) {
        this.hostPosition = pos1
    }

    override fun getHost() = this.host

    override fun setHost(reverseForge: RecursiveReverseForge?) {
        this.host = reverseForge
    }

    override fun getHostType() = RecursiveReverseForge::class.java

    override fun getHostScanPositions(): Array<BlockPos> {
        val x = when (frontFacing) {
            Direction.EAST -> -33
            Direction.WEST -> 33
            else -> 0
        }
        val z = when (frontFacing) {
            Direction.NORTH -> 33
            Direction.SOUTH -> -33
            else -> 0
        }
        return arrayOf(pos.offset(x, 7, z))
    }

    override fun onConnected(host: RecursiveReverseForge) = Unit
}
