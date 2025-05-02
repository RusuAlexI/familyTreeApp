package com.familytree;

import javafx.scene.Parent;
import javafx.scene.control.Label;
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

        double x = 50;
        double y = 50;
        for (Person person : data.getPersons()) {
            VBox node = createPersonNode(person, x, y);
            personNodes.put(person, node);
            canvas.getChildren().add(node);
            x += 200;
        }

        for (Person child : data.getPersons()) {
            for (Person parent : child.getParents()) {
                VBox parentNode = personNodes.get(parent);
                VBox childNode = personNodes.get(child);
                if (parentNode != null && childNode != null) {
                    Line line = new Line();
                    line.setStartX(parentNode.getLayoutX() + 75);
                    line.setStartY(parentNode.getLayoutY() + 75);
                    line.setEndX(childNode.getLayoutX() + 75);
                    line.setEndY(childNode.getLayoutY());
                    canvas.getChildren().add(line);
                }
            }
        }
    }

    private VBox createPersonNode(Person person, double x, double y) {
        VBox box = new VBox();
        box.setLayoutX(x);
        box.setLayoutY(y);
        box.setSpacing(5);
        box.setStyle("-fx-border-color: black; -fx-padding: 10; -fx-background-color: lightblue;");

        Label nameLabel = new Label("Name: " + person.getName());
        Label dobLabel = new Label("Born: " + person.getDateOfBirth());
        Label dodLabel = new Label("Died: " + (person.getDateOfDeath() == null ? "N/A" : person.getDateOfDeath()));
        Label genderLabel = new Label("Gender: " + person.getGender());

        box.getChildren().addAll(nameLabel, dobLabel, dodLabel, genderLabel);

        box.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            selectedPerson = person;
            highlightSelected(box);
        });

        return box;
    }

    private void highlightSelected(VBox selectedBox) {
        for (VBox box : personNodes.values()) {
            box.setStyle("-fx-border-color: black; -fx-padding: 10; -fx-background-color: lightblue;");
        }
        selectedBox.setStyle("-fx-border-color: red; -fx-padding: 10; -fx-background-color: lightyellow;");
    }
}