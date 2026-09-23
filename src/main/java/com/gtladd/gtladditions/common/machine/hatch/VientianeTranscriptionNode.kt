package com.gtladd.gtladditions.common.machine.hatch

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine
import com.gregtechceu.gtceu.data.lang.LangHandler
import com.gregtechceu.gtceu.utils.RedstoneUtil

import com.lowdragmc.lowdraglib.gui.widget.TextBoxWidget
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import com.lowdragmc.lowdraglib.utils.LocalizationUtils
import com.lowdragmc.lowdraglib.utils.Position
import com.lowdragmc.lowdraglib.utils.Size

import net.minecraft.core.Direction
import net.minecraft.util.Mth

class VientianeTranscriptionNode(holder: IMachineBlockEntity) : MultiblockPartMachine(holder) {

    @Persisted
    @DescSynced
    private var min = 48000

    @Persisted
    @DescSynced
    private var max = 105000

    @Persisted
    private var redStoneSignal = 0

    @Persisted
    private var isInverted = false

    @DescSynced
    var controlMachine: Boolean = false

    override fun createUIWidget(): Widget {
        if (!controlMachine) return super.createUIWidget()
        val group = WidgetGroup(Position.ORIGIN, Size(176, 112))
        group.addWidget(TextBoxWidget(8, 35, 65, listOf(LocalizationUtils.format("gui.gtladditions.min_temperature", "K"))))
        group.addWidget(TextBoxWidget(8, 80, 65, listOf(LocalizationUtils.format("gui.gtladditions.max_temperature", "K"))))
        group.addWidget(
            TextFieldWidget(
                80,
                35,
                85,
                18,
                { min.toString() },
                { min = Mth.clamp(it.toInt(), 48000, 105000) }
            ).setNumbersOnly(48000, 105000)
        )
        group.addWidget(
            TextFieldWidget(
                80,
                80,
                85,
                18,
                { max.toString() },
                { max = Mth.clamp(it.toInt(), 48000, 105000) }
            ).setNumbersOnly(48000, 105000)
        )
        group.addWidget(object : ToggleButtonWidget(
            8,
            8,
            20,
            20,
            GuiTextures.INVERT_REDSTONE_BUTTON,
            { this.isInverted },
            { isInverted = it }
        ) {
            override fun updateScreen() {
                super.updateScreen()
                setHoverTooltips(LangHandler.getMultiLang("gui.gtceu.vientiane_transcription_node.invert." + (if (isPressed) "enabled" else "disabled")).map { it.`kjs$self`() })
            }
        })
        return group
    }

    fun update(signal: Int) {
        val output = RedstoneUtil.computeRedstoneBetweenValues(signal.toLong(), max.toFloat(), min.toFloat(), isInverted)
        if (redStoneSignal != output) setRedStoneSignal(output)
    }

    fun setRedStoneSignal(signal: Int) {
        this.redStoneSignal = signal
        updateSignal()
    }

    override fun getOutputSignal(side: Direction?) = if (side == frontFacing.opposite) redStoneSignal else 0

    override fun canConnectRedstone(side: Direction) = false

    override fun getFieldHolder() = MANAGED_FIELD_HOLDER

    companion object {
        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(VientianeTranscriptionNode::class.java, MultiblockPartMachine.MANAGED_FIELD_HOLDER)
    }
}
