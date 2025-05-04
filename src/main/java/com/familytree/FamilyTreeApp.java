package com.familytree;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class FamilyTreeApp extends Application {

    private final FamilyTreeData data = FamilyTreeData.getInstance();
    private final TreeVisualizer visualizer = new TreeVisualizer();

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
        Button exportBtn = new Button("Export");
        Button importBtn = new Button("Import");

        toolBar.getItems().addAll(addButton, editButton, deleteButton, addRelationButton,exportBtn,importBtn);
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
            childDialog.getItems().addAll(FamilyTreeData.getInstance().getPersons());
            childDialog.setTitle("Select Child");
            childDialog.setHeaderText("Choose the child to link to " + parent.getName());
            childDialog.setContentText("Child:");

            Optional<Person> childOpt = childDialog.showAndWait();
            childOpt.ifPresent(child -> {
                child.addParent(parent);
                visualizer.refresh();
            });
        });

        exportBtn.setOnAction(e -> {
            System.out.println("Exporting persons:");
            for (Person p : FamilyTreeData.getInstance().getPersons()) {
                System.out.println(p.getName()); // or a toString() implementation
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Family Tree");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.enable(SerializationFeature.INDENT_OUTPUT);
                    mapper.writeValue(file, FamilyTreeData.getInstance());
                    System.out.println("Export successful.");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        importBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load Family Tree");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    debugLoadTest(file); // Just add this before FamilyTreeIO.loadFromFile()

                    FamilyTreeData loadedData = FamilyTreeIO.loadFromFile(file);
                    FamilyTreeData.setInstance(loadedData); // replace current data
                    visualizer.refresh();
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

    public static void debugLoadTest(File file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            FamilyTreeData loaded = mapper.readValue(file, FamilyTreeData.class);
            System.out.println(">>> DEBUG direct load: " + loaded);
            System.out.println(">>> DEBUG person count: " + (loaded.getPersons() == null ? "null" : loaded.getPersons().size()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}