package com.gtladd.gtladditions.mixin.gtlcore.integration.ae;

import org.gtlcore.gtlcore.integration.ae2.storage.InfinityCellInventory;
import org.gtlcore.gtlcore.integration.ae2.storage.PreciseStorageAmount;

import appeng.api.stacks.AEKey;
import com.gtladd.gtladditions.api.ae2.IMEStorage;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.math.BigInteger;

@Mixin(InfinityCellInventory.class)
public abstract class InfinityCellInventoryMixin implements IMEStorage, PreciseStorageAmount {

    @Shadow(remap = false)
    protected abstract Object2ObjectOpenHashMap<AEKey, BigInteger> getCellItems();

    @Override
    public Object2ObjectMap<AEKey, BigInteger> getInfinityMap() {
        return getCellItems();
    }

    @Override
    public BigInteger getExactStoredAmount(AEKey key) {
        var amount = getCellItems().get(key);
        return amount == null ? BigInteger.ZERO : amount;
    }
}
