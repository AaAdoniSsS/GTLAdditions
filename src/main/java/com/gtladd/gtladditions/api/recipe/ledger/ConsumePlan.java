package com.gtladd.gtladditions.api.recipe.ledger;

import org.gtlcore.gtlcore.utils.NumberUtils;

import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.List;

public final class ConsumePlan {

    final List<PlannedTake> takes = new ObjectArrayList<>();
    final Reference2ObjectOpenHashMap<IGrid, Object2LongOpenHashMap<AEKey>> meDemand = new Reference2ObjectOpenHashMap<>();
    final Reference2ObjectOpenHashMap<IGrid, IActionSource> meSources = new Reference2ObjectOpenHashMap<>();
    public long parallel = 1;

    void addTake(ContentEntry entry, long amount) {
        takes.add(new PlannedTake(entry, amount));
        if (entry.aeKey != null && entry.grid != null && entry.source != null) {
            meDemand.computeIfAbsent(entry.grid, g -> new Object2LongOpenHashMap<>())
                    .computeLongIfPresent(entry.aeKey, (aeKey, aLong) -> NumberUtils.saturatedAdd(aLong, amount));
            meSources.putIfAbsent(entry.grid, entry.source);
        }
    }

    record PlannedTake(ContentEntry entry, long amount) {}
}
