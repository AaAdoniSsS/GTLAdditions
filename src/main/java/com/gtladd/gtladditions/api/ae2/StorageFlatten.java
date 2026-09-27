package com.gtladd.gtladditions.api.ae2;

import org.gtlcore.gtlcore.integration.ae2.storage.PreciseStorageAmount;
import org.gtlcore.gtlcore.mixin.ae2.storage.MEInventoryHandlerDisplayAccessor;

import appeng.api.storage.MEStorage;
import appeng.me.storage.CompositeStorage;
import appeng.me.storage.MEInventoryHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicLong;

public final class StorageFlatten {

    public record Entry(MEStorage storage, @Nullable IMEStorageFilter gate, boolean hidden) {}

    record Pending(MEStorage storage, @Nullable IMEStorageFilter gate, boolean hidden) {}

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
        var out = new ObjectArrayList<Entry>();
        var queue = new ArrayDeque<Pending>();
        var children = new ObjectArrayList<MEStorage>(4);
        var seenVisible = new ReferenceOpenHashSet<MEStorage>();
        var seenHidden = new ReferenceOpenHashSet<MEStorage>();

        root.collectChildStorages(children);
        for (var child : children) {
            if (child != null) queue.add(new Pending(child, null, false));
        }

        while (!queue.isEmpty()) {
            var pending = queue.poll();
            var storage = pending.storage();
            var hidden = pending.hidden();
            if (hidden) {
                if (!seenHidden.add(storage) || seenVisible.contains(storage)) continue;
            } else if (!seenVisible.add(storage)) {
                continue;
            }

            var gate = pending.gate();
            if (storage instanceof IMEStorageFilter filter) gate = IMEStorageFilter.and(gate, filter);

            if (isFolded(storage)) {
                if (storage instanceof MEInventoryHandlerDisplayAccessor accessor && !accessor.gtlcore$allowsDisplayExtraction()) {
                    hidden = true;
                }
                children.clear();
                ((IMEStorageNode) storage).collectChildStorages(children);
                for (var child : children) {
                    if (child != null) queue.add(new Pending(child, gate, hidden));
                }
                continue;
            }
            out.add(new Entry(storage, gate, hidden));
        }

        return new StorageFlatten(out.toArray(Entry[]::new));
    }

    private static boolean isFolded(MEStorage storage) {
        if (!(storage instanceof IMEStorageNode)) return false;
        if (storage instanceof PreciseStorageAmount) return false;
        return storage instanceof MEInventoryHandler || storage instanceof CompositeStorage;
    }
}
