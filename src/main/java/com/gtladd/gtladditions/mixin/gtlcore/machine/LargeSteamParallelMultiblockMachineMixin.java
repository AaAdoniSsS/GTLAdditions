package com.gtladd.gtladditions.mixin.gtlcore.machine;

import com.gtladd.gtladditions.common.machine.hatch.HugeSteamHatchPartMachine;
import org.gtlcore.gtlcore.api.machine.ISteamMachine;
import org.gtlcore.gtlcore.common.data.GTLMachines;
import org.gtlcore.gtlcore.common.data.machines.MultiBlockMachineA;
import org.gtlcore.gtlcore.common.machine.multiblock.part.LargeSteamHatchPartMachine;
import org.gtlcore.gtlcore.common.machine.multiblock.steam.LargeSteamParallelMultiblockMachine;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamEnergyRecipeHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.machine.multiblock.part.SteamHatchPartMachine;

import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

import com.gtladd.gtladditions.api.recipe.ContentList;
import com.gtladd.gtladditions.common.machine.GTLAddMachines;
import com.gtladd.gtladditions.utils.GTRecipeUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(LargeSteamParallelMultiblockMachine.class)
public class LargeSteamParallelMultiblockMachineMixin extends WorkableMultiblockMachine implements ISteamMachine, IVoidable {

    public LargeSteamParallelMultiblockMachineMixin(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Unique
    private static final Map<RecipeCapability<?>, Integer> gTLAdditions$OutputLimited = Map.of(ItemRecipeCapability.CAP, 4);
    @DescSynced
    @Unique
    private boolean gtladditions$isLarge;
    @DescSynced
    @Unique
    private boolean gtladditions$isHuge;
    @Unique
    private boolean gtladditions$isMacerator;
    @Shadow(remap = false)
    private boolean isOC;
    @Shadow(remap = false)
    private int amountOC;
    @Shadow(remap = false)
    @Final
    private int max_parallels;

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public static GTRecipe recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe, double reductionDuration) {
        if (machine instanceof LargeSteamParallelMultiblockMachineMixin machine1) {
            boolean isHuge = machine1.gtladditions$isHuge;
            boolean isLarge = machine1.gtladditions$isLarge;
            if (GTRecipeUtils.INSTANCE.getGetEU(recipe) > (isHuge ? 512 : (isLarge ? 128 : 32))) return null;
            var result = GTRecipeModifiers.accurateParallel(machine, recipe, machine1.max_parallels, false).getFirst();
            recipe = result == recipe ? result.copy() : result;
            if (isHuge) reductionDuration = 0.0;
            if (machine1.getDefinition() == MultiBlockMachineA.DIMENSIONALLY_TRANSCENDENT_STEAM_OVEN) recipe.tickInputs.put(EURecipeCapability.CAP,
                    ContentList.Companion.getEUtList(Math.max(1.0, GTRecipeUtils.INSTANCE.getGetEU(recipe) * 0.01)));
            recipe.duration = (int) Math.max(1.0, (double) recipe.duration * reductionDuration / (isLarge ? Math.pow(2.0, machine1.amountOC) : 1.0));
        }
        return recipe;
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public void onStructureFormed() {
        super.onStructureFormed();
        var handlers = this.capabilitiesProxy.get(IO.IN, FluidRecipeCapability.CAP);
        if (handlers != null) {
            var itr = handlers.iterator();
            while (itr.hasNext()) {
                var handler = itr.next();
                if (handler instanceof NotifiableFluidTank tank) {
                    var machine = tank.getMachine();
                    if (machine instanceof SteamHatchPartMachine || machine instanceof LargeSteamHatchPartMachine || machine instanceof HugeSteamHatchPartMachine) {
                        gtladditions$isLarge = machine.getDefinition() == GTLMachines.LARGE_STEAM_HATCH;
                        gtladditions$isHuge = machine.getDefinition() == GTLAddMachines.HUGE_STEAM_HATCH;
                        this.isOC = gtladditions$isLarge || gtladditions$isHuge;
                        this.gtladditions$isMacerator = this.self().getDefinition() == MultiBlockMachineA.LARGE_STEAM_MACERATOR;
                        itr.remove();
                        this.capabilitiesProxy.put(IO.IN, EURecipeCapability.CAP,
                                List.of(new SteamEnergyRecipeHandler(tank, 0.5 *
                                        (gtladditions$isHuge ? 250.0 : (gtladditions$isLarge ? Math.pow(3.0, amountOC) : 1.0)))));
                        return;
                    }
                }
            }
        }
    }

    @Inject(method = "addDisplayText", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/api/machine/trait/RecipeLogic;isWaiting()Z", shift = At.Shift.BEFORE), remap = false, cancellable = true)
    public void addDisplayText(List<Component> textList, CallbackInfo ci) {
        if (this.recipeLogic.isWaiting()) textList.add(Component.translatable("gtceu.multiblock.steam.low_steam").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        if (gtladditions$isHuge) textList.add(Component.translatable("gtceu.multiblock.steam_duration_modify"));
        else if (gtladditions$isLarge) {
            textList.add(Component.translatable("gtceu.multiblock.oc_amount", amountOC)
                    .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("gtceu.multiblock.steam_parallel_machine.oc")))));
            textList.add(Component.translatable("gtceu.multiblock.steam_parallel_machine.modification_oc")
                    .append(ComponentPanelWidget.withButton(Component.literal("[-] "), "ocSub"))
                    .append(ComponentPanelWidget.withButton(Component.literal("[+]"), "ocAdd")));
        }
        ci.cancel();
    }

    @Override
    public double getConversionRate() {
        return 0.5 * (gtladditions$isHuge ? 250.0 : (gtladditions$isLarge ? Math.pow(3.0, amountOC) : 1.0));
    }

    @Override
    public Map<RecipeCapability<?>, Integer> getOutputLimits() {
        if (this.gtladditions$isMacerator && this.gtladditions$isHuge) return gTLAdditions$OutputLimited;
        else return this.self().getDefinition().getRecipeOutputLimits();
    }
}
