package com.gtladd.gtladditions.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.Branch;
import com.gregtechceu.gtceu.api.recipe.lookup.GTRecipeLookup;

import com.gtladd.gtladditions.mixin.gtceu.api.recipe.IGTRecipeLookupInvoker;

public class MultiGTRecipeLookup extends GTRecipeLookup {

    final GTRecipeType[] types;
    Branch branch;

    public MultiGTRecipeLookup(GTRecipeType... recipeTypes) {
        super(recipeTypes[0]);
        this.types = recipeTypes;
    }

    public Branch getBranch() {
        if (branch == null) branch = initRecipesBranch();
        return branch;
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

    @Override
    public void removeAllRecipes() {
        this.branch = null;
    }
}
