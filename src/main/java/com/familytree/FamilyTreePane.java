package com.familytree;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FamilyTreePane extends Pane {

    private Person selectedPerson;
    private Pane lineLayer = new Pane(); // for drawing lines

    public FamilyTreePane() {
        setPadding(new Insets(20));
        setPrefWidth(800); // or however wide you want it
        setPrefHeight(600);
        setStyle("-fx-background-color: #f4f4f4;");
        getChildren().add(lineLayer); // add before any nodes
    }

    public void drawTree(List<Person> persons) {
        getChildren().removeIf(node -> node != lineLayer);
        lineLayer.getChildren().clear();

        Map<Person, VBox> nodeMap = new HashMap<>();
        double x = 50, y = 50, spacingX = 200, spacingY = 200;

        for (Person person : persons) {
            VBox node = createPersonNode(person);
            node.setLayoutX(x);
            node.setLayoutY(y);
            getChildren().add(node);
            nodeMap.put(person, node);

            x += spacingX;
            if (x > getPrefWidth() - 200) {
                x = 50;
                y += spacingY;
            }
        }

        drawLinesBetweenParentsAndChildren(nodeMap);
    }

    private void drawLinesBetweenParentsAndChildren(Map<Person, VBox> nodeMap) {
        for (Map.Entry<Person, VBox> entry : nodeMap.entrySet()) {
            Person child = entry.getKey();
            VBox childNode = entry.getValue();

            for (Person parent : child.getParents()) {
                VBox parentNode = nodeMap.get(parent);
                if (parentNode != null) {
                    Line line = createConnectionLine(parentNode, childNode);
                    lineLayer.getChildren().add(line);
                    System.out.println("Drawing lines...");
                    System.out.println("Child: " + child.getName() + ", Parents: " + child.getParents().size());
                }
            }
        }
    }

    private Line createConnectionLine(VBox parentNode, VBox childNode) {
        double startX = parentNode.getLayoutX() + parentNode.getWidth() / 2;
        double startY = parentNode.getLayoutY() + parentNode.getHeight();
        double endX = childNode.getLayoutX() + childNode.getWidth() / 2;
        double endY = childNode.getLayoutY();

        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(Color.GRAY);
        line.setStrokeWidth(2);
        return line;
    }

    private Map<Person, Integer> assignLevels(List<Person> persons) {
        Map<Person, Integer> levels = new HashMap<>();

        for (Person person : persons) {
            if (person.getParents().isEmpty()) {
                assignLevelRecursive(person, 0, levels);
            }
        }

        return levels;
    }

    private void assignLevelRecursive(Person person, int level, Map<Person, Integer> levels) {
        if (levels.containsKey(person) && levels.get(person) <= level) {
            return; // already assigned with same or higher level
        }
        levels.put(person, level);

        for (Person child : getChildrenOf(person)) {
            assignLevelRecursive(child, level + 1, levels);
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

    private VBox createPersonNode(Person person) {
        VBox box = new VBox();
        box.setPadding(new Insets(10));
        box.setSpacing(5);
        box.setPrefWidth(140);
        box.setStyle("""
        -fx-background-color: white;
        -fx-border-radius: 10;
        -fx-background-radius: 10;
        -fx-border-color: #444;
        -fx-border-width: 1;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0.2, 2, 2);
    """);

        // Optional: profile image placeholder
        Circle profileCircle = new Circle(30, Color.LIGHTGRAY);
        profileCircle.setStroke(Color.GRAY);
        profileCircle.setStrokeWidth(1);
        String bgColor = switch (person.getGender() != null ? person.getGender().toLowerCase() : "") {
            case "male" -> "#cce5ff";
            case "female" -> "#f8d7da";
            default -> "#f0f0f0";
        };
        box.setStyle("-fx-border-color: #333; -fx-background-color: " + bgColor + ";");
        box.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(4), BorderWidths.DEFAULT)));
        box.setPrefWidth(120);
        Label initials = new Label(person.getName().length() > 0 ?
                person.getName().substring(0, 1).toUpperCase() : "?");
        initials.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        StackPane profileStack = new StackPane(profileCircle, initials);
        profileStack.setPadding(new Insets(5));
        profileStack.setMaxSize(60, 60);

        // Text info
        Label nameLabel = new Label(person.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label dobLabel = new Label("Born: " + person.getDateOfBirth());
        Label dodLabel = new Label("Died: " + person.getDateOfDeath());
        Label genderLabel = new Label("Gender: " + person.getGender());

        box.getChildren().addAll(profileStack, nameLabel, dobLabel, dodLabel, genderLabel);

        // Highlight on click
        box.setOnMouseClicked((MouseEvent e) -> {
            this.selectedPerson = person;
            highlightSelected(box);
        });

        // Hover effect
        box.setOnMouseEntered(e -> box.setStyle(box.getStyle() + "-fx-background-color: #f0f8ff;"));
        box.setOnMouseExited(e -> box.setStyle(box.getStyle().replace("-fx-background-color: #f0f8ff;", "-fx-background-color: white;")));

        return box;
    }

    private void highlightSelected(VBox selectedBox) {
        getChildren().forEach(node -> {
            if (node instanceof VBox) {
                ((VBox) node).setStyle(((VBox) node).getStyle()
                        .replace("-fx-background-color: #e0f0ff;", "-fx-background-color: white;")
                        .replace("-fx-border-color: blue;", "-fx-border-color: #444;"));
            }
        });

        selectedBox.setStyle(selectedBox.getStyle()
                .replace("-fx-background-color: white;", "-fx-background-color: #e0f0ff;")
                .replace("-fx-border-color: #444;", "-fx-border-color: blue;"));
    }
    private void drawConnections(List<Person> persons, Map<Person, VBox> personNodes) {
        for (Person child : persons) {
            List<Person> parents = child.getParents();
            if (parents.size() == 1) {
                VBox parentBox = personNodes.get(parents.get(0));
                VBox childBox = personNodes.get(child);
                if (parentBox != null && childBox != null) {
                    Line line = createLineBetween(parentBox, childBox);
                    getChildren().add(line);
                }
            } else if (parents.size() == 2) {
                VBox parent1Box = personNodes.get(parents.get(0));
                VBox parent2Box = personNodes.get(parents.get(1));
                VBox childBox = personNodes.get(child);
                if (parent1Box != null && parent2Box != null && childBox != null) {
                    Line line = createLineFromTwoParents(parent1Box, parent2Box, childBox);
                    getChildren().add(line);
                }
            }
        }
    }

    private Line createLineBetween(VBox parentBox, VBox childBox) {
        double startX = parentBox.getLayoutX() + parentBox.getWidth() / 2;
        double startY = parentBox.getLayoutY() + parentBox.getHeight();
        double endX = childBox.getLayoutX() + childBox.getWidth() / 2;
        double endY = childBox.getLayoutY();

        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(2);
        return line;
    }

    private Line createLineFromTwoParents(VBox parent1Box, VBox parent2Box, VBox childBox) {
        double parent1CenterX = parent1Box.getLayoutX() + parent1Box.getWidth() / 2;
        double parent1BottomY = parent1Box.getLayoutY() + parent1Box.getHeight();
        double parent2CenterX = parent2Box.getLayoutX() + parent2Box.getWidth() / 2;
        double parent2BottomY = parent2Box.getLayoutY() + parent2Box.getHeight();

        double startX = (parent1CenterX + parent2CenterX) / 2;
        double startY = (parent1BottomY + parent2BottomY) / 2;

        double endX = childBox.getLayoutX() + childBox.getWidth() / 2;
        double endY = childBox.getLayoutY();

        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(Color.DARKGRAY);
        line.setStrokeWidth(2);
        return line;
    }

    public Person getSelectedPerson() {
        return selectedPerson;
    }

    public void clearSelection() {
        this.selectedPerson = null;
    }
}
