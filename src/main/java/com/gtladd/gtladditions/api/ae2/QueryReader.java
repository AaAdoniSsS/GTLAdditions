package com.gtladd.gtladditions.api.ae2;

import org.gtlcore.gtlcore.integration.ae2.storage.PreciseStorageAmount;
import org.gtlcore.gtlcore.mixin.ae2.storage.MEInventoryHandlerDisplayAccessor;
import org.gtlcore.gtlcore.utils.NumberUtils;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.agrona.collections.Object2ObjectHashMap;
import org.agrona.collections.ObjectHashSet;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;

public final class QueryReader {

    public static final QueryReader INSTANCE = new QueryReader();

    record Pending(MEStorage storage, ObjectHashSet<AEKey> keys) {}

    final ArrayDeque<Pending> queue = new ArrayDeque<>();
    final ObjectArrayList<MEStorage> children = new ObjectArrayList<>(4);
    final Reference2ObjectOpenHashMap<MEStorage, ObjectHashSet<AEKey>> seenByKey = new Reference2ObjectOpenHashMap<>();
    final Object2ObjectHashMap<AEKey, BigInteger> exact = new Object2ObjectHashMap<>();
    final Object2ObjectHashMap<AEKey, BigInteger> fallback = new Object2ObjectHashMap<>();

    @Nullable
    IActionSource source;

    public Map<AEKey, BigInteger> query(MEStorage root, List<AEKey> keys, @Nullable IActionSource source) {
        if (root == null || keys == null || keys.isEmpty()) return Map.of();
        this.source = source;
        this.exact.clear();
        this.fallback.clear();
        this.queue.clear();
        this.seenByKey.clear();
        var rootKeys = new ObjectHashSet<AEKey>();
        for (var key : keys) rootKeys.add(key);
        this.queue.add(new Pending(root, rootKeys));

        while (!this.queue.isEmpty()) {
            var pending = this.queue.poll();
            var storage = pending.storage();
            var delta = this.takeUnseen(storage, pending.keys());
            if (delta.isEmpty()) continue;

            if (storage instanceof PreciseStorageAmount precise) {
                this.readPrecise(precise, storage, delta);
                continue;
            }

            if (storage instanceof IMEStorageNode node) {
                var flatten = node.flattenCache();
                if (flatten != null) {
                    for (int i = 0, size = flatten.size(); i < size; i++) {
                        this.queue.add(new Pending(flatten.get(i).storage(), delta));
                    }
                    continue;
                }
                this.children.clear();
                node.collectChildStorages(this.children);
                for (var child : this.children) {
                    if (child != null) this.queue.add(new Pending(child, delta));
                }
                continue;
            }
            this.readLeaf(storage, delta);
        }
        return this.resolve();
    }

    ObjectHashSet<AEKey> takeUnseen(MEStorage storage, ObjectHashSet<AEKey> incoming) {
        var delta = new ObjectHashSet<AEKey>();
        var processed = this.seenByKey.computeIfAbsent(storage, ignored -> new ObjectHashSet<>());
        var filter = storage instanceof IMEStorageFilter f ? f : null;
        MEInventoryHandlerDisplayAccessor display = null;
        if (this.source != null && storage instanceof MEInventoryHandlerDisplayAccessor accessor) {
            if (!accessor.gtlcore$allowsDisplayExtraction()) return delta;
            if (accessor.gtlcore$filtersDisplayExtraction()) display = accessor;
        }
        for (var key : incoming) {
            if (filter != null && !filter.canReport(key)) continue;
            if (display != null && !display.gtlcore$canDisplay(key)) continue;
            if (processed.add(key)) delta.add(key);
        }
        return delta;
    }

    void readPrecise(PreciseStorageAmount precise, MEStorage storage, ObjectHashSet<AEKey> keys) {
        for (var key : keys) {
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

    void readLeaf(MEStorage storage, ObjectHashSet<AEKey> keys) {
        if (storage instanceof IMEStorage table) {
            var map = table.getStorageMap();
            if (map != null) {
                for (var key : keys) {
                    this.accept(key, map.getLong(key));
                }
                return;
            }
        }
        var counter = storage.getAvailableStacks();
        for (var key : keys) {
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
}
