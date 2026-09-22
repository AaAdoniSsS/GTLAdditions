package com.gtladd.gtladditions.api.recipe.ledger;

import org.gtlcore.gtlcore.api.machine.trait.MEStock.IOptimizedMEList;
import org.gtlcore.gtlcore.api.machine.trait.NotifiableCircuitItemStackHandler;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.world.item.ItemStack;

import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import com.hepdd.gtmthings.common.block.machine.trait.CatalystFluidStackHandler;
import com.hepdd.gtmthings.common.block.machine.trait.CatalystItemStackHandler;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

final class HandleCache {

    static final byte K_CATALYST = 0, K_STOCKING = 1, K_ME_MAP = 2, K_PLAIN = 3;

    final IRecipeHandler<?> handler;
    final boolean fluid;
    byte subkind;
    @Nullable
    IGrid grid;
    @Nullable
    IActionSource source;
    boolean changed;
    long version = -1;
    @Nullable
    ObjectArrayList<ContentEntry> list;

    HandleCache(IRecipeHandler<?> handler, boolean fluid) {
        this.handler = handler;
        this.fluid = fluid;
    }

    void refresh(long v) {
        if (version == v && list != null) return;
        version = v;
        if (list == null) {
            resolveClass();
            changed = true;
            list = build();
            return;
        }
        changed = !refreshInPlace();
        if (changed) list = build();
    }

    void resolveClass() {
        if (fluid) {
            if (handler instanceof CatalystFluidStackHandler) {
                subkind = K_CATALYST;
                grid = null;
                source = null;
            } else if (handler instanceof ExportOnlyAEFluidList && handler instanceof IOptimizedMEList ome && ome.isStocking()) {
                var access = LedgerSnapshot.findGridFor(handler);
                if (access != null) {
                    subkind = K_STOCKING;
                    grid = access.grid();
                    source = access.source();
                } else {
                    subkind = K_ME_MAP;
                    grid = null;
                    source = null;
                }
            } else {
                subkind = K_PLAIN;
                grid = null;
                source = null;
            }
            return;
        }
        if (handler instanceof CatalystItemStackHandler || handler instanceof NotifiableCircuitItemStackHandler) {
            subkind = K_CATALYST;
            grid = null;
            source = null;
        } else if (handler instanceof ExportOnlyAEItemList && handler instanceof IOptimizedMEList ome && ome.isStocking()) {
            var access = LedgerSnapshot.findGridFor(handler);
            if (access != null) {
                subkind = K_STOCKING;
                grid = access.grid();
                source = access.source();
            } else {
                subkind = K_ME_MAP;
                grid = null;
                source = null;
            }
        } else {
            subkind = K_PLAIN;
            grid = null;
            source = null;
        }
    }

    boolean refreshInPlace() {
        byte oldKind = subkind;
        var oldGrid = grid;
        resolveClass();
        if (subkind != oldKind || grid != oldGrid) return false;
        int i = 0;
        var entries = list;
        if (fluid) {
            switch (subkind) {
                case K_CATALYST -> {
                    for (var o : handler.getContents()) {
                        if (!(o instanceof FluidStack fs) || fs.isEmpty()) continue;
                        if (i >= entries.size()) return false;
                        var e = entries.get(i++);
                        if (!AEFluidKey.of(fs.getFluid(), fs.getTag()).equals(e.identityKey)) return false;
                        e.amount = Math.max(1, fs.getAmount());
                    }
                }
                case K_STOCKING -> {
                    for (var slot : ((ExportOnlyAEFluidList) handler).getInventory()) {
                        var config = slot.getConfig();
                        if (config == null || !(config.what() instanceof AEFluidKey key)) continue;
                        if (i >= entries.size()) return false;
                        var e = entries.get(i++);
                        if (!key.equals(e.identityKey)) return false;
                        e.amount = -1;
                    }
                }
                case K_ME_MAP -> {
                    for (var fs : ((IOptimizedMEList) handler).getMEFluidList()) {
                        if (fs == null || fs.isEmpty()) continue;
                        if (i >= entries.size()) return false;
                        var e = entries.get(i++);
                        if (!AEFluidKey.of(fs.getFluid(), fs.getTag()).equals(e.identityKey)) return false;
                        e.amount = fs.getAmount();
                    }
                }
                default -> {
                    for (var o : handler.getContents()) {
                        if (!(o instanceof FluidStack fs) || fs.isEmpty()) continue;
                        if (i >= entries.size()) return false;
                        var e = entries.get(i++);
                        if (!AEFluidKey.of(fs.getFluid(), fs.getTag()).equals(e.identityKey)) return false;
                        e.amount = fs.getAmount();
                    }
                }
            }
        } else {
            switch (subkind) {
                case K_CATALYST -> {
                    for (var o : handler.getContents()) {
                        if (!(o instanceof ItemStack st) || st.isEmpty()) continue;
                        if (i >= entries.size()) return false;
                        var e = entries.get(i++);
                        if (!AEItemKey.of(st).equals(e.identityKey)) return false;
                        e.amount = Math.max(1, st.getCount());
                    }
                }
                case K_STOCKING -> {
                    for (var slot : ((ExportOnlyAEItemList) handler).getInventory()) {
                        var config = slot.getConfig();
                        if (config == null || !(config.what() instanceof AEItemKey key)) continue;
                        var stack = key.getReadOnlyStack();
                        if (stack.isEmpty()) continue;
                        if (i >= entries.size()) return false;
                        var e = entries.get(i++);
                        if (!key.equals(e.identityKey)) return false;
                        e.amount = -1;
                    }
                }
                case K_ME_MAP -> {
                    var map = ((IOptimizedMEList) handler).getMEItemMap();
                    if (map == null) return entries.isEmpty();
                    for (var e0 : Object2LongMaps.fastIterable(map)) {
                        var st = e0.getKey();
                        if (st == null || st.isEmpty()) continue;
                        if (i >= entries.size()) return false;
                        var e = entries.get(i++);
                        if (!AEItemKey.of(st).equals(e.identityKey)) return false;
                        e.amount = e0.getLongValue();
                    }
                }
                default -> {
                    for (var o : handler.getContents()) {
                        if (!(o instanceof ItemStack st) || st.isEmpty()) continue;
                        if (i >= entries.size()) return false;
                        var e = entries.get(i++);
                        if (!AEItemKey.of(st).equals(e.identityKey)) return false;
                        e.amount = st.getCount();
                    }
                }
            }
        }
        return i == entries.size();
    }

