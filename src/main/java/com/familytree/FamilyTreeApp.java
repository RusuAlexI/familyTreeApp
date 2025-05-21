package com.familytree;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class FamilyTreeApp extends Application {

    private final FamilyTreeData data = FamilyTreeData.getInstance();
    private final FamilyTreePane visualizer = new FamilyTreePane();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Family Tree Application");

        BorderPane root = new BorderPane();

        ScrollPane scrollPane = new ScrollPane(visualizer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        StackPane stack = new StackPane(scrollPane);
        root.setCenter(stack);
        visualizer.setPrefSize(2000, 1000);

        // Top menu bar
        ToolBar toolBar = new ToolBar();

        Button addButton = new Button("Add Person");
        Button editButton = new Button("Edit Person");
        Button deleteButton = new Button("Delete Person");
        Button addRelationButton = new Button("Add Parent-Child");
        Button exportBtn = new Button("Export");
        Button importBtn = new Button("Import");

        // Theme selector
        ComboBox<String> themeSelector = new ComboBox<>();
        themeSelector.getItems().addAll("Default", "Parchment", "Tree");
        themeSelector.setValue("Default");
        themeSelector.setOnAction(e -> visualizer.applyTheme(themeSelector.getValue()));

        toolBar.getItems().addAll(addButton, editButton, deleteButton, addRelationButton, exportBtn, importBtn, new Label(" Theme:"), themeSelector);
        root.setTop(toolBar);

        addButton.setOnAction(e -> {
            PersonDialog dialog = new PersonDialog(null);
            Optional<Person> result = dialog.showAndWait();
            result.ifPresent(person -> {
                data.addPerson(person);
                visualizer.drawTree(data.getPersons());
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
                visualizer.drawTree(data.getPersons());
            });
        });

        deleteButton.setOnAction(e -> {
            Person selected = visualizer.getSelectedPerson();
            if (selected == null) {
                showAlert("Please select a person to delete.");
                return;
            }
            data.removePerson(selected);
            visualizer.drawTree(data.getPersons());
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
                visualizer.drawTree(data.getPersons());
            });
        });

        exportBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Family Tree");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.enable(SerializationFeature.INDENT_OUTPUT);
                    mapper.writeValue(file, data);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        importBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import Family Tree");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    FamilyTreeData loaded = mapper.readValue(selectedFile, FamilyTreeData.class);

                    data.getPersons().clear();
                    data.getPersons().addAll(loaded.getPersons());

                    visualizer.drawTree(data.getPersons());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
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
