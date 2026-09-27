package com.gtladd.gtladditions.api.machine;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import com.gtladd.gtladditions.api.recipe.ledger.RecipeSearchContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRecipeSearchProvider {

    @Nullable
    RecipeSearchContext getSearchContext();

    void setSearchContext(@Nullable RecipeSearchContext ctx);

    @Nullable
    default RecipeSearchContext getActiveSearchContext() {
        var ctx = getSearchContext();
        return ctx != null && ctx.isCycleActive() ? ctx : null;
    }

    @NotNull
    default RecipeSearchContext beginSearchCycle(@NotNull WorkableElectricMultiblockMachine machine) {
        var ctx = getSearchContext();
        if (ctx == null) setSearchContext(ctx = new RecipeSearchContext(machine));
        if (!ctx.isCycleActive()) ctx.beginCycle();
        return ctx;
    }

    default void endSearchCycle() {
        var ctx = getSearchContext();
        if (ctx != null) ctx.endCycle();
    }
}
