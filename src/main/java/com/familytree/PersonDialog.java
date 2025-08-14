package com.familytree;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Objects;
import java.util.UUID;

public class PersonDialog extends Dialog<Person> {

    private final Person person;
    private final FamilyTreeData data = FamilyTreeData.getInstance();
    private String selectedFilePath;

    public PersonDialog(Person person) {
        this.person = person;
        setTitle("Person Details");
        setHeaderText("Edit details for " + person.getName());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // This is crucial for resizing - ensures the second column expands
        javafx.scene.layout.ColumnConstraints column1 = new javafx.scene.layout.ColumnConstraints();
        javafx.scene.layout.ColumnConstraints column2 = new javafx.scene.layout.ColumnConstraints();
        column2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(column1, column2);

        // Name
        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField(person.getName());
        nameField.setMaxWidth(Double.MAX_VALUE);

        // Birth Date
        Label birthDateLabel = new Label("Birth Date:");
        TextField birthDateField = new TextField(person.getBirthDate());
        birthDateField.setMaxWidth(Double.MAX_VALUE);

        // Death Date
        Label deathDateLabel = new Label("Death Date:");
        TextField deathDateField = new TextField(person.getDeathDate());
        deathDateField.setMaxWidth(Double.MAX_VALUE);

        // Gender
        Label genderLabel = new Label("Gender:");
        ComboBox<String> genderComboBox = new ComboBox<>();
        genderComboBox.getItems().addAll("Male", "Female", "Other");
        genderComboBox.getSelectionModel().select(person.getGender());
        genderComboBox.setMaxWidth(Double.MAX_VALUE);

        // Bio
        Label bioLabel = new Label("Bio:");
        TextArea bioArea = new TextArea(person.getBio());
        bioArea.setPrefRowCount(3);
        bioArea.setMaxWidth(Double.MAX_VALUE);
        bioArea.setWrapText(true);

        // Occupation
        Label occupationLabel = new Label("Occupation:");
        TextField occupationField = new TextField(person.getOccupation());
        occupationField.setMaxWidth(Double.MAX_VALUE);

        // Parents
        Label fatherLabel = new Label("Father:");
        ComboBox<Person> fatherComboBox = new ComboBox<>();
        fatherComboBox.setItems(data.getPersonList());
        fatherComboBox.getSelectionModel().select(data.getPerson(person.getFatherId()));
        fatherComboBox.setMaxWidth(Double.MAX_VALUE);

        Label motherLabel = new Label("Mother:");
        ComboBox<Person> motherComboBox = new ComboBox<>();
        motherComboBox.setItems(data.getPersonList());
        motherComboBox.getSelectionModel().select(data.getPerson(person.getMotherId()));
        motherComboBox.setMaxWidth(Double.MAX_VALUE);

        // Children
        Label childrenLabel = new Label("Children:");
        ListView<Person> childrenListView = new ListView<>();
        childrenListView.getItems().addAll(data.getChildren(person));
        childrenListView.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(childrenListView, Priority.ALWAYS);

        // Spouses
        Label spousesLabel = new Label("Spouses:");
        ListView<Person> spousesListView = new ListView<>();
        spousesListView.getItems().addAll(data.getSpouses(person));
        spousesListView.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(spousesListView, Priority.ALWAYS);
        // Image

        // Add a section for photo selection
        Label photoLabel = new Label("Profile Photo:");
        TextField photoPathField = new TextField(person.getProfilePicturePath());
        photoPathField.setEditable(false);
        photoPathField.setPromptText("No photo selected...");

        Button choosePhotoButton = new Button("Choose Photo...");
        Button removePhotoButton = new Button("Remove Photo");

        choosePhotoButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Profile Picture");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File file = fileChooser.showOpenDialog(getDialogPane().getScene().getWindow());
            if (file != null) {
                selectedFilePath = file.getAbsolutePath();
                photoPathField.setText(selectedFilePath);
            }
        });

        removePhotoButton.setOnAction(e -> {
            selectedFilePath = null;
            photoPathField.setText(null);
        });

        HBox photoControls = new HBox(5, photoPathField, choosePhotoButton, removePhotoButton);
        HBox.setHgrow(photoPathField, Priority.ALWAYS);

