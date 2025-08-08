package com.familytree;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class FamilyTreePane extends Pane {
    private Theme theme;
    private Image backgroundImage;

    private final Canvas backgroundCanvas = new Canvas();
    private final Pane treeDrawingPane = new Pane(); // This pane will hold PersonCells and Lines
    private final TreeVisualizer visualizer;

    // Commented out variables as mouse handlers are disabled
    // private double scale = 1.0;
    // private double dragStartX, dragStartY;
    // private double initialTranslateX, initialTranslateY;

    public FamilyTreePane() {
        // Initialize TreeVisualizer with the treeDrawingPane
        this.visualizer = new TreeVisualizer(treeDrawingPane);

        // Add background canvas and the drawing pane to this FamilyTreePane
        getChildren().addAll(backgroundCanvas, treeDrawingPane);

        // Bind background canvas size to FamilyTreePane's size
        backgroundCanvas.widthProperty().bind(widthProperty());
        backgroundCanvas.heightProperty().bind(heightProperty());

        // CRITICAL: Ensure treeDrawingPane (visualizationPane) also binds its size to its parent
        // This makes sure it has a width/height greater than 0 for drag clamping
        treeDrawingPane.prefWidthProperty().bind(widthProperty());
        treeDrawingPane.prefHeightProperty().bind(heightProperty());

        // Redraw background when size changes
        backgroundCanvas.widthProperty().addListener((obs, oldVal, newVal) -> drawBackground());
        backgroundCanvas.heightProperty().addListener((obs, oldVal, newVal) -> drawBackground());

        // --- START OF TEMPORARILY COMMENTED OUT MOUSE HANDLERS ---
        // Mouse handlers for panning the entire visualization (background and treeDrawingPane)
        /*
        setOnMousePressed(event -> {
            if (event.getTarget() == this || event.getTarget() == backgroundCanvas) {
                visualizer.deselectAllNodes(); // Deselect any selected person when clicking background
                dragStartX = event.getSceneX();
                dragStartY = event.getSceneY();
                initialTranslateX = treeDrawingPane.getTranslateX(); // Pan the drawing pane
                initialTranslateY = treeDrawingPane.getTranslateY();
                event.consume();
            }
        });

        setOnMouseDragged(event -> {
            if (event.getTarget() == this || event.getTarget() == backgroundCanvas) {
                double offsetX = event.getSceneX() - dragStartX;
                double offsetY = event.getSceneY() - dragStartY;
                treeDrawingPane.setTranslateX(initialTranslateX + offsetX);
                treeDrawingPane.setTranslateY(initialTranslateY + offsetY);
                event.consume();
            }
        });

        // Handle zoom for the entire visualization
        setOnScroll(event -> {
            double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
            scale *= zoomFactor;

            treeDrawingPane.setScaleX(scale);
            treeDrawingPane.setScaleY(scale);

            Point2D mouse = new Point2D(event.getX(), event.getY());
            Point2D innerCoord = treeDrawingPane.parentToLocal(mouse);
            double dx = innerCoord.getX() * (1 - zoomFactor);
            double dy = innerCoord.getY() * (1 - zoomFactor);
            treeDrawingPane.setTranslateX(treeDrawingPane.getTranslateX() + dx);
            treeDrawingPane.setTranslateY(treeDrawingY.getTranslateY() + dy);

            event.consume();
        });
        */
        // --- END OF TEMPORARILY COMMENTED OUT MOUSE HANDLERS ---

        // Initial background draw
        drawBackground();
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
        this.backgroundImage = theme.backgroundImage();
        drawBackground();
    }

    private void drawBackground() {
        GraphicsContext gc = backgroundCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, backgroundCanvas.getWidth(), backgroundCanvas.getHeight());
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, backgroundCanvas.getWidth(), backgroundCanvas.getHeight());
        } else {
            gc.setFill(Color.WHITE); // Default background color if no image
            gc.fillRect(0, 0, backgroundCanvas.getWidth(), backgroundCanvas.getHeight());
        }
    }

    public TreeVisualizer getVisualizer() {
        return visualizer;
    }
}