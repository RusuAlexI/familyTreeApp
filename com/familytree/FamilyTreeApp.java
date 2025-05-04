package com.familytree;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class FamilyTreeApp extends Application {
    private TreeVisualizer visualizer;

    @Override
    public void start(Stage primaryStage) {
        FamilyTreeData data = FamilyTreeData.getInstance();
        visualizer = new TreeVisualizer(data);

        Button addButton = new Button("Add Person");
        addButton.setOnAction(e -> {
            PersonDialog dialog = new PersonDialog();
            dialog.showAndWait().ifPresent(person -> {
                data.addPerson(person);
                visualizer.refresh();
            });
        });

        Button exportButton = new Button("Export");
        exportButton.setOnAction(e -> {
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

        Button importButton = new Button("Import");
        importButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import Family Tree");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    FamilyTreeData imported = mapper.readValue(file, FamilyTreeData.class);
                    FamilyTreeData.setInstance(imported);
                    visualizer.setData(imported);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        ToolBar toolBar = new ToolBar(addButton, exportButton, importButton);

        BorderPane root = new BorderPane();
        root.setTop(toolBar);
        root.setCenter(visualizer.getView());

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Family Tree Application");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}