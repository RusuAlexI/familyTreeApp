package com.familytree;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.scene.control.Separator;
import javafx.scene.control.ChoiceBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;

import java.io.File;
import java.io.IOException;

public class FamilyTreeApp extends Application {

    private final FamilyTreePane treePane = new FamilyTreePane();
    private final FamilyTreeData data = FamilyTreeData.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void start(Stage primaryStage) {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        BorderPane root = new BorderPane();

        Button addButton = new Button("Add Member");
        Button editButton = new Button("Edit Member");
        Button deleteButton = new Button("Delete Member");
        Button loadButton = new Button("Load Tree");
        Button saveButton = new Button("Save Tree");
        Button exportImageButton = new Button("Export Image");

        // --- Theme Selector ---
        ChoiceBox<ThemeItem> themeSelector = new ChoiceBox<>();
        ObservableList<ThemeItem> themeOptions = FXCollections.observableArrayList(ThemeLoader.loadThemes());

        // Add the "Upload Custom Image" option
        ThemeItem uploadCustomItem = new ThemeItem("Upload Custom Image...", Theme.CUSTOM, null);
        themeOptions.add(uploadCustomItem);

        themeSelector.setItems(themeOptions);

        // Set initial theme to DEFAULT and select it in the dropdown
        // Find the ThemeItem corresponding to Theme.DEFAULT
        ThemeItem defaultThemeItem = themeOptions.stream()
                .filter(item -> item.getTheme() == Theme.DEFAULT)
                .findFirst()
                .orElse(null);

        if (defaultThemeItem != null) {
            themeSelector.getSelectionModel().select(defaultThemeItem); // CORRECTED: Select ThemeItem
        }
        treePane.setTheme(Theme.DEFAULT); // Apply default theme on startup

        // Listener for theme selection changes
        themeSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (newVal.getTheme() == Theme.CUSTOM) { // This call will now work
                    // This is the "Upload Custom Image" option
                    setCustomBackgroundImage(primaryStage);
                } else {
                    // A predefined theme (DEFAULT, TREE, PARCHMENT) was selected
                    treePane.setTheme(newVal.getTheme()); // This call will now work
                }
            }
        });
        // --- End Theme Selector ---

        addButton.setOnAction(e -> {
            treePane.getVisualizer().addPerson(null);
        });
        editButton.setOnAction(e -> treePane.getVisualizer().editSelectedPerson());
        deleteButton.setOnAction(e -> treePane.getVisualizer().deleteSelectedPerson());

        loadButton.setOnAction(e -> loadFamilyTree(primaryStage));
        saveButton.setOnAction(e -> saveFamilyTree(primaryStage));
        exportImageButton.setOnAction(e -> treePane.exportAsImage(primaryStage));

        ToolBar toolbar = new ToolBar();
        toolbar.getItems().addAll(
                addButton, editButton, deleteButton,
                new Separator(),
                loadButton, saveButton, exportImageButton,
                new Separator(),
                new Label("Theme:"),
                themeSelector
        );

        root.setTop(toolbar);
        root.setCenter(treePane);

        primaryStage.setTitle("Family Tree Visualizer");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMaximized(true);
        primaryStage.setResizable(true);
        primaryStage.show();

        if (data.getAllPeople().isEmpty()) {
            Person initialPerson = new Person("New Root Person");
            initialPerson.setX(800 - 50);
            initialPerson.setY(500 - 100);
            data.addPerson(initialPerson);
        }

        treePane.getVisualizer().refresh();
    }

    private void loadFamilyTree(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Family Tree");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                FamilyTreeData loadedData = objectMapper.readValue(file, FamilyTreeData.class);
                FamilyTreeData.setInstance(loadedData);
                treePane.getVisualizer().refresh();
                showAlert(Alert.AlertType.INFORMATION, "Load Successful", "Family tree loaded from " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load family tree: " + e.getMessage());
            }
        }
    }

    private void saveFamilyTree(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Family Tree");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        fileChooser.setInitialFileName("family_tree.json");
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                objectMapper.writeValue(file, data);
                showAlert(Alert.AlertType.INFORMATION, "Save Successful", "Family tree saved to " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save family tree: " + e.getMessage());
            }
        }
    }

    private void setCustomBackgroundImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Background Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                Theme customTheme = Theme.CUSTOM;
                customTheme.setCustomBackground(file);
                treePane.setTheme(customTheme);

                showAlert(Alert.AlertType.INFORMATION, "Background Set", "Background image updated successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Image Error", "Failed to set background image: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}