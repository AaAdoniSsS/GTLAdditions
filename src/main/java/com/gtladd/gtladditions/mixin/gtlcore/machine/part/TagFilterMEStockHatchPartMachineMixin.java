package com.gtladd.gtladditions.mixin.gtlcore.machine.part;

import org.gtlcore.gtlcore.common.machine.multiblock.part.TagFilterMEStockHatchPartMachine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.integration.ae2.machine.MEInputHatchPartMachine;

import appeng.api.stacks.AEKey;
import appeng.util.prioritylist.IPartitionList;
import com.glodblock.github.extendedae.common.me.taglist.TagPriorityList;
import com.gtladd.gtladditions.api.ae2.MEStockSyncCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TagFilterMEStockHatchPartMachine.class)
public abstract class TagFilterMEStockHatchPartMachineMixin extends MEInputHatchPartMachine {

    @Shadow(remap = false)
    protected String tagWhite;
    @Shadow(remap = false)
    protected String tagBlack;

    @Unique
    private TagPriorityList filter;
    @Unique
    private String lastTagWhite = "";
    @Unique
    private String lastTagBlack = "";

    public TagFilterMEStockHatchPartMachineMixin(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    protected void syncME() {
        var grid = this.getMainNode().getGrid();
        if (grid != null) {
            MEStockSyncCache.syncStock(grid.getStorageService(), this.aeFluidHandler.getInventory());
        }
    }

    @Redirect(method = "refreshList",
              at = @At(value = "INVOKE",
                       target = "Lappeng/util/prioritylist/IPartitionList;isListed(Lappeng/api/stacks/AEKey;)Z"),
              remap = false)
    private boolean refreshList(IPartitionList instance, AEKey aeKey) {
        if (!lastTagWhite.equals(tagWhite) || !lastTagBlack.equals(tagBlack)) {
            filter = null;
            lastTagWhite = tagWhite;
            lastTagBlack = tagBlack;
        }
        if (filter == null) filter = new TagPriorityList(tagWhite, tagBlack);
        return filter.isListed(aeKey);
    }
}
