package com.gtladd.gtladditions.utils;

public final class SpectralTuning {

    public static final double[] YIELD_FACTOR = { 1.5D, 2.3D, 3.2D, 4.0D };
    public static final double[] ENERGY_FACTOR = { 0.8D, 0.6D, 0.2D, 0.05D };
    public static final int[] RANGE = { 6, 9, 11, 13 };
    public static final int MAX_OVERCLOCK_LEVEL = 15;

    private SpectralTuning() {}

    private static int clamp(int tier) {
        return tier < 0 || tier > 3 ? 0 : tier;
    }

    public static double yieldFactor(int tier) {
        return YIELD_FACTOR[clamp(tier)];
    }

    public static double energyFactor(int tier) {
        return ENERGY_FACTOR[clamp(tier)];
    }

    public static int range(int tier) {
        return RANGE[clamp(tier)];
    }

    public static int overclockLevel(long demandPerTick, long availablePower, long maxOutputFactor) {
        if (demandPerTick <= 0L || availablePower < demandPerTick) return 0;
        int level = 0;
        long demand = demandPerTick;
        long output = 1L;
        while (level < MAX_OVERCLOCK_LEVEL && demand <= availablePower / 4L && output <= maxOutputFactor / 2L) {
            demand *= 4L;
            output *= 2L;
            level++;
        }
        return level;
    }
}
