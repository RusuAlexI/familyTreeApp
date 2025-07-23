package com.familytree;

import javafx.scene.image.Image;

import java.io.File;
import java.util.Objects;

public enum Theme {
    DEFAULT("images/Genealogical_Family_Tree_on_Aged_Paper.png", "icons/default.jpg", "Default"),
    TREE("images/tree.jpg", "icons/default.jpg", "Tree Style"),
    PARCHMENT("images/parchment.jpg", "icons/parchment.jpg", "Parchment"),
    CUSTOM(null, null, "Custom");

    private final String backgroundPath;
    private final String iconPath;
    private final String displayName;
    private Image backgroundImage;
    private Image icon;

    Theme(String backgroundPath, String iconPath, String displayName) {
        this.backgroundPath = backgroundPath;
        this.iconPath = iconPath;
        this.displayName = displayName;

        try {
            if (backgroundPath != null) {
                var bgStream = getClass().getResourceAsStream("/" + backgroundPath);
                if (bgStream != null) backgroundImage = new Image(bgStream);
            }

            if (iconPath != null) {
                var iconStream = getClass().getResourceAsStream("/" + iconPath);
                if (iconStream != null) icon = new Image(iconStream);
            }
        } catch (Exception e) {
            System.err.println("Error loading theme: " + displayName + " – " + e.getMessage());
        }
    }

    public Image backgroundImage() {
        return backgroundImage;
    }

    public Image icon() {
        return icon;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isCustom() {
        return this == CUSTOM;
    }

    public void setCustomBackground(File file) {
        if (isCustom() && file != null) {
            this.backgroundImage = new Image(file.toURI().toString());
        }
    }
}
