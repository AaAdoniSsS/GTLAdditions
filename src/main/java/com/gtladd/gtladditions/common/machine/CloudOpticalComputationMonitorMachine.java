package com.gtladd.gtladditions.common.machine;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.network.chat.Component;
import net.minecraftforge.server.ServerLifecycleHooks;

import com.gtladd.gtladditions.api.machine.trait.IOpticalComputationProvider;
import com.gtladd.gtladditions.common.machine.hatch.CloudOpticalComputationHatchMachine;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public class CloudOpticalComputationMonitorMachine extends MetaMachine implements IFancyUIMachine {

    public static final Set<CloudOpticalComputationHatchMachine> CLOUD_HATCH_SET = new ObjectOpenHashSet<>();

    private static final List<IOpticalComputationProvider> PROVIDER_CACHE = new ArrayList<>();
    private static boolean cacheDirty = true;

    private static long lastProbeTick = -1;
    private static long tickRemaining;

    public CloudOpticalComputationMonitorMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public static void markCacheDirty() {
        cacheDirty = true;
        lastProbeTick = -1;
    }

    private static void rebuildProviderCache() {
        PROVIDER_CACHE.clear();
        var seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (var h : CLOUD_HATCH_SET) {
            for (var c : h.getControllers()) {
                if (c instanceof IOpticalComputationProvider p && seen.add(c.self())) {
                    PROVIDER_CACHE.add(p);
                }
            }
        }
        cacheDirty = false;
    }

    private static long getServerTick() {
        var server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? -1 : server.getTickCount();
    }

    public static long getMaxCWU() {
        if (cacheDirty) rebuildProviderCache();
        long sum = 0;
        for (var p : PROVIDER_CACHE) sum += p.getMaxCWU();
        return sum;
    }

    public static long getRemainingCWU() {
        if (cacheDirty) rebuildProviderCache();
        return lastProbeTick == -1 ? getMaxCWU() : tickRemaining;
    }

    public static long requestCWU(long cwu, boolean simulate) {
        if (cacheDirty) rebuildProviderCache();
        long tick = getServerTick();
        if (tick != lastProbeTick) {
            long sum = 0;
            for (var p : PROVIDER_CACHE) sum += p.requestCWU(Integer.MAX_VALUE, true);
            tickRemaining = sum;
            lastProbeTick = tick;
        }
        if (simulate) return Math.min(cwu, tickRemaining);
        long drawn = 0;
        for (var p : PROVIDER_CACHE) {
            drawn += p.requestCWU(cwu - drawn, false);
            if (drawn >= cwu) break;
        }
        tickRemaining -= drawn;
        return drawn;
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 182 + 8, 117 + 8);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 182, 117).setBackground(GuiTextures.DISPLAY)
                .addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()))
                .addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText).setMaxWidthLimit(150)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    private void addDisplayText(List<Component> textList) {
        if (isRemote()) return;
        textList.add(Component.translatable("gui.gtladditions.cloud_computation_monitor.provider_count", CLOUD_HATCH_SET.size()));
        textList.add(Component.translatable("gui.gtladditions.cloud_computation_monitor.max_cwu", FormattingUtil.formatNumbers(getMaxCWU())));
        textList.add(Component.translatable("gui.gtladditions.cloud_computation_monitor.requestable_cwu", FormattingUtil.formatNumbers(getRemainingCWU())));
    }
}
