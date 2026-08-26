package com.gtladd.gtladditions.mixin.gtceu.common.machine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.ResearchStationMachine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import com.gtladd.gtladditions.common.machine.CloudOpticalDataMachine;
import com.hepdd.gtmthings.utils.TeamUtil;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(ResearchStationMachine.class)
public class ResearchStationMachineMixin extends WorkableElectricMultiblockMachine implements CloudOpticalDataMachine.ICloudTeamBindable {

    @Unique
    private UUID teamId;

    public ResearchStationMachineMixin(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Unique
    @Nullable
    @Override
    public UUID getTeamId() {
        return teamId;
    }

    @Unique
    @Override
    public void setTeamId(@Nullable UUID id) {
        this.teamId = id;
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        if (tag.contains("teamId")) teamId = tag.getUUID("teamId");
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putUUID("teamId", teamId);
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
}
