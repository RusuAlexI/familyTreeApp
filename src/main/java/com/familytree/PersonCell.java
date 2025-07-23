package com.familytree;

import com.familytree.Person;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Base64;

public class PersonCell extends StackPane {
    private final Person person;
    private final Rectangle background;
    private final Label nameLabel;

    public PersonCell(Person person) {
        this.person = person;

        background = new Rectangle(150, 80);
        background.setArcWidth(10);
        background.setArcHeight(10);
        background.setFill(Color.LIGHTBLUE);
        background.setStroke(Color.DARKBLUE);

        nameLabel = new Label(person.getName());
        nameLabel.setWrapText(true);

        if (person.getPhotoBase64() != null) {
            byte[] bytes = Base64.getDecoder().decode(person.getPhotoBase64());
            ImageView photo = new ImageView(new Image(new java.io.ByteArrayInputStream(bytes)));
            photo.setFitHeight(50);
            photo.setPreserveRatio(true);
            getChildren().addAll(background, photo, nameLabel);
        } else {
            getChildren().addAll(background, nameLabel);
        }

        setOnMouseEntered(e -> background.setFill(Color.DEEPSKYBLUE));
        setOnMouseExited(e -> background.setFill(Color.LIGHTBLUE));
    }

    public Person getPerson() {
        return person;
    }
}
