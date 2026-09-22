package com.gtladd.gtladditions.mixin.gtlcore.integration.ae;

import org.gtlcore.gtlcore.integration.ae2.storage.FastInfinityCellInventory;
import org.gtlcore.gtlcore.utils.datastructure.Int128;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import com.gtladd.gtladditions.api.ae2.IMEStorage;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FastInfinityCellInventory.class)
public abstract class FastInfinityCellInventoryMixin implements IMEStorage {

    @Shadow(remap = false)
    @Final
    private KeyCounter lists;

    @Shadow(remap = false)
    protected abstract Object2ObjectOpenHashMap<AEKey, Int128> getCellItems();

    @Override
    public Object2LongMap<AEKey> getStorageMap() {
        return null;
    }

    @Override
    public KeyCounter getAvailableCounter() {
        getCellItems();
        return this.lists;
    }
}
