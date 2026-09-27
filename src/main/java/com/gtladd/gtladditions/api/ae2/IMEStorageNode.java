package com.gtladd.gtladditions.api.ae2;

import appeng.api.storage.MEStorage;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface IMEStorageNode {

    void collectChildStorages(Collection<MEStorage> out);

    default @Nullable StorageFlatten flattenCache() {
        return null;
    }
}
