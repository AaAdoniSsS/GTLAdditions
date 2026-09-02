package com.gtladd.gtladditions.api.manage;

import org.gtlcore.gtlcore.common.machine.multiblock.electric.HarmonyMachine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import com.gtladd.gtladditions.common.machine.multiblock.controller.ArcanicAstrograph;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.longs.LongArraySet;

public class HarmonyManager {

    private static final Int2ObjectOpenHashMap<LongArraySet> MACHINE = new Int2ObjectOpenHashMap<>(4);

    public static void update(ServerLevel level, BlockPos pos) {
        MACHINE.computeIfAbsent(level.dimension().location().hashCode(), v -> new LongArraySet()).add(pos.asLong());
    }

    public static void remove(ServerLevel level, BlockPos pos) {
        MACHINE.get(level.dimension().location().hashCode()).remove(pos.asLong());
    }

    public static IntIntPair getMachineCount(Level level) {
        int m1 = 0, m2 = 0;
        for (var it = MACHINE.int2ObjectEntrySet().fastIterator(); it.hasNext();) {
            for (var l : it.next().getValue()) {
                if (MetaMachine.getMachine(level, BlockPos.of(l)) instanceof HarmonyMachine h && h.recipeLogic.isWorking()) {
                    if (h instanceof ArcanicAstrograph) m2++;
                    else m1++;
                }
            }
        }
        return IntIntPair.of(m1, m2);
    }
}
