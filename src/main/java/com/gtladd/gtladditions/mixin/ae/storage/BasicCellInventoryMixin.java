package com.gtladd.gtladditions.mixin.ae.storage;

import appeng.api.stacks.AEKey;
import appeng.me.cells.BasicCellInventory;
import com.gtladd.gtladditions.api.ae2.IMEStorage;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BasicCellInventory.class)
public abstract class BasicCellInventoryMixin implements IMEStorage {

    @Shadow(remap = false)
    protected abstract Object2LongMap<AEKey> getCellItems();

    @Override
    public Object2LongMap<AEKey> getStorageMap() {
        return getCellItems();
    }
}
