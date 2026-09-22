package com.gtladd.gtladditions.api.recipe;

import org.gtlcore.gtlcore.api.machine.trait.IRecipeCapabilityMachine;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.Branch;
import com.gregtechceu.gtceu.api.recipe.lookup.GTRecipeLookup;

import com.gtladd.gtladditions.api.machine.IRecipeSearchProvider;
import com.gtladd.gtladditions.api.recipe.ledger.PartLedger;
import com.gtladd.gtladditions.api.recipe.ledger.RecipeSearchContext;
import com.gtladd.gtladditions.api.recipe.lookup.IBranchAddition;
import com.gtladd.gtladditions.api.recipe.lookup.MultiGTRecipeLookup;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class OptimizedRecipeSearch {

    public static Branch branchOf(GTRecipeLookup lookup) {
        if (lookup instanceof MultiGTRecipeLookup multi) return multi.getBranch();
        return lookup.getLookup();
    }

    @Nullable
    public static GTRecipe find(WorkableElectricMultiblockMachine holder, Branch branch, Predicate<GTRecipe> canHandle) {
        if (!(holder instanceof IRecipeCapabilityMachine rcm)) return null;
        var ctx = activeContext(holder);
        for (var part : rcm.getMEPatternRecipeHandleParts()) {
            for (var recipe : part.getCachedGTRecipe()) {
                if (canHandle.test(recipe) && ctx.tryPlan(recipe, 1) != null) {
                    accept(ctx, recipe);
                    return recipe;
                }
            }
        }
        var hit = ctx.getSearchHit();
        if (hit != null && canHandle.test(hit) && ctx.tryPlan(hit, 1) != null) {
            accept(ctx, hit);
            return hit;
        }
        var found = searchNoCache(holder, branch, canHandle);
        ctx.setSearchHit(found);
        return found;
    }

    @Nullable
    public static GTRecipe searchNoCache(WorkableElectricMultiblockMachine holder, Branch branch, Predicate<GTRecipe> canHandle) {
        if (!(holder instanceof IRecipeCapabilityMachine)) return null;
        var ctx = activeContext(holder);
        ctx.buildSnapshot();
        var filter = leafFilter(ctx, canHandle);
        for (var domain : ctx.getSearchDomains()) {
            var r = dfs(domain, branch, recipe -> {
                if (filter.test(recipe)) {
                    accept(ctx, recipe);
                    return recipe;
                }
                return null;
            });
            if (r != null) return r;
        }
        return null;
    }

    public static List<GTRecipe> collectCandidates(WorkableElectricMultiblockMachine holder, Branch branch,
                                                   Predicate<GTRecipe> canHandle) {
        var result = new ObjectArrayList<GTRecipe>();
        if (!(holder instanceof IRecipeCapabilityMachine rcm)) return result;
        var ctx = activeContext(holder);
        ctx.buildSnapshot();
        var filter = leafFilter(ctx, canHandle);
        var seen = new ObjectOpenHashSet<GTRecipe>();
        for (var part : rcm.getMEPatternRecipeHandleParts()) {
            for (var r : part.getCachedGTRecipe()) {
                if (seen.add(r) && canHandle.test(r) && ctx.tryPlan(r, 1) != null) {
                    accept(ctx, r);
                    result.add(r);
                }
            }
        }
        for (var domain : ctx.getSearchDomains()) {
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

    static Predicate<GTRecipe> leafFilter(RecipeSearchContext ctx, Predicate<GTRecipe> canHandle) {
        return recipe -> canHandle.test(recipe) && ctx.tryPlan(recipe, 1) != null;
    }

    static void accept(RecipeSearchContext ctx, GTRecipe recipe) {
        ctx.setOriginRecipe(recipe);
        ctx.noteMatchedParallel(recipe);
    }

    interface LeafVisitor {

        @Nullable
        GTRecipe test(GTRecipe recipe);
    }

    static final ObjectArrayList<Reference2ObjectOpenHashMap<Branch, long[]>> childMaskPool = new ObjectArrayList<>();
    static long[] usedScratch = new long[0];

    @Nullable
    static GTRecipe dfs(List<PartLedger> ledgers, Branch node,
                        LeafVisitor visitor) {
        int n = ledgers.size();
        if (usedScratch.length < n) usedScratch = new long[Math.max(n, usedScratch.length << 1)];
        Arrays.fill(usedScratch, 0, n, 0L);
        return dfs0(ledgers, node, usedScratch, n, 0, visitor);
    }

    @Nullable
    static GTRecipe dfs0(List<PartLedger> ledgers, Branch node, long[] used,
                         int n, int depth, LeafVisitor visitor) {
        int free = freeOf(ledgers, used, n);
        if (free == 0) return null;
        if (free < minDepth(node)) return null;

        var nodes = node.getNodes();
        var special = node.getSpecialNodes();
        var childMasks = obtainChildMasks(depth);
        GTRecipe hit;
        if (nodes.size() + special.size() <= free) {
            hit = probeTreeSide(ledgers, nodes, used, childMasks, visitor, n);
            if (hit == null) hit = probeTreeSide(ledgers, special, used, childMasks, visitor, n);
        } else {
            hit = probeMachineSide(ledgers, used, childMasks, visitor, nodes, special, n);
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
                    var r = dfs0(ledgers, child, used, n, depth + 1, visitor);
                    used[i] &= ~(1L << bit);
                    if (r != null) return r;
                }
            }
        }
        return null;
    }

    static Reference2ObjectOpenHashMap<Branch, long[]> obtainChildMasks(int depth) {
        while (childMaskPool.size() <= depth) childMaskPool.add(new Reference2ObjectOpenHashMap<>());
        var map = childMaskPool.get(depth);
        map.clear();
        return map;
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
                                  long[] used, Reference2ObjectOpenHashMap<Branch, long[]> childMasks,
                                  LeafVisitor visitor, int n) {
        if (map.isEmpty()) return null;
        var perScratch = new long[n];
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
    static GTRecipe probeMachineSide(List<PartLedger> ledgers, long[] used,
                                     Reference2ObjectOpenHashMap<Branch, long[]> childMasks,
                                     LeafVisitor visitor,
                                     Map<AbstractMapIngredient, Either<GTRecipe, Branch>> nodes,
                                     Map<AbstractMapIngredient, Either<GTRecipe, Branch>> special,
                                     int n) {
        long[] per = new long[n];
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
