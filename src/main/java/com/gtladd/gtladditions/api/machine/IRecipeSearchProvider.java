package com.gtladd.gtladditions.api.machine;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import com.gtladd.gtladditions.api.recipe.ledger.RecipeSearchContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 账本搜索引擎槽位宿主：机器持有常驻 {@link RecipeSearchContext} 的引用（实现类只持有 1 个字段，
 * 唯一槽位，不持久化）。「周期内/周期外」由 ctx 自己的 {@link RecipeSearchContext#isCycleActive()}
 * 表达，不再用第二个引用区分，也就不可能出现「active 指向旧对象、reusable 指向新对象」的不一致；
 * 周期边界由 {@link #beginSearchCycle}/{@link #endSearchCycle} 表达（withSearchContext 收口）。
 */
public interface IRecipeSearchProvider {

    /** 常驻 ctx 槽位（机器生命周期）。 */
    @Nullable
    RecipeSearchContext getSearchContext();

    void setSearchContext(@Nullable RecipeSearchContext ctx);

    /**
     * 周期外返回 null ⇒ 既有读取方（ParallelCalculate.getParallel / OptimizedRecipeSearch.activeContext /
     * GTLAddMultipleRecipesLogic.findAndModifyRecipe）的 {@code ctx != null} 判定语义完全不变。
     */
    @Nullable
    default RecipeSearchContext getActiveSearchContext() {
        var ctx = getSearchContext();
        return ctx != null && ctx.isCycleActive() ? ctx : null;
    }

    /** 开启一次搜索周期：复用或新建并复位周期状态；已在周期内则原样返回（不重置）。 */
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
