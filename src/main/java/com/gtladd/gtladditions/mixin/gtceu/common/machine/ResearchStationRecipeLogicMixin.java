package com.gtladd.gtladditions.mixin.gtceu.common.machine;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.ResearchStationMachine;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import com.gtladd.gtladditions.common.machine.CloudOpticalDataMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "com.gregtechceu.gtceu.common.machine.multiblock.electric.research.ResearchStationMachine$ResearchStationRecipeLogic")
public abstract class ResearchStationRecipeLogicMixin extends RecipeLogic {

    public ResearchStationRecipeLogicMixin(IRecipeLogicMachine machine) {
        super(machine);
    }

    @Shadow(remap = false)
    public abstract ResearchStationMachine getMachine();

    /**
     * @author .
     * @reason .
     */
    @SuppressWarnings("all")
    @Overwrite(remap = false)
    public void onRecipeFinish() {
        super.onRecipeFinish();
        var machine = getMachine();
        var holder = this.getMachine().getObjectHolder();
        holder.setHeldItem(ItemStack.EMPTY);
        var outputItem = ItemStack.EMPTY;
        if (!this.lastRecipe.getOutputContents(ItemRecipeCapability.CAP).isEmpty()) {
            outputItem = ((Ingredient) lastRecipe.getOutputContents(ItemRecipeCapability.CAP).get(0).content).kjs$getFirst();
        }

        var teamId = ((CloudOpticalDataMachine.ICloudTeamBindable) machine).getTeamId();
        if (teamId == null) holder.setDataItem(outputItem);
        else if (CloudOpticalDataMachine.uploadDataStickToCloud(outputItem, teamId)) holder.setDataItem(ItemStack.EMPTY);

        holder.setLocked(false);
    }
}
