package com.familytree;

import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

public class SpouseCell extends HBox {
    private final PersonCell spouse1Cell;
    private final PersonCell spouse2Cell;

    public SpouseCell(PersonCell spouse1Cell, PersonCell spouse2Cell) {
        super(10); // Spacing between the cells
        this.spouse1Cell = spouse1Cell;
        this.spouse2Cell = spouse2Cell;

        // Add both person cells to the HBox
        getChildren().addAll(spouse1Cell, spouse2Cell);
        setAlignment(Pos.CENTER); // Center them within the HBox
        getStyleClass().add("spouse-cell"); // Optional: for CSS styling
    }

    public PersonCell getSpouse1Cell() {
        return spouse1Cell;
    }

    public PersonCell getSpouse2Cell() {
        return spouse2Cell;
    }
}