package com.gtladd.gtladditions.utils;

import org.gtlcore.gtlcore.common.machine.multiblock.electric.AdvancedInfiniteDrillMachine;
import org.gtlcore.gtlcore.common.machine.trait.AdvancedInfiniteDrillLogic;

import com.gregtechceu.gtceu.api.GTValues;

import com.gtladd.gtladditions.common.machine.hatch.OreProcessorHatch;

public final class DrillOverclock {

    private DrillOverclock() {}

    public static int level(AdvancedInfiniteDrillMachine machine, OreProcessorHatch hatch, int rate) {
        if (machine == null || hatch == null || rate <= 0 || machine.isRemote() || !hatch.getMatchAll()) return 0;
        AdvancedInfiniteDrillLogic logic = machine.getRecipeLogic();
        long total = 0L;
        long unit = 0L;
        for (long amount : logic.getVeinFluids().values()) {
            total += amount;
            if (amount > unit) unit = amount;
        }
        long demand = (long) ((GTValues.V[9] + total) * SpectralTuning.energyFactor(hatch.getSecondTier()) * rate);
        var container = machine.getEnergyContainer();
        long available = container.getInputVoltage() * Math.max(1L, container.getInputAmperage());
        long output = unit * rate;
        long maxOutputFactor = output <= 0L ? Long.MAX_VALUE : Math.max(1L, Long.MAX_VALUE / output);
        return SpectralTuning.overclockLevel(demand, available, maxOutputFactor);
    }

    public static long factor(int level) {
        return level <= 0 ? 1L : 1L << level;
    }
}
