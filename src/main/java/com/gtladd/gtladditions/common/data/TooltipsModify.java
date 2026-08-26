package com.gtladd.gtladditions.common.data;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;

import net.minecraft.network.chat.Component;

public class TooltipsModify {

    public static void init() {
        MachineDefinition researchStation = GTResearchMachines.RESEARCH_STATION;
        researchStation.setTooltipBuilder(researchStation.getTooltipBuilder().andThen((itemStack, components) -> {
            components.add(Component.translatable("tooltip.gtladditions.cloud_systems"));
            components.add(Component.translatable("gui.gtladditions.modify").withStyle(style -> style.withColor(TooltipHelper.RAINBOW.getCurrent())));
        }));
    }
}
