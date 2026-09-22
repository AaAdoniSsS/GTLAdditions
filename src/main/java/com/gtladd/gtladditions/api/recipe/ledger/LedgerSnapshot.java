package com.gtladd.gtladditions.api.recipe.ledger;

import org.gtlcore.gtlcore.api.capability.IMERecipeHandler;
import org.gtlcore.gtlcore.api.machine.trait.IRecipeCapabilityMachine;
import org.gtlcore.gtlcore.api.machine.trait.MEPatternRecipeHandlePart;
import org.gtlcore.gtlcore.api.machine.trait.RecipeHandlePart;
import org.gtlcore.gtlcore.common.machine.multiblock.part.MEDualHatchStockPartMachine;
import org.gtlcore.gtlcore.common.machine.multiblock.part.ae.MEPatternBufferPartMachineBase;
import org.gtlcore.gtlcore.common.machine.multiblock.part.ae.MEPatternBufferProxyPartMachine;
import org.gtlcore.gtlcore.mixin.gtm.ae.machine.MEHatchPartMachineAccessor;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.integration.ae2.machine.MEInputBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEInputHatchPartMachine;

import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import com.gtladd.gtladditions.api.ae2.AE2KeyCounterCache;
import com.gtladd.gtladditions.api.ae2.GridStockCache;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.util.List;

final class LedgerSnapshot {

    final IRecipeCapabilityMachine rcm;
    final List<PartLedger> partLedgers = new ObjectArrayList<>();
    final List<SupplyPart> supplyParts = new ObjectArrayList<>();
    final ObjectArrayList<SupplyPart> spareSupplies = new ObjectArrayList<>();
    int supplyCursor;
    @Nullable
    SupplyPart sharedSupply;
    final ObjectOpenHashSet<StockKey> stockingClaims = new ObjectOpenHashSet<>();

    record StockKey(IGrid grid, AEKey key) {}

    boolean snapshotBuilt;
    boolean ledgerBuilt;
    long snapshotVersion;
    static final int LEDGER_CACHE_ENTRIES = 16_384;
    final LedgerCacheBudget ledgerCacheBudget = new LedgerCacheBudget(LEDGER_CACHE_ENTRIES);
    boolean searchDomainsBuilt;
    ObjectArrayList<ObjectArrayList<PartLedger>> searchDomains;
    final ObjectArrayList<ObjectArrayList<PartLedger>> domainPool = new ObjectArrayList<>();
    int domainCursor;
    @Nullable
    ObjectArrayList<IGrid> gridsCache;

    final Object2ObjectOpenHashMap<StockKey, NetworkDeduction> networkDeductions = new Object2ObjectOpenHashMap<>();

    static final class NetworkDeduction {

        long snapshotTick;
        long amount;
    }

    LedgerSnapshot(IRecipeCapabilityMachine rcm) {
        this.rcm = rcm;
    }

    void beginCycle() {
        snapshotBuilt = false;
        ledgerBuilt = false;
        gridsCache = null;
    }

    List<SupplyPart> supplies() {
        return supplyParts;
    }

    void refresh() {
        if (snapshotBuilt) return;
        snapshotBuilt = true;
        walkSources();
    }

    void ensureAmounts() {
        if (ledgerBuilt) return;
        ledgerBuilt = true;
        IGrid grid = null;
        ObjectArrayList<ContentEntry> pending = null;
        for (var ledger : partLedgers) {
            for (var e : ledger.entries) {
                if (e.amount != -1) continue;
                if (e.aeKey == null || e.grid == null) {
                    e.amount = 0;
                    continue;
                }
                if (e.grid != grid) {
                    fillPending(grid, pending);
                    grid = e.grid;
                    pending = new ObjectArrayList<>();
                }
                pending.add(e);
            }
        }
        fillPending(grid, pending);
    }

    void fillPending(@Nullable IGrid grid, @Nullable List<ContentEntry> pending) {
        if (grid == null || pending == null || pending.isEmpty()) return;
        var keys = new ObjectOpenHashSet<AEKey>(pending.size());
        for (var e : pending) keys.add(e.aeKey);
        var amounts = AE2KeyCounterCache.getAmounts(grid, keys);
        long nowTick = GridStockCache.getSnapshotTick(grid.getStorageService());
        for (var e : pending) {
            e.amount = amounts.getValue(e.aeKey);
            if (networkDeductions.isEmpty()) continue;
            var probe = new StockKey(grid, e.aeKey);
            var d = networkDeductions.get(probe);
            if (d == null) continue;
            if (nowTick > d.snapshotTick) {
                networkDeductions.remove(probe);
                continue;
            }
            e.amount = Math.max(0, e.amount - d.amount);
        }
    }

    void recordNetworkDeduction(IGrid grid, AEKey key, long amount) {
        var probe = new StockKey(grid, key);
        var d = networkDeductions.computeIfAbsent(probe, k -> new NetworkDeduction());
        d.snapshotTick = GridStockCache.getSnapshotTick(grid.getStorageService());
        d.amount += amount;
    }

