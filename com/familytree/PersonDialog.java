package com.familytree;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class PersonDialog extends Dialog<Person> {
    public PersonDialog() {
        setTitle("Add New Person");

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();
        Label dobLabel = new Label("Date of Birth:");
        TextField dobField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(dobLabel, 0, 1);
        grid.add(dobField, 1, 1);

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Person(nameField.getText(), dobField.getText());
            }
            return null;
        });
    }
}