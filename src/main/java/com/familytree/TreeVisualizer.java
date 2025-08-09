package com.familytree;

import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TreeVisualizer {

    private final Pane visualizationPane;
    private final FamilyTreeData data = FamilyTreeData.getInstance();
    private final Map<String, javafx.scene.Node> nodeMap = new HashMap<>(); // Using Node to hold PersonCells or HBoxes

    private PersonCell selectedCell;
    private PersonCell overlappingCell;

    public TreeVisualizer(Pane visualizationPane) {
        this.visualizationPane = visualizationPane;
        this.visualizationPane.getStyleClass().add("visualization-pane");

        // Add drag handlers for the background
        final double[] dragOffset = new double[2];

        this.visualizationPane.setOnMousePressed(event -> {
            if (event.getTarget() == visualizationPane) { // Check if the click is on the pane itself
                dragOffset[0] = event.getSceneX();
                dragOffset[1] = event.getSceneY();
                event.consume();
            }
        });

        this.visualizationPane.setOnMouseDragged(event -> {
            if (event.getTarget() == visualizationPane) { // Check if the drag started on the pane
                double deltaX = event.getSceneX() - dragOffset[0];
                double deltaY = event.getSceneY() - dragOffset[1];

                for (javafx.scene.Node node : visualizationPane.getChildren()) {
                    node.setLayoutX(node.getLayoutX() + deltaX);
                    node.setLayoutY(node.getLayoutY() + deltaY);
                }
                dragOffset[0] = event.getSceneX();
                dragOffset[1] = event.getSceneY();
                event.consume();
            }
        });

        // Add zoom handlers
        visualizationPane.setOnScroll(event -> {
            double zoomFactor = 1.05;
            double deltaY = event.getDeltaY();

            if (deltaY < 0) {
                zoomFactor = 2.0 - zoomFactor;
            }

            visualizationPane.setScaleX(visualizationPane.getScaleX() * zoomFactor);
            visualizationPane.setScaleY(visualizationPane.getScaleY() * zoomFactor);
            event.consume();
        });
    }

    public void refresh() {
        System.out.println("TreeVisualizer refresh() called. Person count: " + data.getAllPeople().size());
        clearSelection();

        visualizationPane.getChildren().clear();
        nodeMap.clear();

        Set<String> processedPeople = new HashSet<>();

        // 1. Create and position all visual nodes
        for (Person person : data.getAllPeople()) {
            if (processedPeople.contains(person.getId())) {
                continue;
            }

            if (person.getSpouseIds() != null && !person.getSpouseIds().isEmpty()) {
                String spouseId = person.getSpouseIds().iterator().next();
                Person spouse = data.getPerson(spouseId);
                if (spouse != null && !processedPeople.contains(spouse.getId())) {
                    PersonCell person1Cell = new PersonCell(person);
                    PersonCell person2Cell = new PersonCell(spouse);

                    // Add interactions to the individual cells within the group
                    addInteractionsToPersonCell(person1Cell);
                    addInteractionsToPersonCell(person2Cell);

                    HBox spouseGroup = new HBox(10);
                    spouseGroup.getChildren().addAll(person1Cell, person2Cell);
                    spouseGroup.getStyleClass().add("spouse-group");

                    addSpouseGroupInteractions(spouseGroup, person, spouse);

                    nodeMap.put(person.getId(), spouseGroup);
                    nodeMap.put(spouse.getId(), spouseGroup);
                    processedPeople.add(person.getId());
                    processedPeople.add(spouse.getId());

                    visualizationPane.getChildren().add(spouseGroup);

                    Position layoutPosition = data.getLayoutPosition(person.getId());
                    if (layoutPosition != null) {
                        spouseGroup.setLayoutX(layoutPosition.getX());
                        spouseGroup.setLayoutY(layoutPosition.getY());
                    } else {
                        spouseGroup.setLayoutX(50 + processedPeople.size() * 10 % (visualizationPane.getWidth() - 100));
                        spouseGroup.setLayoutY(50);
                    }
                }
            } else {
                PersonCell personCell = new PersonCell(person);
                addInteractionsToPersonCell(personCell);
                nodeMap.put(person.getId(), personCell);
                processedPeople.add(person.getId());
                visualizationPane.getChildren().add(personCell);

                Position layoutPosition = data.getLayoutPosition(person.getId());
                if (layoutPosition != null) {
                    personCell.setLayoutX(layoutPosition.getX());
                    personCell.setLayoutY(layoutPosition.getY());
                } else {
                    personCell.setLayoutX(50 + processedPeople.size() * 10 % (visualizationPane.getWidth() - 100));
                    personCell.setLayoutY(50);
                }
            }
        }

        // 2. Draw all the connection lines
        for (Person person : data.getAllPeople()) {
            javafx.scene.Node sourceNode = nodeMap.get(person.getId());
            if (sourceNode == null) continue;

            if (person.getFatherId() != null) {
                javafx.scene.Node fatherNode = nodeMap.get(person.getFatherId());
                if (fatherNode != null) {
                    Line line = createConnectionLine(fatherNode, sourceNode);
                    line.setStroke(Color.BLUE);
                    visualizationPane.getChildren().add(0, line);
                }
            }
            if (person.getMotherId() != null) {
                javafx.scene.Node motherNode = nodeMap.get(person.getMotherId());
                if (motherNode != null) {
                    Line line = createConnectionLine(motherNode, sourceNode);
                    line.setStroke(Color.BLUE);
                    visualizationPane.getChildren().add(0, line);
                }
            }

            for (String spouseId : person.getSpouseIds()) {
                if (spouseId.compareTo(person.getId()) > 0) {
                    javafx.scene.Node spouseNode = nodeMap.get(spouseId);
                    if (spouseNode != null) {
                        Line line = createConnectionLine(sourceNode, spouseNode);
                        line.setStroke(Color.ORANGE);
                        line.getStrokeDashArray().addAll(5d, 5d);
                        visualizationPane.getChildren().add(0, line);
                    }
                }
            }
        }
    }

    private Line createConnectionLine(javafx.scene.Node startNode, javafx.scene.Node endNode) {
        Line line = new Line();
        line.setStrokeWidth(2);

        // Bind the start point to the center of the start node
        if (startNode instanceof HBox) {
            HBox spouseGroup = (HBox) startNode;
            line.startXProperty().bind(spouseGroup.layoutXProperty().add(spouseGroup.widthProperty().divide(2)));
            line.startYProperty().bind(spouseGroup.layoutYProperty().add(spouseGroup.heightProperty().divide(2)));
        } else {
            Region startRegion = (Region) startNode;
            line.startXProperty().bind(startRegion.layoutXProperty().add(startRegion.widthProperty().divide(2)));
            line.startYProperty().bind(startRegion.layoutYProperty().add(startRegion.heightProperty().divide(2)));
        }

        // Bind the end point to the center of the end node
        if (endNode instanceof HBox) {
            HBox spouseGroup = (HBox) endNode;
            line.endXProperty().bind(spouseGroup.layoutXProperty().add(spouseGroup.widthProperty().divide(2)));
            line.endYProperty().bind(spouseGroup.layoutYProperty().add(spouseGroup.heightProperty().divide(2)));
        } else {
            Region endRegion = (Region) endNode;
            line.endXProperty().bind(endRegion.layoutXProperty().add(endRegion.widthProperty().divide(2)));
            line.endYProperty().bind(endRegion.layoutYProperty().add(endRegion.heightProperty().divide(2)));
        }

        return line;
    }

    private void addSpouseGroupInteractions(HBox spouseGroup, Person person1, Person person2) {
        final double[] mouseOffset = new double[2];

        spouseGroup.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                clearSelection();
                // Select both people in the group for context menu purposes
                PersonCell person1Cell = (PersonCell) spouseGroup.getChildren().get(0);
                PersonCell person2Cell = (PersonCell) spouseGroup.getChildren().get(1);
                selectedCell = person1Cell;
                person1Cell.getStyleClass().add("selected-cell");
                person2Cell.getStyleClass().add("selected-cell");

                mouseOffset[0] = event.getSceneX() - spouseGroup.getLayoutX();
                mouseOffset[1] = event.getSceneY() - spouseGroup.getLayoutY();
                spouseGroup.toFront();
                event.consume();
            }
        });

        spouseGroup.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                double newX = event.getSceneX() - mouseOffset[0];
                double newY = event.getSceneY() - mouseOffset[1];

                double paneWidth = visualizationPane.getWidth();
                double paneHeight = visualizationPane.getHeight();

                newX = Math.max(0, Math.min(newX, paneWidth - spouseGroup.getWidth()));
                newY = Math.max(0, Math.min(newY, paneHeight - spouseGroup.getHeight()));

                spouseGroup.setLayoutX(newX);
                spouseGroup.setLayoutY(newY);

                // Update the layout positions for both people in the group
                data.setLayoutPosition(person1.getId(), new Position(newX, newY));
                data.setLayoutPosition(person2.getId(), new Position(newX + spouseGroup.getWidth() / 2, newY));

                checkForOverlaps(spouseGroup);

                event.consume();
            }
        });

        // The onMouseReleased for a spouse group should only handle the visual aspect.
        // Relationship creation is now handled by the individual PersonCells.
        spouseGroup.setOnMouseReleased(event -> {
            clearSelection();
            if (overlappingCell != null) {
                overlappingCell.getStyleClass().remove("overlapping-cell");
                overlappingCell = null;
            }
            event.consume();
        });
    }

    private void addInteractionsToPersonCell(PersonCell personCell) {
        // We now add a separate click handler for selection
        personCell.setOnMouseClicked(event -> {
            clearSelection();
            selectedCell = personCell;
            selectedCell.getStyleClass().add("selected-cell");
            event.consume();
        });

        final double[] mouseOffset = new double[2];

        personCell.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                clearSelection();
                selectedCell = personCell;
                selectedCell.getStyleClass().add("selected-cell");
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

                checkForOverlaps(personCell);

                data.setLayoutPosition(personCell.getPerson().getId(), new Position(newX, newY));
                event.consume();
            }
        });

        personCell.setOnMouseReleased(event -> {
            // Check if a cell was dragged onto another cell.
            if (selectedCell != null && overlappingCell != null && selectedCell != overlappingCell) {
                // If a valid overlap was detected during the drag, show the dialog.
                showRelationshipDialog(selectedCell, overlappingCell);
            }

            // Clear selection and overlapping highlights
            if (overlappingCell != null) {
                overlappingCell.getStyleClass().remove("overlapping-cell");
                overlappingCell = null;
            }
            clearSelection();
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
            clearSelection();
            selectedCell = personCell;
            selectedCell.getStyleClass().add("selected-cell");

            contextMenu.show(personCell, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }

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
            if (parentForNew != null && nodeMap.containsKey(parentForNew.getId())) {
                javafx.scene.Node parentNode = nodeMap.get(parentForNew.getId());
                data.setLayoutPosition(p.getId(), new Position(parentNode.getLayoutX(), parentNode.getLayoutY() + 150));
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

        // Also clear selection from spouse group if it was selected
        for(javafx.scene.Node node : nodeMap.values()){
            if(node instanceof HBox){
                ((HBox) node).getChildren().forEach(child -> {
                    if(child.getStyleClass().contains("selected-cell")){
                        child.getStyleClass().remove("selected-cell");
                    }
                });
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

    // New checkForOverlaps method that handles HBoxes
    private void checkForOverlaps(javafx.scene.Node draggedNode) {
        if (overlappingCell != null) {
            overlappingCell.getStyleClass().remove("overlapping-cell");
            overlappingCell = null;
        }

        for (javafx.scene.Node node : visualizationPane.getChildren()) {
            if (node == draggedNode) {
                continue;
            }

            if (node instanceof PersonCell) {
                PersonCell cell = (PersonCell) node;
                javafx.geometry.Bounds draggedBounds = draggedNode.getBoundsInParent();
                javafx.geometry.Bounds cellBounds = cell.getBoundsInParent();

                if (draggedBounds.intersects(cellBounds)) {
                    overlappingCell = cell;
                    overlappingCell.getStyleClass().add("overlapping-cell");
                    return;
                }
            }

            if (node instanceof HBox) {
                HBox spouseGroup = (HBox) node;
                // Check for overlaps with the spouse group as a whole
                javafx.geometry.Bounds draggedBounds = draggedNode.getBoundsInParent();
                javafx.geometry.Bounds groupBounds = spouseGroup.getBoundsInParent();

                if (draggedBounds.intersects(groupBounds)) {
                    // Check which child inside the group the dragged node is overlapping
                    for (javafx.scene.Node child : spouseGroup.getChildren()) {
                        javafx.geometry.Bounds childBounds = child.getBoundsInParent();
                        if (draggedBounds.intersects(childBounds)) {
                            overlappingCell = (PersonCell) child;
                            overlappingCell.getStyleClass().add("overlapping-cell");
                            return;
                        }
                    }
                }
            }
        }
    }

    // Overloaded method for single person cell dragging
    private void checkForOverlaps(PersonCell draggedCell) {
        checkForOverlaps((javafx.scene.Node) draggedCell);
    }


    private void showRelationshipDialog(PersonCell person1Cell, PersonCell person2Cell) {
        Person person1 = person1Cell.getPerson();
        Person person2 = person2Cell.getPerson();

        RelationshipDialog dialog = new RelationshipDialog(person1, person2);
        Optional<RelationshipDialog.RelationshipResult> result = dialog.showAndWait();

        result.ifPresent(relationship -> {
            try {
                switch (relationship.type()) {
                    case PARENT_CHILD -> {
                        Person parent = data.getPerson(relationship.parent().getId());
                        Person child = data.getPerson(relationship.child().getId());
                        data.setParentChildRelationship(parent, child);
                    }
                    case SPOUSE -> {
                        Person spouse1 = data.getPerson(relationship.spouse1().getId());
                        Person spouse2 = data.getPerson(relationship.spouse2().getId());
                        data.setSpouseRelationship(spouse1, spouse2);
                    }
                }
                data.linkAllRelationships();
                refresh();
            } catch (IllegalStateException e) {
                showAlert(Alert.AlertType.WARNING, "Relationship Error", e.getMessage());
                refresh();
            }
        });
    }
}
