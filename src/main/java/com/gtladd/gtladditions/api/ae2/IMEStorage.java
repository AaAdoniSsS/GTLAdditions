package com.gtladd.gtladditions.api.ae2;

import appeng.api.stacks.AEKey;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.agrona.collections.ObjLongConsumer;
import org.jetbrains.annotations.Nullable;

public interface IMEStorage {

    @Nullable
    default Object2ObjectMap<AEKey, ? extends Number> getInfinityMap() {
        return null;
    }

    @Nullable
    default Object2LongMap<AEKey> getStorageMap() {
        return null;
    }

    default void forEachAvailableStack(ObjLongConsumer<AEKey> sink) {}
}
