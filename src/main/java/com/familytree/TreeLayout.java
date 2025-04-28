package com.familytree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeLayout {
    private Map<String, Position> positions = new HashMap<>();
    private double verticalSpacing = 120;
    private double horizontalSpacing = 150;

    public Map<String, Position> calculateLayout(List<Person> persons, List<Relationship> relationships) {
        positions.clear();

        List<Person> roots = findRoots(persons, relationships);

        double startX = 50;
        double startY = 50;

        for (Person root : roots) {
            startX = layoutPerson(root, startX, startY, persons, relationships);
            startX += horizontalSpacing;
        }

        return positions;
    }

    private double layoutPerson(Person person, double x, double y, List<Person> persons, List<Relationship> relationships) {
        List<Person> children = findChildren(person, persons, relationships);

        double originalX = x;
        double subtreeWidth = 0;

        for (Person child : children) {
            x = layoutPerson(child, x, y + verticalSpacing, persons, relationships);
            x += horizontalSpacing;
            subtreeWidth += horizontalSpacing;
        }

        double nodeX;
        if (!children.isEmpty()) {
            nodeX = originalX + subtreeWidth / 2 - horizontalSpacing / 2;
        } else {
            nodeX = originalX;
        }

        positions.put(person.getId(), new Position(nodeX, y, person.getId()));

        return Math.max(x, nodeX);
    }

    private List<Person> findRoots(List<Person> persons, List<Relationship> relationships) {
        List<String> childrenIds = new ArrayList<>();
        for (Relationship relationship : relationships) {
            if (relationship.getType().equalsIgnoreCase("Parent")) {
                childrenIds.add(relationship.getToId());
            }
        }

        List<Person> roots = new ArrayList<>();
        for (Person person : persons) {
            if (!childrenIds.contains(person.getId())) {
                roots.add(person);
            }
        }

        return roots;
    }

    private List<Person> findChildren(Person parent, List<Person> persons, List<Relationship> relationships) {
        List<Person> children = new ArrayList<>();
        for (Relationship relationship : relationships) {
            if (relationship.getType().equalsIgnoreCase("Parent") && relationship.getFromId().equals(parent.getId())) {
                for (Person person : persons) {
                    if (person.getId().equals(relationship.getToId())) {
                        children.add(person);
                        break;
                    }
                }
            }
        }
        return children;
    }
}
