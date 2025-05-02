package com.familytree;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.Optional;

public class FamilyTreeApp extends Application {

    private final FamilyTreeData data = FamilyTreeData.getInstance();
    private final TreeVisualizer visualizer = new TreeVisualizer(data);

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Family Tree Application");

        BorderPane root = new BorderPane();
        root.setCenter(visualizer.getView());

        // Top menu bar
        ToolBar toolBar = new ToolBar();

        Button addButton = new Button("Add Person");
        Button editButton = new Button("Edit Person");
        Button deleteButton = new Button("Delete Person");
        Button addRelationButton = new Button("Add Parent-Child");

        toolBar.getItems().addAll(addButton, editButton, deleteButton, addRelationButton);
        root.setTop(toolBar);

        addButton.setOnAction(e -> {
            PersonDialog dialog = new PersonDialog(null);
            Optional<Person> result = dialog.showAndWait();
            result.ifPresent(person -> {
                data.addPerson(person);
                visualizer.refresh();
            });
        });

        editButton.setOnAction(e -> {
            Person selected = visualizer.getSelectedPerson();
            if (selected == null) {
                showAlert("Please select a person to edit.");
                return;
            }
            PersonDialog dialog = new PersonDialog(selected);
            Optional<Person> result = dialog.showAndWait();
            result.ifPresent(updated -> {
                selected.setName(updated.getName());
                selected.setDateOfBirth(updated.getDateOfBirth());
                selected.setDateOfDeath(updated.getDateOfDeath());
                selected.setGender(updated.getGender());
                visualizer.refresh();
            });
        });

        deleteButton.setOnAction(e -> {
            Person selected = visualizer.getSelectedPerson();
            if (selected == null) {
                showAlert("Please select a person to delete.");
                return;
            }
            data.removePerson(selected);
            visualizer.refresh();
        });

        addRelationButton.setOnAction(e -> {
            Person parent = visualizer.getSelectedPerson();
            if (parent == null) {
                showAlert("Please select the parent person first.");
                return;
            }

            ChoiceDialog<Person> childDialog = new ChoiceDialog<>();
            childDialog.getItems().addAll(data.getPersons());
            childDialog.setTitle("Select Child");
            childDialog.setHeaderText("Choose the child to link to " + parent.getName());
            childDialog.setContentText("Child:");

            Optional<Person> childOpt = childDialog.showAndWait();
            childOpt.ifPresent(child -> {
                child.addParent(parent);
                visualizer.refresh();
            });
        });

        primaryStage.setScene(new Scene(root, 1000, 600));
        primaryStage.show();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}