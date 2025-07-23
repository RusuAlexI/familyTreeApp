package com.familytree;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.image.ImageView;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

import java.util.HashMap;
import java.util.Map;

public class TreeVisualizer {
    private final Pane view = new Pane();
    private final Map<String, VBox> nodeMap = new HashMap<>();
    private FamilyTreeData data;

    private VBox selectedNode;
    private boolean draggingNode = false;
    private double dragStartX, dragStartY;

    public Pane getView() {
        return view;
    }

    public boolean isDraggingNode() {
        return draggingNode;
    }

    public void refresh() {
        data = FamilyTreeData.getInstance();
        System.out.println("DEBUG: TreeVisualizer refresh() called. Current data instance hash: " + data.hashCode());
        System.out.println("DEBUG: Number of persons in data.getAllPeople() for refresh: " + data.getAllPeople().size());

        // --- NEW: 1. Capture the ID of the currently selected person BEFORE clearing ---
        String previouslySelectedPersonId = null;
        if (selectedNode != null) {
            // Find the ID associated with the VBox that was previously selected.
            // This loop is needed because selectedNode might be an old reference after a refresh cycle.
            for (Map.Entry<String, VBox> entry : nodeMap.entrySet()) {
                if (entry.getValue() == selectedNode) {
                    previouslySelectedPersonId = entry.getKey();
                    break;
                }
            }
        }

        view.getChildren().clear();
        nodeMap.clear();

        // Recreate all nodes
        int personCount = 0;
        for (Person person : data.getAllPeople()) {
            System.out.println("DEBUG: Processing person: " + person.getName() + " (ID: " + person.getId() + ")");
            VBox node = createPersonNode(person);
            node.setLayoutX(person.getX());
            node.setLayoutY(person.getY());
            System.out.println("DEBUG: Node for " + person.getName() + " set to X: " + person.getX() + ", Y: " + person.getY());
            view.getChildren().add(node);
            nodeMap.put(person.getId(), node);
            personCount++;
        }
        System.out.println("DEBUG: " + personCount + " person nodes added to view.getChildren().");
        System.out.println("DEBUG: Total children in view pane: " + view.getChildren().size());

        // --- NEW: 2. After all new nodes are in nodeMap, re-establish selectedNode and highlight ---
        selectedNode = null; // Clear old reference
        if (previouslySelectedPersonId != null) {
            selectedNode = nodeMap.get(previouslySelectedPersonId); // Get the NEW VBox instance
        }
        highlightSelectedNode(); // Apply highlighting based on the potentially new selectedNode

        // Draw connections after all nodes are created and positioned
        for (Person person : data.getAllPeople()) {
            // Draw lines to children (Mother-Child and Father-Child)
            if (person.getMotherId() != null) {
                Person mother = data.findById(person.getMotherId());
                if (mother != null) {
                    drawConnection(mother, person, Color.BLUE);
                }
            }
            if (person.getFatherId() != null) {
                Person father = data.findById(person.getFatherId());
                if (father != null) {
                    drawConnection(father, person, Color.BLUE);
                }
            }

            // Draw lines to spouses
            for (String spouseId : person.getSpouseIds()) {
                if (person.getId().compareTo(spouseId) < 0) { // Only draw once for each couple
                    Person spouse = data.findById(spouseId);
                    if (spouse != null) {
                        drawConnection(person, spouse, Color.GREEN);
                    }
                }
            }
        }
    }


