package com.gtladd.gtladditions.api.ae2;

import appeng.api.networking.IGrid;
import appeng.api.stacks.AEKey;
import org.agrona.collections.Object2LongHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class AE2KeyCounterCache {

    public static Object2LongHashMap<AEKey> getAmounts(@Nullable IGrid grid, Collection<? extends AEKey> keys) {
        if (keys.isEmpty() || grid == null) return GridStockCache.EMPTY_O2LMAP;
        return GridStockCache.getAmounts(grid.getStorageService(), keys, GridStockCache.LEDGER_MAX_AGE_TICKS);
    }
}
