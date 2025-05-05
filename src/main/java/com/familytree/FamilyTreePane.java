package com.familytree;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.time.LocalDate;
import java.util.*;

public class FamilyTreePane extends Pane {

    private final double NODE_WIDTH = 140;
    private final double NODE_HEIGHT = 80;
    private final double H_SPACING = 40;
    private final double V_SPACING = 100;

    private Person selectedPerson;

    private final Map<Person, Group> personNodeMap = new HashMap<>();

    public FamilyTreePane() {
        setPrefSize(2000, 1000);
    }

    public void drawTree(List<Person> persons) {
        getChildren().clear();
        personNodeMap.clear();

        List<Person> roots = FamilyTreeData.getInstance().getPersons();
        if (roots.isEmpty()) return;

        double totalWidth = 0;
        for (Person root : roots) {
            totalWidth += getSubtreeWidth(root);
        }
        totalWidth += H_SPACING * (roots.size() - 1);

        double startX = getWidth() / 2 - totalWidth / 2;
        double x = startX;
        double y = 40;

        Set<Person> visited = new HashSet<>();
        for (Person root : roots) {
            double width = drawSubtree(root, x + getSubtreeWidth(root) / 2, y, visited);
            x += width + H_SPACING;
        }

        drawLines();
    }

    private double drawSubtree(Person person, double x, double y, Set<Person> visited) {
        if (visited.contains(person)) return 0;
        visited.add(person);

        List<Person> children = person.getChildren();
        double subtreeWidth = Math.max(NODE_WIDTH, getSubtreeWidth(person));

        // Position for current node
        double startX = x - subtreeWidth / 2;

        // Draw children first
        double currentX = startX;
        for (Person child : children) {
            double childWidth = getSubtreeWidth(child);
            double childXCenter = currentX + childWidth / 2;
            drawSubtree(child, childXCenter, y + NODE_HEIGHT + V_SPACING, visited);
            currentX += childWidth + H_SPACING;
        }

        // Draw this person
        Group nodeGroup = createPersonNode(person, x - NODE_WIDTH / 2, y);
        personNodeMap.put(person, nodeGroup);
        getChildren().add(nodeGroup);

        return subtreeWidth;
    }

    private double getSubtreeWidth(Person person) {
        List<Person> children = person.getChildren();
        if (children.isEmpty()) {
            return NODE_WIDTH;
        }

        double totalWidth = 0;
        for (Person child : children) {
            totalWidth += getSubtreeWidth(child);
        }
        totalWidth += H_SPACING * (children.size() - 1);

        return Math.max(NODE_WIDTH, totalWidth);
    }

    private Group createPersonNode(Person person, double x, double y) {
        Rectangle box = new Rectangle(x, y, NODE_WIDTH, NODE_HEIGHT);
        box.setFill(Color.LIGHTBLUE);
        box.setStroke(Color.DARKBLUE);

        if (person.equals(selectedPerson)) {
            box.setStroke(Color.RED);
            box.setStrokeWidth(3);
        }

        Label nameLabel = new Label(person.getName());
        nameLabel.setLayoutX(x + 10);
        nameLabel.setLayoutY(y + 10);

        StringBuilder details = new StringBuilder();
        if (person.getDateOfBirth() != null)
            details.append("b. ").append(person.getDateOfBirth().toString()).append("\n");
        if (person.getDateOfDeath() != null)
            details.append("d. ").append(person.getDateOfDeath().toString());

        Label detailsLabel = new Label(details.toString());
        detailsLabel.setLayoutX(x + 10);
        detailsLabel.setLayoutY(y + 30);

        Group group = new Group(box, nameLabel, detailsLabel);
        group.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                selectedPerson = person;
                drawTree(FamilyTreeData.getInstance().getPersons());
            }
        });

        return group;
    }

    private void drawLines() {
        for (Person parent : personNodeMap.keySet()) {
            Group parentGroup = personNodeMap.get(parent);
            if (parentGroup == null) continue;
            Bounds parentBounds = parentGroup.getBoundsInParent();

            for (Person child : parent.getChildren()) {
                Group childGroup = personNodeMap.get(child);
                if (childGroup == null) continue;
                Bounds childBounds = childGroup.getBoundsInParent();

                Line line = new Line(
                        parentBounds.getMinX() + NODE_WIDTH / 2,
                        parentBounds.getMinY() + NODE_HEIGHT,
                        childBounds.getMinX() + NODE_WIDTH / 2,
                        childBounds.getMinY()
                );
                line.setStroke(Color.GRAY);
                getChildren().add(line);
            }
        }
    }

    public Person getSelectedPerson() {
        return selectedPerson;
    }

    public Pane getView() {
        return this;
    }
}
