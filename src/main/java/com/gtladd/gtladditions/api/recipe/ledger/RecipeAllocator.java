package com.gtladd.gtladditions.api.recipe.ledger;

import org.gtlcore.gtlcore.api.recipe.IGTRecipe;
import org.gtlcore.gtlcore.utils.NumberUtils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import com.gtladd.gtladditions.api.recipe.ingredient.MapIngredientVariants;
import com.gtladd.gtladditions.utils.GTRecipeUtils;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Predicate;

final class RecipeAllocator {

    final WorkableElectricMultiblockMachine machine;
    final LedgerSnapshot snapshot;

    final ConsumePlan planScratch = new ConsumePlan();
    final Long2LongOpenHashMap takenScratch = new Long2LongOpenHashMap();
    long[] maskScratch = new long[0];

    RecipeAllocator(WorkableElectricMultiblockMachine machine, LedgerSnapshot snapshot) {
        this.machine = machine;
        this.snapshot = snapshot;
    }

    record AllocationResult(@Nullable ConsumePlan plan, double ratio, @Nullable Content shortfall) {

        static AllocationResult of(@Nullable ConsumePlan plan, double ratio) {
            return new AllocationResult(plan, ratio, null);
        }

        static AllocationResult fail(double ratio, Content content) {
            return new AllocationResult(null, ratio, content);
        }
    }

    AllocationResult allocateOn(SupplyPart supply, GTRecipe recipe, long parallel) {
        if (supply.patternPart != null) {
            AllocationResult best = null;
            for (int i = 0; i < supply.ledgers.size(); i++) {
                var res = allocateOnLedgers(supply.ledgers, i, i + 1, recipe, parallel);
                if (res.ratio >= 1.0) return res;
                if (best == null || res.ratio > best.ratio()) best = res;
            }
            return best != null ? best : AllocationResult.of(null, 0);
        }
        return allocateOnLedgers(supply.ledgers, 0, supply.ledgers.size(), recipe, parallel);
    }

    AllocationResult allocateOnLedgers(ObjectArrayList<PartLedger> ledgers, int from, int to,
                                       GTRecipe recipe, long parallel) {
        int n = to - from;
        var plan = obtainPlan();
        takenScratch.clear();
        double ratio = 1.0;
        Content shortfall = null;
        boolean hasInputs = false;
        if (maskScratch.length < n) maskScratch = new long[Math.max(n, maskScratch.length << 1)];
        var masks = maskScratch;
        for (var entry : recipe.inputs.entrySet()) {
            var cap = entry.getKey();
            for (var c : entry.getValue()) {
                if (c.chance <= 0) continue;
                long need = GTRecipeUtils.INSTANCE.amount(c, cap);
                if (need <= 0) continue;
                hasInputs = true;
                var variants = MapIngredientVariants.of(cap, c.content);
                long demanded = NumberUtils.saturatedMultiply(need, parallel);
                long pool = 0;
                long available = 0;
                boolean existence = false;
                for (int i = 0; i < n; i++) {
                    var ledger = ledgers.get(from + i);
                    masks[i] = ledger.ownerMask(variants);
                    pool = NumberUtils.saturatedAdd(pool, ledger.pool(masks[i]));
                    available = NumberUtils.saturatedAdd(available, availableOf(ledger, masks[i], i, takenScratch));
                    if (ledger.hasUsableExistenceEntry(masks[i])) existence = true;
                }
                if (pool == 0) {
                    if (existence) continue;
                    return AllocationResult.fail(0.0, c);
                }
                if (available < demanded) {
                    double r = available <= 0 ? 0.0 : (double) available / demanded;
                    if (r >= 1.0) r = Math.nextDown(1.0);
                    if (r < ratio) {
                        ratio = r;
                        shortfall = c;
                    }
                }
                long remaining = Math.clamp(available, 0, demanded);
                for (int i = 0; i < n && remaining > 0; i++) {
                    long avail = availableOf(ledgers.get(from + i), masks[i], i, takenScratch);
                    long want = Math.min(remaining, avail);
                    if (want <= 0) continue;
                    remaining -= takeFromMask(ledgers.get(from + i), masks[i], want, i, takenScratch, plan);
                }
            }
        }
        if (!hasInputs) return AllocationResult.of(plan, 1.0);
        return new AllocationResult(plan, ratio, shortfall);
    }

    static long denseIdx(int ledgerIdx, int bit) {
        return ((long) ledgerIdx << 6) | bit;
    }

    static long availableOf(PartLedger ledger, long mask, int ledgerIdx,
                            Long2LongOpenHashMap takenByBit) {
        long avail = 0;
        long m = mask;
        while (m != 0) {
            int bit = Long.numberOfTrailingZeros(m);
            m &= m - 1;
            var e = ledger.entry(bit);
            if (e == null || !e.scalesWithParallel || e.amount <= 0) continue;
            long remain = e.amount - takenByBit.get(denseIdx(ledgerIdx, bit));
            if (remain > 0) avail = NumberUtils.saturatedAdd(avail, remain);
        }
        return avail;
    }

    static long takeFromMask(PartLedger ledger, long mask, long want, int ledgerIdx,
                             Long2LongOpenHashMap takenByBit, ConsumePlan plan) {
        long taken = 0;
        long m = mask;
        while (m != 0 && taken < want) {
            int bit = Long.numberOfTrailingZeros(m);
            m &= m - 1;
            var e = ledger.entry(bit);
            if (e == null || !e.scalesWithParallel || e.amount <= 0) continue;
            long idx = denseIdx(ledgerIdx, bit);
            long take = Math.min(want - taken, e.amount - takenByBit.get(idx));
            if (take > 0) {
                plan.addTake(e, take);
                takenByBit.put(idx, takenByBit.get(idx) + take);
                taken += take;
            }
        }
        return taken;
    }

