package com.gtladd.gtladditions.mixin.gtceu.integration.jade;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.integration.jade.provider.MachineModeProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import com.gtladd.gtladditions.api.machine.IGTLAddMachine;
import com.gtladd.gtladditions.api.machine.IMultipleRecipeTypeMachine;
import com.gtladd.gtladditions.api.recipe.MultiGTRecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Arrays;

@Mixin(MachineModeProvider.class)
public class MachineModeProviderMixin {

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    @SuppressWarnings("all")
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        CompoundTag serverData = blockAccessor.getServerData();
        if (serverData.contains("RecipeTypes") && serverData.contains("CurrentRecipeType")) {
            int currentRecipeTypeIndex = serverData.getInt("CurrentRecipeType");
            var recipeTypesTagList = serverData.getList("RecipeTypes", StringTag.TAG_STRING);
            if (blockAccessor.showDetails()) {
                iTooltip.add(Component.translatable("gtceu.top.machine_mode"));
                for (int i = 0; i < recipeTypesTagList.size(); i++) {
                    MutableComponent text;
                    if (currentRecipeTypeIndex == i) {
                        text = Component.literal(" > ").withStyle(ChatFormatting.BLUE);
                    } else text = Component.literal("   ");
                    var tag = recipeTypesTagList.getString(i);
                    if (!tag.contains("&")) {
                        var recipeType = ResourceLocation.tryParse(tag);
                        text.append(Component.translatable("%s.%s".formatted(recipeType.getNamespace(), recipeType.getPath())));
                    } else {
                        var split = tag.split("&");
                        for (int index = 0; index < split.length; index++) {
                            var recipeType = ResourceLocation.tryParse(split[index]);
                            text.append(
                                    Component.translatable("%s.%s".formatted(recipeType.getNamespace(), recipeType.getPath())));
                            if (index + 1 < split.length) text.append(", ");
                        }
                    }
                    iTooltip.add(text);
                }
            } else {
                var tag = recipeTypesTagList.getString(currentRecipeTypeIndex);
                var component = Component.translatable("gtceu.top.machine_mode");
                if (!tag.contains("&")) {
                    var recipeType = ResourceLocation.tryParse(tag);
                    iTooltip.add(component.append(
                            Component.translatable("%s.%s".formatted(recipeType.getNamespace(), recipeType.getPath()))));
                } else {
                    var split = tag.split("&");
                    for (int i = 0; i < split.length; i++) {
                        var recipeType = ResourceLocation.tryParse(split[i]);
                        component.append(
                                Component.translatable("%s.%s".formatted(recipeType.getNamespace(), recipeType.getPath())));
                        if (i + 1 < split.length) component.append(", ");
                    }
                    iTooltip.add(component);
                }
            }
        }
        if (serverData.contains("multiMode")) {
            boolean isMode = serverData.getBoolean("multiMode");
            iTooltip.add(Component.translatable("gtceu.machine.multiple_recipe.gui.2").append(": ")
                    .append(Component.translatable("gtceu.machine.multiple_recipe.gui." + (isMode ? "0" : "1"))));
        }
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof MetaMachineBlockEntity blockEntity) {
            if (blockEntity.getMetaMachine() instanceof IMultipleRecipeTypeMachine machine) {
                var recipeTypes = machine.getMultiRecipeTypes();
                if (recipeTypes.length > 1) {
                    var recipeTypesTagList = new ListTag();
                    for (var type : recipeTypes) {
                        if (type instanceof MultiGTRecipeType mul) {
                            recipeTypesTagList.add(StringTag.valueOf(String.join("&",
                                    Arrays.stream(mul.getTypeList()).map(t -> t.registryName.toString()).toList())));
                        } else recipeTypesTagList.add(StringTag.valueOf(type.registryName.toString()));
                    }
                    compoundTag.put("RecipeTypes", recipeTypesTagList);
                    compoundTag.putInt("CurrentRecipeType", machine.getActiveRecipeType());
                }
            } else {
                var recipeTypes = blockEntity.getMetaMachine().getDefinition().getRecipeTypes();
                if (recipeTypes != null && recipeTypes.length > 1) {
                    if (blockEntity.getMetaMachine() instanceof IRecipeLogicMachine recipeLogicMachine) {
                        var recipeTypesTagList = new ListTag();
                        for (var recipeType : recipeTypes)
                            recipeTypesTagList.add(StringTag.valueOf(recipeType.registryName.toString()));
                        compoundTag.put("RecipeTypes", recipeTypesTagList);
                        compoundTag.putInt("CurrentRecipeType", recipeLogicMachine.getActiveRecipeType());
                    }
                }
            }
            if (blockEntity.getMetaMachine() instanceof IGTLAddMachine machine) {
                if (machine.useModes()) compoundTag.putBoolean("multiMode", machine.isMultipleMode());
            }
        }
    }
}