    void clearNetworkDeductions() {
        networkDeductions.clear();
    }

    void walkSources() {
        long version = ++snapshotVersion;
        if (searchDomains != null) searchDomains.clear();
        searchDomainsBuilt = false;
        sharedSupply = null;
        supplyCursor = 0;
        for (var mePart : rcm.getMEPatternRecipeHandleParts()) {
            walkPatternPart(mePart, version);
        }
        var sharedPart = rcm.isDistinct() ? null : rcm.getSharedRecipeHandlePart();
        if (sharedPart != null) {
            sharedSupply = matchSupply(sharedPart);
            fillOrSkipLedgers(sharedSupply, aggregateNormal(sharedSupply, sharedPart, null, version));
        }
        for (var part : rcm.getNormalRecipeHandlePart(IO.IN)) {
            var supply = matchSupply(part);
            fillOrSkipLedgers(supply, aggregateNormal(supply, part, sharedSupply, version));
        }
        while (supplyParts.size() > supplyCursor) {
            var spare = supplyParts.removeLast();
            spare.retire();
            spareSupplies.add(spare);
        }
        partLedgers.clear();
        for (var supply : supplyParts) partLedgers.addAll(supply.ledgers);
    }

    SupplyPart matchSupply(RecipeHandlePart part) {
        if (supplyCursor < supplyParts.size()) {
            var existing = supplyParts.get(supplyCursor);
            if (existing.patternPart == null && existing.handlePart == part) {
                supplyCursor++;
                return existing;
            }
            truncateSuppliesFromCursor();
        }
        var supply = spareSupplies.isEmpty() ? new SupplyPart() : spareSupplies.pop();
        supply.patternPart = null;
        supply.slot = -1;
        supply.handlePart = part;
        supplyParts.add(supply);
        supplyCursor++;
        return supply;
    }

    SupplyPart matchSlotSupply(MEPatternRecipeHandlePart part, int slot) {
        if (supplyCursor < supplyParts.size()) {
            var existing = supplyParts.get(supplyCursor);
            if (existing.patternPart == part && existing.slot == slot) {
                supplyCursor++;
                return existing;
            }
            truncateSuppliesFromCursor();
        }
        var supply = spareSupplies.isEmpty() ? new SupplyPart() : spareSupplies.pop();
        supply.patternPart = part;
        supply.slot = slot;
        supply.handlePart = null;
        supplyParts.add(supply);
        supplyCursor++;
        return supply;
    }

    void truncateSuppliesFromCursor() {
        while (supplyParts.size() > supplyCursor) {
            var spare = supplyParts.removeLast();
            spare.retire();
            spareSupplies.add(spare);
        }
    }

    ObjectArrayList<ContentEntry> aggregateNormal(SupplyPart supply,
                                                  RecipeHandlePart part,
                                                  @Nullable SupplyPart shared,
                                                  long version) {
        boolean changed = false;
        for (var entry : part.getHandlerFastIterable()) {
            var cap = entry.getKey();
            if (cap != ItemRecipeCapability.CAP && cap != FluidRecipeCapability.CAP) continue;
            boolean fluid = cap == FluidRecipeCapability.CAP;
            for (var handler : entry.getValue()) {
                var cache = supply.handleCaches.computeIfAbsent(handler, k -> new HandleCache(handler, fluid));
                cache.refresh(version);
                changed |= cache.changed;
            }
        }
        for (var it = supply.handleCaches.entrySet().iterator(); it.hasNext();) {
            var e = it.next();
            if (e.getValue().version != version) {
                changed = true;
                it.remove();
            }
        }
        var sharedList = shared == null ? null : shared.aggregated;
        if (!changed && supply.aggregated != null && supply.lastSharedList == sharedList) return supply.aggregated;
        stockingClaims.clear();
        var agg = new ObjectArrayList<ContentEntry>();
        for (var entry : part.getHandlerFastIterable()) {
            var cap = entry.getKey();
            if (cap != ItemRecipeCapability.CAP && cap != FluidRecipeCapability.CAP) continue;
            for (var handler : entry.getValue()) {
                var cache = supply.handleCaches.get(handler);
                if (cache == null || cache.list == null) continue;
                for (var e : cache.list) {
                    if (e.grid != null && e.aeKey != null && !stockingClaims.add(new StockKey(e.grid, e.aeKey))) continue;
                    agg.add(e);
                }
            }
        }
        if (sharedList != null) {
            for (var cache : shared.handleCaches.values()) {
                if (!cache.fluid || cache.list == null) continue;
                for (var e : cache.list) {
                    if (e.grid != null && e.aeKey != null && !stockingClaims.add(new StockKey(e.grid, e.aeKey))) continue;
                    agg.add(e);
                }
            }
        }
        supply.aggregated = agg;
        supply.lastSharedList = sharedList;
        return agg;
    }

