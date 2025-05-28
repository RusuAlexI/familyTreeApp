package com.familytree;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.time.LocalDate;

public class PersonDialog extends Dialog<Person> {

    private final TextField nameField = new TextField();
    private final DatePicker dobPicker = new DatePicker();
    private final DatePicker dodPicker = new DatePicker();
    private final ComboBox<String> genderField = new ComboBox<>();
    private final TextField placeOfBirthField = new TextField();
    private final TextField occupationField = new TextField();
    private final TextArea notesField = new TextArea();
    private String photoPath = null;

    public PersonDialog(Person existingPerson) {
        setTitle(existingPerson == null ? "Add Person" : "Edit Person");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        genderField.getItems().addAll("Male", "Female", "Other");

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
                photoPath = selectedFile.toURI().toString();
                Image img = new Image(photoPath);
                photoPreview.setImage(img);
            }
        });

        notesField.setPrefRowCount(3);

        // Layout
        int row = 0;
        grid.add(new Label("Name:"), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(new Label("Date of Birth:"), 0, row);
        grid.add(dobPicker, 1, row++);

        grid.add(new Label("Date of Death:"), 0, row);
        grid.add(dodPicker, 1, row++);

        grid.add(new Label("Gender:"), 0, row);
        grid.add(genderField, 1, row++);

        grid.add(new Label("Place of Birth:"), 0, row);
        grid.add(placeOfBirthField, 1, row++);

        grid.add(new Label("Occupation:"), 0, row);
        grid.add(occupationField, 1, row++);

        grid.add(new Label("Notes:"), 0, row);
        grid.add(notesField, 1, row++);

        grid.add(new Label("Photo:"), 0, row);
        grid.add(photoPreview, 1, row);
        grid.add(uploadImageBtn, 2, row++);

        getDialogPane().setContent(grid);

        // Fill in existing data
        if (existingPerson != null) {
            nameField.setText(existingPerson.getName());
            if (existingPerson.getDateOfBirth() != null) {
                dobPicker.setValue(LocalDate.parse(existingPerson.getDateOfBirth()));
            }
            if (existingPerson.getDateOfDeath() != null) {
                dodPicker.setValue(LocalDate.parse(existingPerson.getDateOfDeath()));
            }
            genderField.setValue(existingPerson.getGender());
            placeOfBirthField.setText(existingPerson.getPlaceOfBirth());
            occupationField.setText(existingPerson.getOccupation());
            notesField.setText(existingPerson.getNotes());

            if (existingPerson.getPhotoPath() != null) {
                photoPath = existingPerson.getPhotoPath();
                photoPreview.setImage(new Image(photoPath));
            }
        }

        // Return Person object on save
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText().trim();
                LocalDate dob = dobPicker.getValue();
                LocalDate dod = dodPicker.getValue();
                String gender = genderField.getValue();

                if (name.isEmpty() || dob == null || gender == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Missing or invalid fields");
                    alert.setContentText("Please fill in all required fields.");
                    alert.showAndWait();
                    return null;
                }

                Person person = new Person();
                if (existingPerson != null) {
                    person.setId(existingPerson.getId());
                }

                person.setName(name);
                person.setDateOfBirth(dob.toString());
                person.setDateOfDeath(dod != null ? dod.toString() : null);
                person.setGender(gender);
                person.setPlaceOfBirth(placeOfBirthField.getText().trim());
                person.setOccupation(occupationField.getText().trim());
                person.setNotes(notesField.getText().trim());
                person.setPhotoPath(photoPath);

                return person;
            }
            return null;
        });
    }
}
