package com.gtladd.gtladditions.integration;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;

import appeng.client.gui.AEBaseScreen;

import java.util.Collections;
import java.util.List;

public final class SearchBoxDrop {

    public static List<EditBox> getTextFieldAreas(Screen screen) {
        if (!(screen instanceof AEBaseScreen<?> aeScreen)) return Collections.emptyList();
        return aeScreen.children().stream()
                .filter(EditBox.class::isInstance).map(EditBox.class::cast)
                .filter(b -> b.getValue().isEmpty() || b.getValue().matches("[+-]?\\d+"))
                .toList();
    }

    public static boolean fillTextField(Screen screen, EditBox textField, String name) {
        textField.setValue(name);
        textField.setCursorPosition(0);
        textField.setHighlightPos(0);
        screen.setFocused(textField);
        return true;
    }
}
