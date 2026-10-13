package com.gtladd.gtladditions.mixin.ae.recipe;

import org.gtlcore.gtlcore.common.data.GTLItems;
import org.gtlcore.gtlcore.config.ConfigHolder;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

import appeng.recipes.transform.TransformLogic;
import com.gtladd.gtladditions.common.register.GTLAddMaterial;
import com.gtladd.gtladditions.utils.LevelUtil;
import com.gtladd.gtladditions.utils.MathUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(TransformLogic.class)
public class TransformLogicMixin {

    @Unique
    private static final Set<Item> gTLAdditions$transformItem = Set.of(Items.ENDER_PEARL, Items.SPIDER_EYE, Items.GUNPOWDER,
            Items.TOTEM_OF_UNDYING, ChemicalHelper.get(TagPrefix.block, GTMaterials.Steel).getItem(),
            GTLItems.WORLD_FRAGMENTS_OVERWORLD.asItem(), GTLItems.MINING_CRYSTAL.asItem(), GTLItems.TREASURES_CRYSTAL.asItem());
    @Unique
    private static final Set<Item> gTLAdditions$recipe1 = Set.of(Items.ENDER_PEARL, Items.SPIDER_EYE, Items.GUNPOWDER);
    @Unique
    private static final Set<Item> gTLAdditions$recipe2 = Set.of(GTLItems.WORLD_FRAGMENTS_OVERWORLD.asItem(), Items.TOTEM_OF_UNDYING);
    @Unique
    private static final Set<Item> gTLAdditions$recipe3 = Set.of(GTLItems.WORLD_FRAGMENTS_OVERWORLD.asItem(), ChemicalHelper.get(TagPrefix.block, GTMaterials.Steel).getItem());

    @Inject(method = "canTransformInAnyFluid", at = @At("HEAD"), remap = false)
    private static void canTransformInAnyFluid(ItemEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!ConfigHolder.INSTANCE.content.enableSkyBlokeMode) return;
        var level = entity.level();
        var item = entity.getItem().getItem();
        if (!gTLAdditions$transformItem.contains(item)) return;
        var j = Mth.floor(entity.getX());
        var i = Mth.floor((entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0D);
        var k = Mth.floor(entity.getZ());
        var pos = new BlockPos(j, i, k);

        var fluid = entity.level().getFluidState(pos).getType();
        if (item == GTLItems.MINING_CRYSTAL.asItem() && fluid == GTLAddMaterial.MINING_ESSENCE.getFluid()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            entity.getItem().shrink(1);
            var random = MathUtil.INSTANCE.random(100);
            LevelUtil.INSTANCE.throwItemEntity(entity, new ItemStack(item, random >= 50 ? 2 : 1));
        } else if (item == GTLItems.TREASURES_CRYSTAL.asItem() && fluid == GTLAddMaterial.TREASURES_ESSENCE.getFluid()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            entity.getItem().shrink(1);
            var random = MathUtil.INSTANCE.random(100);
            LevelUtil.INSTANCE.throwItemEntity(entity, new ItemStack(item, random >= 50 ? 2 : 1));
        } else gTLAdditions$transformFluid(entity, pos, fluid);
    }

    @Unique
    private static void gTLAdditions$transformFluid(ItemEntity entity, BlockPos pos, Fluid fluid) {
        var level = entity.level();
        var region = new AABB(entity.getX() - 1, entity.getY() - 1, entity.getZ() - 1, entity.getX() + 1,
                entity.getY() + 1, entity.getZ() + 1);
        var itemEntities = level.getEntities(null, region).stream()
                .filter(e -> e instanceof ItemEntity && !e.isRemoved()).map(e -> (ItemEntity) e).toList();
        if (fluid == Fluids.WATER) {
            var find = gTLAdditions$matchItemList(itemEntities, gTLAdditions$recipe1);
            if (!find.isEmpty()) {
                find.forEach(e -> {
                    e.getItem().shrink(1);
                    if (e.getItem().getCount() <= 0) e.discard();
                });
                level.setBlock(pos, GTLAddMaterial.CRYSTALLINE_PROTOPLASM.getFluid().defaultFluidState().createLegacyBlock(), 3);
            }
        } else if (fluid == GTLAddMaterial.CRYSTALLINE_PROTOPLASM.getFluid()) {
            var find = gTLAdditions$matchItemList(itemEntities, gTLAdditions$recipe2);
            if (!find.isEmpty()) {
                find.forEach(e -> {
                    e.getItem().shrink(1);
                    if (e.getItem().getCount() <= 0) e.discard();
                });
                level.setBlock(pos, GTLAddMaterial.TREASURES_ESSENCE.getFluid().defaultFluidState().createLegacyBlock(), 3);
            } else {
                find = gTLAdditions$matchItemList(itemEntities, gTLAdditions$recipe3);
                if (!find.isEmpty()) {
                    find.forEach(e -> {
                        e.getItem().shrink(1);
                        if (e.getItem().getCount() <= 0) e.discard();
                    });
                    level.setBlock(pos, GTLAddMaterial.MINING_ESSENCE.getFluid().defaultFluidState().createLegacyBlock(), 3);
                }
            }
        }
    }

    @Unique
    private static List<ItemEntity> gTLAdditions$matchItemList(List<ItemEntity> entity, Set<Item> itemSet) {
        List<ItemEntity> findItemStack = new ObjectArrayList<>(itemSet.size());
        for (var e : entity) {
            var item = e.getItem();
            if (itemSet.contains(item.getItem())) findItemStack.add(e);
        }
        return findItemStack.size() == itemSet.size() ? findItemStack : List.of();
    }
}
