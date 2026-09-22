package com.gtladd.gtladditions.mixin.ae.api;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mixin(targets = "appeng.api.stacks.VariantCounter$UnorderedVariantMap")
public class UnorderedVariantMapMixin {

    private static final Constructor<?> CONSTRUCTOR;
    private static final Field RECORDS;
    private static final Method CLONE_METHOD;

    static {
        try {
            var cls = Class.forName("appeng.api.stacks.VariantCounter$UnorderedVariantMap");
            CONSTRUCTOR = cls.getDeclaredConstructor();
            CONSTRUCTOR.setAccessible(true);
            RECORDS = cls.getDeclaredField("records");
            RECORDS.setAccessible(true);
            CLONE_METHOD = Object2LongOpenHashMap.class.getMethod("clone");
            CLONE_METHOD.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Inject(method = "copy", at = @At("HEAD"), remap = false, cancellable = true)
    private void onCopy(CallbackInfoReturnable<Object> cir) throws Exception {
        var result = CONSTRUCTOR.newInstance();
        RECORDS.set(result, CLONE_METHOD.invoke(RECORDS.get(this)));
        cir.setReturnValue(result);
    }
}
