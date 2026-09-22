package com.gtladd.gtladditions.api.recipe.ledger;

import org.gtlcore.gtlcore.api.capability.IMERecipeHandler;
import org.gtlcore.gtlcore.common.machine.multiblock.part.ae.MEPatternBufferPartMachineBase;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("all")
final class SlotCache {

    long version = -1;
    final ObjectOpenHashSet<AEItemKey> realItemKeys = new ObjectOpenHashSet<>();
    final ObjectOpenHashSet<AEFluidKey> realFluidKeys = new ObjectOpenHashSet<>();
    @Nullable
    ObjectArrayList<ContentEntry> list;

    @NotNull
    ObjectArrayList<ContentEntry> refresh(long v, @Nullable IMERecipeHandler<?, ?> itemHandler,
                                          @Nullable IMERecipeHandler<?, ?> fluidHandler, int slot,
                                          @Nullable Int2ObjectMap<?> itemLimits,
                                          @Nullable Int2ObjectMap<?> fluidLimits,
                                          @Nullable MEPatternBufferPartMachineBase buffer) {
        if (version == v && list != null) return list;
        version = v;
        if (list == null || !refreshInPlace(itemHandler, fluidHandler, slot, itemLimits, fluidLimits, buffer)) {
            list = build(itemHandler, fluidHandler, slot, itemLimits, fluidLimits, buffer);
        }
        return list;
    }

    boolean refreshInPlace(@Nullable IMERecipeHandler<?, ?> itemHandler,
                           @Nullable IMERecipeHandler<?, ?> fluidHandler, int slot,
                           @Nullable Int2ObjectMap<?> itemLimits,
                           @Nullable Int2ObjectMap<?> fluidLimits,
                           @Nullable MEPatternBufferPartMachineBase buffer) {
        int i = 0;
        var entries = list;
        if (itemHandler != null) {
            var realKeys = realItemKeys;
            realKeys.clear();
            for (var e0 : Object2LongMaps.fastIterable((Object2LongMap<ItemStack>) itemHandler.getSingleSlotStackMap(slot))) {
                var stack = e0.getKey();
                long amount = e0.getLongValue();
                if (stack == null || stack.isEmpty() || amount <= 0) continue;
                if (i >= entries.size()) return false;
                var e = entries.get(i++);
                if (!e.scalesWithParallel || !AEItemKey.of(stack).equals(e.identityKey)) return false;
                e.amount = amount;
                realKeys.add(AEItemKey.of(stack));
            }
            var circuit = buffer != null ? buffer.getCircuitForRecipe(slot) : ItemStack.EMPTY;
            if (!circuit.isEmpty()) {
                if (i >= entries.size()) return false;
                var e = entries.get(i++);
                if (e.scalesWithParallel || e.notConsumedSupply || !AEItemKey.of(circuit).equals(e.identityKey)) return false;
                e.amount = 1;
            }
            if (itemLimits != null) {
                Object contents = itemLimits.get(slot);
                if (contents instanceof List<?> l) {
                    var circuitKey = circuit.isEmpty() ? null : AEItemKey.of(circuit);
                    for (var o : l) {
                        if (!(o instanceof ItemStack st) || st.isEmpty()) continue;
                        if (st.getCount() != Integer.MAX_VALUE) {
                            var key = AEItemKey.of(st);
                            if (realKeys.contains(key) || key.equals(circuitKey)) continue; // 已入账：真实库存/电路
                        }
                        if (i >= entries.size()) return false;
                        var e = entries.get(i++);
                        if (e.scalesWithParallel || !e.notConsumedSupply || !AEItemKey.of(st).equals(e.identityKey)) return false;
                        e.amount = 1;
                    }
                }
            }
        }
        if (fluidHandler != null) {
            var realKeys = realFluidKeys;
            realKeys.clear();
            for (var e0 : Object2LongMaps.fastIterable((Object2LongMap<FluidStack>) fluidHandler.getSingleSlotStackMap(slot))) {
                var fs = e0.getKey();
                long amount = e0.getLongValue();
                if (fs == null || fs.isEmpty() || amount <= 0) continue;
                if (i >= entries.size()) return false;
                var e = entries.get(i++);
                if (!e.scalesWithParallel || !AEFluidKey.of(fs.getFluid(), fs.getTag()).equals(e.identityKey)) return false;
                e.amount = amount;
                realKeys.add(AEFluidKey.of(fs.getFluid())); // 与旧实现一致：realKeys 过滤不掺 tag
            }
            if (fluidLimits != null) {
                Object contents = fluidLimits.get(slot);
                if (contents instanceof List<?> l) {
                    for (var o : l) {
                        if (!(o instanceof FluidStack fs) || fs.isEmpty()) continue;
                        if (fs.getAmount() != Integer.MAX_VALUE && realKeys.contains(AEFluidKey.of(fs.getFluid()))) continue;
                        if (i >= entries.size()) return false;
                        var e = entries.get(i++);
                        if (e.scalesWithParallel || !e.notConsumedSupply || !AEFluidKey.of(fs.getFluid(), fs.getTag()).equals(e.identityKey)) return false;
                        e.amount = 1;
                    }
                }
            }
        }
        return i == entries.size();
    }

