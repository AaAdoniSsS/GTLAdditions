package com.gtladd.gtladditions.mixin.gtlcore.integration.ae;

import org.gtlcore.gtlcore.integration.ae2.AEUtils;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AEUtils.class)
public class AEUtilsMixin {

    @Redirect(method = "reFunds",
              at = @At(value = "INVOKE",
                       target = "Lappeng/api/storage/StorageHelper;poweredInsert(Lappeng/api/networking/energy/IEnergySource;Lappeng/api/storage/MEStorage;Lappeng/api/stacks/AEKey;JLappeng/api/networking/security/IActionSource;)J"),
              remap = false)
    private static long reFunds(IEnergySource energy, MEStorage inv, AEKey input, long amount, IActionSource src) {
        return inv.insert(input, amount, Actionable.MODULATE, src);
    }
}
