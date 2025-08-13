package com.familytree;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polyline;

public class TreeVisualizer {

    private final Pane visualizationPane;
    private final FamilyTreeData data = FamilyTreeData.getInstance();
    private final Map<String, javafx.scene.Node> nodeMap = new HashMap<>(); // Using Node to hold PersonCells or HBoxes

    private PersonCell selectedCell;
    private PersonCell overlappingCell;

    // We now keep a reference to the dragged node (which can be a PersonCell or an HBox)
    private javafx.scene.Node draggedNode;

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

                // Fix for issue 2: Only move the PersonCells and HBoxes.
                // The Line objects will follow automatically due to the bindings.
                for (javafx.scene.Node node : visualizationPane.getChildren()) {
                    if (node instanceof PersonCell || node instanceof HBox) {
                        node.setLayoutX(node.getLayoutX() + deltaX);
                        node.setLayoutY(node.getLayoutY() + deltaY);
                    }
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
        draggedNode = null; // Clear the dragged node on refresh

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
                    // Fix for issue 3: Ensure a position is always set, even on the first refresh.
                    if (layoutPosition != null) {
                        spouseGroup.setLayoutX(layoutPosition.getX());
                        spouseGroup.setLayoutY(layoutPosition.getY());
                    } else {
                        double paneWidth = visualizationPane.getWidth() > 0 ? visualizationPane.getWidth() : 800;
                        double defaultX = 50 + processedPeople.size() * 10 % (paneWidth - 100);
                        spouseGroup.setLayoutX(defaultX);
                        spouseGroup.setLayoutY(50);
                        // Save the default position so it's not "teleported" later
                        data.setLayoutPosition(person.getId(), new Position(defaultX, 50));
                        data.setLayoutPosition(spouse.getId(), new Position(defaultX + spouseGroup.getWidth() / 2, 50));
                    }
                }
            } else {
                PersonCell personCell = new PersonCell(person);
                addInteractionsToPersonCell(personCell);
                nodeMap.put(person.getId(), personCell);
                processedPeople.add(person.getId());
                visualizationPane.getChildren().add(personCell);

                Position layoutPosition = data.getLayoutPosition(person.getId());
                // Fix for issue 3: Ensure a position is always set, even on the first refresh.
                if (layoutPosition != null) {
                    personCell.setLayoutX(layoutPosition.getX());
                    personCell.setLayoutY(layoutPosition.getY());
                } else {
                    double paneWidth = visualizationPane.getWidth() > 0 ? visualizationPane.getWidth() : 800;
                    double paneHeight = visualizationPane.getHeight() > 0 ? visualizationPane.getHeight() : 600;

                    double defaultX = 50 + processedPeople.size() * 10 % (paneWidth - 100);
                    double defaultY = 50 + processedPeople.size() * 10 % (paneHeight - 120);

                    defaultX = Math.max(0, Math.min(defaultX, paneWidth - 100));
                    defaultY = Math.max(0, Math.min(defaultY, paneHeight - 120));

                    personCell.setLayoutX(defaultX);
                    personCell.setLayoutY(defaultY);
                    // Save the default position so it's not "teleported" later
                    data.setLayoutPosition(person.getId(), new Position(defaultX, defaultY));
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
                    Group line = createConnectionLine(fatherNode, sourceNode);
//                    line.setStroke(Color.BLUE);
                    visualizationPane.getChildren().add(0, line);
                }
            }
            if (person.getMotherId() != null) {
                javafx.scene.Node motherNode = nodeMap.get(person.getMotherId());
                if (motherNode != null) {
                    Group line = createConnectionLine(motherNode, sourceNode);
//                    line.setStroke(Color.BLUE);
                    visualizationPane.getChildren().add(0, line);
                }
            }

            for (String spouseId : person.getSpouseIds()) {
                if (spouseId.compareTo(person.getId()) > 0) {
                    javafx.scene.Node spouseNode = nodeMap.get(spouseId);
                    if (spouseNode != null) {
                        Group line = createConnectionLine(sourceNode, spouseNode);
//                        line.setStroke(Color.ORANGE);
//                        line.getStrokeDashArray().addAll(5d, 5d);
                        visualizationPane.getChildren().add(0, line);
                    }
                }
            }
        }

