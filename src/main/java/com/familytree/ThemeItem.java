package com.familytree;

import javafx.scene.image.Image;

public class ThemeItem {
    private final String displayName;
    private final Theme theme; // This holds the actual Theme enum value
    private final Image icon; // Optional icon for the menu item

    public ThemeItem(String displayName, Theme theme, Image icon) {
        this.displayName = displayName;
        this.theme = theme;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Theme getTheme() {
        return theme; // THIS IS THE NEW/CRUCIAL METHOD
    }

    public Image getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return displayName; // This is what will be displayed in the ChoiceBox
    }
}