package com.gtladd.gtladditions.common.machine.hatch

import org.gtlcore.gtlcore.utils.NumberUtils

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.FancyTankConfigurator
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank

import com.lowdragmc.lowdraglib.gui.util.ClickData
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper
import com.lowdragmc.lowdraglib.syncdata.ISubscription
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style

import com.gtladd.gtladditions.utils.ComponentUtil.literal
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import com.hepdd.gtmthings.common.block.machine.multiblock.part.HugeBusPartMachine
import com.hepdd.gtmthings.common.block.machine.trait.CatalystFluidStackHandler
import com.hepdd.gtmthings.utils.FormatUtil

class SuperDualHatchPartMachine(holder: IMachineBlockEntity) : HugeBusPartMachine(holder, 18, IO.IN, 9) {

    @Persisted
    val tank = object : NotifiableFluidTank(this, 24, Long.MAX_VALUE shr 12, IO.IN) {
        override fun canCapOutput() = true
    }

    @Persisted
    val shareTank = CatalystFluidStackHandler(this, 9, 16000L, IO.IN, IO.NONE)
    private var tankSubs: ISubscription? = null
    private var hasFluidTransfer = false
    private var hasItemTransfer = false

    fun getTankInventorySize() = this.tank.storages.size

    override fun attachConfigurators(configuratorPanel: ConfiguratorPanel) {
        super.attachConfigurators(configuratorPanel)
        configuratorPanel.attachConfigurators(
            (
                FancyTankConfigurator(
                    this.shareTank.storages,
                    "gui.gtceu.share_tank.title".toComponent
                )
                ).setTooltips(
                listOf<Component>("gui.gtceu.share_tank.desc.0".toComponent, "gui.gtceu.share_inventory.desc.1".toComponent)
            )
        )
    }

    override fun onLoad() {
        super.onLoad()
        this.tankSubs = this.tank.addChangedListener(this::updateInventorySubscription)
    }

    override fun onUnload() {
        super.onUnload()
        if (this.tankSubs != null) {
            this.tankSubs!!.unsubscribe()
            this.tankSubs = null
        }
    }

    override fun refundAll(clickData: ClickData) {
        super.refundAll(clickData)
        if (this.hasFluidTransfer) this.tank.exportToNearby(this.frontFacing)
    }

    override fun updateInventorySubscription() {
        if (level != null) {
            this.hasItemTransfer = ItemTransferHelper.getItemTransfer(level, this.pos.relative(this.frontFacing), this.frontFacing.opposite) != null
            this.hasFluidTransfer = FluidTransferHelper.getFluidTransfer(level, this.pos.relative(this.frontFacing), this.frontFacing.opposite) != null
        } else {
            this.hasItemTransfer = false
            this.hasFluidTransfer = false
        }
        if (!this.isWorkingEnabled || !this.hasItemTransfer && !this.hasFluidTransfer) {
            if (this.autoIOSubs != null) {
                this.autoIOSubs!!.unsubscribe()
                this.autoIOSubs = null
            }
        } else {
            this.autoIOSubs = this.subscribeServerTick(this.autoIOSubs, this::autoIO)
        }
    }

    override fun autoIO() {
        if (this.offsetTimer % 5L == 0L && this.isWorkingEnabled) {
            if (this.hasItemTransfer) this.getInventory().importFromNearby(this.frontFacing)
            if (this.hasFluidTransfer) this.tank.importFromNearby(this.frontFacing)
        }
    }

    override fun createUIWidget(): Widget {
        val group = WidgetGroup(0, 0, 186, 121)
        val componentPanel = ComponentPanelWidget(8, 5) { this.addDisplayText(it) }.setMaxWidthLimit(162)
        val screen = (DraggableScrollableWidgetGroup(4, 4, 178, 117))
            .setBackground(GuiTextures.DISPLAY).addWidget(componentPanel)
        group.addWidget(screen)
        return group
    }

    private fun addDisplayText(textList: MutableList<Component>) {
        var itemCount = 0
        var tankCount = 0
        for (i in 0..<inventorySize - 1) {
            val `is` = super.getInventory().getStackInSlot(i)
            if (!`is`.isEmpty) {
                textList.add(
                    `is`.displayName.copy().setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW))
                        .append(FormatUtil.formatNumber(`is`.count.toLong()).literal)
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA))
                )
                ++itemCount
            }
        }
        for (i in 0..<getTankInventorySize()) {
            val fs = this.tank.getFluidInTank(i)
            if (!fs.isEmpty) {
                textList.add(
                    fs.displayName.copy().setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD))
                        .append(
                            (if (fs.amount < 1000L) fs.amount.toString() + "mB" else NumberUtils.formatLong(fs.amount / 1000L) + "B").literal
                                .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA))
                        )
                )
                ++tankCount
            }
        }
        if (textList.isEmpty()) textList.add("gtmthings.machine.huge_item_bus.tooltip.3".toComponent)
        textList.add(
            0,
            Component.translatable(
                "gtmthings.machine.huge_item_bus.tooltip.2",
                itemCount,
                inventorySize
            ).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))
        )
        textList.add(
            1,
            Component.translatable(
                "gtmthings.machine.huge_dual_hatch.tooltip.2",
                tankCount,
                getTankInventorySize()
            ).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))
        )
    }

    override fun getFieldHolder() = MANAGED_FIELD_HOLDER

    init {
        this.workingEnabled = false
    }

    companion object {
        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(SuperDualHatchPartMachine::class.java, HugeBusPartMachine.MANAGED_FIELD_HOLDER)
    }
}
