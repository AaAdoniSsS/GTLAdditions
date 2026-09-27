package com.gtladd.gtladditions.api.recipe.lookup;

public final class RecipeTreeGeneration {

    private static volatile int generation;

    private RecipeTreeGeneration() {}

    public static int current() {
        return generation;
    }

    public static void bump() {
        generation++;
    }
}
