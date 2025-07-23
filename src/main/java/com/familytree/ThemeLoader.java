package com.familytree;

import java.util.ArrayList;
import java.util.List;

public class ThemeLoader {
    public static List<ThemeItem> loadThemes() {
        List<ThemeItem> items = new ArrayList<>();

        for (Theme theme : Theme.values()) {
            if (!theme.isCustom()) {
                items.add(new ThemeItem(theme.displayName(), theme, theme.icon()));
            }
        }

        return items;
    }
}
