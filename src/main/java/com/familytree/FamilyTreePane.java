package com.familytree;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.KeyCode;
import javafx.scene.control.Button;
import java.util.*;

public class FamilyTreePane extends Pane {

    private final Map<Person, StackPane> personNodeMap = new HashMap<>();
    private final Group lineGroup = new Group();
    private final Group contentGroup = new Group();  // Zoom/pan group

    private double scale = 1.0;
    private double mouseAnchorX, mouseAnchorY;
    private double translateAnchorX, translateAnchorY;

    private Person selectedPerson;

    public FamilyTreePane() {
        contentGroup.getChildren().add(lineGroup);
        getChildren().add(contentGroup);
        // Handle keyboard zoom and pan
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.PLUS || e.getCode() == KeyCode.EQUALS) {
                zoomAt(getWidth() / 2, getHeight() / 2, 1.2);
            } else if (e.getCode() == KeyCode.MINUS) {
                zoomAt(getWidth() / 2, getHeight() / 2, 1 / 1.2);
            } else if (e.getCode() == KeyCode.DIGIT0) {
                resetZoomAndPan();
            } else if (e.getCode() == KeyCode.LEFT) {
                contentGroup.setTranslateX(contentGroup.getTranslateX() + 20);
            } else if (e.getCode() == KeyCode.RIGHT) {
                contentGroup.setTranslateX(contentGroup.getTranslateX() - 20);
            } else if (e.getCode() == KeyCode.UP) {
                contentGroup.setTranslateY(contentGroup.getTranslateY() + 20);
            } else if (e.getCode() == KeyCode.DOWN) {
                contentGroup.setTranslateY(contentGroup.getTranslateY() - 20);
            }
        });

        // Focus needed to receive key events
        setFocusTraversable(true);

        // Optional: Add reset button to UI
        Button resetButton = new Button("Reset Zoom");
        resetButton.setOnAction(e -> resetZoomAndPan());
        resetButton.setLayoutX(10);
        resetButton.setLayoutY(10);
        getChildren().add(resetButton);

        // Zoom with scroll
        setOnScroll(e -> {
            double delta = 1.2;
            double oldScale = scale;
            if (e.getDeltaY() < 0) {
                scale /= delta;
            } else {
                scale *= delta;
            }
            scale = clamp(scale, 0.2, 5);
            double factor = scale / oldScale;

            contentGroup.setScaleX(scale);
            contentGroup.setScaleY(scale);

            double dx = e.getX() - (contentGroup.getBoundsInParent().getWidth() / 2);
            double dy = e.getY() - (contentGroup.getBoundsInParent().getHeight() / 2);
            contentGroup.setTranslateX(contentGroup.getTranslateX() - factor * dx + dx);
            contentGroup.setTranslateY(contentGroup.getTranslateY() - factor * dy + dy);
        });

        // Pan with mouse drag
        setOnMousePressed(e -> {
            mouseAnchorX = e.getSceneX();
            mouseAnchorY = e.getSceneY();
            translateAnchorX = contentGroup.getTranslateX();
            translateAnchorY = contentGroup.getTranslateY();
        });

        setOnMouseDragged(e -> {
            contentGroup.setTranslateX(translateAnchorX + e.getSceneX() - mouseAnchorX);
            contentGroup.setTranslateY(translateAnchorY + e.getSceneY() - mouseAnchorY);
        });
    }

    public Person getSelectedPerson() {
        return selectedPerson;
    }
    private void zoomAt(double pivotX, double pivotY, double zoomFactor) {
        double oldScale = scale;
        scale *= zoomFactor;
        scale = clamp(scale, 0.2, 5);
        double factor = scale / oldScale;

        contentGroup.setScaleX(scale);
        contentGroup.setScaleY(scale);

        double dx = pivotX - (contentGroup.getBoundsInParent().getWidth() / 2);
        double dy = pivotY - (contentGroup.getBoundsInParent().getHeight() / 2);
        contentGroup.setTranslateX(contentGroup.getTranslateX() - factor * dx + dx);
        contentGroup.setTranslateY(contentGroup.getTranslateY() - factor * dy + dy);
    }

    private void resetZoomAndPan() {
        scale = 1.0;
        contentGroup.setScaleX(scale);
        contentGroup.setScaleY(scale);
        contentGroup.setTranslateX(0);
        contentGroup.setTranslateY(0);
    }

    public void drawTree(List<Person> persons) {
        contentGroup.getChildren().clear();
        personNodeMap.clear();
        lineGroup.getChildren().clear();
        contentGroup.getChildren().add(lineGroup);

        double horizontalSpacing = 120;
        double verticalSpacing = 150;
        double nodeWidth = 120;
        double nodeHeight = 60;

        List<Person> roots = new ArrayList<>();
        for (Person person : persons) {
            if (person.getParents().isEmpty()) {
                roots.add(person);
            }
        }

        Map<Person, Integer> levels = new HashMap<>();
        Set<Person> visited = new HashSet<>();

        for (Person root : roots) {
            assignLevels(root, 0, levels, visited);
        }

        Map<Integer, List<Person>> levelMap = new TreeMap<>();
        for (Map.Entry<Person, Integer> entry : levels.entrySet()) {
            levelMap.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        Map<Person, Double> xPositions = new HashMap<>();
        double currentY = 50;

        for (Map.Entry<Integer, List<Person>> entry : levelMap.entrySet()) {
            List<Person> levelPersons = entry.getValue();
            double currentX = 50;

            for (Person person : levelPersons) {
                StackPane node = createPersonNode(person, nodeWidth, nodeHeight);
                personNodeMap.put(person, node);
                contentGroup.getChildren().add(node);

                node.setLayoutX(currentX);
                node.setLayoutY(currentY);
                xPositions.put(person, currentX);
                currentX += nodeWidth + horizontalSpacing;
            }

            currentY += nodeHeight + verticalSpacing;
        }

        for (Person child : persons) {
            List<Person> parents = child.getParents();
            if (parents.size() == 2) {
                StackPane childNode = personNodeMap.get(child);
                StackPane parent1Node = personNodeMap.get(parents.get(0));
                StackPane parent2Node = personNodeMap.get(parents.get(1));

                if (childNode != null && parent1Node != null && parent2Node != null) {
                    double childCenterX = childNode.getLayoutX() + nodeWidth / 2;
                    double newParent1X = childCenterX - nodeWidth - 10;
                    double newParent2X = childCenterX + 10;

                    parent1Node.setLayoutX(newParent1X);
                    parent2Node.setLayoutX(newParent2X);

                    xPositions.put(parents.get(0), newParent1X);
                    xPositions.put(parents.get(1), newParent2X);
                }
            }
        }

        for (Person child : persons) {
            StackPane childNode = personNodeMap.get(child);
            if (childNode == null) continue;

            Bounds childBounds = childNode.getBoundsInParent();
            double childTopX = childBounds.getMinX() + nodeWidth / 2;
            double childTopY = childBounds.getMinY();

            List<Person> parents = child.getParents();
            if (parents.size() == 2) {
                StackPane parent1Node = personNodeMap.get(parents.get(0));
                StackPane parent2Node = personNodeMap.get(parents.get(1));
                if (parent1Node != null && parent2Node != null) {
                    Bounds bounds1 = parent1Node.getBoundsInParent();
                    Bounds bounds2 = parent2Node.getBoundsInParent();
                    double y = Math.max(bounds1.getMaxY(), bounds2.getMaxY());

                    double x1 = bounds1.getMinX() + nodeWidth / 2;
                    double x2 = bounds2.getMinX() + nodeWidth / 2;

                    Line hLine = new Line(x1, y, x2, y);
                    Line vLine = new Line((x1 + x2) / 2, y, childTopX, childTopY);

                    hLine.setStrokeWidth(2);
                    vLine.setStrokeWidth(2);
                    hLine.setStroke(Color.DARKSLATEGRAY);
                    vLine.setStroke(Color.DARKSLATEGRAY);

                    lineGroup.getChildren().addAll(hLine, vLine);
                }
            } else {
                for (Person parent : parents) {
                    StackPane parentNode = personNodeMap.get(parent);
                    if (parentNode == null) continue;

                    Bounds parentBounds = parentNode.getBoundsInParent();
                    double parentBottomX = parentBounds.getMinX() + nodeWidth / 2;
                    double parentBottomY = parentBounds.getMinY() + nodeHeight;

                    Line line = new Line(parentBottomX, parentBottomY, childTopX, childTopY);
                    line.setStrokeWidth(2);
                    line.setStroke(Color.DARKSLATEGRAY);
                    lineGroup.getChildren().add(line);
                }
            }
        }
    }

    private void assignLevels(Person person, int level, Map<Person, Integer> levels, Set<Person> visited) {
        if (visited.contains(person)) return;
        visited.add(person);

        levels.put(person, Math.max(levels.getOrDefault(person, 0), level));
        for (Person child : getChildrenOf(person)) {
            assignLevels(child, level + 1, levels, visited);
        }
    }

    private List<Person> getChildrenOf(Person parent) {
        List<Person> children = new ArrayList<>();
        for (Person p : FamilyTreeData.getInstance().getPersons()) {
            if (p.getParents().contains(parent)) {
                children.add(p);
            }
        }
        return children;
    }

    private StackPane createPersonNode(Person person, double width, double height) {
        Rectangle bg = new Rectangle(width, height);
        bg.setArcWidth(20);
        bg.setArcHeight(20);
        bg.setFill(Color.LIGHTBLUE);
        bg.setStroke(Color.DARKBLUE);
        bg.setStrokeWidth(2);
        bg.setEffect(new javafx.scene.effect.DropShadow());

        Label nameLabel = new Label(person.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: darkblue;");

        Label dobLabel = new Label(person.getDateOfBirth() == null ? "" : "b. " + person.getDateOfBirth());
        Label dodLabel = new Label(person.getDateOfDeath() == null ? "" : "d. " + person.getDateOfDeath());

        VBoxWithSpacing content = new VBoxWithSpacing(4, nameLabel, dobLabel, dodLabel);

        StackPane node = new StackPane();
        node.getChildren().addAll(bg, content);
        node.setPrefSize(width, height);

        node.setOnMouseClicked(e -> {
            selectedPerson = person;
            System.out.println("Selected: " + person.getName());
        });

        return node;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    // Helper VBox subclass with spacing
    static class VBoxWithSpacing extends javafx.scene.layout.VBox {
        public VBoxWithSpacing(double spacing, javafx.scene.Node... nodes) {
            super(spacing, nodes);
            setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            setStyle("-fx-alignment: center;");
        }
    }
}
