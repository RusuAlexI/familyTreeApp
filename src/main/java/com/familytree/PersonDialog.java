package com.familytree;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.util.Callback;

import java.time.LocalDate;

public class PersonDialog extends Dialog<Person> {

    private final TextField nameField = new TextField();
    private final DatePicker dobPicker = new DatePicker();
    private final DatePicker dodPicker = new DatePicker();
    private final ComboBox<String> genderField = new ComboBox<>();

    public PersonDialog(Person existingPerson) {
        setTitle(existingPerson == null ? "Add Person" : "Edit Person");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        genderField.getItems().addAll("Male", "Female", "Other");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Date of Birth:"), 0, 1);
        grid.add(dobPicker, 1, 1);
        grid.add(new Label("Date of Death:"), 0, 2);
        grid.add(dodPicker, 1, 2);
        grid.add(new Label("Gender:"), 0, 3);
        grid.add(genderField, 1, 3);

        ImageView photoPreview = new ImageView();
        photoPreview.setFitWidth(80);
        photoPreview.setFitHeight(80);
        photoPreview.setPreserveRatio(true);

        Button uploadImageBtn = new Button("Upload Photo");
        uploadImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Profile Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(getDialogPane().getScene().getWindow());
            if (selectedFile != null) {
                Image img = new Image(selectedFile.toURI().toString());
                photoPreview.setImage(img);
            }
        });

        // Add to your layout grid or container:
        grid.add(new Label("Photo:"), 0, 5);
        grid.add(photoPreview, 1, 5);
        grid.add(uploadImageBtn, 2, 5);

        getDialogPane().setContent(grid);

        if (existingPerson != null) {
            if (existingPerson.getPhotoAsImage() != null) {
                photoPreview.setImage(existingPerson.getPhotoAsImage());
            }
            nameField.setText(existingPerson.getName());
            if (existingPerson.getDateOfBirth() != null) {
                dobPicker.setValue(LocalDate.parse(existingPerson.getDateOfBirth()));
            }
            dodPicker.setValue(LocalDate.parse(existingPerson.getDateOfDeath()));
            genderField.setValue(existingPerson.getGender());
        }

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText().trim();
                LocalDate dob = dobPicker.getValue();
                String gender = genderField.getValue();
                LocalDate dod = dodPicker.getValue();

                if (name.isEmpty() || dob == null || gender == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Missing or invalid fields");
                    alert.setContentText("Please fill in all required fields.");
                    alert.showAndWait();
                    return null;
                }

                Person person = existingPerson != null ? existingPerson : new Person();
                person.setName(name);
                person.setDateOfBirth(dob.toString());
                person.setDateOfDeath(dod != null ? dod.toString() : null);
                person.setGender(gender);
                person.setPhotoFromImage(photoPreview.getImage());

                return person;
            }
            return null;
        });


    }
}