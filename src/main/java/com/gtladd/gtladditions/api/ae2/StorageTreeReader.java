package com.gtladd.gtladditions.api.ae2;

import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import it.unimi.dsi.fastutil.objects.*;
import org.agrona.collections.ObjLongConsumer;
import org.agrona.collections.Object2LongHashMap;
import org.agrona.collections.ObjectHashSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Set;

final class StorageTreeReader {

    record Pending(MEStorage storage, @Nullable IMEStorageFilter gate) {}

    final ArrayDeque<Pending> queue = new ArrayDeque<>();
    final List<MEStorage> children = new ObjectArrayList<>(4);
    final Set<MEStorage> seen = new ReferenceOpenHashSet<>();
    final ObjLongConsumer<AEKey> sink = new ObjLongConsumer<>() {

        @Override
        public void accept(AEKey key, long amount) {
            if (amount <= 0 || !keys.contains(key)) return;
            var gate = StorageTreeReader.this.gate;
            if (gate != null && !gate.canReport(key)) return;
            accumulate(key, amount);
        }
    };

    ObjectHashSet<AEKey> keys;
    Object2LongHashMap<AEKey> out;
    @Nullable
    IMEStorageFilter gate;

    void read(MEStorage root, ObjectHashSet<AEKey> keys, Object2LongHashMap<AEKey> out) {
        if (keys.isEmpty()) return;
        this.keys = keys;
        this.out = out;
        for (var key : keys) out.put(key, 0L);
        this.gate = null;
        queue.clear();
        seen.clear();
        queue.add(new Pending(root, null));

        while (!queue.isEmpty()) {
            var pending = queue.poll();
            var storage = pending.storage();
            if (!seen.add(storage)) continue;
            var gate = pending.gate();
            if (storage instanceof IMEStorageFilter filter) {
                gate = IMEStorageFilter.and(gate, filter);
            }
            this.gate = gate;

            if (storage instanceof IMEStorageNode node) {
                var flatten = node.flattenCache();
                if (flatten != null) {
                    for (int i = 0, size = flatten.size(); i < size; i++) {
                        var entry = flatten.get(i);
                        queue.add(new Pending(entry.storage(), IMEStorageFilter.and(gate, entry.gate())));
                    }
                    continue;
                }
                children.clear();
                node.collectChildStorages(children);
                for (var child : children) {
                    if (child != null) queue.add(new Pending(child, gate));
                }
                continue;
            }
            readLeaf(storage, gate);
        }
    }

    void readLeaf(MEStorage storage, @Nullable IMEStorageFilter gate) {
        if (storage instanceof IMEStorage leaf) {
            var big = leaf.getInfinityMap();
            if (big != null) {
                for (var key : keys) {
                    if (gate != null && !gate.canReport(key)) continue;
                    var value = big.get(key);
                    if (value != null) accumulate(key, value.longValue());
                }
                return;
            }
            var map = leaf.getStorageMap();
            if (map != null) {
                for (var key : keys) {
                    if (gate != null && !gate.canReport(key)) continue;
                    accumulate(key, map.getLong(key));
                }
                return;
            }
            this.gate = gate;
            leaf.forEachAvailableStack(sink);
            return;
        }
        var counter = storage.getAvailableStacks();
        for (var key : keys) {
            if (gate != null && !gate.canReport(key)) continue;
            accumulate(key, counter.get(key));
        }
    }

    void accumulate(AEKey key, long amount) {
        if (amount <= 0) return;
        long current = out.getValue(key);
        if (current <= 0) {
            out.put(key, amount);
            return;
        } else if (current == Long.MAX_VALUE) {
            return;
        }
        long sum = current + amount;
        out.put(key, sum < 0 ? Long.MAX_VALUE : sum);
    }
}
