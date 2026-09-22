package com.gtladd.gtladditions.api.ae2;

import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAESlot;

import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.agrona.collections.Object2LongHashMap;

import java.util.Objects;

public final class MEStockSyncCache {

    private static final ObjectOpenHashSet<AEKey> KEYS = new ObjectOpenHashSet<>();

    public static void syncStock(IStorageService service, ExportOnlyAESlot[] slots) {
        var amounts = collectAmounts(service, slots);
        syncStock(amounts, slots);
    }

    public static boolean syncStock(IStorageService service, ExportOnlyAESlot[] itemSlots, ExportOnlyAESlot[] fluidSlots) {
        KEYS.clear();
        collectDualKeys(itemSlots, fluidSlots);
        var amounts = amountsFor(service);

        boolean changed = false;
        int count = Math.min(itemSlots.length, fluidSlots.length);
        for (int i = 0; i < count; i++) {
            var slot = itemSlots[i];
            var config = slot.getConfig();
            if (config == null) {
                slot = fluidSlots[i];
                config = slot.getConfig();
            }
            GenericStack stock = null;
            if (config != null) {
                var key = config.what();
                long amount = amounts.getValue(key);
                if (amount > 0L) {
                    stock = new GenericStack(key, amount);
                }
            }
            if (!Objects.equals(slot.getStock(), stock)) {
                slot.setStock(stock);
                changed = true;
            }
        }
        return changed;
    }

    private static Object2LongHashMap<AEKey> collectAmounts(IStorageService service, ExportOnlyAESlot[] slots) {
        KEYS.clear();
        for (var slot : slots) {
            var config = slot.getConfig();
            if (config != null) KEYS.add(config.what());
        }
        return amountsFor(service);
    }

    private static Object2LongHashMap<AEKey> amountsFor(IStorageService service) {
        return GridStockCache.getAmounts(service, KEYS, GridStockCache.DISPLAY_MAX_AGE_TICKS);
    }

    private static void collectDualKeys(ExportOnlyAESlot[] itemSlots, ExportOnlyAESlot[] fluidSlots) {
        int count = Math.min(itemSlots.length, fluidSlots.length);
        for (int i = 0; i < count; i++) {
            var config = itemSlots[i].getConfig();
            if (config == null) config = fluidSlots[i].getConfig();
            if (config != null) KEYS.add(config.what());
        }
    }

    private static void syncStock(Object2LongHashMap<AEKey> amounts, ExportOnlyAESlot[] slots) {
        for (var slot : slots) {
            var config = slot.getConfig();
            if (config != null) {
                var key = config.what();
                long extracted = amounts.getValue(key);
                if (extracted > 0L) {
                    slot.setStock(new GenericStack(key, extracted));
                    continue;
                }
            }
            slot.setStock(null);
        }
    }
}
