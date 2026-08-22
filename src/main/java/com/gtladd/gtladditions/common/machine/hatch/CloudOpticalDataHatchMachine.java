package com.gtladd.gtladditions.common.machine.hatch;

import org.gtlcore.gtlcore.api.recipe.RecipeResult;

import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.capability.IOpticalDataAccessHatch;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.DataBankMachine;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import com.gtladd.gtladditions.common.machine.CloudOpticalDataMachine;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class CloudOpticalDataHatchMachine extends MultiblockPartMachine implements IOpticalDataAccessHatch {

    public CloudOpticalDataHatchMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public boolean isRecipeAvailable(@NotNull GTRecipe recipe, @NotNull Collection<IDataAccessHatch> seen) {
        seen.add(this);
        return CloudOpticalDataMachine.isRecipeAvailableInCloud(recipe);
    }

    @Override
    public GTRecipe modifyRecipe(GTRecipe recipe) {
        if (CloudOpticalDataMachine.isRecipeAvailableInCloud(recipe)) return recipe;
        for (var c : this.getControllers()) {
            if (c instanceof DataBankMachine) continue;
            RecipeResult.of((IRecipeLogicMachine) c, RecipeResult.FAIL_NO_FIND_RESEARCHED);
        }
        return null;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public boolean isTransmitter() {
        return false;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }
}
