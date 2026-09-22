package com.gtladd.gtladditions.mixin.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.recipe.lookup.Branch;

import com.gtladd.gtladditions.api.recipe.lookup.IBranchAddition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Branch.class)
public abstract class BranchMixin implements IBranchAddition {

    @Unique
    private int gtladd$minDepth;

    @Override
    public int minDepth() {
        return gtladd$minDepth;
    }

    @Override
    public void setMinDepth(int depth) {
        gtladd$minDepth = depth;
    }
}