    private VBox createPersonNode(Person person) {
        ImageView photoView = new ImageView();
        photoView.setFitHeight(50);
        photoView.setFitWidth(50);
        photoView.setPreserveRatio(true);
        if (person.getPhotoBase64() != null && !person.getPhotoBase64().isEmpty()) {
            photoView.setImage(Person.base64ToImage(person.getPhotoBase64()));
        }

        Label nameLabel = new Label(person.getName());
        VBox node = new VBox(5, photoView, nameLabel);
        node.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-padding: 5; -fx-background-color: yellow;");
        node.setPrefSize(100, 100);

        // Event handlers for dragging and selection
        node.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                draggingNode = true;
                selectedNode = node; // Set selectedNode to the CURRENT VBox being pressed
                dragStartX = event.getSceneX() - node.getLayoutX();
                dragStartY = event.getSceneY() - node.getLayoutY();
                highlightSelectedNode(); // Apply highlight immediately on press
                event.consume(); // Consume the event so it doesn't bubble to parent for panning
            }
        });

        node.setOnMouseDragged(event -> {
            if (draggingNode && event.getButton() == MouseButton.PRIMARY) {
                node.setLayoutX(event.getSceneX() - dragStartX);
                node.setLayoutY(event.getSceneY() - dragStartY);
                person.setX(node.getLayoutX());
                person.setY(node.getLayoutY());
                // REMOVED: refresh(); // Do NOT call refresh here
                event.consume(); // Consume the event so it doesn't bubble to parent for panning
            }
        });

        node.setOnMouseReleased(event -> {
            draggingNode = false;
            if (event.getButton() == MouseButton.PRIMARY) {
                person.setX(node.getLayoutX());
                person.setY(node.getLayoutY());
                refresh(); // Call refresh ONLY here, after drag is complete
            }
            // No need to consume here. If the event was consumed by Pressed/Dragged, it won't reach parent anyway.
        });

        node.setOnContextMenuRequested(event -> {
            // Set selectedNode to the CURRENT VBox when context menu is requested
            selectedNode = node;
            highlightSelectedNode(); // Ensure it's highlighted for context actions

            ContextMenu contextMenu = new ContextMenu();
            MenuItem editItem = new MenuItem("Edit Person");
            MenuItem addChildItem = new MenuItem("Add Child");
            MenuItem addSpouseItem = new MenuItem("Add Spouse");
            MenuItem deleteItem = new MenuItem("Delete Person");

            editItem.setOnAction(e -> editSelectedPerson());
            // For addChildItem and addSpouseItem, we need to pass the 'person' associated with 'node'
            addChildItem.setOnAction(e -> addPerson(person)); // Pass the correct person object
            addSpouseItem.setOnAction(e -> {
                TextInputDialog dialog = new TextInputDialog("New Spouse");
                dialog.setTitle("Add Spouse");
                dialog.setHeaderText("Enter name for new spouse:");
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(name -> {
                    Person newSpouse = new Person(name);
                    data.addPerson(newSpouse);
                    data.setSpouseRelationship(person.getId(), newSpouse.getId()); // Use 'person' directly
                    refresh();
                });
            });
            deleteItem.setOnAction(e -> deleteSelectedPerson()); // This will use the selectedNode set above

            contextMenu.getItems().addAll(editItem, addChildItem, addSpouseItem, deleteItem);
            contextMenu.show(node, event.getScreenX(), event.getScreenY());
            event.consume(); // Consume context menu event to prevent parent processing
        });

        return node;
    }

    private void highlightSelectedNode() {
        // Reset all nodes to base style first
        nodeMap.values().forEach(n -> n.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-padding: 5; -fx-background-color: yellow; -fx-pref-width: 100; -fx-pref-height: 100;"));

        // Apply highlight to selected node
        if (selectedNode != null) {
            selectedNode.setStyle("-fx-border-color: blue; -fx-border-width: 3; -fx-padding: 5; -fx-background-color: lightblue; -fx-pref-width: 100; -fx-pref-height: 100;");
        }
    }

    private void drawConnection(Person p1, Person p2, Color color) {
        Node node1 = nodeMap.get(p1.getId());
        Node node2 = nodeMap.get(p2.getId());

        if (node1 == null || node2 == null) return;

        double startX = node1.getLayoutX() + node1.getBoundsInLocal().getWidth() / 2;
        double startY = node1.getLayoutY() + node1.getBoundsInLocal().getHeight() / 2;
        double endX = node2.getLayoutX() + node2.getBoundsInLocal().getWidth() / 2;
        double endY = node2.getLayoutY() + node2.getBoundsInLocal().getHeight() / 2;

        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(color);
        line.setStrokeWidth(2);
        view.getChildren().add(0, line);
    }

    public void addPerson(Person parent) {
        data = FamilyTreeData.getInstance(); // Ensure data is up-to-date
        Person newPerson = new Person("New Person");
        data.addPerson(newPerson);
        if (parent != null) {
            newPerson.setFatherId(parent.getId());
            if (!parent.getChildrenIds().contains(newPerson.getId())) {
                parent.getChildrenIds().add(newPerson.getId());
            }
        }
        if (parent != null && nodeMap.containsKey(parent.getId())) {
            Node parentNode = nodeMap.get(parent.getId());
            newPerson.setX(parentNode.getLayoutX());
            newPerson.setY(parentNode.getLayoutY() + 150);
        } else {
            newPerson.setX(200);
            newPerson.setY(200);
        }
        refresh();
    }

    // --- NEW: Method to deselect all nodes ---
    public void deselectAllNodes() {
        selectedNode = null;
        highlightSelectedNode(); // This will apply the default style to all nodes
    }

    public void editSelectedPerson() {
        data = FamilyTreeData.getInstance();
        if (selectedNode == null) {
            System.out.println("DEBUG: No node selected for editing.");
            return;
        }
        // Iterate through the current nodeMap to find the Person corresponding to selectedNode
        // This is necessary because selectedNode might be an old reference if refresh() occurred
        String personIdToEdit = null;
        for (Map.Entry<String, VBox> entry : nodeMap.entrySet()) {
            if (entry.getValue() == selectedNode) {
                personIdToEdit = entry.getKey();
                break;
            }
        }

        if (personIdToEdit != null) {
            Person person = data.findById(personIdToEdit);
            if (person != null) {
                PersonDialog.editPerson(person, updated -> {
                    refresh();
                });
            } else {
                System.out.println("DEBUG: Person not found with ID: " + personIdToEdit);
            }
        } else {
            System.out.println("DEBUG: Selected node's ID not found in current nodeMap for editing.");
        }
    }

    public void deleteSelectedPerson() {
        data = FamilyTreeData.getInstance();
        if (selectedNode == null) {
            System.out.println("DEBUG: No node selected for deletion.");
            return;
        }

        String toRemoveId = null;
        for (Map.Entry<String, VBox> entry : nodeMap.entrySet()) {
            if (entry.getValue() == selectedNode) {
                toRemoveId = entry.getKey();
                break;
            }
        }

        if (toRemoveId != null) {
            data.removePerson(toRemoveId);
            selectedNode = null; // Clear selection after deletion
            refresh();
        } else {
            System.out.println("DEBUG: Selected node's ID not found in current nodeMap for deletion.");
        }
    }
}