    ConsumePlan obtainPlan() {
        var plan = planScratch;
        plan.takes.clear();
        plan.meDemand.clear();
        plan.meSources.clear();
        plan.parallel = 1;
        return plan;
    }

    long feasiblePoolCap(GTRecipe recipe) {
        var caches = machine.getRecipeLogic().getChanceCaches();
        int euTier = IGTRecipe.of(recipe).getEuTier();
        int tier = machine.getTier();
        long best = 0;
        for (var supply : snapshot.supplies()) {
            long cap = supplyCap(supply, recipe, caches, euTier, tier);
            if (cap > best) best = cap;
        }
        return best;
    }

    long supplyCap(SupplyPart supply, GTRecipe recipe,
                   Map<RecipeCapability<?>, Object2IntMap<?>> caches, int euTier, int tier) {
        if (supply.patternPart != null) {
            if (supply.ledgers.isEmpty()) return 0;
            long bestSlot = 0;
            boolean anyInputs = false;
            for (var ledger : supply.ledgers) {
                long slotCap = Long.MAX_VALUE;
                boolean slotOk = true;
                boolean hasInputs = false;
                for (var entry : recipe.inputs.entrySet()) {
                    var cap = entry.getKey();
                    for (var c : entry.getValue()) {
                        long need = GTRecipeUtils.INSTANCE.amount(c, cap);
                        if (need <= 0) continue;
                        var mask = ledger.ownerMask(MapIngredientVariants.of(cap, c.content));
                        long pool = ledger.pool(mask);
                        if (c.chance <= 0) {
                            if (mask == 0) continue;
                            if (pool >= need || ledger.hasUsableExistenceEntry(mask) || ledger.hasNotConsumedSupplyEntry(mask)) continue;
                            slotOk = false;
                            break;
                        }
                        hasInputs = true;
                        anyInputs = true;
                        if (pool < need) {
                            if (ledger.hasUsableExistenceEntry(mask)) continue;
                            slotOk = false;
                            break;
                        }
                        long capped = inputCap(recipe, cap, c, need, pool, caches, euTier, tier);
                        if (capped < slotCap) slotCap = capped;
                    }
                    if (!slotOk) break;
                }
                if (slotOk && hasInputs && slotCap > bestSlot) bestSlot = slotCap;
            }
            return anyInputs ? bestSlot : Long.MAX_VALUE;
        }
        long supplyCap = Long.MAX_VALUE;
        boolean supplyOk = true;
        boolean hasInputs = false;
        for (var entry : recipe.inputs.entrySet()) {
            var cap = entry.getKey();
            for (var c : entry.getValue()) {
                long need = GTRecipeUtils.INSTANCE.amount(c, cap);
                if (need <= 0) continue;
                var pr = supply.probe(MapIngredientVariants.of(cap, c.content));
                if (c.chance <= 0) {
                    if (!pr.anyClaim) {
                        supplyOk = false;
                        break;
                    }
                    if (pr.pool >= need || pr.existence || pr.notConsumedSupply) continue;
                    supplyOk = false;
                    break;
                }
                hasInputs = true;
                if (pr.pool < need) {
                    if (pr.existence) continue;
                    supplyOk = false;
                    break;
                }
                long capped = inputCap(recipe, cap, c, need, pr.pool, caches, euTier, tier);
                if (capped < supplyCap) supplyCap = capped;
            }
            if (!supplyOk) break;
        }
        return supplyOk ? (hasInputs ? supplyCap : Long.MAX_VALUE) : 0;
    }

    @SuppressWarnings("unchecked")
    long inputCap(GTRecipe recipe, RecipeCapability<?> cap, Content c,
                  long need, long pool, Map<RecipeCapability<?>, Object2IntMap<?>> caches,
                  int euTier, int tier) {
        var boost = recipe.recipeType.getChanceFunction().getBoostedChance(c, euTier, tier);
        if (c.chance >= c.maxChance || boost >= c.maxChance) {
            return pool / need;
        }
        var cacheMap = (Object2IntMap<Predicate<Object>>) caches.get(cap);
        int cached = cacheMap != null ? cacheMap.computeIfAbsent((Predicate<Object>) c.content,
                k -> GTValues.RNG.nextInt(c.maxChance)) : GTValues.RNG.nextInt(c.maxChance);
        double maxP = ((pool / (double) need + 1.0) * c.maxChance - (1 + cached)) / c.chance;
        return maxP < 0 ? 0 : (long) maxP;
    }

    boolean notConsumedGate(GTRecipe recipe) {
        for (var entry : recipe.inputs.entrySet()) {
            var cap = entry.getKey();
            for (var c : entry.getValue()) {
                long need = GTRecipeUtils.INSTANCE.amount(c, cap);
                if (need <= 0 || c.chance > 0) continue;
                var variants = MapIngredientVariants.of(cap, c.content);
                boolean anyClaim = false;
                boolean satisfied = false;
                boolean patternLenient = false;
                for (var supply : snapshot.supplies()) {
                    if (supply.patternPart != null && !supply.ledgers.isEmpty()) patternLenient = true;
                    var pr = supply.probe(variants);
                    if (!pr.anyClaim) continue;
                    anyClaim = true;
                    if (pr.pool >= need || pr.existence || pr.notConsumedSupply) {
                        satisfied = true;
                        break;
                    }
                }
                if (!satisfied && (anyClaim || !patternLenient)) {
                    return false;
                }
            }
        }
        return true;
    }
}
