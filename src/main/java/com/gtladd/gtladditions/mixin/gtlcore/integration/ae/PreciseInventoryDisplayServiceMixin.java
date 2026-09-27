package com.gtladd.gtladditions.mixin.gtlcore.integration.ae;

import org.gtlcore.gtlcore.integration.ae2.storage.PreciseInventoryDisplayService;

import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import com.gtladd.gtladditions.api.ae2.QueryReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Mixin(PreciseInventoryDisplayService.class)
public class PreciseInventoryDisplayServiceMixin {

    @Inject(method = "query(Lappeng/api/storage/MEStorage;Ljava/util/List;Lappeng/api/networking/security/IActionSource;)Ljava/util/Map;",
            at = @At("HEAD"),
            remap = false,
            cancellable = true)
    private static void queryTable(MEStorage root, List<AEKey> keys, IActionSource source, CallbackInfoReturnable<Map<AEKey, BigInteger>> cir) {
        cir.setReturnValue(QueryReader.INSTANCE.query(root, keys, source));
    }
}
