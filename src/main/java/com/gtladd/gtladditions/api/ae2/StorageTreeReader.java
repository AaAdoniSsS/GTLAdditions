package com.gtladd.gtladditions.api.ae2;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import it.unimi.dsi.fastutil.objects.*;
import org.agrona.collections.ObjLongConsumer;
import org.agrona.collections.Object2LongHashMap;
import org.agrona.collections.ObjectHashSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.ToLongFunction;

final class StorageTreeReader {

    private record Pending(MEStorage storage, @Nullable IMEStorageFilter gate) {}

    private record CombinedGate(IMEStorageFilter outer, IMEStorageFilter inner) implements IMEStorageFilter {

        @Override
        public boolean canReport(AEKey key) {
            return outer.canReport(key) && inner.canReport(key);
        }
    }

    private final ArrayDeque<Pending> queue = new ArrayDeque<>();
    private final List<MEStorage> children = new ObjectArrayList<>(4);
    private final Set<MEStorage> seen = Collections.newSetFromMap(new IdentityHashMap<>());
    private final ObjLongConsumer<AEKey> sink = new ObjLongConsumer<>() {

        @Override
        public void accept(AEKey key, long amount) {
            if (amount <= 0 || !keys.contains(key)) return;
            var gate = StorageTreeReader.this.gate;
            if (gate != null && !gate.canReport(key)) return;
            accumulate(key, amount);
        }
    };

    private ObjectHashSet<AEKey> keys;
    private Object2LongHashMap<AEKey> out;
    private @Nullable IMEStorageFilter gate;

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
                gate = gate == null ? filter : new CombinedGate(gate, filter);
            }
            this.gate = gate;

            if (storage instanceof IMEStorageNode node) {
                children.clear();
                node.collectChildStorages(children);
                for (var child : children) {
                    if (child != null) queue.add(new Pending(child, gate));
                }
                continue;
            }
            readLeaf(storage);
        }
    }

    private void readLeaf(MEStorage storage) {
        if (storage instanceof IMEStorage leaf) {
            var map = leaf.getStorageMap();
            if (map != null) {
                readTable(map.size(), map::getLong, Object2LongMaps.fastIterable(map));
                return;
            }
            var counter = leaf.getAvailableCounter();
            if (counter != null) {
                readTable(counter.size(), counter::get, counter);
                return;
            }
            leaf.forEachAvailableStack(sink);
            return;
        }
        var counter = new KeyCounter();
        storage.getAvailableStacks(counter);
        var gate = this.gate;
        for (var key : keys) {
            if (gate != null && !gate.canReport(key)) continue;
            accumulate(key, counter.get(key));
        }
    }

    private void readTable(int entries, ToLongFunction<AEKey> lookup, Iterable<Object2LongMap.Entry<AEKey>> iterable) {
        var gate = this.gate;
        if (keys.size() <= entries) {
            for (var key : keys) {
                if (gate != null && !gate.canReport(key)) continue;
                accumulate(key, lookup.applyAsLong(key));
            }
        } else {
            for (var entry : iterable) {
                sink.accept(entry.getKey(), entry.getLongValue());
            }
        }
    }

    private void accumulate(AEKey key, long amount) {
        if (amount <= 0) return;
        long current = out.getValue(key);
        if (current <= 0) {
            out.put(key, amount);
            return;
        }
        long sum = current + amount;
        out.put(key, sum < 0 ? Long.MAX_VALUE : sum);
    }
}
