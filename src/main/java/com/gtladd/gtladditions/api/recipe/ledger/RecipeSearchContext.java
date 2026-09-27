package com.gtladd.gtladditions.api.recipe.ledger;

import org.gtlcore.gtlcore.api.machine.trait.IRecipeCapabilityMachine;
import org.gtlcore.gtlcore.api.machine.trait.MEPatternRecipeHandlePart;
import org.gtlcore.gtlcore.api.machine.trait.RecipeHandlePart;
import org.gtlcore.gtlcore.api.recipe.RecipeResult;
import org.gtlcore.gtlcore.common.machine.multiblock.part.ae.MEPatternBufferPartMachineBase;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.resources.ResourceLocation;

import appeng.api.config.Actionable;
import com.gtladd.gtladditions.api.ae2.GridStockCache;
import com.gtladd.gtladditions.api.recipe.lookup.RecipeTreeGeneration;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMaps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public final class RecipeSearchContext {

    final WorkableElectricMultiblockMachine machine;
    final LedgerSnapshot snapshot;
    final RecipeAllocator allocator;

    double failRatio = -1.0;
    boolean failReported;

    @Nullable
    @Setter
    GTRecipe originRecipe;

    private static final int SEARCH_HIT_CAPACITY = 12;

    private static final int SEARCH_HIT_MAX_WEIGHT = 64;

    final ObjectArrayList<GTRecipe> searchHits = new ObjectArrayList<>(SEARCH_HIT_CAPACITY);
    final IntArrayList searchHitWeights = new IntArrayList(SEARCH_HIT_CAPACITY);
    final ObjectArrayList<HitDomain> searchHitDomains = new ObjectArrayList<>(SEARCH_HIT_CAPACITY);
    int hitTreeGeneration = RecipeTreeGeneration.current();

    record HitDomain(@Nullable MEPatternRecipeHandlePart part, int slot, @Nullable RecipeHandlePart handle) {

        static HitDomain of(SupplyPart supply) {
            return supply.patternPart != null ? new HitDomain(supply.patternPart, supply.slot, null) : new HitDomain(null, -1, supply.handlePart);
        }
    }

    @Nullable
    Reference2ObjectOpenHashMap<SupplyPart, Reference2LongOpenHashMap<GTRecipe>> domainCapCache;
    final Object2ObjectOpenHashMap<ResourceLocation, SupplyPart> matchedDomains = new Object2ObjectOpenHashMap<>();
    boolean stale;
    @Getter
    boolean cycleActive;

    public RecipeSearchContext(WorkableElectricMultiblockMachine machine) {
        if (!(machine instanceof IRecipeCapabilityMachine rcm)) {
            throw new IllegalArgumentException("machine must implement IRecipeCapabilityMachine");
        }
        this.machine = machine;
        this.snapshot = new LedgerSnapshot(rcm);
        this.allocator = new RecipeAllocator(machine);
    }

    public void beginCycle() {
        this.stale = false;
        this.snapshot.beginCycle();
        this.failRatio = -1.0;
        this.failReported = false;
        this.originRecipe = null;
        clearSearchCaches();
        this.cycleActive = true;
    }

    public void endCycle() {
        this.cycleActive = false;
        this.originRecipe = null;
        clearSearchCaches();
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

    void recordFail(double ratio) {
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
        clearSearchCaches();
        snapshot.clearNetworkDeductions();
        for (var grid : snapshot.grids()) {
            GridStockCache.invalidate(grid.getStorageService());
        }
    }

    void clearSearchCaches() {
        domainCapCache = null;
        matchedDomains.clear();
    }

    boolean hitTableValid() {
        int generation = RecipeTreeGeneration.current();
        if (generation == hitTreeGeneration) return true;
        hitTreeGeneration = generation;
        clearSearchHits();
        return false;
    }

    void clearSearchHits() {
        searchHits.clear();
        searchHitWeights.clear();
        searchHitDomains.clear();
    }

    @Nullable
    public GTRecipe pickSearchHit(Predicate<GTRecipe> canHandle) {
        if (stale || searchHits.isEmpty() || !hitTableValid()) return null;
        buildSnapshot();
        ensureLedger();
        int used = -1;
        for (int i = 0; i < searchHits.size();) {
            var domain = searchHitDomains.get(i);
            var supply = snapshot.supplyOf(domain.part(), domain.slot(), domain.handle());
            if (supply == null) {
                removeHit(i);
                continue;
            }
            if (canHandle.test(searchHits.get(i)) && tryPlanOn(searchHits.get(i), 1, supply) != null) {
                used = i;
                break;
            }
            i++;
        }
        if (used < 0) {
            for (int i = searchHits.size() - 1; i >= 0; i--) demoteHit(i);
            return null;
        }
        var hit = searchHits.get(used);
        raiseHit(used);
        return hit;
    }

    public void recordSearchHit(@Nullable GTRecipe recipe, @Nullable List<PartLedger> domain) {
        if (recipe == null || domain == null || domain.isEmpty() || !hitTableValid()) return;
        var supply = domain.getFirst().owner;
        if (supply == null) return;
        int i = hitIndexOf(recipe);
        if (i < 0) {
            if (searchHits.size() >= SEARCH_HIT_CAPACITY) removeHit(searchHits.size() - 1);
            i = searchHits.size();
            searchHits.add(recipe);
            searchHitWeights.add(1);
            searchHitDomains.add(HitDomain.of(supply));
        } else {
            searchHitDomains.set(i, HitDomain.of(supply));
        }
        raiseHit(i);
    }

    void bumpHit(@Nullable GTRecipe recipe) {
        int i = hitIndexOf(recipe);
        if (i < 0) return;
        int weight = searchHitWeights.getInt(i);
        if (weight < SEARCH_HIT_MAX_WEIGHT) searchHitWeights.set(i, weight + 1);
        raiseHit(i);
    }

    int hitIndexOf(@Nullable GTRecipe recipe) {
        if (recipe == null) return -1;
        var id = recipe.getId();
        for (int i = 0, n = searchHits.size(); i < n; i++) {
            var other = searchHits.get(i);
            if (id != null ? id.equals(other.getId()) : other == recipe) return i;
        }
        return -1;
    }

    void raiseHit(int i) {
        while (i > 0 && searchHitWeights.getInt(i) >= searchHitWeights.getInt(i - 1)) {
            swapHits(i, i - 1);
            i--;
        }
    }

    void sinkHit(int i) {
        while (i + 1 < searchHits.size() && searchHitWeights.getInt(i) < searchHitWeights.getInt(i + 1)) {
            swapHits(i, i + 1);
            i++;
        }
    }

    void demoteHit(int i) {
        int weight = searchHitWeights.getInt(i);
        if (weight <= 1) return;
        searchHitWeights.set(i, weight - 1);
        sinkHit(i);
    }

    void swapHits(int a, int b) {
        var recipe = searchHits.set(a, searchHits.get(b));
        searchHits.set(b, recipe);
        int weight = searchHitWeights.set(a, searchHitWeights.getInt(b));
        searchHitWeights.set(b, weight);
        var domain = searchHitDomains.set(a, searchHitDomains.get(b));
        searchHitDomains.set(b, domain);
    }

    void removeHit(int i) {
        searchHits.remove(i);
        searchHitWeights.removeInt(i);
        searchHitDomains.remove(i);
    }

    @Nullable
    SupplyPart domainOf(GTRecipe recipe) {
        var id = recipe.getId();
        return id == null ? null : matchedDomains.get(id);
    }

    void bind(GTRecipe recipe, SupplyPart supply) {
        var id = recipe.getId();
        if (id != null) matchedDomains.put(id, supply);
    }

    @Nullable
    public ConsumePlan allocate(GTRecipe recipe, long want) {
        if (want <= 0 || stale) return null;
        buildSnapshot();
        ensureLedger();
        var supply = domainOf(recipe);
        if (supply == null) return null;
        long cap = poolCapOf(recipe, supply);
        if (cap <= 0) {
            recordFail(0.0);
            return null;
        }
        long p = Math.min(want, cap);
        for (int attempt = 0; attempt < 4; attempt++) {
            var res = allocator.allocateOn(supply, recipe, p);
            if (res.ratio() >= 1.0) {
                var plan = res.plan();
                double shrink = preCheckME(plan);
                if (shrink >= 1.0) {
                    plan.parallel = p;
                    return plan;
                }
                p = retryParallel(p, shrink);
            } else {
                if (res.shortfall() != null) recordFail(res.ratio());
                p = retryParallel(p, res.ratio());
            }
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
        var supply = domainOf(recipe);
        return supply == null ? null : tryPlanOn(recipe, parallel, supply);
    }

    @Nullable
    public ConsumePlan tryPlan(GTRecipe recipe, long parallel, @Nullable List<PartLedger> domain) {
        if (stale) return null;
        buildSnapshot();
        ensureLedger();
        if (domain == null) return tryPlan(recipe, parallel);
        var supply = domain.isEmpty() ? null : domain.getFirst().owner;
        return supply == null ? null : tryPlanOn(recipe, parallel, supply);
    }

    @Nullable
    public ConsumePlan tryPlan(GTRecipe recipe, long parallel, MEPatternRecipeHandlePart part, int slot) {
        if (stale) return null;
        buildSnapshot();
        ensureLedger();
        var supply = snapshot.slotSupply(part, slot);
        return supply == null ? null : tryPlanOn(recipe, parallel, supply);
    }

    @Nullable
    public MEPatternBufferPartMachineBase getPatternMachine(MEPatternRecipeHandlePart part) {
        return LedgerSnapshot.findPatternBuffer(part);
    }

    @Nullable
    ConsumePlan tryPlanOn(GTRecipe recipe, long parallel, SupplyPart supply) {
        long cap = poolCapOf(recipe, supply);
        if (cap <= 0) {
            recordFail(0.0);
            return null;
        }
        if (cap < parallel) return null;
        var plan = planOf(allocator.allocateOn(supply, recipe, parallel), parallel);
        if (plan != null) bind(recipe, supply);
        return plan;
    }

    @Nullable
    ConsumePlan planOf(RecipeAllocator.AllocationResult res, long parallel) {
        if (res.ratio() < 1.0) {
            if (res.shortfall() != null) recordFail(res.ratio());
            return null;
        }
        res.plan().parallel = parallel;
        return res.plan();
    }

    public double preCheckRecipe(GTRecipe recipe) {
        if (stale) return 0;
        buildSnapshot();
        ensureLedger();
        var supply = domainOf(recipe);
        if (supply == null) return 0;
        var res = allocator.allocateOn(supply, recipe, 1);
        if (res.ratio() >= 1.0) return Math.min(1.0, preCheckME(res.plan()));
        if (res.shortfall() != null) recordFail(res.ratio());
        return res.ratio();
    }

    public long getMaxParallel(GTRecipe recipe, long limit) {
        var plan = allocate(recipe, limit);
        return plan == null ? 0 : plan.parallel;
    }

    long poolCapOf(GTRecipe recipe, SupplyPart supply) {
        var byRecipe = domainCapCache == null ? null : domainCapCache.get(supply);
        if (byRecipe != null && byRecipe.containsKey(recipe)) return byRecipe.getLong(recipe);
        long cap = allocator.feasiblePoolCap(supply, recipe);
        if (byRecipe == null) {
            if (domainCapCache == null) domainCapCache = new Reference2ObjectOpenHashMap<>();
            domainCapCache.put(supply, byRecipe = new Reference2LongOpenHashMap<>());
        }
        byRecipe.put(recipe, cap);
        return cap;
    }

    public long getPoolParallel(GTRecipe recipe, long limit) {
        if (stale || limit <= 0) return 0;
        buildSnapshot();
        ensureLedger();
        var supply = domainOf(recipe);
        return supply == null ? 0 : Math.min(limit, poolCapOf(recipe, supply));
    }

    public void deductRecipe(GTRecipe consumed) {
        bumpHit(consumed);
        fixPatternCache(originRecipe, consumed);
        if (stale) return;
        buildSnapshot();
        ensureLedger();
        deductOnce(consumed);
    }

    public void deduct(@Nullable GTRecipe origin, GTRecipe consumed, ConsumePlan plan) {
        bumpHit(consumed);
        fixPatternCache(origin, consumed);
        deduct(plan);
    }

    boolean deductOnce(GTRecipe recipe) {
        var supply = domainOf(recipe);
        if (supply == null) return false;
        var res = allocator.allocateOn(supply, recipe, 1);
        if (res.ratio() < 1.0) return false;
        deduct(res.plan());
        return true;
    }

    public void deduct(ConsumePlan plan) {
        domainCapCache = null;
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
