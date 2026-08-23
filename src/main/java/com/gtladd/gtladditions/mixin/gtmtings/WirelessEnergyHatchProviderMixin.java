package com.gtladd.gtladditions.mixin.gtmtings;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.hepdd.gtmthings.api.capability.IBindable;
import com.hepdd.gtmthings.common.cover.WirelessEnergyReceiveCover;
import com.hepdd.gtmthings.integration.jade.provider.WirelessEnergyHatchProvider;
import com.hepdd.gtmthings.utils.TeamUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.UUID;

@Mixin(WirelessEnergyHatchProvider.class)
public abstract class WirelessEnergyHatchProviderMixin extends CapabilityBlockProvider<IBindable> {

    protected WirelessEnergyHatchProviderMixin(ResourceLocation uid) {
        super(uid);
    }

    @Override
    protected @Nullable IBindable getCapability(Level level, BlockPos blockPos, @Nullable Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof MetaMachineBlockEntity metaMachineBlockEntity) {
            MetaMachine metaMachine = metaMachineBlockEntity.getMetaMachine();
            if (metaMachine instanceof IBindable bindable) {
                if (bindable.getUUID() != null) {
                    return new IBindable() {

                        public UUID getUUID() {
                            return bindable.getUUID();
                        }

                        public void setUUID(UUID uuid1) {}
                    };
                }
            }

            if (metaMachine instanceof SimpleTieredMachine simpleTieredMachine) {
                for (CoverBehavior cover : simpleTieredMachine.getCoverContainer().getCovers()) {
                    if (cover instanceof WirelessEnergyReceiveCover wirelessCover) {
                        final UUID uuid = wirelessCover.getUuid();
                        return new IBindable() {

                            public UUID getUUID() {
                                return uuid;
                            }

                            public void setUUID(UUID uuid1) {}
                        };
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block, BlockEntity blockEntity, IPluginConfig config) {
        if (blockEntity instanceof MetaMachineBlockEntity machineBlock) {
            MetaMachine metaMachine = machineBlock.getMetaMachine();
            int machineType;
            if (!(metaMachine instanceof IBindable)) {
                if (!(metaMachine instanceof SimpleTieredMachine) && !(metaMachine instanceof BatteryBufferMachine)) return;
                if (!capData.hasUUID("uuid")) return;
                machineType = 2;
            } else {
                machineType = 1;
            }

            if (!capData.hasUUID("uuid")) {
                if (machineType == 1) {
                    tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.1"));
                } else {
                    tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_cover.tooltip.1"));
                }
            } else {
                UUID uuid = capData.getUUID("uuid");
                if (TeamUtil.hasOwner(block.getLevel(), uuid)) {
                    if (machineType == 1) {
                        tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.2", TeamUtil.GetName(block.getLevel(), uuid)));
                    } else {
                        tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_cover.tooltip.2", TeamUtil.GetName(block.getLevel(), uuid)));
                    }
                } else if (machineType == 1) {
                    tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.3", uuid));
                } else {
                    tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_cover.tooltip.3", uuid));
                }
            }

        }
    }
}
