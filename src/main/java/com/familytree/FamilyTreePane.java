package com.familytree;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.*;

public class FamilyTreePane extends Pane {

    private final Map<Person, StackPane> personNodeMap = new HashMap<>();
    private final Group lineGroup = new Group();
    private Person selectedPerson;

    public FamilyTreePane() {
        getChildren().add(lineGroup);
    }

    public Person getSelectedPerson() {
        return selectedPerson;
    }

    public void drawTree(List<Person> persons) {
        getChildren().clear();
        personNodeMap.clear();
        lineGroup.getChildren().clear();
        getChildren().add(lineGroup);

        // Layout constants
        double horizontalSpacing = 120;
        double verticalSpacing = 150;
        double nodeWidth = 120;
        double nodeHeight = 60;

        // Step 1: Find root-level persons (those with no parents)
        List<Person> roots = new ArrayList<>();
        for (Person person : persons) {
            if (person.getParents().isEmpty()) {
                roots.add(person);
            }
        }

        // Step 2: Assign levels (distance from root)
        Map<Person, Integer> levels = new HashMap<>();
        Set<Person> visited = new HashSet<>();

        for (Person root : roots) {
            assignLevels(root, 0, levels, visited);
        }

        // Step 3: Group persons by level
        Map<Integer, List<Person>> levelMap = new TreeMap<>();
        for (Map.Entry<Person, Integer> entry : levels.entrySet()) {
            levelMap.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        // Step 4: Position nodes level by level
        Map<Person, Double> xPositions = new HashMap<>();
        double currentY = 50;

        for (Map.Entry<Integer, List<Person>> entry : levelMap.entrySet()) {
            List<Person> levelPersons = entry.getValue();
            double currentX = 50;

            for (Person person : levelPersons) {
                StackPane node = createPersonNode(person, nodeWidth, nodeHeight);
                personNodeMap.put(person, node);
                getChildren().add(node);

                node.setLayoutX(currentX);
                node.setLayoutY(currentY);
                xPositions.put(person, currentX);
                currentX += nodeWidth + horizontalSpacing;
            }

            currentY += nodeHeight + verticalSpacing;
        }

        // Step 5: Adjust parent positioning for shared children
        for (Person child : persons) {
            List<Person> parents = child.getParents();
            if (parents.size() == 2) {
                StackPane childNode = personNodeMap.get(child);
                StackPane parent1Node = personNodeMap.get(parents.get(0));
                StackPane parent2Node = personNodeMap.get(parents.get(1));

                if (childNode != null && parent1Node != null && parent2Node != null) {
                    // Calculate center X of child
                    double childCenterX = childNode.getLayoutX() + nodeWidth / 2;

                    // Place parents side-by-side centered above the child
                    double parent1X = childCenterX - nodeWidth - 10;
                    double parent2X = childCenterX + 10;
                    double parentY = childNode.getLayoutY() - verticalSpacing - nodeHeight;

                    parent1Node.setLayoutX(parent1X);
                    parent1Node.setLayoutY(parentY);

                    parent2Node.setLayoutX(parent2X);
                    parent2Node.setLayoutY(parentY);

                    // Update xPositions for alignment downstream (optional)
                    xPositions.put(parents.get(0), parent1X);
                    xPositions.put(parents.get(1), parent2X);
                }
            }
        }


        // Step 6: Draw lines from parents to children
        for (Person child : persons) {
            StackPane childNode = personNodeMap.get(child);
            if (childNode == null) continue;

            Bounds childBounds = childNode.getBoundsInParent();
            double childTopX = childBounds.getMinX() + nodeWidth / 2;
            double childTopY = childBounds.getMinY();

            List<Person> parents = child.getParents();
            if (parents.size() == 2) {
                StackPane parent1Node = personNodeMap.get(parents.get(0));
                StackPane parent2Node = personNodeMap.get(parents.get(1));
                if (parent1Node == null || parent2Node == null) continue;

                Bounds b1 = parent1Node.getBoundsInParent();
                Bounds b2 = parent2Node.getBoundsInParent();
                double y = b1.getMaxY();

                double x1 = b1.getMinX() + nodeWidth / 2;
                double x2 = b2.getMinX() + nodeWidth / 2;
                double midX = (x1 + x2) / 2;

                // Horizontal line between parents
                lineGroup.getChildren().add(new Line(x1, y, x2, y));
                // Vertical line down to child
                lineGroup.getChildren().add(new Line(midX, y, childTopX, childTopY));
            } else {
                for (Person parent : parents) {
                    StackPane parentNode = personNodeMap.get(parent);
                    if (parentNode == null) continue;

                    Bounds parentBounds = parentNode.getBoundsInParent();
                    double parentBottomX = parentBounds.getMinX() + nodeWidth / 2;
                    double parentBottomY = parentBounds.getMinY() + nodeHeight;

                    lineGroup.getChildren().add(new Line(parentBottomX, parentBottomY, childTopX, childTopY));
                }
            }
        }

    }

    private void assignLevels(Person person, int level, Map<Person, Integer> levels, Set<Person> visited) {
        if (visited.contains(person)) return;
        visited.add(person);

        levels.put(person, Math.max(levels.getOrDefault(person, 0), level));
        for (Person child : getChildrenOf(person)) {
            assignLevels(child, level + 1, levels, visited);
        }
    }

    private List<Person> getChildrenOf(Person parent) {
        List<Person> children = new ArrayList<>();
        for (Person p : FamilyTreeData.getInstance().getPersons()) {
            if (p.getParents().contains(parent)) {
                children.add(p);
            }
        }
        return children;
    }

    private StackPane createPersonNode(Person person, double width, double height) {
        Rectangle bg = new Rectangle(width, height);
        bg.setArcWidth(20);
        bg.setArcHeight(20);
        bg.setFill(Color.LIGHTBLUE);
        bg.setStroke(Color.DARKBLUE);
        bg.setStrokeWidth(2);
        bg.setEffect(new javafx.scene.effect.DropShadow());

        Label nameLabel = new Label(person.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: darkblue;");

        Label dobLabel = new Label(person.getDateOfBirth() == null ? "" : "b. " + person.getDateOfBirth());
        Label dodLabel = new Label(person.getDateOfDeath() == null ? "" : "d. " + person.getDateOfDeath());

        VBoxWithSpacing content = new VBoxWithSpacing(4, nameLabel, dobLabel, dodLabel);

        StackPane node = new StackPane();
        node.getChildren().addAll(bg, content);
        node.setPrefSize(width, height);

        node.setOnMouseClicked(e -> {
            selectedPerson = person;
            System.out.println("Selected: " + person.getName());
        });

        return node;
    }

    // Helper VBox subclass with spacing
    static class VBoxWithSpacing extends javafx.scene.layout.VBox {
        public VBoxWithSpacing(double spacing, javafx.scene.Node... nodes) {
            super(spacing, nodes);
            setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            setStyle("-fx-alignment: center;");
        }
    }
}
