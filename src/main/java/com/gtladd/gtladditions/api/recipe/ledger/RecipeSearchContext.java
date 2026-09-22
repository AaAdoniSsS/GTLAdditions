package com.gtladd.gtladditions.api.recipe.ledger;

import org.gtlcore.gtlcore.api.machine.trait.IRecipeCapabilityMachine;
import org.gtlcore.gtlcore.api.recipe.RecipeResult;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import appeng.api.config.Actionable;
import com.gtladd.gtladditions.api.ae2.GridStockCache;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import org.agrona.collections.Object2LongHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class RecipeSearchContext {

    private final WorkableElectricMultiblockMachine machine;
    private final LedgerSnapshot snapshot;
    private final RecipeAllocator allocator;

    private double failRatio = -1.0;
    private boolean failReported;

    @Nullable
    @Setter
    private GTRecipe originRecipe;

    @Nullable
    @Getter
    @Setter
    private GTRecipe searchHit;

    @Nullable
    private Object2LongHashMap<GTRecipe> poolCapCache;
    private boolean stale;
    @Getter
    private boolean cycleActive;

    public RecipeSearchContext(WorkableElectricMultiblockMachine machine) {
        if (!(machine instanceof IRecipeCapabilityMachine rcm)) {
            throw new IllegalArgumentException("machine must implement IRecipeCapabilityMachine");
        }
        this.machine = machine;
        this.snapshot = new LedgerSnapshot(rcm);
        this.allocator = new RecipeAllocator(machine, snapshot);
    }

    public void beginCycle() {
        this.stale = false;
        this.snapshot.beginCycle();
        this.failRatio = -1.0;
        this.failReported = false;
        this.originRecipe = null;
        this.searchHit = null;
        this.poolCapCache = null;
        this.cycleActive = true;
    }

    public void endCycle() {
        this.cycleActive = false;
        this.originRecipe = null;
        this.searchHit = null;
        this.poolCapCache = null;
    }

    public void buildSnapshot() {
        snapshot.refresh();
    }

    public void ensureLedger() {
        snapshot.ensureAmounts();
    }

    public List<ObjectArrayList<PartLedger>> getSearchDomains() {
        return snapshot.searchDomains();
    }

    private void recordFail(double ratio) {
        if (ratio <= failRatio) return;
        failRatio = ratio;
        if (!failReported) {
            failReported = true;
            RecipeResult.of(machine, RecipeResult.FAIL_INPUT);
        }
    }

    public void markStale() {
        if (stale) return;
        stale = true;
        poolCapCache = null;
        snapshot.clearNetworkDeductions();
        for (var grid : snapshot.grids()) {
            GridStockCache.invalidate(grid.getStorageService());
        }
    }

    @Nullable
    public ConsumePlan allocate(GTRecipe recipe, long want) {
        if (want <= 0 || stale) return null;
        buildSnapshot();
        ensureLedger();
        if (!allocator.notConsumedGate(recipe)) {
            recordFail(0.0);
            return null;
        }
        long cap = poolCapOf(recipe);
        if (cap <= 0) {
            recordFail(0.0);
            return null;
        }
        long p = Math.min(want, cap);
        for (int attempt = 0; attempt < 4; attempt++) {
            ConsumePlan best = null;
            double bestRatio = 0;
            RecipeAllocator.AllocationResult bestRes = null;
            for (var supply : snapshot.supplies()) {
                var res = allocator.allocateOn(supply, recipe, p);
                if (res.ratio() >= 1.0) {
                    best = res.plan();
                    break;
                }
                if (res.ratio() > bestRatio) {
                    bestRatio = res.ratio();
                    bestRes = res;
                }
            }
            if (best != null) {
                double shrink = preCheckME(best);
                if (shrink >= 1.0) {
                    best.parallel = p;
                    return best;
                }
                p = retryParallel(p, shrink);
                if (p < 1) return null;
                continue;
            }
            if (bestRes != null && bestRes.shortfall() != null) {
                recordFail(bestRatio);
            }
            p = retryParallel(p, bestRatio);
            if (p < 1) return null;
        }
        return null;
    }

    private static long retryParallel(long p, double shrink) {
        long np = (long) Math.floor(p * shrink);
        return np >= p ? p - 1 : np;
    }

    @Nullable
    public ConsumePlan tryPlan(GTRecipe recipe, long parallel) {
        if (stale) return null;
        buildSnapshot();
        ensureLedger();
        return tryPlan0(recipe, parallel);
    }

    @Nullable
    private ConsumePlan tryPlan0(GTRecipe recipe, long parallel) {
        if (!allocator.notConsumedGate(recipe)) {
            recordFail(0.0);
            return null;
        }
        long cap = poolCapOf(recipe);
        if (cap <= 0) {
            recordFail(0.0);
            return null;
        }
        if (cap < parallel) return null;
        for (var supply : snapshot.supplies()) {
            var res = allocator.allocateOn(supply, recipe, parallel);
            if (res.ratio() >= 1.0) {
                res.plan().parallel = parallel;
                return res.plan();
            }
            if (res.shortfall() != null) {
                recordFail(res.ratio());
            }
        }
        return null;
    }

    public double preCheckRecipe(GTRecipe recipe) {
        if (stale) return 0;
        buildSnapshot();
        ensureLedger();
        return preCheckRecipe0(recipe);
    }

    private double preCheckRecipe0(GTRecipe recipe) {
        double bestRatio = 0;
        for (var supply : snapshot.supplies()) {
            var res = allocator.allocateOn(supply, recipe, 1);
            if (res.ratio() >= 1.0) {
                return Math.min(1.0, preCheckME(res.plan()));
            }
            if (res.shortfall() != null) {
                recordFail(res.ratio());
            }
            if (res.ratio() > bestRatio) bestRatio = res.ratio();
        }
        return bestRatio;
    }

    public long getMaxParallel(GTRecipe recipe, long limit) {
        var plan = allocate(recipe, limit);
        return plan == null ? 0 : plan.parallel;
    }

    public void noteMatchedParallel(GTRecipe recipe) {
        if (stale) return;
        buildSnapshot();
        ensureLedger();
        poolCapOf(recipe);
    }

    private long poolCapOf(GTRecipe recipe) {
        if (poolCapCache != null) {
            var v = poolCapCache.get(recipe);
            if (v != null) return v;
        }
        long cap = allocator.feasiblePoolCap(recipe);
        if (poolCapCache == null) poolCapCache = new Object2LongHashMap<>(-1);
        poolCapCache.put(recipe, cap);
        return cap;
    }

    public long getPoolParallel(GTRecipe recipe, long limit) {
        if (stale || limit <= 0) return 0;
        buildSnapshot();
        ensureLedger();
        return Math.min(limit, poolCapOf(recipe));
    }

    public void deductRecipe(GTRecipe consumed) {
        fixPatternCache(originRecipe, consumed);
        if (stale) return;
        buildSnapshot();
        ensureLedger();
        deductOnce(consumed);
    }

    public void deduct(@Nullable GTRecipe origin, GTRecipe consumed, ConsumePlan plan) {
        fixPatternCache(origin, consumed);
        deduct(plan);
    }

    private boolean deductOnce(GTRecipe recipe) {
        for (var supply : snapshot.supplies()) {
            var res = allocator.allocateOn(supply, recipe, 1);
            if (res.ratio() >= 1.0) {
                deduct(res.plan());
                return true;
            }
        }
        return false;
    }

    public void deduct(ConsumePlan plan) {
        poolCapCache = null;
        for (var take : plan.takes) {
            var e = take.entry();
            if (!e.deductOnConsume) continue;
            e.amount = Math.max(0, e.amount - take.amount());
            if (e.grid != null && e.aeKey != null) {
                snapshot.recordNetworkDeduction(e.grid, e.aeKey, take.amount());
            }
        }
    }

    private void fixPatternCache(@Nullable GTRecipe origin, GTRecipe consumed) {
        for (var part : ((IRecipeCapabilityMachine) machine).getMEPatternRecipeHandleParts()) {
            var buffer = LedgerSnapshot.findPatternBuffer(part);
            if (buffer == null) continue;
            var trait = buffer.getMETrait();
            for (var entry : Int2ReferenceMaps.fastIterable(trait.getSlot2RecipesCache())) {
                if (entry.getValue().remove(consumed) && origin != null) {
                    trait.setSlotCacheRecipe(entry.getIntKey(), origin);
                }
            }
        }
    }

    private double preCheckME(ConsumePlan plan) {
        if (plan.meDemand.isEmpty()) return 1.0;
        double shrink = 1.0;
        for (var it = plan.meDemand.reference2ObjectEntrySet().fastIterator(); it.hasNext();) {
            var g = it.next();
            var grid = g.getKey();
            var inv = grid.getStorageService().getInventory();
            var source = plan.meSources.get(grid);
            for (var e : Object2LongMaps.fastIterable(g.getValue())) {
                long need = e.getLongValue();
                long got = inv.extract(e.getKey(), need, Actionable.SIMULATE, source);
                if (got < need) {
                    double r = need > 0 ? (double) got / need : 1.0;
                    recordFail(r);
                    shrink = Math.min(shrink, r);
                }
            }
        }
        return shrink;
    }
}
