package com.familytree;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.SnapshotParameters;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors; // Added for toList()

public class FamilyTreeApp extends Application {

    private FamilyTreePane treePane;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Application starting...");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-border-color: blue; -fx-border-width: 5; -fx-background-color: lightgrey;");

        // --- Menu Bar ---
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: lightgreen;");

        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open...");
        openItem.setOnAction(e -> handleOpen(primaryStage));
        MenuItem saveItem = new MenuItem("Save As...");
        saveItem.setOnAction(e -> handleSave(primaryStage));

        // --- NEW: Add a menu item for saving the tree as an image ---
        MenuItem saveImageItem = new MenuItem("Save as Image...");
        saveImageItem.setOnAction(e -> handleSaveAsImage(primaryStage));

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> primaryStage.close());

        fileMenu.getItems().addAll(openItem, saveItem, saveImageItem, new SeparatorMenuItem(), exitItem);
        System.out.println("File menu created.");

        // Theme Menu
        Menu themeMenu = new Menu("Theme");
        MenuItem defaultThemeItem = new MenuItem("Default Theme");
        MenuItem forestThemeItem = new MenuItem("Forest Theme");

        defaultThemeItem.setOnAction(e -> { if (treePane != null) treePane.setTheme(Theme.DEFAULT); });
        forestThemeItem.setOnAction(e -> { if (treePane != null) treePane.setTheme(Theme.TREE); });

        themeMenu.getItems().addAll(defaultThemeItem, forestThemeItem);
        System.out.println("Theme menu created.");

        menuBar.getMenus().addAll(fileMenu, themeMenu);
        System.out.println("Menu bar added to root.");

        // --- Toolbar ---
        ToolBar toolbar = new ToolBar();
        toolbar.setStyle("-fx-background-color: salmon;");
        Button addButton = new Button("Add");
        addButton.setStyle("-fx-background-color: yellow;");
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");
        Button autoLayoutButton = new Button("Auto Layout");

        treePane = new FamilyTreePane();

        addButton.setOnAction(e -> treePane.getVisualizer().addPerson(null));
        editButton.setOnAction(e -> treePane.getVisualizer().editSelectedPerson());
        deleteButton.setOnAction(e -> treePane.getVisualizer().deleteSelectedPerson());
        autoLayoutButton.setOnAction(e -> handleAutoLayout());

        toolbar.getItems().addAll(addButton, editButton, deleteButton, new Separator(), autoLayoutButton);
        System.out.println("Toolbar created.");

        // Combine MenuBar and ToolBar in a VBox at the top
        VBox topContainer = new VBox(menuBar, toolbar);
        topContainer.setPrefHeight(60);
        topContainer.setStyle("-fx-background-color: purple; -fx-border-color: yellow; -fx-border-width: 2;");
        root.setTop(topContainer);
        root.setCenter(treePane);
        System.out.println("Top container and tree pane set in BorderPane.");

        primaryStage.setTitle("Family Tree");
        primaryStage.setResizable(true);
        primaryStage.setMaximized(false);
        primaryStage.setScene(new Scene(root, 1600, 1000));
        System.out.println("Stage size set to 1600x1000, maximization prevented, and resizable.");

        primaryStage.setX(100);
        primaryStage.setY(100);
        primaryStage.setOnShown(event -> {
            primaryStage.setMaximized(false);
        });

        treePane.setTheme(Theme.DEFAULT);
        System.out.println("Default theme set.");

        primaryStage.setOnShown(event -> {
            System.out.println("Stage shown, performing initial visualizer refresh.");
            treePane.getVisualizer().refresh();
        });

        primaryStage.show();
        System.out.println("Primary stage shown.");
    }

    // Inside FamilyTreeApp.java
    private void handleOpen(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Family Tree File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                JsonManager.loadTree(file);
                FamilyTreeData.getInstance().linkAllRelationships();
                treePane.getVisualizer().refresh();
                showAlert(Alert.AlertType.INFORMATION, "Load Successful", "Family tree loaded from " + file.getName());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Load Failed", "Could not load file: " + e.getMessage());
            }
        }
    }

    private void handleSave(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Family Tree File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        fileChooser.setInitialFileName("family_tree.json");
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                JsonManager.saveTree(file);
                showAlert(Alert.AlertType.INFORMATION, "Save Successful", "Family tree saved to " + file.getName());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Save Failed", "Could not save file: " + e.getMessage());
            }
        }
    }

    private void handleAutoLayout() {
        FamilyTreeData data = FamilyTreeData.getInstance();
        if (data.getAllPeople().isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Layout Not Applied", "No people to layout.");
            return;
        }

        TreeLayout layout = new TreeLayout();
        // Fix: Convert Collection<Person> to List<Person>
        Map<String, Position> newLayoutPositions = layout.layout(data.getAllPeople().stream().collect(Collectors.toList()), null);

        for (Map.Entry<String, Position> entry : newLayoutPositions.entrySet()) {
            data.setLayoutPosition(entry.getKey(), entry.getValue());
        }
        treePane.getVisualizer().refresh();
        showAlert(Alert.AlertType.INFORMATION, "Layout Applied", "Family tree nodes have been re-arranged.");
    }

    /**
     * Handles saving the family tree visualization as a high-definition image file.
     * This method captures a snapshot of the entire treePane and saves it as a PNG.
     *
     * @param stage The main application stage for the file chooser.
     */
    private void handleSaveAsImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Family Tree as Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                // We'll create a snapshot of the treePane to capture everything inside it
                SnapshotParameters params = new SnapshotParameters();

                // --- Set a high scale for a high-definition image ---
                double scale = 2.0; // This will double the resolution
                params.setTransform(javafx.scene.transform.Scale.scale(scale, scale));

                WritableImage image = treePane.snapshot(params, null);

                // Save the image to the selected file
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);

                showAlert(Alert.AlertType.INFORMATION, "Save Successful", "Family tree saved as " + file.getName());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Save Failed", "Could not save image: " + e.getMessage());
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