    void fillOrSkipLedgers(SupplyPart supply, ObjectArrayList<ContentEntry> agg) {
        if (supply.filledList == agg) return;
        supply.filledList = agg;
        int used = 0;
        PartLedger cur = null;
        for (var entry : agg) {
            if (cur == null || cur.ownerCount() >= PartLedger.MAX_ENTRIES) {
                if (used < supply.ledgers.size()) {
                    cur = supply.ledgers.get(used);
                    cur.reset();
                } else {
                    supply.ledgers.add(cur = new PartLedger(ledgerCacheBudget));
                }
                used++;
            }
            cur.addEntry(entry);
        }
        while (supply.ledgers.size() > used) {
            supply.ledgers.removeLast().reset();
        }
    }

    void walkPatternPart(MEPatternRecipeHandlePart part, long version) {
        var buffer = findPatternBuffer(part);
        IMERecipeHandler<?, ?> itemHandler = null;
        IMERecipeHandler<?, ?> fluidHandler = null;
        for (var h : part.getMERecipeHandlers()) {
            var cap = h.getCapability();
            if (cap == ItemRecipeCapability.CAP && itemHandler == null) itemHandler = h;
            else if (cap == FluidRecipeCapability.CAP && fluidHandler == null) fluidHandler = h;
        }
        int[] slots = itemHandler != null ? itemHandler.getActiveSlots() : fluidHandler != null ? fluidHandler.getActiveSlots() : new int[0];
        var itemLimits = slotLimitContents(itemHandler);
        var fluidLimits = slotLimitContents(fluidHandler);
        for (int slot : slots) {
            var supply = matchSlotSupply(part, slot);
            var cache = supply.slotCache;
            if (cache == null) supply.slotCache = cache = new SlotCache();
            fillOrSkipLedgers(supply, cache.refresh(version, itemHandler, fluidHandler, slot, itemLimits, fluidLimits, buffer));
        }
    }

    @Nullable
    static Int2ObjectMap<?> slotLimitContents(@Nullable IMERecipeHandler<?, ?> handler) {
        return handler == null ? null : handler.getActiveAndUnCachedSlotsLimitContentsMap();
    }

    @Nullable
    static MEPatternBufferPartMachineBase findPatternBuffer(MEPatternRecipeHandlePart part) {
        for (var h : part.getMERecipeHandlers()) {
            if (h instanceof MachineTrait trait) {
                var machine = trait.getMachine();
                if (machine instanceof MEPatternBufferProxyPartMachine proxy) return proxy.getBuffer();
                if (machine instanceof MEPatternBufferPartMachineBase base) return base;
            }
        }
        return null;
    }

    static GridAccess findGridFor(Object handler) {
        if (!(handler instanceof MachineTrait trait)) return null;
        var part = trait.getMachine();
        if (part instanceof MEDualHatchStockPartMachine dual) {
            var grid = dual.getMainNode().getGrid();
            return grid == null ? null : new GridAccess(grid, dual.getActionSource());
        }
        if (part instanceof MEInputBusPartMachine bus) {
            var grid = bus.getMainNode().getGrid();
            return grid == null ? null : new GridAccess(grid, bus.getActionSource());
        }
        if (part instanceof MEInputHatchPartMachine hatch) {
            var grid = hatch.getMainNode().getGrid();
            return grid == null ? null : new GridAccess(grid, ((MEHatchPartMachineAccessor) hatch).getActionSource());
        }
        return null;
    }

    record GridAccess(IGrid grid, IActionSource source) {}

    List<ObjectArrayList<PartLedger>> searchDomains() {
        if (searchDomainsBuilt) return searchDomains;
        searchDomainsBuilt = true;
        var domains = searchDomains;
        if (domains == null) searchDomains = domains = new ObjectArrayList<>();
        else domains.clear();
        domainCursor = 0;
        for (var supply : supplyParts) {
            if (supply.patternPart != null) {
                for (var ledger : supply.ledgers) {
                    var domain = obtainDomain();
                    domain.add(ledger);
                    domains.add(domain);
                }
            } else if (!supply.ledgers.isEmpty()) {
                var domain = obtainDomain();
                domain.addAll(supply.ledgers);
                domains.add(domain);
            }
        }
        return domains;
    }

    ObjectArrayList<PartLedger> obtainDomain() {
        ObjectArrayList<PartLedger> domain;
        if (domainCursor < domainPool.size()) domain = domainPool.get(domainCursor);
        else domainPool.add(domain = new ObjectArrayList<>());
        domainCursor++;
        domain.clear();
        return domain;
    }

    List<IGrid> grids() {
        if (gridsCache == null) {
            gridsCache = new ObjectArrayList<>();
            for (var ledger : partLedgers) {
                for (var e : ledger.entries) {
                    if (e.grid != null && !gridsCache.contains(e.grid)) gridsCache.add(e.grid);
                }
            }
        }
        return gridsCache;
    }
}
