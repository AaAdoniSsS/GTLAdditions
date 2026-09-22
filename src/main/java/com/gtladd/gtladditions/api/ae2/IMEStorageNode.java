package com.gtladd.gtladditions.api.ae2;

import appeng.api.storage.MEStorage;

import java.util.Collection;

public interface IMEStorageNode {

    void collectChildStorages(Collection<MEStorage> out);
}
