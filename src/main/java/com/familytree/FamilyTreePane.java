    package com.familytree;

    import javafx.geometry.Bounds;
    import javafx.geometry.Insets;
    import javafx.scene.Group;
    import javafx.scene.control.Label;
    import javafx.scene.image.Image;
    import javafx.scene.layout.*;
    import javafx.scene.paint.Color;
    import javafx.scene.shape.Line;
    import javafx.scene.shape.Rectangle;
    import javafx.scene.input.KeyCode;
    import javafx.scene.control.Button;
    import lombok.Getter;

    import java.util.*;

    public class FamilyTreePane extends Pane {

        private final Map<Person, StackPane> personNodeMap = new HashMap<>();
        private final Group lineGroup = new Group();
        private final Group contentGroup = new Group();  // Zoom/pan group

        private double scale = 1.0;
        private double mouseAnchorX, mouseAnchorY;
        private double translateAnchorX, translateAnchorY;

        private Person selectedPerson;
        private Background defaultBackground;
        private Background parchmentBackground;
        private Background treeBackground;
        @Getter
        private Theme currentTheme = Theme.DEFAULT;

        public FamilyTreePane() {
            loadBackgrounds();
            setBackground(defaultBackground);
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

        public void setTheme(Theme theme) {
            this.currentTheme = theme;
            applyTheme(theme.name());
        }

        private void loadBackgrounds() {
            defaultBackground = new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY));

            try {
                Image parchment = new Image(getClass().getResource("/images/Genealogical_Family_Tree_on_Aged_Paper.png").toExternalForm());
                parchmentBackground = new Background(
                        new BackgroundImage(parchment, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                                BackgroundPosition.CENTER, new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true))
                );
            } catch (Exception e) {
                parchmentBackground = defaultBackground;
            }

            try {
                Image tree = new Image(getClass().getResource("/images/Intricate_Tree_Branches_on_Parchment.png").toExternalForm());
                treeBackground = new Background(
                        new BackgroundImage(tree, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                                BackgroundPosition.CENTER, new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true))
                );
            } catch (Exception e) {
                treeBackground = defaultBackground;
            }
        }

        public void setCustomBackground(String imageUrl) {
            BackgroundImage bgImage = new BackgroundImage(
                    new Image(imageUrl, true),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
            );
            setBackground(new Background(bgImage));
        }

        private void setNodeStyle(String style) {
            for (StackPane box : personNodeMap.values()) {
                box.setStyle(style);
            }
        }
        public void applyTheme(String theme) {
            switch (currentTheme) {
                case PARCHMENT:
                    setStyle("-fx-background-image: url('/images/Intricate_Tree_Branches_on_Parchment.png'); " +
                            "-fx-background-size: cover;");
                    break;
                case TREE_BACKGROUND:
                    setStyle("-fx-background-image: url('/images/Genealogical_Family_Tree_on_Aged_Paper.png'); " +
                            "-fx-background-size: cover;");
                    break;
                default:
                    setStyle("-fx-background-color: white;");
                    break;
            }

            // Optionally, update node styles if needed (e.g. different border colors)
            for (StackPane node : personNodeMap.values()) {
                Rectangle rect = (Rectangle) node.getChildren().get(0);
                switch (currentTheme) {
                    case PARCHMENT:
                        rect.setFill(Color.BEIGE);
                        rect.setStroke(Color.SADDLEBROWN);
                        break;
                    case TREE_BACKGROUND:
                        rect.setFill(Color.LIGHTGREEN);
                        rect.setStroke(Color.DARKGREEN);
                        break;
                    default:
                        rect.setFill(Color.LIGHTBLUE);
                        rect.setStroke(Color.DARKBLUE);
                }
            }
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

            // Step 1: Determine levels
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

            // Group persons by level
            Map<Integer, List<Person>> levelMap = new TreeMap<>();
            for (Map.Entry<Person, Integer> entry : levels.entrySet()) {
                levelMap.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
            }

            Map<Person, Double> xPositions = new HashMap<>();
            double currentY = 50;

            // Step 2: Position nodes by level
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

            // Step 3: Adjust positions of paired parents
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

            // Step 4: Draw connecting lines
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

                        double midX = (x1 + x2) / 2;

                        // Horizontal line between parents
                        Line hLine = new Line(x1, y, x2, y);
                        // Vertical line from midpoint to child
                        Line vLine = new Line(midX, y, childTopX, childTopY);

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
        private void drawSingleParentLine(Person parent, Person child) {
            StackPane parentNode = personNodeMap.get(parent);
            StackPane childNode = personNodeMap.get(child);
            if (parentNode == null || childNode == null) return;

            Bounds parentBounds = parentNode.getBoundsInParent();
            Bounds childBounds = childNode.getBoundsInParent();

            double startX = parentBounds.getMinX() + parentBounds.getWidth() / 2;
            double startY = parentBounds.getMaxY();
            double endX = childBounds.getMinX() + childBounds.getWidth() / 2;
            double endY = childBounds.getMinY();

            Line line = new Line(startX, startY, endX, endY);
            getChildren().add(line);
        }

        private void drawTwoParentLines(Person parent1, Person parent2, Person child) {
            StackPane node1 = personNodeMap.get(parent1);
            StackPane node2 = personNodeMap.get(parent2);
            StackPane childNode = personNodeMap.get(child);
            if (node1 == null || node2 == null || childNode == null) return;

            Bounds b1 = node1.getBoundsInParent();
            Bounds b2 = node2.getBoundsInParent();
            Bounds bc = childNode.getBoundsInParent();

            double y = Math.max(b1.getMaxY(), b2.getMaxY());

            double x1 = b1.getMinX() + b1.getWidth() / 2;
            double x2 = b2.getMinX() + b2.getWidth() / 2;
            double mx = (x1 + x2) / 2;

            double cy = bc.getMinY();
            double cx = bc.getMinX() + bc.getWidth() / 2;

            // Draw horizontal line between parents
            Line hLine = new Line(x1, y, x2, y);
            Line vLine = new Line(mx, y, mx, cy);
            Line childLine = new Line(mx, cy, cx, cy);

            getChildren().addAll(hLine, vLine, childLine);
        }

        private Map<Integer, List<Person>> buildGenerations(List<Person> persons) {
            Map<Person, Integer> levels = new HashMap<>();
            for (Person p : persons) {
                if (p.getParents().isEmpty()) {
                    assignLevels(p, 0, levels);
                }
            }

            Map<Integer, List<Person>> generations = new TreeMap<>();
            for (Map.Entry<Person, Integer> entry : levels.entrySet()) {
                generations.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
            }

            return generations;
        }

        private void assignLevels(Person person, int level, Map<Person, Integer> levels) {
            if (levels.containsKey(person) && levels.get(person) <= level) return;
            levels.put(person, level);
            for (Person child : getChildrenOf(person)) {
                assignLevels(child, level + 1, levels);
            }
        }
        private StackPane createPersonNode(Person person, double width, double height) {
            Rectangle bg = new Rectangle(width, height);
            bg.setArcWidth(20);
            bg.setArcHeight(20);
            bg.setFill(Color.LIGHTBLUE);
            bg.setStroke(Color.DARKBLUE);
            bg.setStrokeWidth(2);
            bg.setEffect(new javafx.scene.effect.DropShadow(5, Color.GRAY));

            Label nameLabel = new Label(person.getName());
            nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

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
