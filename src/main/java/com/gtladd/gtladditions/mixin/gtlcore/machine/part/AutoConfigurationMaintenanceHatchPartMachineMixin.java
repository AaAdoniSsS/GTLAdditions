package com.gtladd.gtladditions.mixin.gtlcore.machine.part;

import org.gtlcore.gtlcore.common.machine.multiblock.part.maintenance.*;
import org.gtlcore.gtlcore.utils.Registries;
import org.gtlcore.gtlcore.utils.TextUtil;

import com.gregtechceu.gtceu.api.capability.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.client.util.TooltipHelper;

import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import dev.architectury.patchedmixin.staticmixin.spongepowered.asm.mixin.Overwrite;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;
import java.util.function.Supplier;

import static org.gtlcore.gtlcore.common.machine.multiblock.part.maintenance.ICleaningRoom.DUMMY_CLEANROOM;

@SuppressWarnings("all")
@Mixin(AutoConfigurationMaintenanceHatchPartMachine.class)
public class AutoConfigurationMaintenanceHatchPartMachineMixin extends TieredPartMachine implements IMachineLife, IAutoConfigurationMaintenanceHatch {

    private float maxDuration = getMax();
    private float minDuration = getMin();
    private static final Item BIOWARE_MAINFRAME = Registries.getItem("kubejs:bioware_mainframe");
    private static final Item COSMIC_MAINFRAME = Registries.getItem("kubejs:cosmic_mainframe");
    private static final Item CREATIVE_MAINFRAME = Registries.getItem("kubejs:suprachronal_mainframe_complex");
    @Persisted
    private final NotifiableItemStackHandler gtladditions$max = this.createMachineStorage();

    private NotifiableItemStackHandler createMachineStorage() {
        var storage = new NotifiableItemStackHandler(this, 1, IO.NONE, IO.BOTH,
                slots -> new ItemStackTransfer(1) {

                    @Override
                    public int getSlotLimit(int slot) {
                        return 1;
                    }
                })
                .setFilter(itemStack -> itemStack.is(BIOWARE_MAINFRAME) || itemStack.is(COSMIC_MAINFRAME) || itemStack.is(CREATIVE_MAINFRAME));
        storage.addChangedListener(this::upDataConfig);
        return storage;
    }

    @Shadow(remap = false)
    private float durationMultiplier;

    @Shadow(remap = false)
    private static Component getTextWidgetText(Supplier<Float> multiplier) {
        return null;
    }

    @Shadow(remap = false)
    public float getDurationMultiplier() {
        return 0;
    }

    @Override
    public void setDurationMultiplier(float count) {
        this.durationMultiplier = Mth.clamp(count, getMin(), getMax());
    }

    public AutoConfigurationMaintenanceHatchPartMachineMixin(IMachineBlockEntity holder) {
        super(holder, 5);
    }

    @Unique
    public boolean canShared() {
        return true;
    }

    @Overwrite(remap = false)
    public void incInternalMultiplier(int multiplier) {
        durationMultiplier = Math.min(durationMultiplier + 0.01F * (float) multiplier, maxDuration);
    }

    @Overwrite(remap = false)
    private void decInternalMultiplier(int multiplier) {
        durationMultiplier = Math.max(durationMultiplier - 0.01F * (float) multiplier, minDuration);
    }

