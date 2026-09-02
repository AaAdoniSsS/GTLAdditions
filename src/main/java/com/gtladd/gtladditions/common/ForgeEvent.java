package com.gtladd.gtladditions.common;

import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.gtladd.gtladditions.GTLAdditions;
import com.gtladd.gtladditions.api.async.AsyncFluidTransform;

@Mod.EventBusSubscriber(modid = GTLAdditions.MOD_ID)
public class ForgeEvent {

    @SubscribeEvent
    public void onLevelUnload(LevelEvent.Unload event) {
        AsyncFluidTransform.onLevelUnload((Level) event.getLevel());
    }
}
