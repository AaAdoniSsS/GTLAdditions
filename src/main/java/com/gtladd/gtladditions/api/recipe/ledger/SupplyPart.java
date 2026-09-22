package com.gtladd.gtladditions.api.recipe.ledger;

import org.gtlcore.gtlcore.api.machine.trait.MEPatternRecipeHandlePart;
import org.gtlcore.gtlcore.api.machine.trait.RecipeHandlePart;
import org.gtlcore.gtlcore.utils.NumberUtils;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.List;

final class SupplyPart {

    @Nullable
    MEPatternRecipeHandlePart patternPart;
    int slot = -1;
    @Nullable
    RecipeHandlePart handlePart;
    final ObjectArrayList<PartLedger> ledgers = new ObjectArrayList<>();
    final Reference2ObjectOpenHashMap<IRecipeHandler<?>, HandleCache> handleCaches = new Reference2ObjectOpenHashMap<>();
    @Nullable
    SlotCache slotCache;
    @Nullable
    ObjectArrayList<ContentEntry> aggregated;
    @Nullable
    ObjectArrayList<ContentEntry> filledList;
    @Nullable
    ObjectArrayList<ContentEntry> lastSharedList;

    void retire() {
        filledList = null;
        aggregated = null;
        lastSharedList = null;
        handleCaches.clear();
        slotCache = null;
    }

    static final class Probe {

        boolean anyClaim;
        long pool;
        boolean existence;
        boolean notConsumedSupply;

        Probe set(boolean anyClaim, long pool, boolean existence, boolean notConsumedSupply) {
            this.anyClaim = anyClaim;
            this.pool = pool;
            this.existence = existence;
            this.notConsumedSupply = notConsumedSupply;
            return this;
        }
    }

    final Probe probeScratch = new Probe();

    Probe probe(List<AbstractMapIngredient> variants) {
        boolean anyClaim = false;
        long pool = 0;
        boolean existence = false;
        boolean notConsumedSupply = false;
        for (var ledger : ledgers) {
            long mask = ledger.ownerMask(variants);
            if (mask == 0) continue;
            anyClaim = true;
            long p = ledger.pool(mask);
            if (patternPart != null) {
                if (p > pool) pool = p;
            } else {
                pool = NumberUtils.saturatedAdd(pool, p);
            }
            if (!existence && ledger.hasUsableExistenceEntry(mask)) existence = true;
            if (!notConsumedSupply && ledger.hasNotConsumedSupplyEntry(mask)) notConsumedSupply = true;
        }
        return probeScratch.set(anyClaim, pool, existence, notConsumedSupply);
    }
}
