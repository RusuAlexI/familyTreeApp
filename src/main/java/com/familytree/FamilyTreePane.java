package com.familytree;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class FamilyTreePane extends Pane {
    private Theme theme;
    private Image backgroundImage;

    private final Canvas backgroundCanvas = new Canvas();
    private final Group contentGroup = new Group();
    private final TreeVisualizer visualizer = new TreeVisualizer();

    private double scale = 1.0;
    private double dragStartX, dragStartY;
    private double initialTranslateX, initialTranslateY;

    public FamilyTreePane() {
        setPrefSize(1600, 1000);
        getChildren().addAll(backgroundCanvas, contentGroup);
        backgroundCanvas.widthProperty().bind(widthProperty());
        backgroundCanvas.heightProperty().bind(heightProperty());

        backgroundCanvas.widthProperty().addListener((obs, oldVal, newVal) -> drawBackground());
        backgroundCanvas.heightProperty().addListener((obs, oldVal, newVal) -> drawBackground());

        contentGroup.getChildren().add(visualizer.getView());

        // Mouse handlers for panning the background
        setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (event.getTarget() == backgroundCanvas || event.getTarget() == contentGroup || event.getTarget() == this) {
                    visualizer.deselectAllNodes();
                    dragStartX = event.getSceneX();
                    dragStartY = event.getSceneY();
                    initialTranslateX = contentGroup.getTranslateX();
                    initialTranslateY = contentGroup.getTranslateY();
                    event.consume();
                }
            }
        });

        setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (!visualizer.isDraggingNode()) {
                    double offsetX = event.getSceneX() - dragStartX;
                    double offsetY = event.getSceneY() - dragStartY;
                    contentGroup.setTranslateX(initialTranslateX + offsetX);
                    contentGroup.setTranslateY(initialTranslateY + offsetY);
                    event.consume();
                }
            }
        });

        setOnMouseReleased(event -> {
            // Nothing specific needed here as the draggingNode flag is cleared by the node itself.
            // If background was dragged, it's already updated.
            // If a node was clicked, its released handler consumed the event.
        });


        // Handle zoom
        setOnScroll(this::handleZoom);

        // Initial background draw
        drawBackground();
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
        // Directly use the Image loaded by the Theme enum itself
        this.backgroundImage = theme.backgroundImage(); // Use the getter that returns Image

        // No need for try-catch block here; Theme enum constructor handles image loading errors.
        // There is no `backgroundColor()` method in your Theme enum.
        drawBackground();
    }

    private void drawBackground() {
        GraphicsContext gc = backgroundCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, backgroundCanvas.getWidth(), backgroundCanvas.getHeight());
        if (backgroundImage != null) { // Check if the Theme provided a background image
            gc.drawImage(backgroundImage, 0, 0, backgroundCanvas.getWidth(), backgroundCanvas.getHeight());
        } else {
            // If no background image is set for the current theme, use a default color (e.g., white)
            // as your Theme enum does not have a `backgroundColor()` method.
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, backgroundCanvas.getWidth(), backgroundCanvas.getHeight());
        }
    }


    private void handleZoom(ScrollEvent event) {
        double delta = event.getDeltaY() > 0 ? 1.1 : 0.9;
        scale *= delta;
        contentGroup.setScaleX(scale);
        contentGroup.setScaleY(scale);

        Point2D mouse = new Point2D(event.getX(), event.getY());
        Point2D innerCoord = contentGroup.parentToLocal(mouse);
        double dx = innerCoord.getX() * (1 - delta);
        double dy = innerCoord.getY() * (1 - delta);
        contentGroup.setTranslateX(contentGroup.getTranslateX() + dx);
        contentGroup.setTranslateY(contentGroup.getTranslateY() + dy);

        event.consume();
    }

    public TreeVisualizer getVisualizer() {
        return visualizer;
    }

    public void exportAsImage(Stage stage) {
        WritableImage image = this.snapshot(new SnapshotParameters(), null);
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export as PNG");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            } catch (IOException ex) {
                System.err.println("Error exporting image: " + ex.getMessage());
            }
        }
    }
}