package com.gtladd.gtladditions.api.ae2;

import appeng.api.stacks.AEKey;
import org.jetbrains.annotations.Nullable;

public interface IMEStorageFilter {

    boolean canReport(AEKey key);

    static @Nullable IMEStorageFilter and(@Nullable IMEStorageFilter outer, @Nullable IMEStorageFilter inner) {
        if (outer == null) return inner;
        if (inner == null) return outer;
        return new Combined(outer, inner);
    }

    record Combined(IMEStorageFilter outer, IMEStorageFilter inner) implements IMEStorageFilter {

        @Override
        public boolean canReport(AEKey key) {
            return outer.canReport(key) && inner.canReport(key);
        }
    }
}
