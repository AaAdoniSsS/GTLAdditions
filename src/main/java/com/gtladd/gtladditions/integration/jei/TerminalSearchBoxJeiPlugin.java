package com.gtladd.gtladditions.integration.jei;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import appeng.client.gui.AEBaseScreen;
import com.gtladd.gtladditions.GTLAdditions;
import com.gtladd.gtladditions.integration.SearchBoxDrop;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IGuiHandlerRegistration;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class TerminalSearchBoxJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return GTLAdditions.id("terminal_search_box");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(AEBaseScreen.class, new TerminalSearchBoxGhostHandler());
    }

    private static class TerminalSearchBoxGhostHandler implements IGhostIngredientHandler<AEBaseScreen> {

        @Override
        public <I> List<Target<I>> getTargetsTyped(AEBaseScreen gui, ITypedIngredient<I> ingredient, boolean doStart) {
            var name = ingredientName(ingredient);
            if (name == null) return List.of();
            List<Target<I>> targets = new ArrayList<>();
            for (var aeTextField : SearchBoxDrop.getTextFieldAreas(gui)) {
                var area = new Rect2i(aeTextField.getX() - 2, aeTextField.getY() - 2, aeTextField.getWidth() + 5, aeTextField.getHeight() + 4);
                targets.add(new TextFieldTarget<>(area, name, aeTextField, gui));
            }
            return targets.isEmpty() ? List.of() : targets;
        }

        @Override
        public void onComplete() {}
    }

    private record TextFieldTarget<I>(Rect2i area, String name, EditBox textField, AEBaseScreen<?> screen) implements IGhostIngredientHandler.Target<I> {

        @Override
        public Rect2i getArea() {
            return area;
        }

        @Override
        public void accept(I ingredient) {
            SearchBoxDrop.fillTextField(screen, textField, name);
        }
    }

    static String ingredientName(ITypedIngredient<?> ingredient) {
        var itemStack = ingredient.getIngredient(VanillaTypes.ITEM_STACK);
        if (itemStack.isPresent() && !itemStack.get().isEmpty()) {
            return itemStack.get().getHoverName().getString();
        }

        var fluidStack = ingredient.getIngredient(ForgeTypes.FLUID_STACK);
        if (fluidStack.isPresent() && !fluidStack.get().isEmpty()) {
            return fluidStack.get().getDisplayName().getString();
        }

        var direct = ingredient.getIngredient();
        if (direct instanceof ItemStack directItem && !directItem.isEmpty()) {
            return directItem.getHoverName().getString();
        }
        if (direct instanceof FluidStack directFluid && !directFluid.isEmpty()) {
            return directFluid.getDisplayName().getString();
        }

        return null;
    }
}
