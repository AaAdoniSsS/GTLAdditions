package com.gtladd.gtladditions.api.ae2;

import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.hooks.ticking.TickHandler;
import org.agrona.collections.Object2LongHashMap;
import org.agrona.collections.ObjectHashSet;

import java.util.Collection;
import java.util.WeakHashMap;

public final class GridStockCache {

    static final Object2LongHashMap<AEKey> EMPTY_O2LMAP = new Object2LongHashMap<>(0) {

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public boolean containsValue(long value) {
            return false;
        }

        @Override
        public long getOrDefault(Object key, long defaultValue) {
            return defaultValue;
        }

        @Override
        public Long get(Object key) {
            return 0L;
        }

        @Override
        public long getValue(AEKey key) {
            return 0;
        }

        @Override
        public Long put(AEKey key, Long value) {
            return value;
        }

        @Override
        public long put(AEKey key, long value) {
            return value;
        }

        @Override
        public boolean remove(Object key, Object value) {
            return false;
        }

        @Override
        public boolean remove(Object key, long value) {
            return false;
        }

        @Override
        public Long remove(Object key) {
            return 0L;
        }

        @Override
        public long removeKey(AEKey key) {
            return 0;
        }
    };

    static final class CacheEntry {

        long tick;
        boolean valid;
        final ObjectHashSet<AEKey> known = new ObjectHashSet<>();
        final Object2LongHashMap<AEKey> amounts = new Object2LongHashMap<>(-1);
    }

    static final WeakHashMap<IStorageService, CacheEntry> CACHE = new WeakHashMap<>();
    static final StorageTreeReader READER = new StorageTreeReader();

    public static Object2LongHashMap<AEKey> getAmounts(IStorageService service, Collection<? extends AEKey> keys, int maxAgeTicks) {
        if (keys.isEmpty()) return EMPTY_O2LMAP;
        long now = TickHandler.instance().getCurrentTick();
        synchronized (CACHE) {
            var entry = CACHE.computeIfAbsent(service, s -> new CacheEntry());
            ObjectHashSet<AEKey> missing = null;
            for (var key : keys) {
                if (entry.known.add(key)) {
                    if (missing == null) missing = new ObjectHashSet<>();
                    missing.add(key);
                }
            }
            if (!entry.valid || now - entry.tick > maxAgeTicks) {
                READER.read(service.getInventory(), entry.known, entry.amounts);
                entry.tick = now;
                entry.valid = true;
            } else if (missing != null) {
                READER.read(service.getInventory(), missing, entry.amounts);
            }
            return new Object2LongHashMap<>(entry.amounts);
        }
    }

    public static void invalidate(IStorageService service) {
        synchronized (CACHE) {
            var entry = CACHE.get(service);
            if (entry != null) entry.valid = false;
        }
    }

    public static long getSnapshotTick(IStorageService service) {
        synchronized (CACHE) {
            var entry = CACHE.get(service);
            return entry == null || !entry.valid ? Long.MAX_VALUE : entry.tick;
        }
    }
}
