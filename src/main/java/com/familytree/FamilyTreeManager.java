package com.familytree;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Map;

public class FamilyTreeManager {

    private TreeLayout treeLayout;
    private Map<String, Position> positions;

    public FamilyTreeManager() {
        this.treeLayout = new TreeLayout();
        reloadLayout();
    }

    public void reloadLayout() {
        positions = treeLayout.calculateLayout(
                FamilyTreeData.getInstance().getPersons(),
                FamilyTreeData.getInstance().getRelationships()
        );
    }

    public void drawTree(GraphicsContext gc) {
        if (positions == null || positions.isEmpty()) {
            return;
        }

        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        // First draw connections
        for (Relationship rel : FamilyTreeData.getInstance().getRelationships()) {
            Position from = positions.get(rel.getFromId());
            Position to = positions.get(rel.getToId());

            if (from != null && to != null) {
                gc.setStroke(Color.GRAY);
                gc.strokeLine(
                        from.getX() + 50, from.getY() + 50,
                        to.getX() + 50, to.getY()
                );
            }
        }

        // Then draw persons
        for (Person person : FamilyTreeData.getInstance().getPersons()) {
            Position pos = positions.get(person.getId());
            if (pos != null) {
                gc.setFill(Color.LIGHTBLUE);
                gc.fillRoundRect(pos.getX(), pos.getY(), 100, 50, 10, 10);

                gc.setStroke(Color.BLACK);
                gc.strokeRoundRect(pos.getX(), pos.getY(), 100, 50, 10, 10);

                gc.setFill(Color.BLACK);
                gc.fillText(person.getName(), pos.getX() + 10, pos.getY() + 25);
            }
        }
    }
}
