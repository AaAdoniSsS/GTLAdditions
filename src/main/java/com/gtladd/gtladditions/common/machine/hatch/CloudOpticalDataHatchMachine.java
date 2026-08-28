package com.gtladd.gtladditions.common.machine.hatch;

import org.gtlcore.gtlcore.api.recipe.RecipeResult;

import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.capability.IOpticalDataAccessHatch;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.DataBankMachine;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import com.gtladd.gtladditions.common.machine.CloudOpticalDataMachine;
import com.hepdd.gtmthings.api.capability.IBindable;
import com.hepdd.gtmthings.utils.TeamUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public class CloudOpticalDataHatchMachine extends MultiblockPartMachine implements IOpticalDataAccessHatch, IMachineLife, IDataStickInteractable, IBindable {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CloudOpticalDataHatchMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Getter
    @Persisted
    @DescSynced
    private UUID player;

    public CloudOpticalDataHatchMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (player instanceof Player p) this.player = p.getUUID();
    }

    @Override
    public InteractionResult onDataStickRightClick(Player player, ItemStack stack) {
        this.player = player.getUUID();
        if (player instanceof ServerPlayer sp) sp.sendSystemMessage(Component.translatable("gui.gtladditions.cloud.bind_success", TeamUtil.GetName(sp)));
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onDataStickLeftClick(Player player, ItemStack stack) {
        this.player = null;
        if (player instanceof ServerPlayer sp) sp.sendSystemMessage(Component.translatable("gui.gtladditions.cloud.unbind_success"));
        return true;
    }

    @Override
    public boolean isRecipeAvailable(@NotNull GTRecipe recipe, @NotNull Collection<IDataAccessHatch> seen) {
        seen.add(this);
        return this.player != null && CloudOpticalDataMachine.isRecipeAvailableInCloud(recipe, this.player);
    }

    @Override
    public GTRecipe modifyRecipe(GTRecipe recipe) {
        if (recipe.conditions.stream().noneMatch(ResearchCondition.class::isInstance)) return recipe;
        if (this.player != null && CloudOpticalDataMachine.isRecipeAvailableInCloud(recipe, this.player)) return recipe;
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

    @Override
    public UUID getUUID() {
        return this.player;
    }

    @Override
    public void setUUID(UUID uuid) {
        this.player = uuid;
    }
}
