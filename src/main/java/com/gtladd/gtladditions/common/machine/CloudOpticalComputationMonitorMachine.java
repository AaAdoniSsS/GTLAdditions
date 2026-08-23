package com.gtladd.gtladditions.common.machine;

import org.gtlcore.gtlcore.api.gui.ExtendLabelWidget;

import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.gtladd.gtladditions.api.machine.trait.IOpticalComputationProvider;
import com.gtladd.gtladditions.client.renderer.ClientCloudHighlighter;
import com.gtladd.gtladditions.common.machine.hatch.CloudOpticalComputationHatchMachine;
import com.gtladd.gtladditions.utils.MathUtil;
import com.hepdd.gtmthings.utils.TeamUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class CloudOpticalComputationMonitorMachine extends MetaMachine implements IFancyUIMachine, IMachineLife, IDataStickInteractable {

    public static final Set<CloudOpticalComputationHatchMachine> CLOUD_TRANSMITTER_HATCH_SET = new ObjectOpenHashSet<>();
    public static final Set<CloudOpticalComputationHatchMachine> CLOUD_RECEIVER_HATCH_SET = new ObjectOpenHashSet<>();

    private static final class TeamState {

        final List<IOpticalComputationProvider> providers = new ArrayList<>();
        final List<MetaMachine> receiverControllers = new ArrayList<>();

        void clear() {
            providers.clear();
            receiverControllers.clear();
        }
    }

    private static final Map<UUID, TeamState> TEAM_STATES = new Object2ObjectOpenHashMap<>();
    private static boolean cacheDirty = true;

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CloudOpticalComputationMonitorMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);

    @Getter
    @Persisted
    @DescSynced
    private UUID teamId;

    public CloudOpticalComputationMonitorMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private void bindTeam(Player player) {
        this.teamId = TeamUtil.getTeamUUID(player.getUUID());
    }

    @Override
    public InteractionResult onDataStickRightClick(Player player, ItemStack stack) {
        if (isRemote() || player == null) return InteractionResult.PASS;
        bindTeam(player);
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("gui.gtladditions.cloud.bind_success", TeamUtil.GetName(sp)));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onDataStickLeftClick(Player player, ItemStack stack) {
        return false;
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (!isRemote() && player instanceof Player p) {
            bindTeam(p);
        }
    }

    public static void markCacheDirty() {
        cacheDirty = true;
    }

    private static void rebuildProviderCache() {
        TEAM_STATES.values().forEach(TeamState::clear);
        for (var h : CLOUD_TRANSMITTER_HATCH_SET) {
            var team = h.getTeamId();
            var state = TEAM_STATES.computeIfAbsent(team, k -> new TeamState());
            for (var c : h.getControllers()) if (c instanceof IOpticalComputationProvider p) state.providers.add(p);
        }
        for (var h : CLOUD_RECEIVER_HATCH_SET) {
            var team = h.getTeamId();
            var state = TEAM_STATES.computeIfAbsent(team, k -> new TeamState());
            for (var c : h.getControllers()) if (c instanceof MetaMachine m) state.receiverControllers.add(m);
        }
        cacheDirty = false;
    }

    private static TeamState getTeamState(UUID teamId) {
        if (cacheDirty) rebuildProviderCache();
        return TEAM_STATES.computeIfAbsent(teamId, k -> new TeamState());
    }

    public static long getMaxCWU(UUID teamId) {
        var state = getTeamState(teamId);
        return MathUtil.INSTANCE.getSafeToLong(state.providers.stream().mapToDouble(IOpticalComputationProvider::getMaxCWU).sum());
    }

    public static long getRemainingCWU(UUID teamId) {
        var state = getTeamState(teamId);
        return MathUtil.INSTANCE.getSafeToLong(state.providers.stream().mapToDouble(IOpticalComputationProvider::remainCWU).sum());
    }

    public static long requestCWU(UUID teamId, long cwu, boolean simulate) {
        var state = getTeamState(teamId);
        long drawn = 0;
        for (var p : state.providers) {
            long d = p.requestCWU(cwu - drawn, simulate);
            drawn += Math.min(d, cwu - drawn);
            if (drawn >= cwu) break;
        }
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
        textList.add(Component.translatable("gui.gtladditions.cloud_computation_monitor.provider_count", getTeamState(teamId).providers.size()));
        textList.add(Component.translatable("gui.gtladditions.cloud_computation_monitor.max_cwu", FormattingUtil.formatNumbers(getMaxCWU(this.teamId))));
        textList.add(Component.translatable("gui.gtladditions.cloud_computation_monitor.requestable_cwu", FormattingUtil.formatNumbers(getRemainingCWU(this.teamId))));
    }

    @Override
    public void attachSideTabs(TabsWidget tabs) {
        IFancyUIMachine.super.attachSideTabs(tabs);
        tabs.attachSubTab(new CloudOverviewPage());
    }

    private class CloudOverviewPage implements IFancyUIProvider {

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            return new CloudOverviewWidget(CloudOpticalComputationMonitorMachine.this);
        }

        @Override
        public IGuiTexture getTabIcon() {
            return GuiTextures.ICON_CALCULATOR;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("gui.gtladditions.cloud_monitor.overview");
        }

        @Override
        public List<Component> getTabTooltips() {
            return List.of(getTitle());
        }
    }

    private static class CloudOverviewWidget extends WidgetGroup {

        CloudOverviewWidget(CloudOpticalComputationMonitorMachine monitor) {
            super(0, 0, 280, 222);

            addWidget(new ExtendLabelWidget(6, 4, Component.translatable("gui.gtladditions.cloud_monitor.providers")));
            var providerScroll = new DraggableScrollableWidgetGroup(4, 18, 272, 95).setBackground(GuiTextures.DISPLAY);
            providerScroll.setYScrollBarWidth(4).setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1.0F));
            int i = 0;
            var state = getTeamState(monitor.getTeamId());
            int otherProviders = 0;
            for (var h : CLOUD_TRANSMITTER_HATCH_SET) if (!Objects.equals(h.getTeamId(), monitor.getTeamId())) otherProviders++;
            for (var p : state.providers) {
                if (p instanceof MetaMachine m) providerScroll.addWidget(new RowWidgets((i++) * 20, true, RowWidgets.Kind.MACHINE, m, 0));
            }
            if (i == 0) providerScroll.addWidget(new RowWidgets(0, true, RowWidgets.Kind.NO_ENTRIES, null, 0));
            if (otherProviders > 0) providerScroll.addWidget(new RowWidgets(i * 20, true, RowWidgets.Kind.OTHER_TEAM, null, otherProviders));

            addWidget(providerScroll);

            addWidget(new ExtendLabelWidget(6, 117, Component.translatable("gui.gtladditions.cloud_monitor.requesters")));
            var receiverScroll = new DraggableScrollableWidgetGroup(4, 131, 272, 87).setBackground(GuiTextures.DISPLAY);
            receiverScroll.setYScrollBarWidth(4).setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1.0F));
            i = 0;
            int otherReceivers = 0;
            for (var h : CLOUD_RECEIVER_HATCH_SET) if (!Objects.equals(h.getTeamId(), monitor.getTeamId())) otherReceivers++;
            for (var c : state.receiverControllers) {
                receiverScroll.addWidget(new RowWidgets((i++) * 20, false, RowWidgets.Kind.MACHINE, c, 0));
            }
            if (i == 0) receiverScroll.addWidget(new RowWidgets(0, false, RowWidgets.Kind.NO_ENTRIES, null, 0));
            if (otherReceivers > 0) receiverScroll.addWidget(new RowWidgets(i * 20, false, RowWidgets.Kind.OTHER_TEAM, null, otherReceivers));

            addWidget(receiverScroll);
        }
    }

    private static class RowWidgets extends WidgetGroup {

        enum Kind {
            EMPTY,
            MACHINE,
            NO_ENTRIES,
            OTHER_TEAM
        }

        final boolean provider;
        @Nullable
        final MetaMachine machine;

        Kind kind;
        String dim = "";
        BlockPos pos;
        ItemStack item = ItemStack.EMPTY;
        long current;
        long max;
        int cwu;
        int otherCount;

        long lastCurrent;
        long lastMax;
        int lastCwu;

        int tick = 0;

        final ImageWidget icon;
        final ComponentPanelWidget label;
        final ButtonWidget button;

        RowWidgets(int y, boolean provider, Kind kind, @Nullable MetaMachine machine, int otherCount) {
            super(4, y + 4, 260, 18);
            this.provider = provider;
            this.kind = kind;
            this.machine = machine;
            this.otherCount = otherCount;
            if (kind == Kind.MACHINE && machine != null) {
                this.dim = machine.getLevel() != null ? machine.getLevel().dimension().location().toString() : "";
                this.pos = machine.getPos();
                this.item = machine.getDefinition().asStack();
                refreshValues();
            }
            this.icon = new ImageWidget(0, 0, 18, 18, () -> new ItemStackTexture(item));
            this.icon.setHoverTooltips(item.getHoverName());
            this.label = new ComponentPanelWidget(24, 4, this::buildText).setMaxWidthLimit(172);
            this.label.setClientSideWidget();
            this.button = new ButtonWidget(200, 2, 56, 14,
                    new TextTexture(Component.translatable("gui.gtladditions.cloud_monitor.highlight").getString(), 16777045),
                    cd -> {
                        if (pos != null && !dim.isEmpty()) ClientCloudHighlighter.highlight(pos, dim);
                    });
            this.addWidget(icon);
            this.addWidget(label);
            this.addWidget(button);
        }

        private void buildText(List<Component> list) {
            switch (kind) {
                case MACHINE -> {
                    if (provider) {
                        list.add(Component.translatable("gui.gtladditions.cloud_monitor.provider_info",
                                FormattingUtil.formatNumbers(current), FormattingUtil.formatNumbers(max)));
                    } else {
                        list.add(Component.translatable("gui.gtladditions.cloud_monitor.requester_info",
                                FormattingUtil.formatNumbers(cwu)));
                    }
                }
                case NO_ENTRIES -> list.add(Component.translatable(provider ? "gui.gtladditions.cloud_monitor.no_providers" : "gui.gtladditions.cloud_monitor.no_requesters").withStyle(ChatFormatting.GRAY));
                case OTHER_TEAM -> list.add(Component.translatable(provider ? "gui.gtladditions.cloud_monitor.other_team_providers" : "gui.gtladditions.cloud_monitor.other_team_receivers", otherCount).withStyle(ChatFormatting.YELLOW));
                default -> {}
            }
        }

        private void refreshValues() {
            if (provider && machine instanceof IOpticalComputationProvider p) {
                try {
                    current = p.remainCWU();
                    max = p.getMaxCWU();
                } catch (Exception e) {
                    current = 0;
                    max = 0;
                }
            } else if (machine instanceof IRecipeLogicMachine rm) {
                try {
                    var recipe = rm.getRecipeLogic().getLastRecipe();
                    cwu = 0;
                    if (recipe != null && rm.getRecipeLogic().isWorking()) {
                        var cwuInputs = recipe.tickInputs.get(CWURecipeCapability.CAP);
                        if (cwuInputs != null) {
                            cwu = cwuInputs.stream().map(Content::getContent).mapToInt(CWURecipeCapability.CAP::of).sum();
                        }
                    }
                } catch (Exception e) {
                    cwu = 0;
                }
            }
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buffer) {
            super.writeInitialData(buffer);
            buffer.writeByte(kind.ordinal());
            buffer.writeUtf(dim);
            buffer.writeBlockPos(pos == null ? BlockPos.ZERO : pos);
            buffer.writeItem(item);
            buffer.writeLong(current);
            buffer.writeLong(max);
            buffer.writeInt(cwu);
            buffer.writeInt(otherCount);
            lastCurrent = current;
            lastMax = max;
            lastCwu = cwu;
        }

        @Override
        public void readInitialData(FriendlyByteBuf buffer) {
            super.readInitialData(buffer);
            kind = Kind.values()[buffer.readByte()];
            dim = buffer.readUtf();
            pos = buffer.readBlockPos();
            item = buffer.readItem();
            current = buffer.readLong();
            max = buffer.readLong();
            cwu = buffer.readInt();
            otherCount = buffer.readInt();
        }

        @Override
        public void detectAndSendChanges() {
            super.detectAndSendChanges();
            if (tick++ % 20 == 0) refreshValues();
            if (current != lastCurrent || max != lastMax || cwu != lastCwu) {
                lastCurrent = current;
                lastMax = max;
                lastCwu = cwu;
                writeUpdateInfo(0, buffer -> {
                    buffer.writeLong(current);
                    buffer.writeLong(max);
                    buffer.writeInt(cwu);
                });
            }
        }

        @Override
        public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
            if (id == 0) {
                current = buffer.readLong();
                max = buffer.readLong();
                cwu = buffer.readInt();
            } else {
                super.readUpdateInfo(id, buffer);
            }
        }
    }
}
