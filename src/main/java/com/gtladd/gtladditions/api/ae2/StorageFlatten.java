package com.gtladd.gtladditions.api.ae2;

import appeng.api.storage.MEStorage;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicLong;

public final class StorageFlatten {

    public record Entry(MEStorage storage, @Nullable IMEStorageFilter gate, boolean hidden) {}

    static final AtomicLong TOPOLOGY_VERSION = new AtomicLong();

    public static void invalidateTopology() {
        TOPOLOGY_VERSION.incrementAndGet();
    }

    final Entry[] entries;
    final long version;

    StorageFlatten(Entry[] entries) {
        this.entries = entries;
        this.version = TOPOLOGY_VERSION.get();
    }

    public boolean isUpToDate() {
        return this.version == TOPOLOGY_VERSION.get();
    }

    public int size() {
        return this.entries.length;
    }

    public Entry get(int index) {
        return this.entries[index];
    }

    public static StorageFlatten build(IMEStorageNode root) {
        var children = new ObjectArrayList<MEStorage>();
        var entries = new ObjectArrayList<Entry>();
        root.collectChildStorages(children);
        for (var child : children) {
            if (child != null) {
                entries.add(new Entry(child, null, false));
            }
        }
        return new StorageFlatten(entries.toArray(Entry[]::new));
    }
}
