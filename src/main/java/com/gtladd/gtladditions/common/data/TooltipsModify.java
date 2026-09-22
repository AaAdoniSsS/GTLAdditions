package com.gtladd.gtladditions.common.data;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class TooltipsModify {

    public static void init() {
        MachineDefinition researchStation = GTResearchMachines.RESEARCH_STATION;
        researchStation.setTooltipBuilder(researchStation.getTooltipBuilder().andThen((itemStack, components) -> {
            components.add(Component.translatable("tooltip.gtladditions.cloud_systems"));
            components.add(Component.translatable("gui.gtladditions.modify").withStyle(style -> style.withColor(TooltipHelper.RAINBOW.getCurrent())));
        }));
    }

    public static void onItemTooltip(ItemTooltipEvent event) {
        if (FMLEnvironment.dist != Dist.CLIENT) return;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (!isCharge(id.getPath())) return;

        event.getToolTip().add(Component.translatable("gui.gtladditions.modify")
                .withStyle(style -> style.withColor(TooltipHelper.RAINBOW.getCurrent())));
    }

    private static boolean isCharge(String path) {
        return path.contains("naquadria_charge") || path.contains("leptonic_charge") || path.contains("quantum_chromodynamic_charge");
    }
}
