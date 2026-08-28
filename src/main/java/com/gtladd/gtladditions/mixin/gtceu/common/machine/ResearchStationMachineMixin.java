package com.gtladd.gtladditions.mixin.gtceu.common.machine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.ResearchStationMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.gtladd.gtladditions.common.machine.CloudOpticalDataMachine;
import com.hepdd.gtmthings.utils.TeamUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.UUID;

@Mixin(ResearchStationMachine.class)
public class ResearchStationMachineMixin extends WorkableElectricMultiblockMachine implements CloudOpticalDataMachine.ICloudTeamBindable {

    @Unique
    @Getter
    private UUID teamId;

    public ResearchStationMachineMixin(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        if (tag.contains("teamId")) teamId = tag.getUUID("teamId");
        else teamId = null;
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (teamId != null) {
            tag.putUUID("teamId", teamId);
        } else {
            tag.remove("teamId");
        }
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        if (isFormed()) {
            if (teamId != null && getLevel() != null && TeamUtil.hasOwner(getLevel(), teamId)) {
                textList.add(Component.translatable("gui.gtladditions.cloud.bind_success", TeamUtil.GetName(getLevel(), teamId)));
            } else {
                textList.add(Component.translatable("gui.gtladditions.cloud.not_bound"));
            }
        }
        super.addDisplayText(textList);
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        var item = player.getMainHandItem();
        if (!item.is(GTItems.TOOL_DATA_STICK.asItem())) return super.onUse(state, world, pos, player, hand, hit);
        this.teamId = player.getUUID();
        if (player instanceof ServerPlayer sp) sp.sendSystemMessage(Component.translatable("gui.gtladditions.cloud.bind_success", TeamUtil.GetName(sp)));
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onLeftClick(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        var item = player.getMainHandItem();
        if (!item.is(GTItems.TOOL_DATA_STICK.asItem())) return super.onLeftClick(player, world, hand, pos, direction);
        this.teamId = null;
        if (player instanceof ServerPlayer sp) sp.sendSystemMessage(Component.translatable("gui.gtladditions.cloud.unbind_success"));
        return true;
    }
}
