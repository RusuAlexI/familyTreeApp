package com.familytree;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID; // Import UUID
import java.util.stream.Collectors;

public class PersonDialog extends Dialog<Person> {

    private final TextField nameField;
    private final TextField placeOfBirthField;
    private final TextField occupationField;
    private final TextArea notesArea;
    private ImageView photoView;
    private String photoBase64String;

    // ComboBoxes for selecting parents (by name, but store ID)
    private final ComboBox<Person> motherComboBox;
    private final ComboBox<Person> fatherComboBox;

    private final FamilyTreeData data = FamilyTreeData.getInstance(); // Get the singleton instance

    public PersonDialog(Person person) {
        setTitle("Person Details");
        setHeaderText("Edit Person Information");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        nameField = new TextField();
        nameField.setPromptText("Name");
        placeOfBirthField = new TextField();
        placeOfBirthField.setPromptText("Place of Birth");
        occupationField = new TextField();
        occupationField.setPromptText("Occupation");
        notesArea = new TextArea();
        notesArea.setPromptText("Notes");
        notesArea.setWrapText(true);
        notesArea.setPrefRowCount(4);

        // Photo selection
        Button selectPhotoButton = new Button("Select Photo");
        photoView = new ImageView();
        photoView.setFitHeight(100);
        photoView.setFitWidth(100);
        photoView.setPreserveRatio(true);
        selectPhotoButton.setOnAction(e -> selectPhoto());

        // ComboBoxes for parents
        List<Person> allPeople = data.getAllPeople().stream()
                .filter(p -> !p.getId().equals(person.getId())) // Don't let a person be their own parent
                .collect(Collectors.toList());

        motherComboBox = new ComboBox<>();
        motherComboBox.getItems().add(null); // Option for no mother
        motherComboBox.getItems().addAll(allPeople);
        motherComboBox.setConverter(new PersonStringConverter()); // Custom converter for display
        motherComboBox.setPromptText("Select Mother");

        fatherComboBox = new ComboBox<>();
        fatherComboBox.getItems().add(null); // Option for no father
        fatherComboBox.getItems().addAll(allPeople);
        fatherComboBox.setConverter(new PersonStringConverter()); // Custom converter for display
        fatherComboBox.setPromptText("Select Father");


        // Populate fields if editing an existing person
        if (person != null) {
            nameField.setText(person.getName());
            placeOfBirthField.setText(person.getPlaceOfBirth());
            occupationField.setText(person.getOccupation());
            notesArea.setText(person.getNotes());
            if (person.getPhotoBase64() != null && !person.getPhotoBase64().isEmpty()) {
                Image photo = Person.base64ToImage(person.getPhotoBase64());
                if (photo != null) {
                    photoView.setImage(photo);
                    photoBase64String = person.getPhotoBase64();
                }
            }
            // Set initial selection for mother/father ComboBoxes
            if (person.getMotherId() != null) {
                motherComboBox.getSelectionModel().select(data.findById(person.getMotherId()));
            }
            if (person.getFatherId() != null) {
                fatherComboBox.getSelectionModel().select(data.findById(person.getFatherId()));
            }
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Place of Birth:"), 0, 1);
        grid.add(placeOfBirthField, 1, 1);
        grid.add(new Label("Occupation:"), 0, 2);
        grid.add(occupationField, 1, 2);
        grid.add(new Label("Notes:"), 0, 3);
        grid.add(notesArea, 1, 3);
        grid.add(new Label("Photo:"), 0, 4);
        grid.add(new HBox(10, selectPhotoButton, photoView), 1, 4); // Use HBox for button and image
        grid.add(new Label("Mother:"), 0, 5);
        grid.add(motherComboBox, 1, 5);
        grid.add(new Label("Father:"), 0, 6);
        grid.add(fatherComboBox, 1, 6);

        getDialogPane().setContent(grid);

        // Convert the result when the save button is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Create a new Person object to hold the data from the dialog fields.
                // It's crucial to set its ID to match the original person's ID,
                // so it represents the updated state of that specific person.
                Person tempPerson = new Person();
                if (person != null) { // If editing an existing person, use their ID
                    tempPerson.setId(person.getId());
                } else { // Fallback for new person (if flow changes)
                    tempPerson.setId(UUID.randomUUID().toString());
                }

                tempPerson.setName(nameField.getText());
                tempPerson.setPlaceOfBirth(placeOfBirthField.getText());
                tempPerson.setOccupation(occupationField.getText());
                tempPerson.setNotes(notesArea.getText());
                tempPerson.setPhotoBase64(photoBase64String);

                // Handle parent relationships
                Person selectedMother = motherComboBox.getSelectionModel().getSelectedItem();
                Person selectedFather = fatherComboBox.getSelectionModel().getSelectedItem();

                // Store only the IDs
                tempPerson.setMotherId(selectedMother != null ? selectedMother.getId() : null);
                tempPerson.setFatherId(selectedFather != null ? selectedFather.getId() : null);

                return tempPerson; // Return this temporary person object
            }
            return null;
        });
    }

    private void selectPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(getDialogPane().getScene().getWindow());

        if (selectedFile != null) {
            try (FileInputStream fis = new FileInputStream(selectedFile)) {
                Image image = new Image(fis);
                photoView.setImage(image);
                photoBase64String = Person.imageToBase64(image); // Convert to Base64
            } catch (IOException e) {
                e.printStackTrace();
                // Handle error loading image
            } catch (Exception e) {
                e.printStackTrace(); // For imageToBase64
            }
        }
    }

    // Static method to show the dialog
    public static void editPerson(Person person, java.util.function.Consumer<Person> callback) {
        PersonDialog dialog = new PersonDialog(person);
        Optional<Person> result = dialog.showAndWait();
        result.ifPresent(updatedPerson -> { // updatedPerson here is the temporary person from the dialog
            // Apply the updates to the original person object outside the dialog
            // This ensures FamilyTreeData can properly manage relationships
            // We use copyFrom to update the personal details (name, notes, etc.)
            person.copyFrom(updatedPerson); // Use the copyFrom method

            // Now, update relationships in FamilyTreeData using the IDs from the dialog's result
            // This is crucial to ensure both sides of the relationship are set/unset correctly.

            // Handle Mother relationship changes
            String oldMotherId = person.getMotherId(); // original person's mother ID BEFORE copyFrom
            String newMotherId = updatedPerson.getMotherId(); // dialog's updated mother ID

            if (!Objects.equals(oldMotherId, newMotherId)) {
                // If old mother existed, remove old relationship
                if (oldMotherId != null) {
                    FamilyTreeData.getInstance().removeParentChildRelationship(oldMotherId, person.getId());
                }
                // If new mother exists, establish new relationship
                if (newMotherId != null) {
                    FamilyTreeData.getInstance().setParentChildRelationship(newMotherId, person.getId(), "mother");
                }
            }
            // After all updates, the person's own motherId is set.
            // This also handles the case where oldMotherId != newMotherId
            person.setMotherId(newMotherId);


            // Handle Father relationship changes
            String oldFatherId = person.getFatherId(); // original person's father ID BEFORE copyFrom
            String newFatherId = updatedPerson.getFatherId(); // dialog's updated father ID

            if (!Objects.equals(oldFatherId, newFatherId)) {
                // If old father existed, remove old relationship
                if (oldFatherId != null) {
                    FamilyTreeData.getInstance().removeParentChildRelationship(oldFatherId, person.getId());
                }
                // If new father exists, establish new relationship
                if (newFatherId != null) {
                    FamilyTreeData.getInstance().setParentChildRelationship(newFatherId, person.getId(), "father");
                }
            }
            // After all updates, the person's own fatherId is set.
            person.setFatherId(newFatherId);

            callback.accept(person); // Pass the original, now fully updated, person object
        });
    }

    // Custom StringConverter for ComboBox to display Person names
    private static class PersonStringConverter extends javafx.util.StringConverter<Person> {
        @Override
        public String toString(Person person) {
            return person == null ? "None" : person.getName();
        }

        @Override
        public Person fromString(String string) {
            // This method is used when user types, not needed for selection from list
            return null;
        }
    }
}