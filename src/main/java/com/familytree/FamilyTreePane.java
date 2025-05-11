package com.familytree;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FamilyTreePane extends Pane {

    private Person selectedPerson;

    public FamilyTreePane() {
        setPadding(new Insets(20));
        setPrefWidth(800); // or however wide you want it
        setPrefHeight(600);
        setStyle("-fx-background-color: #f4f4f4;");
    }

    public void drawTree(List<Person> persons) {
        getChildren().clear();
        double x = 50;
        double y = 50;
        double spacing = 150;

        Map<Person, VBox> personNodes = new HashMap<>();

        // Step 1: Draw nodes
        for (Person person : persons) {
            VBox personNode = createPersonNode(person);
            personNode.setLayoutX(x);
            personNode.setLayoutY(y);
            getChildren().add(personNode);

            personNodes.put(person, personNode);

            x += spacing;
            if (x > 800) {
                x = 50;
                y += spacing;
            }
        }

        // Step 2: Draw lines from parents to children
        for (Person child : persons) {
            VBox childNode = personNodes.get(child);
            if (childNode == null) continue;

            for (Person parent : child.getParents()) {
                VBox parentNode = personNodes.get(parent);
                if (parentNode == null) continue;

                double startX = parentNode.getLayoutX() + parentNode.getWidth() / 2;
                double startY = parentNode.getLayoutY() + parentNode.getHeight();

                double endX = childNode.getLayoutX() + childNode.getWidth() / 2;
                double endY = childNode.getLayoutY();

                Line line = new Line(startX, startY, endX, endY);
                line.setStroke(Color.GRAY);
                line.setStrokeWidth(2);
                getChildren().add(line);
            }
        }
    }

    private VBox createPersonNode(Person person) {
        VBox box = new VBox();
        box.setPadding(new Insets(10));
        box.setSpacing(5);
        String bgColor = switch (person.getGender() != null ? person.getGender().toLowerCase() : "") {
            case "male" -> "#cce5ff";
            case "female" -> "#f8d7da";
            default -> "#f0f0f0";
        };
        box.setStyle("-fx-border-color: #333; -fx-background-color: " + bgColor + ";");
        box.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(4), BorderWidths.DEFAULT)));
        box.setPrefWidth(120);

        Label nameLabel = new Label("Name: " + person.getName());
        Label dobLabel = new Label("Born: " + person.getDateOfBirth());
        Label dodLabel = new Label("Died: " + person.getDateOfDeath());
        Label genderLabel = new Label("Gender: " + person.getGender());

        box.getChildren().addAll(nameLabel, dobLabel, dodLabel, genderLabel);

        box.setOnMouseClicked((MouseEvent e) -> {
            this.selectedPerson = person;
            highlightSelected(box);
        });

        return box;
    }

    private void highlightSelected(VBox selectedBox) {
        getChildren().forEach(node -> {
            if (node instanceof VBox) {
                ((VBox) node).setStyle("-fx-border-color: #333; -fx-background-color: #fff;");
            }
        });

        selectedBox.setStyle("-fx-border-color: blue; -fx-background-color: #e0f0ff;");
    }

    public Person getSelectedPerson() {
        return selectedPerson;
    }

    public void clearSelection() {
        this.selectedPerson = null;
    }
}
