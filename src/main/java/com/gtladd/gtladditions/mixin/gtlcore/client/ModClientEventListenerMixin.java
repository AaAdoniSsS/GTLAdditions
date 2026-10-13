package com.gtladd.gtladditions.mixin.gtlcore.client;

import org.gtlcore.gtlcore.api.event.SourceTooltipRegistrationEvent;
import org.gtlcore.gtlcore.client.ModClientEventListener;
import org.gtlcore.gtlcore.common.data.GTLItems;
import org.gtlcore.gtlcore.config.ConfigHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.gtladd.gtladditions.common.register.GTLAddMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModClientEventListener.class)
public class ModClientEventListenerMixin {

    @Inject(method = "onSourceTooltipRegistration", at = @At("HEAD"), remap = false)
    private static void onSourceTooltipRegistration(SourceTooltipRegistrationEvent event, CallbackInfo ci) {
        if (!ConfigHolder.INSTANCE.content.enableSkyBlokeMode) return;
        event.register(GTLAddMaterial.MINING_ESSENCE.getFluid()).get_or_create$custom(Component.empty(), () -> ConfigHolder.INSTANCE.content.enableSkyBlokeMode)
                .add(Component.translatable("tooltip.gtladditions.item.mining_essence_bucket.0").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD),
                        Component.translatable("tooltip.gtladditions.item.mining_essence_bucket.1").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD),
                        Component.translatable("tooltip.gtladditions.item.mining_essence_bucket.2").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        event.register(GTLAddMaterial.TREASURES_ESSENCE.getFluid()).get_or_create$custom(Component.empty(), () -> ConfigHolder.INSTANCE.content.enableSkyBlokeMode)
                .add(Component.translatable("tooltip.gtladditions.item.treasures_essence_bucket.0").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                        Component.translatable("tooltip.gtladditions.item.treasures_essence_bucket.1").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                        Component.translatable("tooltip.gtladditions.item.treasures_essence_bucket.2").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        event.register(GTLAddMaterial.CRYSTALLINE_PROTOPLASM.getFluid()).get_or_create$custom(Component.empty(), () -> ConfigHolder.INSTANCE.content.enableSkyBlokeMode)
                .add(Component.translatable("tooltip.gtladditions.item.crystalline_protoplasm_bucket").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        event.register(GTLItems.MINING_CRYSTAL.asItem()).get_or_create$custom(Component.empty(), () -> ConfigHolder.INSTANCE.content.enableSkyBlokeMode)
                .add(Component.translatable("tooltip.gtladditions.add.0").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                        Component.translatable("tooltip.gtladditions.item.mining_crystal.0").withStyle(ChatFormatting.DARK_GRAY),
                        Component.translatable("tooltip.gtladditions.item.mining_crystal.1").withStyle(ChatFormatting.DARK_GRAY));
        event.register(GTLItems.TREASURES_CRYSTAL.asItem()).get_or_create$custom(Component.empty(), () -> ConfigHolder.INSTANCE.content.enableSkyBlokeMode)
                .add(Component.translatable("tooltip.gtladditions.add.0").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                        Component.translatable("tooltip.gtladditions.item.treasures_crystal.0").withStyle(ChatFormatting.DARK_PURPLE),
                        Component.translatable("tooltip.gtladditions.item.treasures_crystal.1").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
