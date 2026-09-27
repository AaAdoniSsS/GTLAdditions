package com.gtladd.gtladditions.mixin.mc;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;

import com.gtladd.gtladditions.config.ConfigHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {

    @Redirect(method = "tickChunks",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/level/NaturalSpawner;spawnForChunk(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/NaturalSpawner$SpawnState;ZZZ)V"))
    private void tickChunks(ServerLevel level, LevelChunk chunk, NaturalSpawner.SpawnState spawnState, boolean spawnFriendlies, boolean spawnMonsters, boolean forcedDespawn) {
        if (level.getGameTime() % ConfigHolder.INSTANCE.spawnRefreshInterval != 0L) return;
        NaturalSpawner.spawnForChunk(level, chunk, spawnState, spawnFriendlies, spawnMonsters, forcedDespawn);
    }
}
