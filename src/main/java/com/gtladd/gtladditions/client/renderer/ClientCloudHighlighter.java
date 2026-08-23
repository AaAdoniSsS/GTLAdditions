package com.gtladd.gtladditions.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.gtladd.gtladditions.GTLAdditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = GTLAdditions.MOD_ID, value = Dist.CLIENT)
public class ClientCloudHighlighter {

    private record Entry(BlockPos pos, String dim, long expireTick) {}

    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final long HIGHLIGHT_TICKS = 300;
    private static final long COLOR_CYCLE_MS = 4000;
    private static final float LINE_WIDTH = 5.0f;

    private static final RenderType HIGHLIGHT_LINES = RenderType.create(
            "gtladditions_highlight_lines",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            65536,
            false, false,
            RenderType.CompositeState.builder()
                    .setTransparencyState(new RenderStateShard.TransparencyStateShard("gtl_glint_transparency",
                            () -> {
                                RenderSystem.enableBlend();
                                RenderSystem.defaultBlendFunc();
                                RenderSystem.lineWidth(LINE_WIDTH);
                            },
                            () -> {
                                RenderSystem.disableBlend();
                                RenderSystem.lineWidth(1.0f);
                            }))
                    .setTextureState(new RenderStateShard.EmptyTextureStateShard(() -> {}, () -> {}))
                    .setDepthTestState(new RenderStateShard.DepthTestStateShard("gtl_no_depth_test", 519))
                    .setCullState(new RenderStateShard.CullStateShard(false))
                    .setLightmapState(new RenderStateShard.LightmapStateShard(false))
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, true))
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLinesShader))
                    .createCompositeState(false));

    public static void highlight(BlockPos pos, String dim) {
        var mc = Minecraft.getInstance();
        var level = mc.level;
        if (level == null) return;
        long expire = level.getGameTime() + HIGHLIGHT_TICKS;
        ENTRIES.removeIf(e -> e.pos.equals(pos) && e.dim.equals(dim));
        ENTRIES.add(new Entry(pos, dim, expire));
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        var mc = Minecraft.getInstance();
        var level = mc.level;
        if (level == null) return;

        ENTRIES.removeIf(e -> e.expireTick < level.getGameTime());
        if (ENTRIES.isEmpty()) return;

        float hue = (System.currentTimeMillis() % COLOR_CYCLE_MS) / (float) COLOR_CYCLE_MS;
        Color color = Color.getHSBColor(hue, 1.0f, 1.0f);

        var bufferSource = mc.renderBuffers().bufferSource();
        var consumer = bufferSource.getBuffer(HIGHLIGHT_LINES);
        var cam = event.getCamera().getPosition();
        var poseStack = event.getPoseStack();
        poseStack.pushPose();
        for (var e : ENTRIES) {
            if (!e.dim.equals(level.dimension().location().toString())) continue;
            var box = new AABB(e.pos).move(-cam.x, -cam.y, -cam.z);
            LevelRenderer.renderLineBox(poseStack, consumer, box,
                    color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 0.95f);
        }
        poseStack.popPose();
        RenderSystem.lineWidth(LINE_WIDTH);
        bufferSource.endBatch(HIGHLIGHT_LINES);
        RenderSystem.lineWidth(1);
    }
}
