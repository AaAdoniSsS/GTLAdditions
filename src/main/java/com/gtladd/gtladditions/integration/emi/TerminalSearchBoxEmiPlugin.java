package com.gtladd.gtladditions.integration.emi;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

import appeng.client.gui.AEBaseScreen;
import com.gtladd.gtladditions.integration.SearchBoxDrop;
import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

@EmiEntrypoint
public class TerminalSearchBoxEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addGenericDragDropHandler(new TerminalSearchBoxDragHandler());
    }

    private static class TerminalSearchBoxDragHandler implements EmiDragDropHandler<Screen> {

        @Override
        public boolean dropStack(Screen screen, EmiIngredient stack, int x, int y) {
            if (!(screen instanceof AEBaseScreen<?>)) return false;
            for (var textField : SearchBoxDrop.getTextFieldAreas(screen)) {
                for (var emiStack : stack.getEmiStacks()) {
                    if (!emiStack.isEmpty() && SearchBoxDrop.fillTextField(screen, textField, emiStack.getName().getString())) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void render(Screen screen, EmiIngredient dragged, GuiGraphics draw, int mouseX, int mouseY, float delta) {
            if (!(screen instanceof AEBaseScreen<?>) || dragged.getEmiStacks().stream().allMatch(EmiStack::isEmpty)) return;

            for (var textField : SearchBoxDrop.getTextFieldAreas(screen)) {
                var area = new Rect2i(textField.getX() - 2, textField.getY() - 2, textField.getWidth() + 5, textField.getHeight() + 4);
                draw.fill(area.getX(), area.getY(), area.getX() + area.getWidth(), area.getY() + area.getHeight(), 0x8822BB33);
            }
        }
    }
}
