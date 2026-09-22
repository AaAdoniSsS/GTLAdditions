package com.gtladd.gtladditions.common;

import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;

import com.gtladd.gtladditions.api.async.AsyncFluidTransform;
import com.gtladd.gtladditions.api.async.StagedBlastManager;
import com.gtladd.gtladditions.utils.Registries;

public final class ForgeEvent {

    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!event.side.isServer()) return;
        StagedBlastManager.onLevelTick(event);
    }

    public static void onServerStarted(ServerStartedEvent event) {
        StagedBlastManager.restoreAll(event.getServer().getAllLevels());
    }

    public static void onLevelUnload(LevelEvent.Unload event) {
        AsyncFluidTransform.onLevelUnload((Level) event.getLevel());
        StagedBlastManager.onLevelUnload((Level) event.getLevel());
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getSide().isServer() || event.isCanceled()) return;
        var level = event.getLevel();
        var block = level.getBlockState(event.getPos()).getBlock();
        var stack = event.getItemStack();
        if (stack.getItem() == GTItems.QUANTUM_STAR.asItem()) {
            if (block.getDescriptionId().contains("naquadria_charge")) {
                if (!event.getEntity().isCreative()) stack.shrink(1);
                StagedBlastManager.spawn((ServerLevel) level, event.getPos(), 125);
            }
        } else if (stack.getItem() == GTItems.GRAVI_STAR.asItem()) {
            if (block.getDescriptionId().contains("leptonic_charge")) {
                if (!event.getEntity().isCreative()) stack.shrink(1);
                StagedBlastManager.spawn((ServerLevel) level, event.getPos(), 250);
            }
        } else if (stack.getItem() == Registries.INSTANCE.getGetItem("kubejs:unstable_star")) {
            if (block.getDescriptionId().contains("quantum_chromodynamic_charge")) {
                if (!event.getEntity().isCreative()) stack.shrink(1);
                StagedBlastManager.spawn((ServerLevel) level, event.getPos(), 500);
            }
        }
    }
}
