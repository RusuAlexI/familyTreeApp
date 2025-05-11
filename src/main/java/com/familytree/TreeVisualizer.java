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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TreeVisualizer {
    private final Pane canvas;
    private final Map<Person, VBox> personNodes = new HashMap<>();
    private Person selectedPerson;


    private final List<Line> connectionLines = new ArrayList<>();


    public TreeVisualizer() {
        this.canvas = new Pane();
        refresh();
    }


    public Parent getView() {
        return canvas;
    }

    public Person getSelectedPerson() {
        return selectedPerson;
    }

//    public void setData(FamilyTreeData loadedData) {
//        this.data = loadedData;
//        refresh();
//    }



    public void refresh() {
        System.out.println("Refreshing... persons count: " + FamilyTreeData.getInstance().getPersons().size());

        canvas.getChildren().clear();
        personNodes.clear();
        connectionLines.clear();


        // Find root ancestors (people without parents)
        List<Person> roots = FamilyTreeData.getInstance().getPersons().stream()
                .filter(p -> p.getParents().isEmpty())
                .collect(Collectors.toList());

        double startX = 50;
        double startY = 50;
        for (Person root : roots) {
            startX = layoutTree(root, startX, startY);
        }

        // Draw lines between parents and children
        for (Person parent : FamilyTreeData.getInstance().getPersons()) {
            VBox parentNode = personNodes.get(parent);
            if (parentNode == null) continue;

            for (Person child : getChildren(parent, FamilyTreeData.getInstance().getPersons())) {
                VBox childNode = personNodes.get(child);
                if (childNode == null) continue;

                double startXLine = parentNode.getLayoutX() + parentNode.getWidth() / 2;
                double startYLine = parentNode.getLayoutY() + parentNode.getHeight();

                double endXLine = childNode.getLayoutX() + childNode.getWidth() / 2;
                double endYLine = childNode.getLayoutY();

                Line line = new Line(startXLine, startYLine, endXLine, endYLine);
                canvas.getChildren().add(line);
            }
        }

        canvas.getChildren().addAll(personNodes.values());
    }

    private double layoutTree(Person person, double x, double y) {
        double boxWidth = 150;
        double boxHeight = 100;
        double spacingX = 40;
        double spacingY = 120;

        List<Person> children = getChildren(person, FamilyTreeData.getInstance().getPersons());
        if (children.isEmpty()) {
            VBox node = createPersonNode(person, x, y);
            personNodes.put(person, node);
            return x + boxWidth + spacingX;
        }

        double currentX = x;
        double midX = x;
        for (Person child : children) {
            currentX = layoutTree(child, currentX, y + spacingY);
        }

        midX = (x + currentX - spacingX) / 2;

        VBox node = createPersonNode(person, midX, y);
        personNodes.put(person, node);

        return currentX;
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
        for (Person person : FamilyTreeData.getInstance().getPersons()) {
            assignLevelRecursive(person, 0, levels);
        }
        return levels;
    }

    private void assignLevelRecursive(Person person, int level, Map<Person, Integer> levels) {
        if (levels.containsKey(person) && levels.get(person) <= level) {
            return;
        }
        levels.put(person, level);
        for (Person child : getChildren(person, FamilyTreeData.getInstance().getPersons())) {
            assignLevelRecursive(child, level + 1, levels);
        }
    }

    private void highlightSelected(VBox selectedBox) {
        for (VBox box : personNodes.values()) {
            box.setStyle("-fx-border-color: black; -fx-padding: 10; -fx-background-color: lightblue;");
        }
        selectedBox.setStyle("-fx-border-color: red; -fx-padding: 10; -fx-background-color: lightyellow;");
    }
    private List<Person> getChildren(Person person, List<Person> allPersons) {
        List<Person> children = new ArrayList<>();
        for (Person p : allPersons) {
            if (p.getParents().contains(person)) {
                children.add(p);
            }
        }
        return children;
    }

}