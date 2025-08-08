package com.familytree;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import java.util.Optional;
import java.util.Objects;
import java.util.UUID;

public class PersonDialog extends Dialog<Person> {

    private final Person person;
    private final FamilyTreeData data = FamilyTreeData.getInstance();

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

        // Add all UI components to the grid
        int row = 0;
        grid.add(nameLabel, 0, row);
        grid.add(nameField, 1, row++);
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
}