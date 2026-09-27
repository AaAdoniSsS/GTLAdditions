package com.gtladd.gtladditions.api.recipe;

import org.gtlcore.gtlcore.api.machine.trait.IRecipeCapabilityMachine;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.Branch;

import com.gtladd.gtladditions.api.machine.IRecipeSearchProvider;
import com.gtladd.gtladditions.api.recipe.ledger.PartLedger;
import com.gtladd.gtladditions.api.recipe.ledger.RecipeSearchContext;
import com.gtladd.gtladditions.api.recipe.lookup.IBranchAddition;
import com.gtladd.gtladditions.mixin.gtlcore.machine.part.InternalSlotAccessor;
import com.gtladd.gtladditions.mixin.gtlcore.machine.part.MEPatternBufferPartMachineBaseInvoker;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class OptimizedRecipeSearch {

    @Nullable
    public static GTRecipe find(WorkableElectricMultiblockMachine holder, Branch branch, Predicate<GTRecipe> canHandle) {
        if (!(holder instanceof IRecipeCapabilityMachine rcm)) return null;
        var ctx = activeContext(holder);
        for (var part : rcm.getMEPatternRecipeHandleParts()) {
            var machine = ctx.getPatternMachine(part);
            if (machine == null) continue;
            var cacheRecipe = machine.getMETrait().getSlot2RecipesCache();
            for (var slot : ((MEPatternBufferPartMachineBaseInvoker) machine).getActiveSlots()) {
                var index = ((InternalSlotAccessor) slot).getSlotIndex();
                var recipes = cacheRecipe.get(index);
                if (recipes == null) continue;
                for (var recipe : recipes) {
                    if (canHandle.test(recipe) && ctx.tryPlan(recipe, 1, part, index) != null) {
                        accept(ctx, recipe);
                        return recipe;
                    }
                }
            }
        }
        var hit = ctx.pickSearchHit(canHandle);
        if (hit != null) {
            accept(ctx, hit);
            return hit;
        }
        return searchNoCache(holder, branch, canHandle);
    }

    @Nullable
    public static GTRecipe searchNoCache(WorkableElectricMultiblockMachine holder, Branch branch, Predicate<GTRecipe> canHandle) {
        if (!(holder instanceof IRecipeCapabilityMachine)) return null;
        var ctx = activeContext(holder);
        ctx.buildSnapshot();
        for (var domain : ctx.getSearchDomains()) {
            var filter = leafFilter(ctx, canHandle, domain);
            var r = dfs(domain, branch, recipe -> {
                if (filter.test(recipe)) {
                    accept(ctx, recipe);
                    return recipe;
                }
                return null;
            });
            if (r != null) {
                ctx.recordSearchHit(r, domain);
                return r;
            }
        }
        return null;
    }

    public static List<GTRecipe> collectCandidates(WorkableElectricMultiblockMachine holder, Branch branch,
                                                   Predicate<GTRecipe> canHandle) {
        var result = new ObjectArrayList<GTRecipe>();
        if (!(holder instanceof IRecipeCapabilityMachine rcm)) return result;
        var ctx = activeContext(holder);
        ctx.buildSnapshot();
        var seen = new ObjectOpenHashSet<GTRecipe>();
        for (var part : rcm.getMEPatternRecipeHandleParts()) {
            var machine = ctx.getPatternMachine(part);
            if (machine == null) continue;
            var cacheRecipe = machine.getMETrait().getSlot2RecipesCache();
            for (var slot : ((MEPatternBufferPartMachineBaseInvoker) machine).getActiveSlots()) {
                var index = ((InternalSlotAccessor) slot).getSlotIndex();
                var recipes = cacheRecipe.get(index);
                if (recipes == null) continue;
                for (var recipe : recipes) {
                    if (canHandle.test(recipe) && ctx.tryPlan(recipe, 1, part, index) != null) {
                        accept(ctx, recipe);
                        result.add(recipe);
                        seen.add(recipe);
                    }
                }
            }
        }
        for (var domain : ctx.getSearchDomains()) {
            var filter = leafFilter(ctx, canHandle, domain);
            dfs(domain, branch, recipe -> {
                if (seen.add(recipe) && filter.test(recipe)) {
                    accept(ctx, recipe);
                    result.add(recipe);
                }
                return null;
            });
        }
        return result;
    }

    static RecipeSearchContext activeContext(WorkableElectricMultiblockMachine machine) {
        var ctx = machine instanceof IRecipeSearchProvider p ? p.getActiveSearchContext() : null;
        return ctx != null ? ctx : new RecipeSearchContext(machine);
    }

    static Predicate<GTRecipe> leafFilter(RecipeSearchContext ctx, Predicate<GTRecipe> canHandle, List<PartLedger> domain) {
        return recipe -> canHandle.test(recipe) && ctx.tryPlan(recipe, 1, domain) != null;
    }

    static void accept(RecipeSearchContext ctx, GTRecipe recipe) {
        ctx.setOriginRecipe(recipe);
    }

    interface LeafVisitor {

        @Nullable
        GTRecipe test(GTRecipe recipe);
    }

    static final class SearchScratch {

        final ObjectArrayList<Reference2ObjectOpenHashMap<Branch, long[]>> layers = new ObjectArrayList<>();

        long[] used = new long[0];
        long[] per = new long[0];
        boolean busy;

        Reference2ObjectOpenHashMap<Branch, long[]> masks(int depth) {
            while (layers.size() <= depth) layers.add(new Reference2ObjectOpenHashMap<>());
            var map = layers.get(depth);
            map.clear();
            return map;
        }

        long[] usedFor(int n) {
            if (used.length < n) used = new long[Math.max(n, used.length << 1)];
            Arrays.fill(used, 0, n, 0L);
            return used;
        }

        long[] perFor(int n) {
            if (per.length != n) per = new long[n];
            return per;
        }

        void release() {
            for (int i = 0, size = layers.size(); i < size; i++) layers.get(i).clear();
        }
    }

    static final ThreadLocal<SearchScratch> searchScratch = ThreadLocal.withInitial(SearchScratch::new);

    @Nullable
    static GTRecipe dfs(List<PartLedger> ledgers, Branch node, LeafVisitor visitor) {
        var scratch = searchScratch.get();
        if (scratch.busy) return runSearch(new SearchScratch(), ledgers, node, visitor);
        scratch.busy = true;
        try {
            return runSearch(scratch, ledgers, node, visitor);
        } finally {
            scratch.release();
            scratch.busy = false;
        }
    }

    @Nullable
    static GTRecipe runSearch(SearchScratch scratch, List<PartLedger> ledgers, Branch node, LeafVisitor visitor) {
        int n = ledgers.size();
        return dfs0(ledgers, node, scratch.usedFor(n), n, 0, scratch, visitor);
    }

    @Nullable
    static GTRecipe dfs0(List<PartLedger> ledgers, Branch node, long[] used,
                         int n, int depth, SearchScratch scratch, LeafVisitor visitor) {
        int free = freeOf(ledgers, used, n);
        if (free == 0) return null;
        if (free < minDepth(node)) return null;

        var nodes = node.getNodes();
        var special = node.getSpecialNodes();
        var childMasks = scratch.masks(depth);
        var perScratch = scratch.perFor(n);
        GTRecipe hit;
        if (nodes.size() + special.size() <= free) {
            hit = probeTreeSide(ledgers, nodes, used, perScratch, childMasks, visitor, n);
            if (hit == null) hit = probeTreeSide(ledgers, special, used, perScratch, childMasks, visitor, n);
        } else {
            hit = probeMachineSide(ledgers, used, perScratch, childMasks, visitor, nodes, special, n);
        }
        if (hit != null) return hit;
        for (var it = childMasks.reference2ObjectEntrySet().fastIterator(); it.hasNext();) {
            var e = it.next();
            var perLedger = e.getValue();
            var child = e.getKey();
            for (int i = 0; i < perLedger.length; i++) {
                long m = perLedger[i];
                while (m != 0) {
                    int bit = Long.numberOfTrailingZeros(m);
                    m &= m - 1;
                    used[i] |= 1L << bit;
                    var r = dfs0(ledgers, child, used, n, depth + 1, scratch, visitor);
                    used[i] &= ~(1L << bit);
                    if (r != null) return r;
                }
            }
        }
        return null;
    }

    static int freeOf(List<PartLedger> ledgers, long[] used, int n) {
        int free = 0;
        for (int i = 0; i < n; i++) {
            free += ledgers.get(i).ownerCount() - Long.bitCount(used[i]);
        }
        return free;
    }

    static void mergeChild(Reference2ObjectOpenHashMap<Branch, long[]> childMasks, Branch child, long[] perLedger) {
        long[] cur = childMasks.get(child);
        if (cur == null) {
            childMasks.put(child, perLedger.clone());
        } else {
            for (int i = 0; i < cur.length; i++) cur[i] |= perLedger[i];
        }
    }

    static Iterable<Object2ObjectMap.Entry<AbstractMapIngredient, Either<GTRecipe, Branch>>> fastEntries(Map<AbstractMapIngredient, Either<GTRecipe, Branch>> map) {
        return Object2ObjectMaps.fastIterable((Object2ObjectMap<AbstractMapIngredient, Either<GTRecipe, Branch>>) map);
    }

    @Nullable
    static GTRecipe probeTreeSide(List<PartLedger> ledgers,
                                  Map<AbstractMapIngredient, Either<GTRecipe, Branch>> map,
                                  long[] used, long[] perScratch,
                                  Reference2ObjectOpenHashMap<Branch, long[]> childMasks,
                                  LeafVisitor visitor, int n) {
        if (map.isEmpty()) return null;
        for (var e : fastEntries(map)) {
            var key = e.getKey();
            var either = e.getValue();
            if (either.left().isPresent()) {
                if (!matchesDomain(ledgers, key)) continue;
                var recipe = either.left().get();
                var hit = visitor.test(recipe);
                if (hit != null) return hit;
            } else if (either.right().isPresent()) {
                boolean any = false;
                for (int i = 0; i < n; i++) {
                    long mask = ledgers.get(i).ownerMaskOf(key) & ~used[i];
                    perScratch[i] = mask;
                    if (mask != 0) any = true;
                }
                if (any) mergeChild(childMasks, either.right().get(), perScratch);
            }
        }
        return null;
    }

    static boolean matchesDomain(List<PartLedger> ledgers, AbstractMapIngredient key) {
        for (int i = 0, n = ledgers.size(); i < n; i++) {
            if (ledgers.get(i).ownerMaskOf(key) != 0) return true;
        }
        return false;
    }

    @Nullable
    static GTRecipe probeMachineSide(List<PartLedger> ledgers, long[] used, long[] per,
                                     Reference2ObjectOpenHashMap<Branch, long[]> childMasks,
                                     LeafVisitor visitor,
                                     Map<AbstractMapIngredient, Either<GTRecipe, Branch>> nodes,
                                     Map<AbstractMapIngredient, Either<GTRecipe, Branch>> special,
                                     int n) {
        Arrays.fill(per, 0, n, 0L);
        for (int i = 0; i < n; i++) {
            var ledger = ledgers.get(i);
            long free = ledger.ownerCount() >= 64 ? ~used[i] : ~used[i] & ((1L << ledger.ownerCount()) - 1);
            while (free != 0) {
                int bit = Long.numberOfTrailingZeros(free);
                free &= free - 1;
                var entry = ledger.entry(bit);
                if (entry == null) continue;
                for (var v : entry.variants) {
                    var either = (v.isSpecialIngredient() ? special : nodes).get(v);
                    if (either == null) continue;
                    if (either.left().isPresent()) {
                        var recipe = either.left().get();
                        var hit = visitor.test(recipe);
                        if (hit != null) return hit;
                    } else if (either.right().isPresent()) {
                        per[i] = 1L << bit;
                        mergeChild(childMasks, either.right().get(), per);
                        per[i] = 0;
                    }
                }
            }
        }
        return null;
    }

    static int minDepth(Branch node) {
        var holder = (IBranchAddition) node;
        int cached = holder.minDepth();
        if (cached != 0) return cached;
        holder.setMinDepth(Integer.MAX_VALUE);
        try {
            int min = scanMinDepth(node.getNodes(), Integer.MAX_VALUE);
            min = scanMinDepth(node.getSpecialNodes(), min);
            holder.setMinDepth(min);
            return min;
        } catch (RuntimeException | Error e) {
            holder.setMinDepth(0);
            throw e;
        }
    }

    static int scanMinDepth(Map<AbstractMapIngredient, Either<GTRecipe, Branch>> map, int min) {
        for (var e : fastEntries(map)) {
            var either = e.getValue();
            if (either.left().isPresent()) return 1;
            else if (either.right().isPresent()) {
                int depth = minDepth(either.right().get());
                if (depth != Integer.MAX_VALUE) min = Math.min(min, depth + 1);
            }
        }
        return min;
    }
}
