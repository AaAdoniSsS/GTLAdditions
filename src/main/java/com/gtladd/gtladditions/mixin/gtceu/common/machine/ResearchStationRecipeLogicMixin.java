package com.gtladd.gtladditions.mixin.gtceu.common.machine;

import com.gregtechceu.gtceu.api.capability.IObjectHolder;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.ResearchStationMachine;

import net.minecraft.world.item.ItemStack;

import com.gtladd.gtladditions.common.machine.CloudOpticalDataMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(targets = "com.gregtechceu.gtceu.common.machine.multiblock.electric.research.ResearchStationMachine$ResearchStationRecipeLogic")
public abstract class ResearchStationRecipeLogicMixin {

    @Shadow(remap = false)
    public abstract ResearchStationMachine getMachine();

    /**
     * 配方完成后自动往空闲的云端研究数据存储器上传研究数据。
     */
    @Inject(
            method = "onRecipeFinish",
            at = @At("TAIL"),
            remap = false)
    private void injectCloudUpload(CallbackInfo ci) {
        ResearchStationMachine machine = getMachine();
        if (!(machine instanceof CloudOpticalDataMachine.ICloudTeamBindable bindable)) return;

        UUID teamId = bindable.getTeamId();
        if (teamId == null) return;

        IObjectHolder holder = machine.getObjectHolder();

        ItemStack dataStick = holder.getDataItem(false);
        if (dataStick.isEmpty()) return;

        boolean uploaded = CloudOpticalDataMachine.uploadDataStickToCloud(dataStick, teamId);

        if (uploaded) {
            holder.setDataItem(ItemStack.EMPTY);
        }
    }
}
