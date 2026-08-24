package com.gtladd.gtladditions.common.machine.hatch;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import com.gtladd.gtladditions.api.machine.trait.CloudOpticalComputationContainer;
import com.gtladd.gtladditions.common.machine.CloudOpticalComputationMonitorMachine;
import com.hepdd.gtmthings.api.capability.IBindable;
import com.hepdd.gtmthings.utils.TeamUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class CloudOpticalComputationHatchMachine extends MultiblockPartMachine implements IMachineLife, IDataStickInteractable, IBindable {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CloudOpticalComputationHatchMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Getter
    private final boolean transmitter;
    @Getter
    private CloudOpticalComputationContainer computationContainer;

    @Getter
    @Persisted
    @DescSynced
    private UUID teamId;

    public CloudOpticalComputationHatchMachine(IMachineBlockEntity holder, boolean transmitter) {
        super(holder);
        this.transmitter = transmitter;
        this.computationContainer = new CloudOpticalComputationContainer(this, transmitter ? IO.OUT : IO.IN, transmitter);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    private void bindTeam(Player player) {
        this.teamId = player.getUUID();
    }

    @Override
    public InteractionResult onDataStickRightClick(Player player, ItemStack stack) {
        bindTeam(player);
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("gui.gtladditions.cloud.bind_success", TeamUtil.GetName(sp)));
        }
        CloudOpticalComputationMonitorMachine.markCacheDirty();
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onDataStickLeftClick(Player player, ItemStack stack) {
        this.teamId = null;
        CloudOpticalComputationMonitorMachine.markCacheDirty();
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("gui.gtladditions.cloud.unbind_success"));
        }
        return true;
    }

    @Override
    public void onMachineRemoved() {
        if (transmitter) CloudOpticalComputationMonitorMachine.CLOUD_TRANSMITTER_HATCH_SET.remove(this);
        else CloudOpticalComputationMonitorMachine.CLOUD_RECEIVER_HATCH_SET.remove(this);
        CloudOpticalComputationMonitorMachine.markCacheDirty();
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (player instanceof Player p) {
            bindTeam(p);
            if (transmitter) CloudOpticalComputationMonitorMachine.CLOUD_TRANSMITTER_HATCH_SET.add(this);
            else CloudOpticalComputationMonitorMachine.CLOUD_RECEIVER_HATCH_SET.add(this);
            CloudOpticalComputationMonitorMachine.markCacheDirty();
        }
    }

    @Override
    public void onLoad() {
        if (isRemote()) return;
        if (transmitter) CloudOpticalComputationMonitorMachine.CLOUD_TRANSMITTER_HATCH_SET.add(this);
        else CloudOpticalComputationMonitorMachine.CLOUD_RECEIVER_HATCH_SET.add(this);
        CloudOpticalComputationMonitorMachine.markCacheDirty();
    }

    @Override
    public void onUnload() {
        if (isRemote()) return;
        if (transmitter) CloudOpticalComputationMonitorMachine.CLOUD_TRANSMITTER_HATCH_SET.remove(this);
        else CloudOpticalComputationMonitorMachine.CLOUD_RECEIVER_HATCH_SET.remove(this);
        CloudOpticalComputationMonitorMachine.markCacheDirty();
    }

    @Override
    public void addedToController(@NotNull IMultiController controller) {
        super.addedToController(controller);
        if (isRemote()) return;
        if (transmitter) CloudOpticalComputationMonitorMachine.CLOUD_TRANSMITTER_HATCH_SET.add(this);
        else CloudOpticalComputationMonitorMachine.CLOUD_RECEIVER_HATCH_SET.add(this);
        CloudOpticalComputationMonitorMachine.markCacheDirty();
    }

    @Override
    public void removedFromController(@NotNull IMultiController controller) {
        super.removedFromController(controller);
        if (isRemote()) return;
        if (transmitter) CloudOpticalComputationMonitorMachine.CLOUD_TRANSMITTER_HATCH_SET.remove(this);
        else CloudOpticalComputationMonitorMachine.CLOUD_RECEIVER_HATCH_SET.remove(this);
        CloudOpticalComputationMonitorMachine.markCacheDirty();
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

    @Override
    public UUID getUUID() {
        return teamId;
    }

    @Override
    public void setUUID(UUID uuid) {
        this.teamId = uuid;
    }
}
