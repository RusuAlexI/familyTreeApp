package com.familytree;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

import java.util.*;

public class FamilyTreePaneGood extends Pane {
    private Person selectedPerson;
    private final Map<Person, VBox> personNodes = new HashMap<>();
    private final double horizontalSpacing = 200;
    private final double verticalSpacing = 100;
    private final double nodeWidth = 160;
    private final double nodeHeight = 80;

    public FamilyTreePaneGood() {
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #f4f4f4;");
    }

    public void drawTree(List<Person> persons) {
        getChildren().clear();
        personNodes.clear();

        if (persons.isEmpty()) return;

        // Create all nodes first
        for (Person p : persons) {
            VBox node = createPersonNode(p);
            personNodes.put(p, node);
            getChildren().add(node);
        }

        // Calculate positions and draw connections
        layoutTree(persons);
    }

    private VBox createPersonNode(Person person) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(8));
        box.setPrefSize(nodeWidth, nodeHeight);
        box.setMinSize(nodeWidth, nodeHeight);
        box.setStyle("-fx-background-color: white; -fx-border-color: darkgray; -fx-border-radius: 5;");

        Label nameLabel = new Label(person.getName());
        nameLabel.setFont(Font.font("Arial", 14));
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label dobLabel = new Label("Born: " + person.getDateOfBirth());
        dobLabel.setFont(Font.font("Arial", 10));

        box.getChildren().addAll(nameLabel, dobLabel);

        box.setOnMouseClicked((MouseEvent e) -> {
            selectedPerson = person;
            highlightSelected(box);
            e.consume();
        });

        return box;
    }

    private void layoutTree(List<Person> persons) {
        // Find root nodes (people without parents)
        List<Person> roots = new ArrayList<>();
        for (Person p : persons) {
            if (p.getParents().isEmpty()) {
                roots.add(p);
            }
        }

        // Position roots at the top center
        double startY = 20;
        double startX = (getWidth() - (roots.size() * horizontalSpacing)) / 2;

        // Map to track positions
        Map<Person, Double> xPositions = new HashMap<>();
        Map<Person, Double> yPositions = new HashMap<>();

        // Position roots
        for (Person root : roots) {
            xPositions.put(root, startX);
            yPositions.put(root, startY);
            startX += horizontalSpacing;
        }

        // Position children using BFS
        Queue<Person> queue = new LinkedList<>(roots);
        while (!queue.isEmpty()) {
            Person current = queue.poll();
            VBox node = personNodes.get(current);
            if (node != null) {
                node.setLayoutX(xPositions.get(current));
                node.setLayoutY(yPositions.get(current));
            }

            List<Person> children = getChildrenOf(current);
            if (!children.isEmpty()) {
                double childY = yPositions.get(current) + verticalSpacing;
                double totalWidth = children.size() * horizontalSpacing;
                double childStartX = xPositions.get(current) - (totalWidth/2) + (horizontalSpacing/2);

                for (Person child : children) {
                    xPositions.put(child, childStartX);
                    yPositions.put(child, childY);
                    childStartX += horizontalSpacing;
                    queue.add(child);
                }
            }
        }

        // Draw connections after all nodes are positioned
        drawConnections(xPositions, yPositions);
    }

    private void drawConnections(Map<Person, Double> xPositions, Map<Person, Double> yPositions) {
        // First remove any existing lines
        getChildren().removeIf(node -> node instanceof Line);

        for (Map.Entry<Person, VBox> entry : personNodes.entrySet()) {
            Person child = entry.getKey();
            VBox childBox = entry.getValue();

            for (Person parent : child.getParents()) {
                VBox parentBox = personNodes.get(parent);
                if (parentBox != null) {
                    // Calculate connection points
                    double startX = xPositions.get(parent) + nodeWidth/2;
                    double startY = yPositions.get(parent) + nodeHeight;
                    double endX = xPositions.get(child) + nodeWidth/2;
                    double endY = yPositions.get(child);

                    Line line = new Line(startX, startY, endX, endY);
                    line.setStroke(Color.DARKGRAY);
                    line.setStrokeWidth(1.5);
                    getChildren().add(0, line); // Add to back
                }
            }
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

    private void highlightSelected(VBox selectedBox) {
        for (Node node : getChildren()) {
            if (node instanceof VBox) {
                node.setStyle("-fx-background-color: white; -fx-border-color: darkgray;");
            }
        }
        selectedBox.setStyle("-fx-background-color: #e6f3ff; -fx-border-color: #0066cc;");
    }

    public Person getSelectedPerson() {
        return selectedPerson;
    }
}