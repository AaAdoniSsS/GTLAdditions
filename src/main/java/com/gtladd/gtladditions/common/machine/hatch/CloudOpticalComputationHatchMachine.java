package com.gtladd.gtladditions.common.machine.hatch;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import com.gtladd.gtladditions.api.machine.trait.CloudOpticalComputationContainer;
import com.gtladd.gtladditions.common.machine.CloudOpticalComputationMonitorMachine;
import lombok.Getter;

import java.util.Objects;

public class CloudOpticalComputationHatchMachine extends MultiblockPartMachine implements IMachineLife {

    @Getter
    private final boolean transmitter;
    @Getter
    private CloudOpticalComputationContainer computationContainer;

    public CloudOpticalComputationHatchMachine(IMachineBlockEntity holder, boolean transmitter) {
        super(holder);
        this.transmitter = transmitter;
        this.computationContainer = new CloudOpticalComputationContainer(this, transmitter ? IO.OUT : IO.IN, transmitter);
    }

    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public void onMachineRemoved() {
        if (transmitter) {
            CloudOpticalComputationMonitorMachine.CLOUD_HATCH_SET.remove(this);
            CloudOpticalComputationMonitorMachine.markCacheDirty();
        }
    }

    @Override
    public void onLoad() {
        if (transmitter && !isRemote()) {
            CloudOpticalComputationMonitorMachine.CLOUD_HATCH_SET.add(this);
            CloudOpticalComputationMonitorMachine.markCacheDirty();
        }
    }

    @Override
    public void onUnload() {
        if (transmitter && !isRemote()) {
            CloudOpticalComputationMonitorMachine.CLOUD_HATCH_SET.remove(this);
            CloudOpticalComputationMonitorMachine.markCacheDirty();
        }
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        if (transmitter && !isRemote()) {
            CloudOpticalComputationMonitorMachine.CLOUD_HATCH_SET.add(this);
            CloudOpticalComputationMonitorMachine.markCacheDirty();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getPos(), getLevel());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CloudOpticalComputationHatchMachine h) {
            return this.getPos().equals(h.getPos()) && Objects.equals(getLevel(), h.getLevel());
        } else if (obj instanceof BlockPos pos) {
            return this.getPos().equals(pos);
        }
        return false;
    }
}
