package com.familytree;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.StageStyle;
import java.util.Optional;

public class RelationshipDialog extends Dialog<RelationshipDialog.RelationshipResult> {

    // A record to hold the result of the dialog
    public record RelationshipResult(
            RelationshipType type,
            Person parent,
            Person child,
            Person spouse1,
            Person spouse2
    ) {}

    // An enum for the relationship types
    public enum RelationshipType {
        PARENT_CHILD, SPOUSE
    }

    public RelationshipDialog(Person person1, Person person2) {
        setTitle("Set Family Relationship");
        setHeaderText("Define the relationship between " + person1.getName() + " and " + person2.getName());
        initStyle(StageStyle.UTILITY); // This makes the dialog modal

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ToggleGroup group = new ToggleGroup();

        // RadioButton for Parent-Child
        RadioButton parentChildRadio = new RadioButton("Parent-Child");
        parentChildRadio.setToggleGroup(group);

        // ComboBoxes for Parent-Child
        ComboBox<Person> parentComboBox = new ComboBox<>();
        parentComboBox.getItems().addAll(person1, person2);
        parentComboBox.getSelectionModel().select(0);

        Label parentLabel = new Label("Parent:");
        Label childLabel = new Label("Child:");

        grid.add(parentChildRadio, 0, 0, 2, 1);
        grid.add(parentLabel, 0, 1);
        grid.add(parentComboBox, 1, 1);

        // This is a bit of a hack to get the child combo box to automatically update
        ComboBox<Person> childComboBox = new ComboBox<>();
        childComboBox.getItems().addAll(parentComboBox.getItems());
        parentComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            childComboBox.getItems().clear();
            if (newVal.equals(person1)) {
                childComboBox.getItems().add(person2);
            } else {
                childComboBox.getItems().add(person1);
            }
            childComboBox.getSelectionModel().selectFirst();
        });
        childComboBox.getSelectionModel().selectFirst();

        grid.add(childLabel, 0, 2);
        grid.add(childComboBox, 1, 2);


        // RadioButton for Spouse
        RadioButton spouseRadio = new RadioButton("Spouse");
        spouseRadio.setToggleGroup(group);

        Label spouseLabel1 = new Label("Spouse 1:");
        Label spouseLabel2 = new Label("Spouse 2:");

        ComboBox<Person> spouseComboBox1 = new ComboBox<>();
        spouseComboBox1.getItems().addAll(person1, person2);
        spouseComboBox1.getSelectionModel().select(0);

        ComboBox<Person> spouseComboBox2 = new ComboBox<>();
        spouseComboBox2.getItems().addAll(person1, person2);
        spouseComboBox2.getSelectionModel().selectLast();

        grid.add(spouseRadio, 0, 4, 2, 1);
        grid.add(spouseLabel1, 0, 5);
        grid.add(spouseComboBox1, 1, 5);
        grid.add(spouseLabel2, 0, 6);
        grid.add(spouseComboBox2, 1, 6);


        // Enable/disable combos based on radio button selection
        parentChildRadio.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            parentComboBox.setDisable(!isSelected);
            childComboBox.setDisable(!isSelected);
        });
        spouseRadio.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            spouseComboBox1.setDisable(!isSelected);
            spouseComboBox2.setDisable(!isSelected);
        });

        // Select one radio button by default
        parentChildRadio.setSelected(true);


        getDialogPane().setContent(grid);

        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                if (parentChildRadio.isSelected()) {
                    return new RelationshipResult(
                            RelationshipType.PARENT_CHILD,
                            parentComboBox.getValue(),
                            childComboBox.getValue(),
                            null,
                            null
                    );
                } else if (spouseRadio.isSelected()) {
                    return new RelationshipResult(
                            RelationshipType.SPOUSE,
                            null,
                            null,
                            spouseComboBox1.getValue(),
                            spouseComboBox2.getValue()
                    );
                }
            }
            return null;
        });
    }
}