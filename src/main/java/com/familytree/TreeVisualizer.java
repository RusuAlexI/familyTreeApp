package com.familytree;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

public class TreeVisualizer extends Pane {

    private final Canvas canvas;
    private final FamilyTreeManager manager;

    public TreeVisualizer() {
        this.canvas = new Canvas(1200, 800); // You can adjust size
        this.manager = new FamilyTreeManager();

        this.getChildren().add(canvas);

        this.widthProperty().addListener((obs, oldVal, newVal) -> resizeCanvas());
        this.heightProperty().addListener((obs, oldVal, newVal) -> resizeCanvas());
    }

    private void resizeCanvas() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        redraw();
    }

    public void redraw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        manager.drawTree(gc);
    }

    public void addPerson(Person p) {
        FamilyTreeData.getInstance().addPerson(p);
        manager.reloadLayout();
        redraw();
    }

    public void deletePerson(Person p) {
        FamilyTreeData.getInstance().deletePerson(p);
        manager.reloadLayout();
        redraw();
    }

    public void editPerson(Person p) {
        // Already edited in FamilyTreeData, just reload layout
        manager.reloadLayout();
        redraw();
    }
}
