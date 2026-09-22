package com.gtladd.gtladditions.api.recipe.ledger;

import org.gtlcore.gtlcore.utils.NumberUtils;

import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapItemStackIngredient;

import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import org.agrona.collections.Object2LongHashMap;
import org.agrona.collections.ObjectLongToLongFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class PartLedger {

    static final int MAX_ENTRIES = 64;
    static final int MAX_PROBE_CACHE = 256;
    static final int MAX_LIST_CACHE = 64;

    final List<ContentEntry> entries = new ObjectArrayList<>();
    final Object2LongHashMap<AbstractMapIngredient> variantOwners = new Object2LongHashMap<>(0);

    final LedgerCacheBudget cacheBudget;
    @Nullable
    Int2LongOpenHashMap itemHashMask;
    @Nullable
    Reference2LongOpenHashMap<AbstractMapIngredient> probeMaskCache;
    @Nullable
    Reference2LongOpenHashMap<List<AbstractMapIngredient>> listMaskCache;

    PartLedger(@NotNull LedgerCacheBudget cacheBudget) {
        this.cacheBudget = cacheBudget;
    }

    public boolean addEntry(ContentEntry entry) {
        if (entries.size() >= MAX_ENTRIES) return false;
        invalidateMaskCaches();
        int bit = entries.size();
        entries.add(entry);
        long n = 1L << bit;
        for (var v : entry.variants) {
            variantOwners.compute(v, (ObjectLongToLongFunction<? super AbstractMapIngredient>) (a, l) -> l | n);
            if (v instanceof MapItemStackIngredient) addItemHashMask(v, n);
        }
        return true;
    }

    void addItemHashMask(@NotNull AbstractMapIngredient variant, long bit) {
        var hm = itemHashMask;
        if (hm == null) itemHashMask = hm = new Int2LongOpenHashMap(8, 0.3f);
        int h = variant.hashCode();
        hm.put(h, hm.get(h) | bit);
    }

    public int ownerCount() {
        return entries.size();
    }

    void reset() {
        invalidateMaskCaches();
        entries.clear();
        variantOwners.clear();
        if (itemHashMask != null) itemHashMask.clear();
    }

    @Nullable
    public ContentEntry entry(int bit) {
        return bit >= 0 && bit < entries.size() ? entries.get(bit) : null;
    }

    public long ownerMaskOf(AbstractMapIngredient variant) {
        var cache = probeMaskCache;
        if (cache != null && cache.containsKey(variant)) return cache.getLong(variant);
        long mask = variantOwners.getValue(variant);
        if (mask == 0) mask = scanOwnerMask(variant);
        cacheProbe(variant, mask, cache);
        return mask;
    }

    public long ownerMask(List<AbstractMapIngredient> variants) {
        var cache = listMaskCache;
        if (cache != null && cache.containsKey(variants)) return cache.getLong(variants);
        long mask = 0;
        for (var v : variants) {
            long m = variantOwners.getValue(v);
            if (m != 0) mask |= m;
            else mask |= ownerMaskOf(v);
        }
        cacheList(variants, mask, cache);
        return mask;
    }

    long scanOwnerMask(AbstractMapIngredient variant) {
        if (!(variant instanceof MapItemStackIngredient)) return 0L;
        var hm = itemHashMask;
        if (hm == null) return 0L;
        long candidates = hm.get(variant.hashCode());
        long mask = 0;
        while (candidates != 0) {
            int bit = Long.numberOfTrailingZeros(candidates);
            candidates &= candidates - 1;
            var entry = entries.get(bit);
            for (var v : entry.variants) {
                if (v.equals(variant) || variant.equals(v)) {
                    mask |= 1L << bit;
                    break;
                }
            }
        }
        return mask;
    }

    void cacheProbe(AbstractMapIngredient variant, long mask,
                    @Nullable Reference2LongOpenHashMap<AbstractMapIngredient> cache) {
        if (cache != null && cache.size() >= MAX_PROBE_CACHE) return;
        if (!cacheBudget.tryAcquire()) return;
        if (cache == null) probeMaskCache = cache = new Reference2LongOpenHashMap<>(12, 0.3f);
        cache.put(variant, mask);
    }

    void cacheList(List<AbstractMapIngredient> variants, long mask,
                   @Nullable Reference2LongOpenHashMap<List<AbstractMapIngredient>> cache) {
        if (cache != null && cache.size() >= MAX_LIST_CACHE) return;
        if (!cacheBudget.tryAcquire()) return;
        if (cache == null) listMaskCache = cache = new Reference2LongOpenHashMap<>(12, 0.3f);
        cache.put(variants, mask);
    }

    void invalidateMaskCaches() {
        var probes = probeMaskCache;
        if (probes != null) {
            cacheBudget.refund(probes.size());
            probeMaskCache = null;
        }
        var lists = listMaskCache;
        if (lists != null) {
            cacheBudget.refund(lists.size());
            listMaskCache = null;
        }
    }

    public long pool(long mask) {
        long total = 0;
        while (mask != 0) {
            int bit = Long.numberOfTrailingZeros(mask);
            mask &= mask - 1;
            var e = entries.get(bit);
            if (e.scalesWithParallel && e.amount > 0) total = NumberUtils.saturatedAdd(total, e.amount);
        }
        return total;
    }

    public boolean hasNotConsumedSupplyEntry(long mask) {
        while (mask != 0) {
            int bit = Long.numberOfTrailingZeros(mask);
            mask &= mask - 1;
            if (entries.get(bit).notConsumedSupply) return true;
        }
        return false;
    }

    public boolean hasUsableExistenceEntry(long mask) {
        boolean anyReal = false;
        while (mask != 0) {
            int bit = Long.numberOfTrailingZeros(mask);
            mask &= mask - 1;
            var e = entries.get(bit);
            if (e.scalesWithParallel) return false;
            if (!e.notConsumedSupply) anyReal = true;
        }
        return anyReal;
    }
}
