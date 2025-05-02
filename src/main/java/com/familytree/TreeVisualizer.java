package com.familytree;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.HashMap;
import java.util.Map;

public class TreeVisualizer {
    private final FamilyTreeData data;
    private final Pane canvas;
    private final Map<Person, VBox> personNodes = new HashMap<>();
    private Person selectedPerson;

    public TreeVisualizer(FamilyTreeData data) {
        this.data = data;
        this.canvas = new Pane();
        refresh();
    }

    public Parent getView() {
        return canvas;
    }

    public Person getSelectedPerson() {
        return selectedPerson;
    }

    public void refresh() {
        canvas.getChildren().clear();
        personNodes.clear();

        Map<Person, Integer> levels = assignGenerations();
        Map<Integer, Double> levelY = new HashMap<>();
        double baseY = 50;
        double levelHeight = 150;

        for (Integer level : levels.values()) {
            levelY.put(level, baseY + level * levelHeight);
        }

        Map<Integer, Integer> levelCounts = new HashMap<>();

        for (Person person : data.getPersons()) {
            int level = levels.getOrDefault(person, 0);
            int index = levelCounts.getOrDefault(level, 0);
            double x = 100 + index * 180;
            double y = levelY.get(level);

            VBox node = createPersonNode(person, x, y);
            personNodes.put(person, node);
            canvas.getChildren().add(node);

            levelCounts.put(level, index + 1);
        }

        for (Person child : data.getPersons()) {
            for (Person parent : child.getParents()) {
                VBox parentNode = personNodes.get(parent);
                VBox childNode = personNodes.get(child);
                if (parentNode != null && childNode != null) {
                    Line line = new Line();
                    line.setStartX(parentNode.getLayoutX() + 75);
                    line.setStartY(parentNode.getLayoutY() + 100); // bottom of parent
                    line.setEndX(childNode.getLayoutX() + 75);
                    line.setEndY(childNode.getLayoutY()); // top of child
                    line.setStroke(Color.GRAY);
                    canvas.getChildren().add(line);
                }
            }
        }
    }
    private VBox createPersonNode(Person person, double x, double y) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: black; -fx-background-color: lightblue;");
        box.setAlignment(Pos.CENTER);
        box.setLayoutX(x);
        box.setLayoutY(y);
        box.setPrefWidth(150);

        Label nameLabel = new Label("Name: " + person.getName());
        Label dobLabel = new Label("Born: " + person.getDateOfBirth());
        Label dodLabel = new Label("Died: " + (person.getDateOfDeath() != null ? person.getDateOfDeath() : "N/A"));
        Label genderLabel = new Label("Gender: " + (person.getGender() != null ? person.getGender() : "Unknown"));

        box.getChildren().addAll(nameLabel, dobLabel, dodLabel, genderLabel);

        box.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                selectedPerson = person;
            }
        });

        return box;
    }


    private Map<Person, Integer> assignGenerations() {
        Map<Person, Integer> levels = new HashMap<>();
        for (Person person : data.getPersons()) {
            assignLevelRecursive(person, 0, levels);
        }
        return levels;
    }

    private void assignLevelRecursive(Person person, int level, Map<Person, Integer> levels) {
        if (levels.containsKey(person) && levels.get(person) <= level) {
            return;
        }
        levels.put(person, level);
        for (Person child : person.getChildren()) {
            assignLevelRecursive(child, level + 1, levels);
        }
    }

    private void highlightSelected(VBox selectedBox) {
        for (VBox box : personNodes.values()) {
            box.setStyle("-fx-border-color: black; -fx-padding: 10; -fx-background-color: lightblue;");
        }
        selectedBox.setStyle("-fx-border-color: red; -fx-padding: 10; -fx-background-color: lightyellow;");
    }
}