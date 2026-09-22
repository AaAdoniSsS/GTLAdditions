package com.gtladd.gtladditions.api.recipe.ledger;

import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;

import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ContentEntry {

    public long amount;
    public final List<AbstractMapIngredient> variants;
    @Nullable
    public final AEKey aeKey;
    public final AEKey identityKey;
    public final boolean scalesWithParallel;
    public final boolean deductOnConsume;
    @Nullable
    public final IGrid grid;
    @Nullable
    public final IActionSource source;
    public final boolean notConsumedSupply;

    public ContentEntry(long amount, List<AbstractMapIngredient> variants, @Nullable AEKey aeKey,
                        AEKey identityKey, boolean scalesWithParallel, boolean deductOnConsume,
                        @Nullable IGrid grid, @Nullable IActionSource source, boolean notConsumedSupply) {
        this.amount = amount;
        this.variants = variants;
        this.aeKey = aeKey;
        this.identityKey = identityKey;
        this.scalesWithParallel = scalesWithParallel;
        this.deductOnConsume = deductOnConsume;
        this.grid = grid;
        this.source = source;
        this.notConsumedSupply = notConsumedSupply;
    }
}
