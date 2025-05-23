package com.familytree;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ThemeOption {
    private final String name;
    private final Image icon;

    public ThemeOption(String name, String iconPath) {
        this.name = name;
        this.icon = new Image(getClass().getResourceAsStream(iconPath));
    }

    public String getName() {
        return name;
    }

    public ImageView getIconView() {
        ImageView view = new ImageView(icon);
        view.setFitWidth(32);
        view.setFitHeight(32);
        return view;
    }

    @Override
    public String toString() {
        return name;
    }
}
