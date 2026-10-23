package com.gtladd.gtladditions.api.ae2;

import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.agrona.collections.ObjLongConsumer;
import org.agrona.collections.Object2LongHashMap;
import org.agrona.collections.ObjectHashSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.List;

final class StorageTreeReader {

    record Pending(MEStorage storage, ObjectHashSet<AEKey> keys) {}

    final ArrayDeque<Pending> queue = new ArrayDeque<>();
    final List<MEStorage> children = new ObjectArrayList<>(4);
    final Reference2ObjectOpenHashMap<MEStorage, ObjectHashSet<AEKey>> seenByKey = new Reference2ObjectOpenHashMap<>();
    @Nullable
    ObjectHashSet<AEKey> activeLeafKeys;
    Object2LongHashMap<AEKey> out;

    final ObjLongConsumer<AEKey> sink = (key, amount) -> {
        if (amount <= 0 || activeLeafKeys == null || !activeLeafKeys.contains(key)) return;
        accumulate(key, amount);
    };

    void read(MEStorage root, ObjectHashSet<AEKey> keys, Object2LongHashMap<AEKey> out) {
        if (keys.isEmpty()) return;
        this.out = out;
        for (var key : keys) out.put(key, 0L);
        queue.clear();
        seenByKey.clear();
        activeLeafKeys = null;
        queue.add(new Pending(root, keys));

        while (!queue.isEmpty()) {
            var pending = queue.poll();
            var storage = pending.storage();
            var delta = takeUnseen(storage, pending.keys());
            if (delta.isEmpty()) continue;

            if (storage instanceof IMEStorageNode node) {
                var flatten = node.flattenCache();
                if (flatten != null) {
                    for (int i = 0, size = flatten.size(); i < size; i++) {
                        queue.add(new Pending(flatten.get(i).storage(), delta));
                    }
                    continue;
                }
                children.clear();
                node.collectChildStorages(children);
                for (var child : children) {
                    if (child != null) queue.add(new Pending(child, delta));
                }
                continue;
            }
            readLeaf(storage, delta);
        }
    }

    ObjectHashSet<AEKey> takeUnseen(MEStorage storage, ObjectHashSet<AEKey> incoming) {
        var delta = new ObjectHashSet<AEKey>();
        var processed = seenByKey.computeIfAbsent(storage, ignored -> new ObjectHashSet<>());
        var filter = storage instanceof IMEStorageFilter f ? f : null;
        for (var key : incoming) {
            if (filter != null && !filter.canReport(key)) continue;
            if (processed.add(key)) delta.add(key);
        }
        return delta;
    }

    void readLeaf(MEStorage storage, ObjectHashSet<AEKey> keys) {
        if (storage instanceof IMEStorage leaf) {
            var big = leaf.getInfinityMap();
            if (big != null) {
                for (var key : keys) {
                    var value = big.get(key);
                    if (value != null) accumulate(key, value.longValue());
                }
                return;
            }
            var map = leaf.getStorageMap();
            if (map != null) {
                for (var key : keys) accumulate(key, map.getLong(key));
                return;
            }
            activeLeafKeys = keys;
            leaf.forEachAvailableStack(sink);
            activeLeafKeys = null;
            return;
        }
        var counter = storage.getAvailableStacks();
        for (var key : keys) accumulate(key, counter.get(key));
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
