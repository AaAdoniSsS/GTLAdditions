package com.gtladd.gtladditions.common;

import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;

import com.gtladd.gtladditions.api.async.AsyncFluidTransform;

public final class ForgeEvent {

    public static void onLevelUnload(LevelEvent.Unload event) {
        AsyncFluidTransform.onLevelUnload((Level) event.getLevel());
    }
}
