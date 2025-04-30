package com.familytree;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.UUID;

public class FamilyTreeApp extends Application {

    private ListView<Person> personListView;
    private TreeVisualizer treeVisualizer;



    @Override
    public void start(Stage primaryStage) {
        FamilyTreeData.getInstance().loadData(); // Load JSON data

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        personListView = new ListView<>();
        personListView.getItems().addAll(FamilyTreeData.getInstance().getPersons());
        personListView.setPrefWidth(200);

        treeVisualizer = new TreeVisualizer();
        treeVisualizer.drawTree(FamilyTreeData.getInstance().getPersons());

        VBox controls = new VBox(10);
        controls.setPadding(new Insets(10));

        Button addRelationshipButton = new Button("Add Relationship");
        addRelationshipButton.setOnAction(e -> openRelationshipDialog());

        Button addButton = new Button("Add Person");
        addButton.setOnAction(e -> {
            showPersonDialog(null);
        });

        Button editButton = new Button("Edit Selected");
        editButton.setOnAction(e -> {
            Person selected = personListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showPersonDialog(selected);
            }
        });

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            Person selected = personListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                FamilyTreeData.getInstance().removePerson(selected);
                refreshView();
            }
        });

        controls.getChildren().addAll(addButton, editButton, deleteButton);

        root.setLeft(new VBox(new Label("Persons:"), personListView, controls));
        root.setCenter(treeVisualizer);

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("Family Tree Builder");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openRelationshipDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Relationship");

        ComboBox<Person> fromBox = new ComboBox<>(FXCollections.observableArrayList(FamilyTreeData.getInstance().getPersons()));
        ComboBox<Person> toBox = new ComboBox<>(FXCollections.observableArrayList(FamilyTreeData.getInstance().getPersons()));
        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList("Parent", "Spouse"));

        fromBox.setPromptText("From");
        toBox.setPromptText("To");
        typeBox.setPromptText("Type");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("From:"), 0, 0);
        grid.add(fromBox, 1, 0);
        grid.add(new Label("To:"), 0, 1);
        grid.add(toBox, 1, 1);
        grid.add(new Label("Type:"), 0, 2);
        grid.add(typeBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Person from = fromBox.getValue();
                Person to = toBox.getValue();
                String type = typeBox.getValue();

                if (from != null && to != null && type != null) {
                    FamilyTreeData.getInstance().addRelationship(new Relationship(from.getId(), to.getId(), type));
                    refreshView(); // Refresh visualization
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showPersonDialog(Person person) {
        Dialog<Person> dialog = new Dialog<>();
        dialog.setTitle(person == null ? "Add Person" : "Edit Person");

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();
        Label dobLabel = new Label("Date of Birth:");
        TextField dobField = new TextField();
        Label dodLabel = new Label("Date of Death:");
        TextField dodField = new TextField();
        Label genderLabel = new Label("Gender:");
        TextField genderField = new TextField();

        if (person != null) {
            nameField.setText(person.getName());
            dobField.setText(person.getDateOfBirth());
            dodField.setText(person.getDateOfDeath());
            genderField.setText(person.getGender());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(dobLabel, 0, 1);
        grid.add(dobField, 1, 1);
        grid.add(dodLabel, 0, 2);
        grid.add(dodField, 1, 2);
        grid.add(genderLabel, 0, 3);
        grid.add(genderField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String id = person == null ? UUID.randomUUID().toString() : person.getId();
                return new Person(id, nameField.getText(), dobField.getText(), dodField.getText(), genderField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (person == null) {
                FamilyTreeData.getInstance().addPerson(result);
            } else {
                person.setName(result.getName());
                person.setDateOfBirth(result.getDateOfBirth());
                person.setDateOfDeath(result.getDateOfDeath());
                person.setGender(result.getGender());
            }
            refreshView();
        });
    }

    private void refreshView() {
        personListView.getItems().setAll(FamilyTreeData.getInstance().getPersons());
        treeVisualizer.drawTree(FamilyTreeData.getInstance().getPersons());
        FamilyTreeData.getInstance().saveData(); // Save to JSON
    }

    public static void main(String[] args) {
        launch(args);
    }
}