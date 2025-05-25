package com.familytree;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.prefs.Preferences;

public class FamilyTreeApp extends Application {

    private final FamilyTreeData data = FamilyTreeData.getInstance();
    private final FamilyTreePane visualizer = new FamilyTreePane();

    private static final Preferences prefs = Preferences.userNodeForPackage(FamilyTreeApp.class);
    private static final String THEME_KEY = "selectedTheme";

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

        // Theme selector with icons
        ComboBox<ThemeItem> themeSelector = new ComboBox<>();
        themeSelector.setButtonCell(createThemeCell());
        themeSelector.setCellFactory(list -> createThemeCell());

        themeSelector.getItems().addAll(
                new ThemeItem("Default", Theme.DEFAULT, new Image(getClass().getResourceAsStream("/icons/default.jpg"))),
                new ThemeItem("Parchment", Theme.PARCHMENT, new Image(getClass().getResourceAsStream("/icons/parchment.jpg"))),
                new ThemeItem("Tree", Theme.TREE_BACKGROUND, new Image(getClass().getResourceAsStream("/icons/tree.jpg"))),
                new ThemeItem("Custom...", null, new Image(getClass().getResourceAsStream("/icons/custom.jpg")))
        );

        // Restore saved theme
        String savedTheme = prefs.get(THEME_KEY, "Default");
        themeSelector.getItems().stream()
                .filter(item -> item.name().equals(savedTheme))
                .findFirst()
                .ifPresent(themeSelector::setValue);

        applyTheme(themeSelector.getValue(), visualizer);

        themeSelector.setOnAction(e -> {
            ThemeItem selectedItem = themeSelector.getValue();
            if (selectedItem.name().equals("Custom...")) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Choose Custom Background Image");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
                );
                File file = fileChooser.showOpenDialog(primaryStage);
                if (file != null) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        Image customImage = new Image(fis);
//                        Theme customTheme = new Theme("Custom", customImage, true);
                        visualizer.setTheme(Theme.DEFAULT);
                        prefs.put(THEME_KEY, "Custom"); // Save that custom was chosen
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    // If canceled, revert to previous selection
                    themeSelector.setValue(themeSelector.getItems().stream()
                            .filter(item -> item.name().equals(savedTheme))
                            .findFirst().orElse(themeSelector.getItems().get(0)));
                }
            } else {
                applyTheme(selectedItem, visualizer);
                prefs.put(THEME_KEY, selectedItem.name());
            }
        });

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
                    FamilyTreeDataDTO loadedDto = mapper.readValue(selectedFile, FamilyTreeDataDTO.class);

                    // Convert back to domain objects
                    FamilyTreeData loaded = loadedDto.toDomain();

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

    private void applyTheme(ThemeItem item, FamilyTreePane pane) {
        if (item != null && item.theme() != null) {
            pane.setTheme(item.theme());
        }
    }

    private ListCell<ThemeItem> createThemeCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(ThemeItem item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    ImageView imageView = new ImageView(item.icon());
                    imageView.setFitWidth(24);
                    imageView.setFitHeight(24);
                    setGraphic(imageView);
                    setText(item.name());
                    setPadding(new Insets(4));
                }
            }
        };
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
