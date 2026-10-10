package com.gtladd.gtladditions.mixin.gtlcore.machine;

import org.gtlcore.gtlcore.common.machine.multiblock.electric.AdvancedInfiniteDrillMachine;
import org.gtlcore.gtlcore.common.machine.multiblock.part.TemperatureSensorPartMachine;
import org.gtlcore.gtlcore.common.machine.trait.AdvancedInfiniteDrillLogic;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.material.Fluid;

import com.gtladd.gtladditions.common.machine.hatch.OreProcessorHatch;
import com.gtladd.gtladditions.utils.DrillOverclock;
import com.gtladd.gtladditions.utils.SpectralTuning;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(AdvancedInfiniteDrillMachine.class)
public abstract class AdvancedInfiniteDrillMachineMixin {

    @Unique
    private OreProcessorHatch gtladditions$hatch;

    @Shadow(remap = false)
    protected int currentHeat;
    @Shadow(remap = false)
    protected int process;
    @Shadow(remap = false)
    public int maxHeat;
    @Shadow(remap = false)
    private TemperatureSensorPartMachine sensorMachines;

    @Unique
    private OreProcessorHatch gtladditions$hatch() {
        var parts = ((AdvancedInfiniteDrillMachine) (Object) this).getParts();
        if (gtladditions$hatch != null && parts.contains(gtladditions$hatch)) return gtladditions$hatch;
        gtladditions$hatch = null;
        for (var part : parts) {
            if (part instanceof OreProcessorHatch hatch) {
                gtladditions$hatch = hatch;
                break;
            }
        }
        return gtladditions$hatch;
    }

    @Unique
    private void gtladditions$syncRange(OreProcessorHatch hatch) {
        var self = (AdvancedInfiniteDrillMachine) (Object) this;
        if (self.isRemote()) return;
        int target = SpectralTuning.range(hatch.getThirdTier());
        AdvancedInfiniteDrillLogic logic = self.getRecipeLogic();
        if (logic.getRange() != target) {
            logic.setRange(target);
            logic.getVeinFluids().clear();
        }
    }

    @Inject(method = "getRate", at = @At("RETURN"), cancellable = true, remap = false)
    private void gtladditions$applyYield(CallbackInfoReturnable<Integer> cir) {
        OreProcessorHatch hatch = gtladditions$hatch();
        if (hatch == null) return;
        cir.setReturnValue((int) (cir.getReturnValueI() * SpectralTuning.yieldFactor(hatch.getFirstTier())));
    }

    @Inject(method = "heatUpdate", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtladditions$perfectTuning(CallbackInfo ci) {
        OreProcessorHatch hatch = gtladditions$hatch();
        if (hatch == null) return;
        gtladditions$syncRange(hatch);
        if (!hatch.getMatchAll() || maxHeat < 0) return;
        currentHeat = maxHeat;
        process = 0;
        if (sensorMachines != null) sensorMachines.update(currentHeat);
        ci.cancel();
    }

    @Redirect(method = "addDisplayText",
              at = @At(value = "INVOKE",
                       target = "Lorg/gtlcore/gtlcore/common/machine/trait/AdvancedInfiniteDrillLogic;getVeinFluids()Ljava/util/Map;"),
              remap = false)
    private Map<Fluid, Long> gtladditions$scaledVeinFluids(AdvancedInfiniteDrillLogic logic) {
        Map<Fluid, Long> fluids = logic.getVeinFluids();
        OreProcessorHatch hatch = gtladditions$hatch();
        if (hatch == null) return fluids;
        var self = (AdvancedInfiniteDrillMachine) (Object) this;
        long factor = DrillOverclock.factor(DrillOverclock.level(self, hatch, self.getRate()));
        if (factor <= 1L) return fluids;
        Map<Fluid, Long> scaled = new HashMap<>(fluids.size());
        fluids.forEach((fluid, amount) -> scaled.put(fluid, amount * factor));
        return scaled;
    }

    @Redirect(method = "addDisplayText",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent gtladditions$rangeText(String text) {
        if (!"5x5".equals(text)) return Component.literal(text);
        AdvancedInfiniteDrillLogic logic = ((AdvancedInfiniteDrillMachine) (Object) this).getRecipeLogic();
        int range = logic.getRange();
        return Component.literal(range + "x" + range);
    }
}
