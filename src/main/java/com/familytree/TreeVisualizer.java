package com.familytree;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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
        selectedBox.getChildren().forEach(node -> {
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