package com.gtladd.gtladditions.api.ae2;

import org.gtlcore.gtlcore.integration.ae2.storage.PreciseStorageAmount;
import org.gtlcore.gtlcore.mixin.ae2.storage.MEInventoryHandlerDisplayAccessor;
import org.gtlcore.gtlcore.utils.NumberUtils;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.agrona.collections.Object2ObjectHashMap;
import org.agrona.collections.ObjectHashSet;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class QueryReader {

    public static final QueryReader INSTANCE = new QueryReader();

    record Pending(MEStorage storage, @Nullable IMEStorageFilter gate) {}

    final ArrayDeque<Pending> queue = new ArrayDeque<>();
    final ObjectArrayList<MEStorage> children = new ObjectArrayList<>(4);
    final ObjectHashSet<MEStorage> seen = new ObjectHashSet<>();
    final Object2ObjectHashMap<AEKey, BigInteger> exact = new Object2ObjectHashMap<>();
    final Object2ObjectHashMap<AEKey, BigInteger> fallback = new Object2ObjectHashMap<>();

    Collection<? extends AEKey> keys = List.of();
    @Nullable
    IActionSource source;

    public Map<AEKey, BigInteger> query(MEStorage root, List<AEKey> keys, @Nullable IActionSource source) {
        if (root == null || keys == null || keys.isEmpty()) return Map.of();
        this.keys = keys;
        this.source = source;
        this.exact.clear();
        this.fallback.clear();
        this.queue.clear();
        this.seen.clear();
        this.queue.add(new Pending(root, null));

        while (!this.queue.isEmpty()) {
            var pending = this.queue.poll();
            var storage = pending.storage();
            if (!this.seen.add(storage)) continue;
            var gate = pending.gate();
            if (storage instanceof IMEStorageFilter filter) {
                gate = IMEStorageFilter.and(gate, filter);
            }

            if (storage instanceof PreciseStorageAmount precise) {
                this.readPrecise(precise, storage, gate);
                continue;
            }

            if (storage instanceof IMEStorageNode node) {
                var flatten = node.flattenCache();
                if (flatten != null) {
                    var hideFiltered = this.source != null;
                    for (int i = 0, size = flatten.size(); i < size; i++) {
                        var entry = flatten.get(i);
                        if (hideFiltered && entry.hidden()) continue;
                        this.queue.add(new Pending(entry.storage(), IMEStorageFilter.and(gate, entry.gate())));
                    }
                    continue;
                }
                if (storage instanceof MEInventoryHandlerDisplayAccessor filter) {
                    if (this.source != null) {
                        if (!filter.gtlcore$allowsDisplayExtraction()) continue;
                        if (filter.gtlcore$filtersDisplayExtraction() && !gateAllows(gate)) continue;
                    }
                }
                this.children.clear();
                node.collectChildStorages(this.children);
                for (var child : this.children) {
                    if (child != null) this.queue.add(new Pending(child, gate));
                }
                continue;
            }
            this.readLeaf(storage, gate);
        }
        return this.resolve();
    }

    void readPrecise(PreciseStorageAmount precise, MEStorage storage, @Nullable IMEStorageFilter gate) {
        for (var key : this.keys) {
            if (gate != null && !gate.canReport(key)) continue;
            var amount = precise.getExactStoredAmount(key);
            if (amount == null || amount.signum() <= 0) continue;
            if (this.source != null) {
                long extractable = storage.extract(key, Long.MAX_VALUE, Actionable.SIMULATE, this.source);
                if (extractable < Long.MAX_VALUE) {
                    if (extractable <= 0) continue;
                    amount = BigInteger.valueOf(extractable);
                }
            }
            this.exact.merge(key, amount, BigInteger::add);
        }
    }

    void readLeaf(MEStorage storage, @Nullable IMEStorageFilter gate) {
        if (storage instanceof IMEStorage table) {
            var map = table.getStorageMap();
            if (map != null) {
                for (var key : this.keys) {
                    if (gate != null && !gate.canReport(key)) continue;
                    this.accept(key, map.getLong(key));
                }
                return;
            }
        }
        var counter = storage.getAvailableStacks();
        for (var key : this.keys) {
            if (gate != null && !gate.canReport(key)) continue;
            this.accept(key, counter.get(key));
        }
    }

    void accept(AEKey key, long amount) {
        if (amount <= 0) return;
        if (this.source != null) this.exact.merge(key, BigInteger.valueOf(amount), BigInteger::add);
        else this.fallback.merge(key, BigInteger.valueOf(amount), BigInteger::add);
    }

    Map<AEKey, BigInteger> resolve() {
        if (!this.exact.isEmpty()) this.fallback.putAll(this.exact);
        this.fallback.values().removeIf(v -> v.compareTo(NumberUtils.BIG_INTEGER_MAX_LONG) <= 0);
        return new Object2ObjectHashMap<>(this.fallback);
    }

    boolean gateAllows(@Nullable IMEStorageFilter gate) {
        if (gate == null) return true;
        for (var key : this.keys) if (gate.canReport(key)) return true;
        return false;
    }
}
