package com.familytree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeLayout {
    private Map<String, Position> layoutMap = new HashMap<>();

    public Map<String, Position> layout(List<Person> persons, List<Relationship> relationships) {
        layoutMap.clear();

        int x = 50;
        int y = 50;
        int verticalSpacing = 150;
        int horizontalSpacing = 200;

        for (int i = 0; i < persons.size(); i++) {
            Person p = persons.get(i);
            layoutMap.put(p.getName(), new Position(x, y, p.getName()));
            x += horizontalSpacing;
            if (x > 800) {
                x = 50;
                y += verticalSpacing;
            }
        }

        return layoutMap;
    }

    public Position getPosition(String personId) {
        return layoutMap.get(personId);
    }
}