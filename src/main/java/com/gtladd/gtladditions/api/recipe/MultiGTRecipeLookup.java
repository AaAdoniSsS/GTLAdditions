package com.gtladd.gtladditions.api.recipe;

import org.gtlcore.gtlcore.api.machine.trait.IRecipeCapabilityMachine;
import org.gtlcore.gtlcore.api.recipe.IRecipeIterator;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.Branch;
import com.gregtechceu.gtceu.api.recipe.lookup.GTRecipeLookup;

import com.gtladd.gtladditions.mixin.gtceu.recipe.IGTRecipeLookupInvoker;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class MultiGTRecipeLookup extends GTRecipeLookup {

    protected final GTRecipeType[] types;
    private Branch branch;

    public MultiGTRecipeLookup(GTRecipeType... recipeTypes) {
        super(recipeTypes[0]);
        this.types = recipeTypes;
    }

    public MultiRecipeIterator getMultiRecipeIterator(IRecipeLogicMachine holder, Predicate<GTRecipe> canHandle) {
        var list = this.prepareRecipeFind(holder);
        var iterator = new MultiRecipeIterator(getBranch(), list, canHandle);
        if (holder instanceof IRecipeCapabilityMachine rcm) {
            var parts = rcm.getMEPatternRecipeHandleParts();
            if (!parts.isEmpty()) {
                List<GTRecipe> meRecipes = new ObjectArrayList<>();
                for (var part : parts) meRecipes.addAll(part.getCachedGTRecipe());
                if (!meRecipes.isEmpty()) iterator.setCacheRecipe(meRecipes);
            }
        }
        return iterator;
    }

    @Override
    public @Nullable GTRecipe find(@NotNull IRecipeCapabilityHolder holder, @NotNull Predicate<GTRecipe> canHandle) {
        var iterator = this.getMultiRecipeIterator((IRecipeLogicMachine) holder, canHandle);
        return iterator.getRecipe();
    }

    public void addRecipeToBranch(GTRecipe recipe, Branch branch) {
        var lists = fromRecipe(recipe);
        ((IGTRecipeLookupInvoker) this).useIngredientTreeAdd(recipe, lists, branch, 0, 0);
    }

    public Branch initRecipesBranch() {
        var branch = new Branch();
        for (var type : this.types)
            type.getLookup().getLookup().getRecipes(true)
                    .forEach(r -> this.addRecipeToBranch(r, branch));
        return branch;
    }

    private Branch getBranch() {
        if (branch == null) branch = initRecipesBranch();
        return branch;
    }

    @Override
    public void removeAllRecipes() {
        this.branch = null;
    }

    public static class MultiRecipeIterator {

        final Branch branch;
        @Nullable
        final List<List<AbstractMapIngredient>> ingredients;
        final Predicate<GTRecipe> canHandle;
        @Setter
        @Nullable
        List<GTRecipe> cacheRecipe;

        MultiRecipeIterator(Branch branch, @Nullable List<List<AbstractMapIngredient>> ingredients, Predicate<GTRecipe> canHandle) {
            this.branch = branch;
            this.ingredients = ingredients;
            this.canHandle = canHandle;
        }

        public GTRecipe getRecipe() {
            if (cacheRecipe != null && !cacheRecipe.isEmpty()) {
                for (var r : cacheRecipe) if (canHandle.test(r)) return r;
            } else {
                if (ingredients != null) {
                    for (var ing : ingredients) {
                        var recipe = IRecipeIterator.diveIngredientTreeFindRecipe(ing, branch, canHandle);
                        if (recipe != null) return recipe;
                    }
                }
            }
            return null;
        }

        public List<GTRecipe> getMultipleRecipes() {
            List<GTRecipe> recipeList = new ObjectArrayList<>();
            if (cacheRecipe != null) recipeList.addAll(cacheRecipe);
            if (ingredients != null) {
                Set<GTRecipe> setRecipe = new ObjectOpenHashSet<>();
                for (var ing : ingredients) {
                    IRecipeIterator.diveIngredientTreeFindRecipeCollection(ing, branch, canHandle, setRecipe);
                    recipeList.addAll(setRecipe);
                    setRecipe.clear();
                }
            }
            return recipeList;
        }
    }
}
