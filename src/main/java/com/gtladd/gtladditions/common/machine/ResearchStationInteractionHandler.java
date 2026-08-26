package com.gtladd.gtladditions.common.machine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.ResearchStationMachine;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.hepdd.gtmthings.utils.TeamUtil;

public class ResearchStationInteractionHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();

        if (!ResearchManager.isStackDataItem(stack, true)) return;

        MetaMachine machine = MetaMachine.getMachine(level, pos);
        if (!(machine instanceof ResearchStationMachine)) return;

        if (machine instanceof CloudOpticalDataMachine.ICloudTeamBindable bindable) {
            bindable.setTeamId(player.getUUID());
            if (player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("gui.gtladditions.cloud.bind_success", TeamUtil.GetName(sp)));
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();

        if (!ResearchManager.isStackDataItem(stack, true)) return;

        MetaMachine machine = MetaMachine.getMachine(level, pos);
        if (!(machine instanceof ResearchStationMachine)) return;

        if (machine instanceof CloudOpticalDataMachine.ICloudTeamBindable bindable) {
            bindable.setTeamId(null);
            if (player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("gui.gtladditions.cloud.unbind_success"));
            }
            event.setCanceled(true);
        }
    }
}