    @Overwrite(remap = false)
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 150, 70);
        group.addWidget((new DraggableScrollableWidgetGroup(4, 4, 142, 62)).setBackground(GuiTextures.DISPLAY).addWidget((new ComponentPanelWidget(4, 5, (list) -> {
            list.add(getTextWidgetText(this::getDurationMultiplier));
            MutableComponent buttonText = Component.translatable("gtceu.maintenance.configurable_duration.modify");
            buttonText.append(" ");
            buttonText.append(ComponentPanelWidget.withButton(Component.literal("[-]"), "sub"));
            buttonText.append(" ");
            buttonText.append(ComponentPanelWidget.withButton(Component.literal("[+]"), "add"));
            list.add(buttonText);
        })).setMaxWidthLimit(130).clickHandler((componentData, clickData) -> {
            if (!clickData.isRemote) {
                int multiplier = clickData.isCtrlClick ? 100 : (clickData.isShiftClick ? 10 : 1);
                if (componentData.equals("sub")) this.setSubDuration(multiplier);
                else if (componentData.equals("add")) this.setAddDuration(multiplier);
            }
        }))).setBackground(GuiTextures.BACKGROUND_INVERSE);
        var slot = new SlotWidget(gtladditions$max.storage, 0, 120, 40) {

            @Override
            public List<Component> getTooltipTexts() {
                List<Component> tooltips = new ArrayList<>();
                tooltips.addAll(gtladditions$setMaxTooltips());
                return tooltips;
            }

            @Override
            protected void drawTooltipTexts(int mouseX, int mouseY) {
                if (isMouseOverElement(mouseX, mouseY) && getHoverElement(mouseX, mouseY) == this && gui != null && gui.getModularUIGui() != null) {
                    this.setHoverTooltips(gtladditions$setMaxTooltips());
                    gui.getModularUIGui().setHoverTooltip(tooltipTexts, getRealStack(slotReference.getItem()), null, null);
                }
            }
        }
                .setBackground(GuiTextures.SLOT);
        group.addWidget(slot);
        this.setSubDuration(0);
        this.setAddDuration(0);
        return group;
    }

    private void setDurationMultiplier() {
        maxDuration = getMax();
        minDuration = getMin();
    }

    private void setSubDuration(int multiplier) {
        this.setDurationMultiplier();
        this.decInternalMultiplier(multiplier);
    }

    private void setAddDuration(int multiplier) {
        this.setDurationMultiplier();
        this.incInternalMultiplier(multiplier);
    }

    @Unique
    public void onMachineRemoved() {
        this.clearInventory(this.gtladditions$max.storage);
    }

    private @NotNull List<Component> gtladditions$setMaxTooltips() {
        List<Component> list = new ArrayList<>();
        list.add(Component.translatable("gtceu.universal.enabled"));
        list.add(Component.translatable("gtceu.multiblock.use_different_mainframe"));
        list.add(Component.translatable("gtceu.multiblock.use_bioware_mainframe", 3.0, 0.15));
        list.add(Component.translatable("gtceu.multiblock.use_cosmic_mainframe", 7.5, 0.1));
        list.add(Component.translatable("gtceu.multiblock.use_suprachronal_mainframe_complex", 25.0, 0.05));
        list.add(Component.literal(TextUtil.full_color(Component.translatable("gui.gtladditions.modify").getString()))
                .withStyle(style -> style.withColor(TooltipHelper.RAINBOW.getCurrent())));
        return list;
    }

    private float getMax() {
        ItemStack stack = gtladditions$max != null ? gtladditions$max.storage.getStackInSlot(0) : null;
        if (stack != null) {
            if (stack.is(BIOWARE_MAINFRAME)) return 3.0F;
            else if (stack.is(COSMIC_MAINFRAME)) return 7.5F;
            else if (stack.is(CREATIVE_MAINFRAME)) return 25.0F;
        }
        return 1.2F;
    }

    private float getMin() {
        ItemStack stack = gtladditions$max != null ? gtladditions$max.storage.getStackInSlot(0) : null;
        if (stack != null) {
            if (stack.is(BIOWARE_MAINFRAME)) return 0.15F;
            else if (stack.is(COSMIC_MAINFRAME)) return 0.1F;
            else if (stack.is(CREATIVE_MAINFRAME)) return 0.05F;
        }
        return 0.2F;
    }

    @Unique
    public void addedToController(@NotNull IMultiController controller) {
        super.addedToController(controller);
        if (controller instanceof ICleanroomReceiver receiver) receiver.setCleanroom(null);
    }

    @Unique
    public void removedFromController(@NotNull IMultiController controller) {
        super.removedFromController(controller);
        if (controller instanceof ICleanroomReceiver receiver) if (receiver.getCleanroom() == DUMMY_CLEANROOM) receiver.setCleanroom(null);
    }

    private void upDataConfig() {
        this.setSubDuration(0);
        this.setAddDuration(0);
    }
}