//        grid.add(photoLabel, 0, 6);
//        grid.add(photoControls, 1, 6);

        // Add all UI components to the grid
        int row = 0;
        grid.add(nameLabel, 0, row);
        grid.add(nameField, 1, row++);
        grid.add(photoLabel, 0, row);
        grid.add(photoControls, 1, row++);
        grid.add(birthDateLabel, 0, row);
        grid.add(birthDateField, 1, row++);
        grid.add(deathDateLabel, 0, row);
        grid.add(deathDateField, 1, row++);
        grid.add(genderLabel, 0, row);
        grid.add(genderComboBox, 1, row++);
        grid.add(bioLabel, 0, row);
        grid.add(bioArea, 1, row++);
        grid.add(occupationLabel, 0, row);
        grid.add(occupationField, 1, row++);
        grid.add(fatherLabel, 0, row);
        grid.add(fatherComboBox, 1, row++);
        grid.add(motherLabel, 0, row);
        grid.add(motherComboBox, 1, row++);
        grid.add(childrenLabel, 0, row);
        grid.add(childrenListView, 1, row++);
        grid.add(spousesLabel, 0, row);
        grid.add(spousesListView, 1, row++);

        getDialogPane().setContent(grid);

        // Convert the result of the dialog to a Person object when Save is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Update the person object with new data
                person.setName(nameField.getText());
                person.setBirthDate(birthDateField.getText());
                person.setDeathDate(deathDateField.getText());
                person.setGender(genderComboBox.getValue());
                person.setBio(bioArea.getText());
                person.setOccupation(occupationField.getText());

                // Handle the profile picture
                if (selectedFilePath != null) {
                    try {
                        Path sourcePath = Paths.get(selectedFilePath);
                        Path destPath = Paths.get("data", "photos");
                        if (!Files.exists(destPath)) {
                            Files.createDirectories(destPath);
                        }
                        String newFileName = UUID.randomUUID().toString() + "." + getFileExtension(selectedFilePath);
                        Path newFilePath = destPath.resolve(newFileName);
                        Files.copy(sourcePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
                        person.setProfilePicturePath(newFilePath.toAbsolutePath().toString());
                    } catch (IOException e) {
                        System.err.println("Failed to copy image file: " + e.getMessage());
                        // Keep the old path if copy fails
                    }
                } else {
                    // This handles the "Remove Photo" case
                    person.setProfilePicturePath(null);
                }

                // Update father relationship
                Person newFather = fatherComboBox.getValue();
                if (newFather != null && !Objects.equals(person.getFatherId(), newFather.getId())) {
                    if (person.getFatherId() != null) {
                        data.getPerson(person.getFatherId()).removeChildId(person.getId());
                    }
                    person.setFatherId(newFather.getId());
                    newFather.addChildId(person.getId());
                } else if (newFather == null && person.getFatherId() != null) {
                    data.getPerson(person.getFatherId()).removeChildId(person.getId());
                    person.setFatherId(null);
                }

                // Update mother relationship
                Person newMother = motherComboBox.getValue();
                if (newMother != null && !Objects.equals(person.getMotherId(), newMother.getId())) {
                    if (person.getMotherId() != null) {
                        data.getPerson(person.getMotherId()).removeChildId(person.getId());
                    }
                    person.setMotherId(newMother.getId());
                    newMother.addChildId(person.getId());
                } else if (newMother == null && person.getMotherId() != null) {
                    data.getPerson(person.getMotherId()).removeChildId(person.getId());
                    person.setMotherId(null);
                }

                return person;
            }
            return null;
        });
    }

    private String getFileExtension(String filePath) {
        String fileName = new File(filePath).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    private Image loadImageFromFile(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            try {
                return new Image(new FileInputStream(filePath));
            } catch (FileNotFoundException e) {
                System.err.println("Profile picture not found: " + filePath);
            }
        }
        return null;
    }

    private void handleSaveAsImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Family Tree as Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
//            try {
                // We'll create a snapshot of the treePane to capture everything inside it
                SnapshotParameters params = new SnapshotParameters();

                // --- Set a high scale for a high-definition image ---
                double scale = 2.0; // This will double the resolution
                params.setTransform(javafx.scene.transform.Scale.scale(scale, scale));

//                WritableImage image = treePane.snapshot(params, null);

                // Save the image to the selected file
//                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);

//                showAlert(Alert.AlertType.INFORMATION, "Save Successful", "Family tree saved as " + file.getName());
//            }
//            catch (IOException e) {
//                showAlert(Alert.AlertType.ERROR, "Save Failed", "Could not save image: " + e.getMessage());
//            }
        }
    }
}