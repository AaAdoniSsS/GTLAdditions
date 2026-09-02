package com.gtladd.gtladditions.common.machine;

import org.gtlcore.gtlcore.utils.TextUtil;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.ResearchManager;

import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.hepdd.gtmthings.utils.TeamUtil;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CloudOpticalDataMachine extends TieredEnergyMachine implements IMachineLife, IFancyUIMachine, IDataStickInteractable {

    public static final Set<CloudOpticalDataMachine> CLOUD_DATA_MACHINE_SET = new ObjectOpenHashSet<>();
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CloudOpticalDataMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    private static final long ENERGY_PER_DATA = GTValues.V[GTValues.UV] * 3 / 4;

    @Getter
    @Persisted
    @DescSynced
    private UUID player;

    @Persisted
    protected final NotifiableItemStackHandler importItems;
    @Persisted
    protected final NotifiableItemStackHandler createItem;
    private final IntSet recipes = new IntOpenHashSet();
    private boolean recipesDirty = true, amountDirty = true, hasPower = true;
    @Persisted
    private int dataAmount = 0;
    @Persisted
    private boolean isCreate = false;
    private TickableSubscription energySubs;

    public CloudOpticalDataMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.UIV);
        this.importItems = new NotifiableItemStackHandler(this, 90, IO.BOTH);
        this.importItems.setFilter(stack -> ResearchManager.isStackDataItem(stack, true) && ResearchManager.hasResearchTag(stack))
                .addChangedListener(this::markRecipesDirty);
        this.createItem = new NotifiableItemStackHandler(this, 1, IO.NONE, IO.BOTH,
                slots -> new ItemStackTransfer(1) {

                    public int getSlotLimit(int slot) {
                        return 1;
                    }
                });
        this.createItem.setFilter(stack -> stack.is(GTResearchMachines.CREATIVE_DATA_ACCESS_HATCH.getItem()))
                .addChangedListener(() -> this.isCreate = this.createItem.getStackInSlot(0).is(GTResearchMachines.CREATIVE_DATA_ACCESS_HATCH.getItem()));
    }

    @Override
    protected @NotNull NotifiableEnergyContainer createEnergyContainer(Object @NotNull... args) {
        return NotifiableEnergyContainer.receiverContainer(this,
                64L * GTValues.V[GTValues.UIV], GTValues.V[GTValues.UIV], 16);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            CLOUD_DATA_MACHINE_SET.add(this);
            markRecipesDirty();
            energySubs = subscribeServerTick(energySubs, this::updateEnergy);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (!isRemote()) CLOUD_DATA_MACHINE_SET.remove(this);
        if (this.energySubs != null) {
            this.energySubs.unsubscribe();
            this.energySubs = null;
        }
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (player instanceof Player p) this.player = p.getUUID();
        CLOUD_DATA_MACHINE_SET.add(this);
        markRecipesDirty();
    }

    @Override
    public InteractionResult onDataStickRightClick(Player player, ItemStack stack) {
        this.player = player.getUUID();
        if (player instanceof ServerPlayer sp) sp.sendSystemMessage(Component.translatable("gui.gtladditions.cloud.bind_success", TeamUtil.GetName(sp)));
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onDataStickLeftClick(Player player, ItemStack stack) {
        this.player = null;
        if (player instanceof ServerPlayer sp) sp.sendSystemMessage(Component.translatable("gui.gtladditions.cloud.unbind_success"));
        return true;
    }

    @Override
    public void onMachineRemoved() {
        CLOUD_DATA_MACHINE_SET.remove(this);
        clearInventory(importItems);
        clearInventory(createItem);
    }

    public long getDataCount() {
        if (!this.amountDirty) return dataAmount + 1;
        dataAmount = 0;
        for (int i = 0; i < importItems.getSlots(); i++) if (!importItems.getStackInSlot(i).isEmpty()) dataAmount++;
        this.amountDirty = false;
        return dataAmount + 1;
    }

    public long getEnergyDemand() {
        if (!isCreate) return getDataCount() * ENERGY_PER_DATA;
        else return ENERGY_PER_DATA;
    }

    private void updateEnergy() {
        long demand = getEnergyDemand();
        if (demand <= 0) {
            hasPower = true;
            return;
        }
        hasPower = energyContainer.removeEnergy(demand) >= demand;
    }

    public void rebuildData() {
        if (getLevel() == null) return;
        this.recipes.clear();
        for (int i = 0; i < importItems.getSlots(); i++) {
            var stack = importItems.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            var researchId = ResearchManager.readResearchId(stack);
            if (researchId == null || !ResearchManager.isStackDataItem(stack, true)) continue;
            var entries = researchId.getFirst().getDataStickEntry(researchId.getSecond());
            if (entries != null) this.recipes.addAll(entries.stream().mapToInt(GTRecipe::hashCode).collect(IntOpenHashSet::new, IntSet::add, IntSet::addAll));
        }
    }

    private void markRecipesDirty() {
        this.recipesDirty = true;
        this.amountDirty = true;
    }

    private void refreshRecipesIfNeeded() {
        if (!this.recipesDirty) return;
        if (getLevel() == null) return;
        rebuildData();
        this.recipesDirty = false;
    }

    public IntSet getRecipes() {
        refreshRecipesIfNeeded();
        return recipes;
    }

    public interface ICloudTeamBindable {

        @Nullable
        UUID getTeamId();
    }

    public static boolean uploadDataStickToCloud(ItemStack dataStick, UUID uuid) {
        if (dataStick.isEmpty() || uuid == null) return false;
        if (!ResearchManager.isStackDataItem(dataStick, true)) return false;
        for (var machine : CLOUD_DATA_MACHINE_SET) {
            if (machine.player == null || !FTBTeamsAPI.api().getManager().arePlayersInSameTeam(machine.player, uuid)) continue;
            for (int i = 0; i < machine.importItems.getSlots(); i++) {
                if (machine.importItems.getStackInSlot(i).isEmpty()) {
                    machine.importItems.setStackInSlot(i, dataStick);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isRecipeAvailableInCloud(GTRecipe recipe, UUID uuid) {
        for (var machine : CLOUD_DATA_MACHINE_SET) {
            if (machine.player == null || !FTBTeamsAPI.api().getManager().arePlayersInSameTeam(machine.player, uuid)) continue;
            if (machine.hasPower) {
                if (machine.isCreate) return true;
                else if (machine.getRecipes().contains(recipe.hashCode())) return true;
            }
        }
        return false;
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 176, 145);
        group.addWidget(new ComponentPanelWidget(5, 5, this::addDisplayText).setMaxWidthLimit(160));
        var slotScroll = new DraggableScrollableWidgetGroup(5, 84, 168, 54);
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                slotScroll.addWidget(new SlotWidget(importItems, row * 9 + col, 2 + col * 18, row * 18) {

                    @Override
                    public boolean isEnabled() {
                        return true;
                    }
                }
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }
        group.addWidget(slotScroll);
        group.addWidget(new SlotWidget(createItem, 0, group.getSizeWidth() - 30, 20)
                .setBackgroundTexture(GuiTextures.SLOT)
                .appendHoverTooltips(Component.translatable("gui.gtladditions.cloud_data_machine.create")));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    private void addDisplayText(List<Component> textList) {
        textList.add(self().getBlockState().getBlock().getName());
        if (player == null) {
            textList.add(Component.translatable("gui.gtladditions.cloud.not_bound"));
        } else {
            if (TeamUtil.hasOwner(getLevel(), player)) textList.add(Component.translatable("gui.gtladditions.cloud.bind_success", TeamUtil.GetName(getLevel(), player)));
            textList.add(Component.translatable("gui.gtladditions.cloud_data_machine.cloud_count",
                    CLOUD_DATA_MACHINE_SET.stream().filter(m -> FTBTeamsAPI.api().getManager().arePlayersInSameTeam(m.player, player)).count()));
        }
        if (!isCreate) {
            textList.add(Component.translatable("gui.gtladditions.cloud_data_machine.recipes_count.0", getRecipes().size()));
        } else {
            textList.add(Component.translatable("gui.gtladditions.cloud_data_machine.recipes_count.1", Component.literal(TextUtil.full_color(Component.translatable("gui.gtladditions.cloud_data_machine.recipes_count.2").getString()))
                    .withStyle(style -> style.withColor(TooltipHelper.RAINBOW.getCurrent()))));
        }
        textList.add(Component.translatable("gui.gtladditions.cloud_data_machine.energy_demand", FormattingUtil.formatNumbers(getEnergyDemand())));
        textList.add(Component.translatable(hasPower ? "gui.gtladditions.cloud_data_machine.power_normal" : "gui.gtladditions.cloud_data_machine.power_insufficient")
                .withStyle(style -> style.withColor(hasPower ? 0x55FF55 : 0xFF5555)));
    }
}
