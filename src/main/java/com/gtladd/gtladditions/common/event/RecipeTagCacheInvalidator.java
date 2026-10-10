package com.gtladd.gtladditions.common.event;

import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.gtladd.gtladditions.GTLAdditions;
import com.gtladd.gtladditions.api.recipe.ingredient.MapIngredientVariants;

@Mod.EventBusSubscriber(modid = GTLAdditions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RecipeTagCacheInvalidator {

    private RecipeTagCacheInvalidator() {}

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        MapIngredientVariants.invalidateAll();
    }
}
