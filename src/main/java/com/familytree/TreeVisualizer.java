package com.familytree;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeVisualizer extends Canvas {

    private Map<Person, TreeLayout.Position> layoutMap = new HashMap<>();

    public TreeVisualizer() {
        this.setWidth(1200);
        this.setHeight(800);
    }

    public void drawTree(List<Person> persons) {
        layoutMap = TreeLayout.computeLayout(persons);

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        gc.setStroke(Color.GRAY);
        gc.setLineWidth(2);
        gc.setFont(Font.font(14));

        // 1. Draw relationships
        for (Person child : persons) {
            if (child.getParentIds() != null) {
                for (String parentId : child.getParentIds()) {
                    Person parent = findPersonById(parentId);
                    if (parent != null) {
                        TreeLayout.Position from = layoutMap.get(parent);
                        TreeLayout.Position to = layoutMap.get(child);
                        if (from != null && to != null) {
                            gc.strokeLine(from.getX() + 60, from.getY() + 30, to.getX() + 60, to.getY());
                        }
                    }
                }
            }
        }

        // 2. Draw nodes (people)
        for (Person person : persons) {
            TreeLayout.Position pos = layoutMap.get(person);
            if (pos != null) {
                gc.setFill(Color.LIGHTBLUE);
                gc.fillRoundRect(pos.getX(), pos.getY(), 120, 60, 10, 10);
                gc.setStroke(Color.BLACK);
                gc.strokeRoundRect(pos.getX(), pos.getY(), 120, 60, 10, 10);

                gc.setFill(Color.BLACK);
                String text = person.getName() + "\n" +
                        "Born: " + (person.getDateOfBirth() != null ? person.getDateOfBirth() : "-") + "\n" +
                        "Gender: " + (person.getGender() != null ? person.getGender() : "-");
                gc.fillText(text, pos.getX() + 5, pos.getY() + 15);
            }
        }

        List<Relationship> relationships = FamilyTreeData.getInstance().getRelationships();
        for (Relationship r : relationships) {
            Person from = findPersonById(r.getFromId());
            Person to = findPersonById(r.getToId());
            if (from != null && to != null) {
                TreeLayout.Position fromPos = layoutMap.get(from.getId());
                TreeLayout.Position toPos = layoutMap.get(to.getId());

                if (fromPos != null && toPos != null) {
                    gc.setStroke(r.getType().equals("Parent") ? Color.BLUE : Color.GREEN);
                    gc.setLineWidth(2);
                    gc.strokeLine(fromPos.getX() + 50, fromPos.getY() + 50, toPos.getX() + 50, toPos.getY() + 50);
                }
            }
        }
    }

    private Person findPersonById(String id) {
        return FamilyTreeData.getInstance().getPersons().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
