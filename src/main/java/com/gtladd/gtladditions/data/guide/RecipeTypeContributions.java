package com.gtladd.gtladditions.data.guide;

import org.gtlcore.gtlcore.common.data.machines.GCyMMachines;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMachines;

import net.minecraft.world.item.ItemStack;

import com.gtladd.gtladditions.api.gui.GTLytSlotGrid;
import com.gtladd.gtladditions.utils.ComponentUtil;
import guideme.compiler.tags.RecipeTypeMappingSupplier;
import guideme.document.block.recipes.LytStandardRecipeBox;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

import static com.gregtechceu.gtceu.common.data.GCyMRecipeTypes.ALLOY_BLAST_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTMachines.LARGE_CHEMICAL_REACTOR;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.ATOMIC_TRANSMUTATION_CORE;
import static com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine.TITAN_CRIP_EARTHBORE;
import static com.gtladd.gtladditions.common.recipe.GTLAddRecipesTypes.*;
import static org.gtlcore.gtlcore.common.data.GTLRecipeTypes.*;
import static org.gtlcore.gtlcore.common.data.machines.AdvancedMultiBlockMachineA.*;
import static org.gtlcore.gtlcore.common.data.machines.AdvancedMultiBlockMachineB.*;
import static org.gtlcore.gtlcore.common.data.machines.MultiBlockMachineA.*;
import static org.gtlcore.gtlcore.common.data.machines.MultiBlockMachineA.SLAUGHTERHOUSE;

public class RecipeTypeContributions implements RecipeTypeMappingSupplier {

    private static final Map<GTRecipeType, ItemStack> TYPE_MAP = new Object2ObjectOpenHashMap<>();

    @Override
    public void collect(RecipeTypeMappingSupplier.RecipeTypeMappings mappings) {
        for (var e : TYPE_MAP.entrySet()) mappings.add(e.getKey(), r -> getGTRecipe(r, e.getKey(), e.getValue()));
    }

    private LytStandardRecipeBox<GTRecipe> getGTRecipe(GTRecipe recipe, GTRecipeType gtRecipeType, ItemStack itemStack) {
        var builder = LytStandardRecipeBox.builder().icon(itemStack).title(ComponentUtil.INSTANCE.getTranslatable(gtRecipeType.registryName.toLanguageKey()));
        var slotGrid = GTLytSlotGrid.builder(recipe);
        builder.input(slotGrid.getRecipeInput());
        builder.output(slotGrid.getRecipeOutput());
        return builder.build(recipe);
    }

    static {
        TYPE_MAP.put(TRANSMUTATION_BLOCK_CONVERSION, ATOMIC_TRANSMUTATION_CORE.asStack());
        TYPE_MAP.put(BLAST_RECIPES, GTMachines.ELECTRIC_BLAST_FURNACE.asStack());
        TYPE_MAP.put(TECTONIC_FAULT_GENERATOR, TITAN_CRIP_EARTHBORE.asStack());
        TYPE_MAP.put(MATTER_FABRICATOR_RECIPES, MATTER_FABRICATOR.asStack());
        TYPE_MAP.put(ASSEMBLER_RECIPES, GTMachines.ASSEMBLER[10].asStack());
        TYPE_MAP.put(DECAY_HASTENER_RECIPES, DECAY_HASTENER.asStack());
        TYPE_MAP.put(DISTORT_RECIPES, CHEMICAL_DISTORT.asStack());
        TYPE_MAP.put(QFT_RECIPES, QFT.asStack());
        TYPE_MAP.put(ALLOY_BLAST_RECIPES, GCyMMachines.BLAST_ALLOY_SMELTER.asStack());
        TYPE_MAP.put(GREENHOUSE_RECIPES, GREENHOUSE.asStack());
        TYPE_MAP.put(INCUBATOR_RECIPES, INCUBATOR.asStack());
        TYPE_MAP.put(MIXER_RECIPES, GTMachines.MIXER[2].asStack());
        TYPE_MAP.put(MAGIC_MANUFACTURER_RECIPES, SLAUGHTERHOUSE.asStack());
        TYPE_MAP.put(LARGE_CHEMICAL_RECIPES, LARGE_CHEMICAL_REACTOR.asStack());
    }
}
