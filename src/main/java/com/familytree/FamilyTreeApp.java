package com.familytree;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class FamilyTreeApp extends Application {

    private ListView<Person> listView;
    private TreeVisualizer treeVisualizer;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Family Tree Creator");

        // Left side: ListView and Buttons
        VBox leftPane = new VBox(10);
        leftPane.setPadding(new Insets(10));

        listView = new ListView<>();
        listView.getItems().addAll(FamilyTreeData.getInstance().getPersons());

        Button addButton = new Button("Add Person");
        Button editButton = new Button("Edit Person");
        Button deleteButton = new Button("Delete Person");

        leftPane.getChildren().addAll(listView, addButton, editButton, deleteButton);

        // Center: TreeVisualizer
        treeVisualizer = new TreeVisualizer();
        ScrollPane scrollPane = new ScrollPane(treeVisualizer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        HBox root = new HBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(leftPane, scrollPane);

        Scene scene = new Scene(root, 1400, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Button actions
        addButton.setOnAction(e -> showAddPersonDialog());
        editButton.setOnAction(e -> showEditPersonDialog());
        deleteButton.setOnAction(e -> deleteSelectedPerson());

        // Double-click to edit
        listView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                showEditPersonDialog();
            }
        });
    }

    private void refreshList() {
        listView.getItems().setAll(FamilyTreeData.getInstance().getPersons());
    }

    private void showAddPersonDialog() {
        Dialog<Person> dialog = new Dialog<>();
        dialog.setTitle("Add New Person");

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();
        Label dobLabel = new Label("Date of Birth (yyyy-mm-dd):");
        TextField dobField = new TextField();
        Label dodLabel = new Label("Date of Death (optional, yyyy-mm-dd):");
        TextField dodField = new TextField();
        Label genderLabel = new Label("Gender:");
        TextField genderField = new TextField();

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

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new Person(nameField.getText(), dobField.getText(), dodField.getText(), genderField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(person -> {
            treeVisualizer.addPerson(person);
            refreshList();
        });
    }

    private void showEditPersonDialog() {
        Person selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Dialog<Person> dialog = new Dialog<>();
        dialog.setTitle("Edit Person");

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField(selected.getName());
        Label dobLabel = new Label("Date of Birth (yyyy-mm-dd):");
        TextField dobField = new TextField(selected.getDateOfBirth());
        Label dodLabel = new Label("Date of Death (optional, yyyy-mm-dd):");
        TextField dodField = new TextField(selected.getDateOfDeath());
        Label genderLabel = new Label("Gender:");
        TextField genderField = new TextField(selected.getGender());

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

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                selected.setName(nameField.getText());
                selected.setDateOfBirth(dobField.getText());
                selected.setDateOfDeath(dodField.getText());
                selected.setGender(genderField.getText());
                return selected;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(person -> {
            treeVisualizer.editPerson(person);
            refreshList();
        });
    }

    private void deleteSelectedPerson() {
        Person selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        treeVisualizer.deletePerson(selected);
        refreshList();
    }

    public static void main(String[] args) {
        launch(args);
    }
}