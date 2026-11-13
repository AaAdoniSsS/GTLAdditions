package com.gtladd.gtladditions.client.render.machine

import org.gtlcore.gtlcore.GTLCore
import org.gtlcore.gtlcore.api.machine.IGTLRenderDisabled
import org.gtlcore.gtlcore.client.ClientUtil
import org.gtlcore.gtlcore.common.machine.multiblock.electric.HarmonyMachine
import org.gtlcore.gtlcore.utils.RenderUtil

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart
import com.gregtechceu.gtceu.client.renderer.machine.IControllerRenderer
import com.gregtechceu.gtceu.client.renderer.machine.WorkableCasingMachineRenderer

import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad
import com.lowdragmc.lowdraglib.client.model.ModelFactory

import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.resources.model.ModelState
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.client.model.data.ModelData

import com.mojang.blaze3d.vertex.PoseStack
import org.joml.Quaternionf

import java.util.function.Consumer
import kotlin.math.cos
import kotlin.math.sin

class ArcanicAstrographRender : WorkableCasingMachineRenderer(GTLCore.id("block/create_casing"), GTCEu.id("block/multiblock/cosmos_simulation")), IControllerRenderer {

    companion object {
        private val SPACE_MODEL = GTLCore.id("obj/space")
        val STAR_MODEL: ResourceLocation = GTLCore.id("obj/star")
        private val ORBIT_OBJECTS = listOf(GTLCore.id("obj/the_nether"), GTLCore.id("obj/overworld"), GTLCore.id("obj/the_end"))
    }

    @OnlyIn(Dist.CLIENT)
    override fun render(blockEntity: BlockEntity, partialTicks: Float, poseStack: PoseStack, buffer: MultiBufferSource, combinedLight: Int, combinedOverlay: Int) {
        if (blockEntity is IMachineBlockEntity && blockEntity.metaMachine is HarmonyMachine &&
            (blockEntity.metaMachine as HarmonyMachine).isActive &&
            !IGTLRenderDisabled.isRenderDisabled(blockEntity.metaMachine)
        ) {
            val machine = blockEntity.metaMachine as HarmonyMachine
            val tick = RenderUtil.getSmoothTick(machine, partialTicks)
            var x = 0.5
            val y = 0.5
            var z = 0.5

            when (machine.frontFacing) {
                Direction.NORTH -> z = 16.5
                Direction.SOUTH -> z = -15.5
                Direction.WEST -> x = 16.5
                Direction.EAST -> x = -15.5
                Direction.DOWN -> null
                Direction.UP -> null
            }

            poseStack.pushPose()
            poseStack.translate(x, y, z)
            renderStar(tick, poseStack, buffer)
            renderOrbitObjects(tick, poseStack, buffer, x, y, z)
            renderOuterSpaceShell(poseStack, buffer)
            poseStack.popPose()
        }
    }

    @OnlyIn(Dist.CLIENT)
    private fun renderStar(tick: Float, poseStack: PoseStack, buffer: MultiBufferSource) {
        poseStack.pushPose()
        poseStack.scale(0.02f, 0.02f, 0.02f)
        poseStack.mulPose(Quaternionf().fromAxisAngleDeg(0f, 1f, 1f, (tick / 2) % 360f))
        ClientUtil.modelRenderer().renderModel(
            poseStack.last(), buffer.getBuffer(RenderType.translucent()), null,
            ClientUtil.getBakedModel(STAR_MODEL), 1.0f, 1.0f, 1.0f, LightTexture.FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.translucent()
        )
        poseStack.popPose()
    }

    @OnlyIn(Dist.CLIENT)
    private fun renderOrbitObjects(tick: Float, poseStack: PoseStack, buffer: MultiBufferSource, x: Double, y: Double, z: Double) {
        for (a in 1..3) {
            val scale = 0.007f + 0.003f * a
            poseStack.pushPose()
            poseStack.scale(scale, scale, scale)
            poseStack.mulPose(Quaternionf().fromAxisAngleDeg(1f, 0f, 1f, (tick * 1.5f / a) % 360f))
            val offsetX = (a * 100 + 160) * sin(tick * a / 80 + 0.4)
            val offsetZ = (a * 100 + 160) * cos(tick * a / 80 + 0.4)
            poseStack.translate(x + offsetX, y, z + offsetZ)
            ClientUtil.modelRenderer().renderModel(
                poseStack.last(), buffer.getBuffer(RenderType.solid()), null,
                ClientUtil.getBakedModel(ORBIT_OBJECTS[a - 1]),
                1.0f, 1.0f, 1.0f, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid()
            )
            poseStack.popPose()
        }
    }

    @OnlyIn(Dist.CLIENT)
    private fun renderOuterSpaceShell(poseStack: PoseStack, buffer: MultiBufferSource) {
        val scale = 0.01f * 17.5f
        poseStack.pushPose()
        poseStack.scale(scale, scale, scale)
        ClientUtil.modelRenderer().renderModel(
            poseStack.last(), buffer.getBuffer(RenderType.solid()), null,
            ClientUtil.getBakedModel(SPACE_MODEL),
            1.0f, 1.0f, 1.0f, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid()
        )
        poseStack.popPose()
    }

    override fun onAdditionalModel(registry: Consumer<ResourceLocation>) {
        super.onAdditionalModel(registry)
        registry.accept(SPACE_MODEL)
        registry.accept(STAR_MODEL)
        registry.accept(ORBIT_OBJECTS[0])
        registry.accept(ORBIT_OBJECTS[1])
        registry.accept(ORBIT_OBJECTS[2])
    }

    @OnlyIn(Dist.CLIENT)
    override fun renderPartModel(list: MutableList<BakedQuad>, iMultiController: IMultiController, iMultiPart: IMultiPart, direction: Direction, direction1: Direction?, randomSource: RandomSource, direction2: Direction?, modelState: ModelState) {
        if (direction1 != null && direction2 != null) {
            list.add(FaceQuad.bakeFace(direction2, ModelFactory.getBlockSprite(GTCEu.id("block/casings/hpca/high_power_casing")), modelState))
        }
    }

    @OnlyIn(Dist.CLIENT)
    override fun hasTESR(blockEntity: BlockEntity) = true

    @OnlyIn(Dist.CLIENT)
    override fun isGlobalRenderer(blockEntity: BlockEntity) = true

    @OnlyIn(Dist.CLIENT)
    override fun getViewDistance() = 128
}
