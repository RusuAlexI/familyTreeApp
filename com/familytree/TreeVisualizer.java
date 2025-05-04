package com.familytree;

import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class TreeVisualizer {
    private FamilyTreeData data;
    private Pane canvas;

    public TreeVisualizer(FamilyTreeData data) {
        this.data = data;
        canvas = new Pane();
        refresh();
    }

    public void refresh() {
        canvas.getChildren().clear();
        int y = 20;
        for (Person person : data.getPersons()) {
            Text text = new Text(20, y, person.getName() + " - " + person.getDateOfBirth());
            canvas.getChildren().add(text);
            y += 30;
        }
    }

    public Parent getView() {
        return canvas;
    }

    public void setData(FamilyTreeData newData) {
        this.data = newData;
        refresh();
    }
}