    ObjectArrayList<ContentEntry> build(@Nullable IMERecipeHandler<?, ?> itemHandler,
                                        @Nullable IMERecipeHandler<?, ?> fluidHandler, int slot,
                                        @Nullable Int2ObjectMap<?> itemLimits,
                                        @Nullable Int2ObjectMap<?> fluidLimits,
                                        @Nullable MEPatternBufferPartMachineBase buffer) {
        var out = new ObjectArrayList<ContentEntry>();
        if (itemHandler != null) {
            var realKeys = realItemKeys;
            realKeys.clear();
            for (var e0 : Object2LongMaps.fastIterable((Object2LongMap<ItemStack>) itemHandler.getSingleSlotStackMap(slot))) {
                var stack = e0.getKey();
                long amount = e0.getLongValue();
                if (stack == null || stack.isEmpty() || amount <= 0) continue;
                var e = HandleCache.itemEntry(amount, stack, null, null, true, true, null, null, false);
                if (e != null) {
                    out.add(e);
                    realKeys.add(AEItemKey.of(stack));
                }
            }
            var circuit = buffer != null ? buffer.getCircuitForRecipe(slot) : ItemStack.EMPTY;
            if (!circuit.isEmpty()) {
                var e = HandleCache.itemEntry(1, circuit, null, null, false, false, null, null, false);
                if (e != null) out.add(e);
            }
            if (itemLimits != null) {
                Object contents = itemLimits.get(slot);
                if (contents instanceof List<?> l) {
                    var circuitKey = circuit.isEmpty() ? null : AEItemKey.of(circuit);
                    for (var o : l) {
                        if (!(o instanceof ItemStack st) || st.isEmpty()) continue;
                        if (st.getCount() != Integer.MAX_VALUE) {
                            var key = AEItemKey.of(st);
                            if (realKeys.contains(key) || key.equals(circuitKey)) continue; // 已入账：真实库存/电路
                        }
                        var e = HandleCache.itemEntry(1, st, null, null, false, false, null, null, true);
                        if (e != null) out.add(e);
                    }
                }
            }
        }
        if (fluidHandler != null) {
            var realKeys = realFluidKeys;
            realKeys.clear();
            for (var e0 : Object2LongMaps.fastIterable((Object2LongMap<FluidStack>) fluidHandler.getSingleSlotStackMap(slot))) {
                var fs = e0.getKey();
                long amount = e0.getLongValue();
                if (fs == null || fs.isEmpty() || amount <= 0) continue;
                var e = HandleCache.fluidEntry(amount, fs, null, null, true, true, null, null, false);
                if (e != null) {
                    out.add(e);
                    realKeys.add(AEFluidKey.of(fs.getFluid()));
                }
            }
            if (fluidLimits != null) {
                Object contents = fluidLimits.get(slot);
                if (contents instanceof List<?> l) {
                    for (var o : l) {
                        if (!(o instanceof FluidStack fs) || fs.isEmpty()) continue;
                        if (fs.getAmount() != Integer.MAX_VALUE && realKeys.contains(AEFluidKey.of(fs.getFluid()))) continue;
                        var e = HandleCache.fluidEntry(1, fs, null, null, false, false, null, null, true);
                        if (e != null) out.add(e);
                    }
                }
            }
        }
        return out;
    }
}