//        drawRelationships();
    }


    /**
     * Creates a connection line between two nodes with a 90-degree angle.
     * The line is composed of three dynamically bound Line objects, ensuring it
     * moves with the connected nodes.
     *
     * @param startNode The starting node for the connection.
     * @param endNode The ending node for the connection.
     * @return A Group object containing the three Line segments that form the angled connection.
     */
    private Group createConnectionLine(javafx.scene.Node startNode, javafx.scene.Node endNode) {
        // Create a group to hold the three line segments
        Group lineGroup = new Group();

        // Bindings for the center of the start node
        DoubleBinding startX, startY;
        if (startNode instanceof HBox) {
            HBox spouseGroup = (HBox) startNode;
            startX = spouseGroup.layoutXProperty().add(spouseGroup.widthProperty().divide(2));
            startY = spouseGroup.layoutYProperty().add(spouseGroup.heightProperty().divide(2));
        } else {
            Region startRegion = (Region) startNode;
            startX = startRegion.layoutXProperty().add(startRegion.widthProperty().divide(2));
            startY = startRegion.layoutYProperty().add(startRegion.heightProperty().divide(2));
        }

        // Bindings for the center of the end node
        DoubleBinding endX, endY;
        if (endNode instanceof HBox) {
            HBox spouseGroup = (HBox) endNode;
            endX = spouseGroup.layoutXProperty().add(spouseGroup.widthProperty().divide(2));
            endY = spouseGroup.layoutYProperty().add(spouseGroup.heightProperty().divide(2));
        } else {
            Region endRegion = (Region) endNode;
            endX = endRegion.layoutXProperty().add(endRegion.widthProperty().divide(2));
            endY = endRegion.layoutYProperty().add(endRegion.heightProperty().divide(2));
        }

        // Calculate the midpoint of the Y coordinates. This will be the location of the horizontal segment.
        DoubleBinding midY = Bindings.createDoubleBinding(
                () -> startY.get() + (endY.get() - startY.get()) / 2,
                startY, endY
        );

        // --- Create the three line segments ---

        // Line 1: Vertical segment from the start node
        Line line1 = new Line();
        line1.startXProperty().bind(startX);
        line1.startYProperty().bind(startY);
        line1.endXProperty().bind(startX); // Stays at the same X
        line1.endYProperty().bind(midY); // Ends at the midpoint Y

        // Line 2: Horizontal segment at the midpoint
        Line line2 = new Line();
        line2.startXProperty().bind(startX); // Starts at the start node's X
        line2.startYProperty().bind(midY); // Stays at the midpoint Y
        line2.endXProperty().bind(endX); // Ends at the end node's X
        line2.endYProperty().bind(midY); // Stays at the midpoint Y

        // Line 3: Vertical segment to the end node
        Line line3 = new Line();
        line3.startXProperty().bind(endX); // Starts at the end node's X
        line3.startYProperty().bind(midY); // Starts at the midpoint Y
        line3.endXProperty().bind(endX); // Stays at the same X
        line3.endYProperty().bind(endY); // Ends at the center of the end node

        // Apply common styling
        line1.setStrokeWidth(2);
        line2.setStrokeWidth(2);
        line3.setStrokeWidth(2);
        line1.setStroke(Color.BLACK);
        line2.setStroke(Color.BLACK);
        line3.setStroke(Color.BLACK);

        // Add the lines to the group
        lineGroup.getChildren().addAll(line1, line2, line3);

        return lineGroup;
    }



    /**
     * This method adds drag-and-drop interactions to the HBox that represents a spouse group.
     * The drag handler now ensures the entire group moves as a single unit.
     */
    private void addSpouseGroupInteractions(HBox spouseGroup, Person person1, Person person2) {
        final double[] mouseOffset = new double[2];

        spouseGroup.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                clearSelection();
                // Select both people in the group for context menu purposes
                PersonCell person1Cell = (PersonCell) spouseGroup.getChildren().get(0);
                PersonCell person2Cell = (PersonCell) spouseGroup.getChildren().get(1);
                selectedCell = person1Cell; // Use one of them as the selected cell for context menu
                person1Cell.getStyleClass().add("selected-cell");
                person2Cell.getStyleClass().add("selected-cell");

                draggedNode = spouseGroup; // Set the dragged node to the HBox
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

        spouseGroup.setOnMouseReleased(event -> {
            // The onMouseReleased for a spouse group should not trigger relationship dialogs,
            // as a group itself cannot form a relationship with another cell.
            clearOverlapHighlighting();
            clearSelection();
            draggedNode = null;
            event.consume();
        });
    }

    /**
     * This method adds drag-and-drop and context menu interactions to a single PersonCell.
     * The drag handler now correctly identifies if a cell is part of a spouse group and
     * handles the drag accordingly.
     */
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

                // Check if the cell is part of a spouse group. If so, the dragged node is the group.
                if (personCell.getParent() instanceof HBox) {
                    draggedNode = (HBox) personCell.getParent();
                    HBox spouseGroup = (HBox) draggedNode;
                    mouseOffset[0] = event.getSceneX() - spouseGroup.getLayoutX();
                    mouseOffset[1] = event.getSceneY() - spouseGroup.getLayoutY();
                } else {
                    draggedNode = personCell;
                    mouseOffset[0] = event.getSceneX() - personCell.getLayoutX();
                    mouseOffset[1] = event.getSceneY() - personCell.getLayoutY();
                }
                draggedNode.toFront();
                event.consume();
            }
        });

        personCell.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown() && draggedNode != null) {
                double newX = event.getSceneX() - mouseOffset[0];
                double newY = event.getSceneY() - mouseOffset[1];

                double paneWidth = visualizationPane.getWidth();
                double paneHeight = visualizationPane.getHeight();

                if (draggedNode instanceof PersonCell) {
                    PersonCell cell = (PersonCell) draggedNode;
                    newX = Math.max(0, Math.min(newX, paneWidth - cell.getWidth()));
                    newY = Math.max(0, Math.min(newY, paneHeight - cell.getHeight()));
                    cell.setLayoutX(newX);
                    cell.setLayoutY(newY);
                    data.setLayoutPosition(cell.getPerson().getId(), new Position(newX, newY));
                } else if (draggedNode instanceof HBox) {
                    HBox spouseGroup = (HBox) draggedNode;
                    newX = Math.max(0, Math.min(newX, paneWidth - spouseGroup.getWidth()));
                    newY = Math.max(0, Math.min(newY, paneHeight - spouseGroup.getHeight()));
                    spouseGroup.setLayoutX(newX);
                    spouseGroup.setLayoutY(newY);
                    // Update positions for both people in the group
                    PersonCell p1 = (PersonCell) spouseGroup.getChildren().get(0);
                    PersonCell p2 = (PersonCell) spouseGroup.getChildren().get(1);
                    data.setLayoutPosition(p1.getPerson().getId(), new Position(newX, newY));
                    data.setLayoutPosition(p2.getPerson().getId(), new Position(newX + spouseGroup.getWidth() / 2, newY));
                }

                checkForOverlaps(draggedNode);
                event.consume();
            }
        });

        /**
         * Refactored logic to fix the drag-and-drop issue.
         * The key change is to iterate over all children to find an overlap
         * with an HBox, and then get the PersonCell from that HBox.
         */
        personCell.setOnMouseReleased(event -> {
            if (draggedNode instanceof PersonCell) {
                PersonCell draggedPersonCell = (PersonCell) draggedNode;
                PersonCell dropTargetCell = null;

                for (javafx.scene.Node node : visualizationPane.getChildren()) {
                    if (node == draggedPersonCell || !(node instanceof PersonCell || node instanceof HBox)) {
                        continue;
                    }

                    javafx.geometry.Bounds draggedBounds = draggedPersonCell.getBoundsInParent();
                    javafx.geometry.Bounds nodeBounds = node.getBoundsInParent();

                    if (draggedBounds.intersects(nodeBounds)) {
                        if (node instanceof PersonCell) {
                            dropTargetCell = (PersonCell) node;
                            break;
                        } else if (node instanceof HBox) {
                            // If dropped on an HBox (spouse group), pick the first PersonCell inside it as the target.
                            // The logic in showRelationshipDialog handles the spouse correctly.
                            HBox spouseGroup = (HBox) node;
                            if (!spouseGroup.getChildren().isEmpty() && spouseGroup.getChildren().get(0) instanceof PersonCell) {
                                dropTargetCell = (PersonCell) spouseGroup.getChildren().get(0);
                                break;
                            }
                        }
                    }
                }

                if (dropTargetCell != null && draggedPersonCell != dropTargetCell) {
                    showRelationshipDialog(draggedPersonCell, dropTargetCell);
                }
            }

            clearOverlapHighlighting();
            clearSelection();
            draggedNode = null;
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
            // Set the appropriate parent based on gender
            if ("Male".equals(parentForNew.getGender())) {
                newPerson.setFatherId(parentForNew.getId());
            } else {
                newPerson.setMotherId(parentForNew.getId());
            }
            // Check for spouse and add the other parent
            if (parentForNew.getSpouseIds() != null && !parentForNew.getSpouseIds().isEmpty()) {
                String spouseId = parentForNew.getSpouseIds().iterator().next();
                Person spouse = data.getPerson(spouseId);
                if (spouse != null) {
                    if ("Male".equals(spouse.getGender())) {
                        newPerson.setFatherId(spouse.getId());
                    } else {
                        newPerson.setMotherId(spouse.getId());
                    }
                }
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

    /**
     * Refactored method to handle auto-linking of a spouse when a parent is added during an edit.
     * The method now compares the old and new parent IDs and, if a new parent is set, checks
     * if that parent has a spouse. If so, it presents a confirmation dialog.
     */
    public void editSelectedPerson() {
        if (selectedCell != null && selectedCell.getPerson() != null) {
            Person originalPerson = selectedCell.getPerson();
//            Person personToEdit = new Person(originalPerson); // Create a copy to edit in the dialog
            Person personToEdit = selectedCell.getPerson();; // Create a copy to edit in the dialog

            PersonDialog dialog = new PersonDialog(personToEdit);
            dialog.setResizable(true);
            Optional<Person> result = dialog.showAndWait();

            result.ifPresent(updatedPerson -> {
                // Check if a new parent has been set in the dialog
                boolean fatherChanged = !Objects.equals(originalPerson.getFatherId(), updatedPerson.getFatherId());
                boolean motherChanged = !Objects.equals(originalPerson.getMotherId(), updatedPerson.getMotherId());

                Person newParent = null;
                if (fatherChanged && updatedPerson.getFatherId() != null) {
                    newParent = data.getPerson(updatedPerson.getFatherId());
                } else if (motherChanged && updatedPerson.getMotherId() != null) {
                    newParent = data.getPerson(updatedPerson.getMotherId());
                }

                if (newParent != null && newParent.getSpouseIds() != null && !newParent.getSpouseIds().isEmpty()) {
                    String spouseId = newParent.getSpouseIds().iterator().next();
                    Person spouse = data.getPerson(spouseId);

                    // Check if the other parent is not already set
                    boolean otherParentNotSet = ("Male".equals(newParent.getGender()) && updatedPerson.getMotherId() == null) ||
                            ("Female".equals(newParent.getGender()) && updatedPerson.getFatherId() == null);

                    if (spouse != null && otherParentNotSet) {
                        showSetOtherParentDialog(updatedPerson, newParent, spouse);
                    } else {
                        data.updatePerson(updatedPerson);
                        data.linkAllRelationships();
                        refresh();
                    }
                } else {
                    data.updatePerson(updatedPerson);
                    data.linkAllRelationships();
                    refresh();
                }
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "No Person Selected", "Please select a person to edit.");
        }
    }

    /**
     * Helper dialog for confirming the addition of the other parent when editing a person.
     * This is a new method created to address the second issue.
     */
    private void showSetOtherParentDialog(Person child, Person newParent, Person spouse) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Set Other Parent");
        dialog.setHeaderText("Do you want to set " + spouse.getName() + " as the other parent?");

        VBox vbox = new VBox(10,
                new Label(newParent.getName() + " is in a couple with " + spouse.getName() + "."),
                new Label("This will set " + spouse.getName() + " as " + child.getName() + "'s " +
                        ("Male".equals(spouse.getGender()) ? "father." : "mother.")),
                new Label("Do you want to proceed?")
        );
        dialog.getDialogPane().setContent(vbox);

        ButtonType confirmButton = new ButtonType("Yes");
        ButtonType cancelButton = new ButtonType("No");
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton, cancelButton);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == confirmButton) {
            if ("Male".equals(spouse.getGender())) {
                child.setFatherId(spouse.getId());
            } else {
                child.setMotherId(spouse.getId());
            }
        }
        data.updatePerson(child);
        data.linkAllRelationships();
        refresh();
    }

    public void deleteSelectedPerson() {
        if (selectedCell != null && selectedCell.getPerson() != null) {
            data.removePerson(selectedCell.getPerson());
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

    private void clearOverlapHighlighting() {
        if (overlappingCell != null) {
            overlappingCell.getStyleClass().remove("overlapping-cell");
            overlappingCell = null;
        }
    }

    /**
     * Checks for overlaps between the dragged node and other nodes.
     * This method is now more robust and handles both single cells and HBoxes.
     */
    private void checkForOverlaps(javafx.scene.Node draggedNode) {
        clearOverlapHighlighting();

        // If the dragged node is a spouse group, do not allow overlaps
        if (draggedNode instanceof HBox) {
            return;
        }

        for (javafx.scene.Node node : visualizationPane.getChildren()) {
            if (node == draggedNode || !(node instanceof PersonCell || node instanceof HBox)) {
                continue;
            }

            javafx.geometry.Bounds draggedBounds = draggedNode.getBoundsInParent();
            javafx.geometry.Bounds nodeBounds = node.getBoundsInParent();

            if (draggedBounds.intersects(nodeBounds)) {
                if (node instanceof PersonCell) {
                    // Overlapping with a single person cell
                    PersonCell cell = (PersonCell) node;
                    if (draggedNode != cell) {
                        overlappingCell = cell;
                        overlappingCell.getStyleClass().add("overlapping-cell");
                        return;
                    }
                } else if (node instanceof HBox) {
                    // Overlapping with a spouse group
                    HBox spouseGroup = (HBox) node;
                    for (javafx.scene.Node child : spouseGroup.getChildren()) {
                        javafx.geometry.Bounds childBounds = child.getBoundsInParent();
                        if (draggedBounds.intersects(childBounds)) {
                            PersonCell cell = (PersonCell) child;
                            if (draggedNode != cell) {
                                overlappingCell = cell;
                                overlappingCell.getStyleClass().add("overlapping-cell");
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This method correctly determines the relationship type based on the drop target.
     */
    private void showRelationshipDialog(PersonCell person1Cell, PersonCell person2Cell) {
        Person person1 = person1Cell.getPerson();
        Person person2 = person2Cell.getPerson();
        Person father = null;
        Person mother = null;

        // Check if the dropped-on cell is part of a spouse group.
        boolean isSpouseGroup = person2Cell.getParent() instanceof HBox;

        if (isSpouseGroup) {
            // Drop onto a couple. We will make person1 a child of this couple.
            HBox spouseGroup = (HBox) person2Cell.getParent();
            PersonCell p1InGroup = (PersonCell) spouseGroup.getChildren().get(0);
            PersonCell p2InGroup = (PersonCell) spouseGroup.getChildren().get(1);

            // Determine father and mother from the group
            if ("Male".equals(p1InGroup.getPerson().getGender())) {
                father = p1InGroup.getPerson();
                mother = p2InGroup.getPerson();
            } else {
                father = p2InGroup.getPerson();
                mother = p1InGroup.getPerson();
            }

            showSetParentDialog(person1, father, mother);
        } else {
            // Drop onto a single person. We will ask if it's a child or a spouse.
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Set Relationship");
            dialog.setHeaderText("How do you want to relate " + person1.getName() + " to " + person2.getName() + "?");

            ButtonType childButton = new ButtonType("Set as Child");
            ButtonType spouseButton = new ButtonType("Set as Spouse");
            ButtonType cancelButton = new ButtonType("Cancel");
            dialog.getDialogPane().getButtonTypes().addAll(childButton, spouseButton, cancelButton);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent()) {
                if (result.get() == childButton) {
                    try {
                        data.setParentChildRelationship(person2, person1);
                        refresh();
                    } catch (IllegalStateException e) {
                        showAlert(Alert.AlertType.WARNING, "Relationship Error", e.getMessage());
                        refresh();
                    }
                } else if (result.get() == spouseButton) {
                    try {
                        data.setSpouseRelationship(person1, person2);
                        refresh();
                    } catch (IllegalStateException e) {
                        showAlert(Alert.AlertType.WARNING, "Relationship Error", e.getMessage());
                        refresh();
                    }
                } else {
                    refresh(); // Refresh to clear any highlighting/selection
                }
            } else {
                refresh();
            }
        }
    }

    /**
     * This new dialog is called when a user drops a person onto a couple (HBox).
     * It sets both people in the couple as parents to the dropped person.
     */
    private void showSetParentDialog(Person child, Person father, Person mother) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Set Parent-Child Relationship");
        dialog.setHeaderText("Set " + child.getName() + " as the child of " + father.getName() + " and " + mother.getName() + "?");

        VBox vbox = new VBox(10,
                new Label("This will set both " + father.getName() + " and " + mother.getName() + " as parents of " + child.getName() + "."),
                new Label("Do you want to proceed?")
        );
        dialog.getDialogPane().setContent(vbox);

        ButtonType confirmButton = new ButtonType("Confirm");
        ButtonType cancelButton = new ButtonType("Cancel");
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton, cancelButton);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == confirmButton) {
            try {
                // Set the father and mother
                child.setFatherId(father.getId());
                child.setMotherId(mother.getId());
                // Add the child to the parents' child lists
                father.addChildId(child.getId());
                mother.addChildId(child.getId());

                data.linkAllRelationships();
                refresh();
            } catch (IllegalStateException e) {
                showAlert(Alert.AlertType.WARNING, "Relationship Error", e.getMessage());
                refresh();
            }
        } else {
            refresh(); // Refresh to clear any highlighting/selection
        }
    }
}