    ObjectArrayList<ContentEntry> build() {
        var out = new ObjectArrayList<ContentEntry>();
        if (fluid) {
            switch (subkind) {
                case K_CATALYST -> {
                    for (var o : handler.getContents()) {
                        if (o instanceof FluidStack fs && !fs.isEmpty()) {
                            addEntry(out, fluidEntry(Math.max(1, fs.getAmount()), fs, null, null, false, false, null, null, false));
                        }
                    }
                }
                case K_STOCKING -> {
                    for (var slot : ((ExportOnlyAEFluidList) handler).getInventory()) {
                        var config = slot.getConfig();
                        if (config == null || !(config.what() instanceof AEFluidKey key)) continue;
                        var fs = FluidStack.create(key.getFluid(), 1);
                        addEntry(out, fluidEntry(-1, fs, key, key, true, true, grid, source, false));
                    }
                }
                case K_ME_MAP -> {
                    for (var fs : ((IOptimizedMEList) handler).getMEFluidList()) {
                        if (fs == null || fs.isEmpty()) continue;
                        addEntry(out, fluidEntry(fs.getAmount(), fs, null, null, true, true, null, null, false));
                    }
                }
                default -> {
                    for (var o : handler.getContents()) {
                        if (o instanceof FluidStack fs && !fs.isEmpty()) {
                            addEntry(out, fluidEntry(fs.getAmount(), fs, null, null, true, true, null, null, false));
                        }
                    }
                }
            }
        } else {
            switch (subkind) {
                case K_CATALYST -> {
                    for (var o : handler.getContents()) {
                        if (o instanceof ItemStack st && !st.isEmpty()) {
                            addEntry(out, itemEntry(Math.max(1, st.getCount()), st, null, null, false, false, null, null, false));
                        }
                    }
                }
                case K_STOCKING -> {
                    for (var slot : ((ExportOnlyAEItemList) handler).getInventory()) {
                        var config = slot.getConfig();
                        if (config == null || !(config.what() instanceof AEItemKey key)) continue;
                        var stack = key.getReadOnlyStack();
                        if (stack.isEmpty()) continue;
                        addEntry(out, itemEntry(-1, stack, key, key, true, true, grid, source, false));
                    }
                }
                case K_ME_MAP -> {
                    var map = ((IOptimizedMEList) handler).getMEItemMap();
                    if (map == null) return out;
                    for (var e : Object2LongMaps.fastIterable(map)) {
                        var st = e.getKey();
                        if (st == null || st.isEmpty()) continue;
                        addEntry(out, itemEntry(e.getLongValue(), st, null, null, true, true, null, null, false));
                    }
                }
                default -> {
                    for (var o : handler.getContents()) {
                        if (o instanceof ItemStack st && !st.isEmpty()) {
                            addEntry(out, itemEntry(st.getCount(), st, null, null, true, true, null, null, false));
                        }
                    }
                }
            }
        }
        return out;
    }

    static void addEntry(ObjectArrayList<ContentEntry> out, @Nullable ContentEntry e) {
        if (e != null) out.add(e);
    }

    static ContentEntry itemEntry(long amount, ItemStack st, @Nullable AEKey key,
                                  @Nullable AEKey identityKey, boolean scalesWithParallel,
                                  boolean deductOnConsume, @Nullable IGrid grid,
                                  @Nullable IActionSource source, boolean notConsumedSupply) {
        var variants = ItemRecipeCapability.CAP.convertToMapIngredient(st);
        if (variants.isEmpty()) return null;
        return new ContentEntry(amount, variants, key,
                identityKey != null ? identityKey : AEItemKey.of(st),
                scalesWithParallel, deductOnConsume, grid, source, notConsumedSupply);
    }

    static ContentEntry fluidEntry(long amount, FluidStack fs, @Nullable AEKey key,
                                   @Nullable AEKey identityKey, boolean scalesWithParallel,
                                   boolean deductOnConsume, @Nullable IGrid grid,
                                   @Nullable IActionSource source, boolean notConsumedSupply) {
        var variants = FluidRecipeCapability.CAP.convertToMapIngredient(fs);
        if (variants.isEmpty()) return null;
        return new ContentEntry(amount, variants, key,
                identityKey != null ? identityKey : AEFluidKey.of(fs.getFluid(), fs.getTag()),
                scalesWithParallel, deductOnConsume, grid, source, notConsumedSupply);
    }
}
