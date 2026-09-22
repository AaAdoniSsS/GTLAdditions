package com.gtladd.gtladditions.common.machine.hatch

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity

import com.lowdragmc.lowdraglib.gui.util.ClickData

import net.minecraft.network.chat.Component

import com.hepdd.gtmthings.common.block.machine.multiblock.part.HugeDualHatchPartMachine

class HugeOutputDualHatch(holder: IMachineBlockEntity, tier: Int, io: IO, vararg args: Any?) :
    HugeDualHatchPartMachine(holder, tier, io, *args) {
    override fun attachConfigurators(configuratorPanel: ConfiguratorPanel) {
        val button = IFancyConfiguratorButton.Toggle(
            GuiTextures.BUTTON_POWER.getSubTexture(0.0f, 0.0f, 1.0f, 0.5f),
            GuiTextures.BUTTON_POWER.getSubTexture(0.0f, 0.5f, 1.0f, 0.5f),
            this::isWorkingEnabled
        ) { _: ClickData, pressed: Boolean -> this.isWorkingEnabled = pressed }.setTooltipsSupplier { pressed ->
            listOf(
                Component.translatable(
                    if (pressed) {
                        "behaviour.soft_hammer.enabled"
                    } else {
                        "behaviour.soft_hammer.disabled"
                    }
                )
            )
        }
        configuratorPanel.attachConfigurators(button)
    }
}
