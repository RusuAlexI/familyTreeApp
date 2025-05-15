package com.familytree;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

import java.util.*;

public class FamilyTreePane extends Pane {

    private Person selectedPerson;
    private final Map<Person, VBox> personNodes = new HashMap<>();
    private final Map<Integer, List<Person>> levelMap = new HashMap<>();
    private final double horizontalSpacing = 180;
    private final double verticalSpacing = 150;

    public FamilyTreePane() {
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #f4f4f4;");
    }

    public void drawTree(List<Person> persons) {
        getChildren().clear();
        personNodes.clear();
        levelMap.clear();

        if (persons.isEmpty()) return;

        assignLevels(persons);

        for (List<Person> levelPersons : levelMap.values()) {
            for (Person p : levelPersons) {
                VBox node = createStyledPersonNode(p);
                personNodes.put(p, node);
                getChildren().add(node);
            }
        }

        layoutNodes();
        drawConnections();
    }

    private void assignLevels(List<Person> persons) {
        Set<Person> visited = new HashSet<>();
        for (Person p : persons) {
            if (p.getParents().isEmpty()) {
                assignLevelRecursive(p, 0, visited);
            }
        }
    }

    private void assignLevelRecursive(Person person, int level, Set<Person> visited) {
        if (visited.contains(person)) return;
        visited.add(person);
        levelMap.computeIfAbsent(level, k -> new ArrayList<>()).add(person);
        for (Person child : getChildrenOf(person)) {
            assignLevelRecursive(child, level + 1, visited);
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

    private void layoutNodes() {
        for (Map.Entry<Integer, List<Person>> entry : levelMap.entrySet()) {
            int level = entry.getKey();
            List<Person> people = entry.getValue();

            double y = level * verticalSpacing + 40;
            double totalWidth = (people.size() - 1) * horizontalSpacing;
            double x = (getWidth() - totalWidth) / 2;

            for (Person p : people) {
                VBox node = personNodes.get(p);
                if (node != null) {
                    node.setLayoutX(x);
                    node.setLayoutY(y);
                    x += horizontalSpacing;
                }
            }
        }
    }

    private VBox createStyledPersonNode(Person person) {
        VBox box = new VBox();
        box.setPadding(new Insets(8));
        box.setSpacing(4);
        box.setPrefWidth(140);
        box.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
        box.setBorder(new Border(new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(1))));
        box.setStyle("-fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.15), 5, 0, 2, 2);");

        Label nameLabel = new Label(person.getName());
        nameLabel.setFont(Font.font("Arial", 14));
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label dobLabel = new Label("Born: " + person.getDateOfBirth());
        Label dodLabel = new Label("Died: " + person.getDateOfDeath());
        Label genderLabel = new Label("Gender: " + person.getGender());

        box.getChildren().addAll(nameLabel, dobLabel, dodLabel, genderLabel);

        box.setOnMouseClicked((MouseEvent e) -> {
            selectedPerson = person;
            highlightSelected(box);
        });

        return box;
    }

    private void highlightSelected(VBox selectedBox) {
        for (Node node : getChildren()) {
            if (node instanceof VBox) {
                node.setStyle("-fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.15), 5, 0, 2, 2);");
            }
        }
        selectedBox.setStyle("-fx-effect: dropshadow(two-pass-box, rgba(30,144,255,0.8), 10, 0, 3, 3);");
    }

    private void drawConnections() {
        // Draw lines between parents and children using their centered bottom and top points
        for (Map.Entry<Person, VBox> entry : personNodes.entrySet()) {
            Person child = entry.getKey();
            VBox childNode = entry.getValue();

            for (Person parent : child.getParents()) {
                VBox parentNode = personNodes.get(parent);
                if (parentNode == null) continue;

                // Ensure layout is valid
                parentNode.applyCss();
                parentNode.layout();
                childNode.applyCss();
                childNode.layout();

                Bounds parentBounds = parentNode.getBoundsInParent();
                Bounds childBounds = childNode.getBoundsInParent();
                double startY = 20;
                List<Person> roots = new ArrayList<>();
                for (Person p : FamilyTreeData.getInstance().getPersons()) {
                    if (p.getParents().isEmpty()) {
                        roots.add(p);
                    }
                }
                double startX = (getWidth() - (roots.size() * horizontalSpacing)) / 2;
                // Map to track positions
                Map<Person, Double> xPositions = new HashMap<>();
                Map<Person, Double> yPositions = new HashMap<>();

                // Position roots
                for (Person root : roots) {
                    xPositions.put(root, startX);
                    yPositions.put(root, startY);
                    startX += horizontalSpacing;
                }
// Position children using BFS
                Queue<Person> queue = new LinkedList<>(roots);
                while (!queue.isEmpty()) {
                    Person current = queue.poll();
                    VBox node = personNodes.get(current);
                    if (node != null) {
                        node.setLayoutX(xPositions.get(current));
                        node.setLayoutY(yPositions.get(current));
                    }

                    List<Person> children = getChildrenOf(current);
                    if (!children.isEmpty()) {
                        double childY = yPositions.get(current) + verticalSpacing;
                        double totalWidth = children.size() * horizontalSpacing;
                        double childStartX = xPositions.get(current) - (totalWidth/2) + (horizontalSpacing/2);

                        for (Person child1 : children) {
                            xPositions.put(child1, childStartX);
                            yPositions.put(child1, childY);
                            childStartX += horizontalSpacing;
                            queue.add(child1);
                        }
                    }
                }
                 startX = xPositions.get(parent) + 160/2;
                 startY = yPositions.get(parent) + 80;
                double endX = xPositions.get(child) + 160/2;
                double endY = yPositions.get(child);

                Line line = new Line(startX, startY, endX, endY);
                line.setStroke(Color.DARKGRAY);
                line.setStrokeWidth(2);
                getChildren().add(line);
            }
        }
    }

    public Person getSelectedPerson() {
        return selectedPerson;
    }

    public void clearSelection() {
        this.selectedPerson = null;
    }
}
//try the working FamilyTreePaneGood to add here, in order to have good looking cells like here + the working lines from there