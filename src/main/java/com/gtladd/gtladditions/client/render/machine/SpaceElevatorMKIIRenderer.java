package com.gtladd.gtladditions.client.render.machine;

import org.gtlcore.gtlcore.GTLCore;
import org.gtlcore.gtlcore.client.ClientUtil;
import org.gtlcore.gtlcore.client.renderer.RenderBufferHelper;
import org.gtlcore.gtlcore.common.machine.multiblock.electric.SpaceElevatorMachine;
import org.gtlcore.gtlcore.utils.RenderUtil;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.WorkableCasingMachineRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.function.Consumer;

public class SpaceElevatorMKIIRenderer extends WorkableCasingMachineRenderer {

    private static final ResourceLocation CLIMBER_MODEL = GTLCore.id("obj/climber");

    public SpaceElevatorMKIIRenderer() {
        super(GTLCore.id("block/space_elevator_mechanical_casing"), GTCEu.id("block/multiblock/data_bank"));
    }

    @OnlyIn(Dist.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity) {
            MetaMachine var9 = machineBlockEntity.getMetaMachine();
            if (var9 instanceof SpaceElevatorMachine machine) {
                if (machine.isFormed()) {
                    float tick = RenderUtil.getSmoothTick(machine, partialTicks);
                    double x = 0.5F;
                    double y = 1.0F;
                    double z = 0.5F;
                    switch (machine.getFrontFacing()) {
                        case NORTH -> z = 3.5F;
                        case SOUTH -> z = -2.5F;
                        case WEST -> x = 3.5F;
                        case EAST -> x = -2.5F;
                    }

                    poseStack.pushPose();
                    RenderBufferHelper.renderCylinder(poseStack, buffer.getBuffer(GTRenderTypes.getLightRing()), (float) x, (float) (y - (double) 2.0F), (float) z, 0.3F, 360.0F, 10, 0.0F, 0.0F, 0.0F, 255.0F);
                    poseStack.translate(x, y + (double) 230.0F + (double) 140.0F * Math.sin(tick / 160.0F), z);
                    this.renderClimber(poseStack, buffer);
                    poseStack.popPose();
                }
            }
        }
    }

    private void renderClimber(PoseStack poseStack, MultiBufferSource buffer) {
        float scale = 4.0F;
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        ClientUtil.modelRenderer().renderModel(poseStack.last(), buffer.getBuffer(RenderType.solid()), null, ClientUtil.getBakedModel(CLIMBER_MODEL), 1.0F, 1.0F, 1.0F, 15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
        poseStack.popPose();
    }

    public void onAdditionalModel(Consumer<ResourceLocation> registry) {
        super.onAdditionalModel(registry);
        registry.accept(CLIMBER_MODEL);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isGlobalRenderer(BlockEntity blockEntity) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public int getViewDistance() {
        return 512;
    }
}
