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

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gtladd.gtladditions.api.machine.trait.IOpticalComputationProvider;
import com.gtladd.gtladditions.client.renderer.ClientCloudHighlighter;
import com.gtladd.gtladditions.common.machine.hatch.CloudOpticalComputationHatchMachine;
import com.gtladd.gtladditions.utils.MathUtil;
import com.hepdd.gtmthings.utils.TeamUtil;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.hepdd.gtmthings.utils.TeamUtil.getTeamUUID;

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
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private void bindTeam(Player player) {
        this.teamId = player.getUUID();
    }

    @Override
    public InteractionResult onDataStickRightClick(Player player, ItemStack stack) {
        bindTeam(player);
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("gui.gtladditions.cloud.bind_success", TeamUtil.GetName(sp)));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onDataStickLeftClick(Player player, ItemStack stack) {
        this.teamId = null;
        markCacheDirty();
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("gui.gtladditions.cloud.unbind_success"));
        }
        return true;
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (player instanceof Player p) {
            bindTeam(p);
        }
    }

    public static void markCacheDirty() {
        cacheDirty = true;
    }

    private static void rebuildProviderCache() {
        TEAM_STATES.values().forEach(TeamState::clear);
        for (var h : CLOUD_TRANSMITTER_HATCH_SET) {
            var team = h.getPlayer();
            var state = TEAM_STATES.computeIfAbsent(getTeamUUID(team), k -> new TeamState());
            for (var c : h.getControllers()) if (c instanceof IOpticalComputationProvider p) state.providers.add(p);
        }
        for (var h : CLOUD_RECEIVER_HATCH_SET) {
            var team = h.getPlayer();
            var state = TEAM_STATES.computeIfAbsent(getTeamUUID(team), k -> new TeamState());
            for (var c : h.getControllers()) if (c instanceof MetaMachine m) state.receiverControllers.add(m);
        }
        cacheDirty = false;
    }

    private static TeamState getTeamState(UUID teamId) {
        if (cacheDirty) rebuildProviderCache();
        return TEAM_STATES.computeIfAbsent(getTeamUUID(teamId), k -> new TeamState());
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
                .addWidget(new ComponentPanelWidget(4, 5, this::addDisplayText).setMaxWidthLimit(150)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    private void addDisplayText(List<Component> textList) {
        if (isRemote()) return;
        if (teamId == null) {
            textList.add(Component.translatable("gui.gtladditions.cloud.not_bound")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        textList.add(self().getBlockState().getBlock().getName());
        if (TeamUtil.hasOwner(getLevel(), teamId)) textList.add(Component.translatable("gui.gtladditions.cloud.bind_success", TeamUtil.GetName(getLevel(), teamId)));
        textList.add(Component.translatable("gui.gtladditions.cloud_computation_monitor.max_cwu", FormattingUtil.formatNumbers(getMaxCWU(this.teamId))));
        textList.add(Component.translatable("gui.gtladditions.cloud_computation_monitor.requestable_cwu", FormattingUtil.formatNumbers(getRemainingCWU(this.teamId))));
    }

    @Override
    public void attachSideTabs(TabsWidget tabs) {
        IFancyUIMachine.super.attachSideTabs(tabs);
        if (teamId != null) tabs.attachSubTab(new CloudOverviewPage(this));
    }

    private record CloudOverviewPage(CloudOpticalComputationMonitorMachine machine) implements IFancyUIProvider {

        public static final IGuiTexture ICON = new ResourceTexture("gtceu:textures/item/computer_monitor_cover.png");

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            return new CloudOverviewWidget(this.machine.teamId);
        }

        @Override
        public IGuiTexture getTabIcon() {
            return ICON;
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

        CloudOverviewWidget(UUID uuid) {
            super(0, 0, 280, calcFittedHeight());

            int scrollHeight = (getSize().height - 36) / 2;

            addWidget(new ExtendLabelWidget(6, 4, Component.translatable("gui.gtladditions.cloud_monitor.providers")));
            var providerScroll = new DraggableScrollableWidgetGroup(4, 18, 272, scrollHeight).setBackground(GuiTextures.DISPLAY);
            providerScroll.setYScrollBarWidth(4).setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1.0F));
            var providerRows = new ArrayList<RowWidgets>();
            int i = 0;
            var state = getTeamState(uuid);
            int otherProviders = 0;
            for (var h : CLOUD_TRANSMITTER_HATCH_SET) if (!FTBTeamsAPI.api().getManager().arePlayersInSameTeam(h.getPlayer(), uuid)) otherProviders++;
            for (var p : state.providers) {
                if (p instanceof MetaMachine m) providerRows.add(new RowWidgets((i++) * 20, true, RowWidgets.Kind.MACHINE, m, 0));
            }
            if (i == 0) providerRows.add(new RowWidgets(i++ * 20, true, RowWidgets.Kind.NO_ENTRIES, null, 0));
            if (otherProviders > 0) providerRows.add(new RowWidgets(i * 20, true, RowWidgets.Kind.OTHER_TEAM, null, otherProviders));
            providerRows.forEach(providerScroll::addWidget);

            addWidget(providerScroll);
            addWidget(createSortButton(3, providerScroll, providerRows, true));

            addWidget(new ExtendLabelWidget(6, 22 + scrollHeight, Component.translatable("gui.gtladditions.cloud_monitor.requesters")));
            var receiverScroll = new DraggableScrollableWidgetGroup(4, 36 + scrollHeight, 272, getSize().height - 36 - scrollHeight).setBackground(GuiTextures.DISPLAY);
            receiverScroll.setYScrollBarWidth(4).setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1.0F));
            var receiverRows = new ArrayList<RowWidgets>();
            i = 0;
            int otherReceivers = 0;
            for (var h : CLOUD_RECEIVER_HATCH_SET) if (!FTBTeamsAPI.api().getManager().arePlayersInSameTeam(h.getPlayer(), uuid)) otherReceivers++;
            for (var c : state.receiverControllers) {
                receiverRows.add(new RowWidgets((i++) * 20, false, RowWidgets.Kind.MACHINE, c, 0));
            }
            if (i == 0) receiverRows.add(new RowWidgets(i++ * 20, false, RowWidgets.Kind.NO_ENTRIES, null, 0));
            if (otherReceivers > 0) receiverRows.add(new RowWidgets(i * 20, false, RowWidgets.Kind.OTHER_TEAM, null, otherReceivers));
            receiverRows.forEach(receiverScroll::addWidget);

            addWidget(receiverScroll);
            addWidget(createSortButton(21 + scrollHeight, receiverScroll, receiverRows, false));
        }

        private ButtonWidget createSortButton(int y, DraggableScrollableWidgetGroup scroll, List<RowWidgets> rows, boolean byMax) {
            boolean[] descending = { true };
            var button = new ButtonWidget(258, y, 18, 13,
                    new TextTexture(() -> descending[0] ? "▼" : "▲").setColor(16777045),
                    cd -> {
                        applySort(scroll, rows, byMax, descending[0]);
                        descending[0] = !descending[0];
                    });
            button.setClientSideWidget();
            button.setHoverTooltips(Component.translatable("gui.gtladditions.cloud_monitor.sort"));
            return button;
        }

        private void applySort(DraggableScrollableWidgetGroup scroll, List<RowWidgets> rows, boolean byMax, boolean descending) {
            scroll.setScrollYOffset(0);
            rows.sort((a, b) -> {
                if (a.machine == null || b.machine == null) return Boolean.compare(a.machine == null, b.machine == null);
                int c = byMax ? Long.compare(a.max, b.max) : Integer.compare(a.cwu, b.cwu);
                return descending ? -c : c;
            });
            for (int j = 0; j < rows.size(); j++) rows.get(j).setSelfPositionY(j * 20 + 4);
        }

        private static int calcFittedHeight() {
            if (!LDLib.isRemote()) return 150;
            return Math.max(150, Minecraft.getInstance().getWindow().getGuiScaledHeight() - 126);
        }
    }

    private static class RowWidgets extends WidgetGroup {

        enum Kind {
            MACHINE,
            NO_ENTRIES,
            OTHER_TEAM
        }

        final boolean provider;
        @Nullable
        final MetaMachine machine;

        String dim = "";
        BlockPos pos;
        BlockPos frontPos;
        long current;
        long max;
        int cwu;

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
            this.machine = machine;
            ItemStack item;
            if (kind == Kind.MACHINE && machine != null) {
                this.dim = machine.getLevel() != null ? machine.getLevel().dimension().location().toString() : "";
                this.pos = machine.getPos();
                this.frontPos = machine.getLevel() != null ? machine.getPos().relative(machine.getFrontFacing()) : null;
                item = machine.getDefinition().asStack();
                refreshValues();
            } else {
                item = ItemStack.EMPTY;
            }
            this.icon = new ImageWidget(0, 0, 18, 18, () -> new ItemStackTexture(item));
            this.icon.setHoverTooltips(item.getHoverName());
            this.label = new ComponentPanelWidget(24, 4, list -> {
                switch (kind) {
                    case MACHINE -> {
                        if (provider) list.add(Component.translatable("gui.gtladditions.cloud_monitor.provider_info", FormattingUtil.formatNumbers(current), FormattingUtil.formatNumbers(max)));
                        else list.add(Component.translatable("gui.gtladditions.cloud_monitor.requester_info", FormattingUtil.formatNumbers(cwu)));
                    }
                    case NO_ENTRIES -> list.add(Component.translatable(provider ? "gui.gtladditions.cloud_monitor.no_providers" : "gui.gtladditions.cloud_monitor.no_requesters").withStyle(ChatFormatting.GRAY));
                    case OTHER_TEAM -> list.add(Component.translatable(provider ? "gui.gtladditions.cloud_monitor.other_team_providers" : "gui.gtladditions.cloud_monitor.other_team_receivers", otherCount).withStyle(ChatFormatting.YELLOW));
                    default -> {}
                }
            }).setMaxWidthLimit(172);
            this.label.setClientSideWidget();
            this.button = new ButtonWidget(200, 2, 56, 14,
                    new TextTexture(Component.translatable("gui.gtladditions.cloud_monitor.highlight").getString(), 16777045),
                    cd -> {
                        if (pos != null && !dim.isEmpty()) ClientCloudHighlighter.highlight(pos, dim);
                    }) {

                @Override
                @OnlyIn(Dist.CLIENT)
                public boolean mouseClicked(double mouseX, double mouseY, int button) {
                    if (isMouseOverElement(mouseX, mouseY)) {
                        ClickData clickData = new ClickData();
                        writeClientAction(1, clickData::writeToBuf);
                        if (onPressCallback != null) {
                            onPressCallback.accept(clickData);
                            var mc = Minecraft.getInstance();
                            var level = mc.level;
                            if (level != null) {
                                if (mc.screen != null) mc.setScreen(null);
                                var player = gui.entityPlayer;
                                if (player != null) {
                                    if (dim.equals(level.dimension().location().toString())) {
                                        player.lookAt(EntityAnchorArgument.Anchor.EYES,
                                                new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                                    } else if (pos != null && !dim.isEmpty()) {
                                        sendCrossDimensionMessage(player);
                                    }
                                }
                            }
                        }
                        playButtonClickSound();
                        return true;
                    }
                    return false;
                }
            };
            if (kind != Kind.MACHINE) {
                button.setActive(false);
                button.setVisible(false);
                this.setSizeHeight(26);
                label.setSelfPosition(0, 4);
                label.setMaxWidthLimit(252);
            } else {
                button.setHoverTooltips(
                        Component.translatable("gui.gtladditions.cloud_monitor.tooltip_dim", dim),
                        Component.translatable("gui.gtladditions.cloud_monitor.tooltip_pos",
                                pos.getX(), pos.getY(), pos.getZ()));
            }
            this.addWidget(icon);
            this.addWidget(label);
            this.addWidget(button);
        }

        @OnlyIn(Dist.CLIENT)
        private void sendCrossDimensionMessage(Player player) {
            BlockPos tpPos = frontPos != null ? frontPos : pos;
            String command = "/execute in " + dim + " run tp @s " + (tpPos.getX() + 0.5) + " " + tpPos.getY() + " " + (tpPos.getZ() + 0.5);
            Component coords = Component.literal("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]")
                    .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                            .withUnderlined(true).withColor(ChatFormatting.GREEN));
            player.displayClientMessage(Component.translatable("gui.gtladditions.cloud_monitor.cross_dim",
                    Component.literal("[" + dim + "]")
                            .withStyle(style -> style.withColor(ChatFormatting.GREEN)),
                    coords), false);
        }

        private void refreshValues() {
            if (provider && machine instanceof IOpticalComputationProvider p) {
                current = p.remainCWU();
                max = p.getMaxCWU();
            } else if (machine instanceof IRecipeLogicMachine rm) {
                var recipe = rm.getRecipeLogic().getLastRecipe();
                cwu = 0;
                if (recipe != null && rm.getRecipeLogic().isWorking()) {
                    var cwuInputs = recipe.tickInputs.get(CWURecipeCapability.CAP);
                    if (cwuInputs != null) {
                        cwu = cwuInputs.stream().map(Content::getContent).mapToInt(CWURecipeCapability.CAP::of).sum();
                    }
                }
            }
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buffer) {
            super.writeInitialData(buffer);
            buffer.writeUtf(dim);
            buffer.writeBlockPos(pos == null ? BlockPos.ZERO : pos);
            buffer.writeBlockPos(frontPos == null ? BlockPos.ZERO : frontPos);
            buffer.writeLong(current);
            buffer.writeLong(max);
            buffer.writeInt(cwu);
            lastCurrent = current;
            lastMax = max;
            lastCwu = cwu;
        }

        @Override
        public void readInitialData(FriendlyByteBuf buffer) {
            super.readInitialData(buffer);
            dim = buffer.readUtf();
            pos = buffer.readBlockPos();
            frontPos = buffer.readBlockPos();
            if (frontPos.equals(BlockPos.ZERO)) frontPos = null;
            current = buffer.readLong();
            max = buffer.readLong();
            cwu = buffer.readInt();
        }

        @Override
        public void detectAndSendChanges() {
            super.detectAndSendChanges();
            if (tick++ % 20 == 0) refreshValues();
            if (current != lastCurrent || max != lastMax || cwu != lastCwu) {
                lastCurrent = current;
                lastMax = max;
                lastCwu = cwu;
                writeUpdateInfo(31, buffer -> {
                    buffer.writeLong(current);
                    buffer.writeLong(max);
                    buffer.writeInt(cwu);
                });
            }
        }

        @Override
        public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
            if (id == 31) {
                current = buffer.readLong();
                max = buffer.readLong();
                cwu = buffer.readInt();
            } else {
                super.readUpdateInfo(id, buffer);
            }
        }
    }
}
