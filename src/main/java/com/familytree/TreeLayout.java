package com.familytree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeLayout {
    private final Map<String, Position> layoutMap = new HashMap<>();

    public Map<String, Position> layout(List<Person> persons, List<Relationship> relationships) {
        layoutMap.clear();

        int horizontalSpacing = 250;
        int verticalSpacing = 200;
        int x = 100;
        int y = 100;

        for (int i = 0; i < persons.size(); i++) {
            Person person = persons.get(i);
            layoutMap.put(person.getId(), new Position(x, y, person.getId()));
            x += horizontalSpacing;

            // Wrap to new row if needed
            if (x > 1200) {
                x = 100;
                y += verticalSpacing;
            }
        }

        return layoutMap;
    }

    public Position getPosition(String personId) {
        return layoutMap.get(personId);
    }
}
