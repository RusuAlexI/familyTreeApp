package com.familytree;

import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class TreeVisualizer {

    private final Pane visualizationPane;
    private final FamilyTreeData data = FamilyTreeData.getInstance();
    private final Map<String, PersonCell> personCellMap = new HashMap<>();

    // This is the correct way to track the selected cell
    private PersonCell selectedCell;

    // We need to pass the Pane to the constructor
    public TreeVisualizer(Pane visualizationPane) {
        this.visualizationPane = visualizationPane;
        this.visualizationPane.getStyleClass().add("visualization-pane");
    }

    public void refresh() {
        System.out.println("TreeVisualizer refresh() called. Person count: " + data.getAllPeople().size());
        clearSelection();

        // Clear all existing nodes and lines
        visualizationPane.getChildren().clear();
        personCellMap.clear();

        // 1. Create and position PersonCells
        for (Person person : data.getAllPeople()) {
            PersonCell personCell = new PersonCell(person);
            personCellMap.put(person.getId(), personCell);

            // Set position based on saved layout or default
            Position layoutPosition = data.getLayoutPosition(person.getId());
            if (layoutPosition != null && !Double.isNaN(layoutPosition.getX()) && !Double.isNaN(layoutPosition.getY())) {
                personCell.setLayoutX(layoutPosition.getX());
                personCell.setLayoutY(layoutPosition.getY());
            } else {
                double paneWidth = visualizationPane.getWidth() > 0 ? visualizationPane.getWidth() : 800;
                double paneHeight = visualizationPane.getHeight() > 0 ? visualizationPane.getHeight() : 600;

                double defaultX = 50 + (personCellMap.size() * 10 % (paneWidth - 100));
                double defaultY = 50 + (personCellMap.size() * 10 % (paneHeight - 100));

                defaultX = Math.max(0, Math.min(defaultX, paneWidth - personCell.getPrefWidth()));
                defaultY = Math.max(0, Math.min(defaultY, paneHeight - personCell.getPrefHeight()));

                personCell.setLayoutX(defaultX);
                personCell.setLayoutY(defaultY);
                data.setLayoutPosition(person.getId(), new Position(defaultX, defaultY));
            }

            addInteractionsToPersonCell(personCell);
            visualizationPane.getChildren().add(personCell);
        }

        // 2. Draw relationships (lines)
        for (Person person : data.getAllPeople()) {
            PersonCell sourceCell = personCellMap.get(person.getId());
            if (sourceCell == null) continue;

            // Draw parent-child relationships
            if (person.getFatherId() != null) {
                PersonCell fatherCell = personCellMap.get(person.getFatherId());
                if (fatherCell != null) {
                    Line line = createConnectionLine(fatherCell, sourceCell);
                    line.setStroke(Color.BLUE);
                    visualizationPane.getChildren().add(0, line); // Add to back
                }
            }
            if (person.getMotherId() != null) {
                PersonCell motherCell = personCellMap.get(person.getMotherId());
                if (motherCell != null) {
                    Line line = createConnectionLine(motherCell, sourceCell);
                    line.setStroke(Color.BLUE);
                    visualizationPane.getChildren().add(0, line); // Add to back
                }
            }

            // Draw spouse relationships
            for (String spouseId : person.getSpouseIds()) {
                if (spouseId.compareTo(person.getId()) > 0) {
                    PersonCell spouseCell = personCellMap.get(spouseId);
                    if (spouseCell != null) {
                        Line line = createConnectionLine(sourceCell, spouseCell);
                        line.setStroke(Color.ORANGE);
                        line.getStrokeDashArray().addAll(5d, 5d);
                        visualizationPane.getChildren().add(0, line); // Add to back
                    }
                }
            }
        }

        System.out.println("TreeVisualizer refresh() finished.");
    }

    private Line createConnectionLine(javafx.scene.Node startNode, javafx.scene.Node endNode) {
        Line line = new Line();
        line.setStrokeWidth(2);

        // Bind the line's endpoints to the center of the nodes
        line.startXProperty().bind(startNode.layoutXProperty().add(((Region)startNode).widthProperty().divide(2)));
        line.startYProperty().bind(startNode.layoutYProperty().add(((Region)startNode).heightProperty().divide(2)));
        line.endXProperty().bind(endNode.layoutXProperty().add(((Region)endNode).widthProperty().divide(2)));
        line.endYProperty().bind(endNode.layoutYProperty().add(((Region)endNode).heightProperty().divide(2)));

        return line;
    }

    private void addInteractionsToPersonCell(PersonCell personCell) {
        // Correct selection logic using getStyleClass() for CSS styling
        personCell.setOnMouseClicked(event -> {
            clearSelection();
            selectedCell = personCell;
            selectedCell.getStyleClass().add("selected-cell");
            event.consume();
        });

        // Dragging functionality
        final double[] mouseOffset = new double[2];

        personCell.setOnMousePressed(event -> {
            // Drag only starts if the left mouse button is pressed
            if (event.isPrimaryButtonDown()) {
                mouseOffset[0] = event.getSceneX() - personCell.getLayoutX();
                mouseOffset[1] = event.getSceneY() - personCell.getLayoutY();
                personCell.toFront();
                event.consume();
            }
        });

        personCell.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                double newX = event.getSceneX() - mouseOffset[0];
                double newY = event.getSceneY() - mouseOffset[1];

                double paneWidth = visualizationPane.getWidth();
                double paneHeight = visualizationPane.getHeight();

                newX = Math.max(0, Math.min(newX, paneWidth - personCell.getWidth()));
                newY = Math.max(0, Math.min(newY, paneHeight - personCell.getHeight()));

                personCell.setLayoutX(newX);
                personCell.setLayoutY(newY);

                data.setLayoutPosition(personCell.getPerson().getId(), new Position(newX, newY));
                event.consume();
            }
        });

        personCell.setOnMouseReleased(event -> {
            // A click is a mouse press and release without dragging.
            // If the cell moved, we don't treat it as a click.
            // No need for refresh() here as bindings handle line updates.
            event.consume();
        });

        // Context Menu
        ContextMenu contextMenu = new ContextMenu();

        MenuItem addChildItem = new MenuItem("Add Child");
        addChildItem.setOnAction(event -> addPerson(personCell.getPerson()));

        MenuItem addSpouseItem = new MenuItem("Add Spouse");
        addSpouseItem.setOnAction(event -> {
            Person newSpouseCandidate = new Person(UUID.randomUUID().toString(), "New Spouse");
            PersonDialog spouseDialog = new PersonDialog(newSpouseCandidate);
            spouseDialog.setResizable(true);
            Optional<Person> spouseResult = spouseDialog.showAndWait();
            spouseResult.ifPresent(spouse -> {
                data.addPerson(spouse);
                data.setLayoutPosition(spouse.getId(), new Position(personCell.getLayoutX() + 150, personCell.getLayoutY()));
                personCell.getPerson().addSpouseId(spouse.getId());
                spouse.addSpouseId(personCell.getPerson().getId());
                data.linkAllRelationships();
                refresh();
            });
        });

        MenuItem editItem = new MenuItem("Edit Person");
        editItem.setOnAction(event -> editSelectedPerson());

        MenuItem deleteItem = new MenuItem("Delete Person");
        deleteItem.setOnAction(event -> deleteSelectedPerson());

        contextMenu.getItems().addAll(addChildItem, addSpouseItem, editItem, deleteItem);

        personCell.setOnContextMenuRequested(event -> {
            // Ensure the context menu only shows for the selected cell
            clearSelection();
            selectedCell = personCell;
            selectedCell.getStyleClass().add("selected-cell");

            contextMenu.show(personCell, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }

    // Methods to interact with the tree (add, edit, delete)
    public void addPerson(Person parentForNew) {
        Person newPerson = new Person(UUID.randomUUID().toString(), "New Person");

        if (parentForNew != null) {
            if ("Male".equals(parentForNew.getGender())) {
                newPerson.setFatherId(parentForNew.getId());
            } else {
                newPerson.setMotherId(parentForNew.getId());
            }
        }

        PersonDialog dialog = new PersonDialog(newPerson);
        dialog.setResizable(true);
        Optional<Person> result = dialog.showAndWait();

        result.ifPresent(p -> {
            data.addPerson(p);
            if (parentForNew != null && personCellMap.containsKey(parentForNew.getId())) {
                PersonCell parentCell = personCellMap.get(parentForNew.getId());
                data.setLayoutPosition(p.getId(), new Position(parentCell.getLayoutX(), parentCell.getLayoutY() + 150));
            } else {
                double paneWidth = visualizationPane.getWidth() > 0 ? visualizationPane.getWidth() : 800;
                double paneHeight = visualizationPane.getHeight() > 0 ? visualizationPane.getHeight() : 600;

                double defaultX = 50 + (data.getAllPeople().size() * 10 % (paneWidth - 100));
                double defaultY = 50 + (data.getAllPeople().size() * 10 % (paneHeight - 120));

                defaultX = Math.max(0, Math.min(defaultX, paneWidth - 100));
                defaultY = Math.max(0, Math.min(defaultY, paneHeight - 120));

                data.setLayoutPosition(p.getId(), new Position(defaultX, defaultY));
            }
            data.linkAllRelationships();
            refresh();
        });
    }

    public void editSelectedPerson() {
        if (selectedCell != null && selectedCell.getPerson() != null) {
            Person personToEdit = selectedCell.getPerson();
            PersonDialog dialog = new PersonDialog(personToEdit);
            dialog.setResizable(true);
            Optional<Person> result = dialog.showAndWait();
            result.ifPresent(updatedPerson -> {
                data.updatePerson(updatedPerson);
                data.linkAllRelationships();
                refresh();
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "No Person Selected", "Please select a person to edit.");
        }
    }

    public void deleteSelectedPerson() {
        if (selectedCell != null && selectedCell.getPerson() != null) {
            data.removePerson(selectedCell.getPerson().getId());
            refresh();
        } else {
            showAlert(Alert.AlertType.WARNING, "No Person Selected", "Please select a person to delete.");
        }
    }

    public Person getSelectedPerson() {
        return selectedCell != null ? selectedCell.getPerson() : null;
    }

    private void clearSelection() {
        if (selectedCell != null) {
            selectedCell.getStyleClass().remove("selected-cell");
            selectedCell = null;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}