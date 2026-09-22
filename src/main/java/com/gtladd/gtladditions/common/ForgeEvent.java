package com.gtladd.gtladditions.common;

import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;

import com.gtladd.gtladditions.api.async.AsyncFluidTransform;
import com.gtladd.gtladditions.api.async.StagedBlastManager;

public class ForgeEvent {

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
}
