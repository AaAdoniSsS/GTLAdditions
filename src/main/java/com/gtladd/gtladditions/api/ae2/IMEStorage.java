package com.gtladd.gtladditions.api.ae2;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import org.agrona.collections.ObjLongConsumer;
import org.jetbrains.annotations.Nullable;

public interface IMEStorage {

    @Nullable
    Object2LongMap<AEKey> getStorageMap();

    default @Nullable KeyCounter getAvailableCounter() {
        return null;
    }

    default void forEachAvailableStack(ObjLongConsumer<AEKey> sink) {
        var map = getStorageMap();
        if (map != null) {
            for (var entry : Object2LongMaps.fastIterable(map)) {
                sink.accept(entry.getKey(), entry.getLongValue());
            }
            return;
        }
        var counter = getAvailableCounter();
        if (counter != null) {
            for (var entry : counter) {
                sink.accept(entry.getKey(), entry.getLongValue());
            }
        }
    }
}
