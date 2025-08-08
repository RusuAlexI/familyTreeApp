package com.familytree;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;

public class PersonCell extends StackPane {
    private final Person person;
    private ImageView imageView;

    public PersonCell(Person person) {
        this.person = person;
        setPrefSize(100, 120); // Set a preferred size
        setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: white; -fx-border-radius: 5; -fx-background-radius: 5;");

        // Use a Circle to display the profile picture
        Circle clip = new Circle(40);
        clip.setCenterX(50);
        clip.setCenterY(40);

        // Load the image from the file path
        Image profileImage = loadImageFromFile(person.getProfilePicturePath());
        if (profileImage == null) {
            // Use a default image if no profile picture is set or file is not found
            profileImage = new Image(getClass().getResourceAsStream("/imagess/default-profile.png"));
        }

        this.imageView = new ImageView(profileImage);
        this.imageView.setFitWidth(80);
        this.imageView.setFitHeight(80);
        this.imageView.setClip(clip);

        Text nameText = new Text(person.getName());

        // Position the elements inside the StackPane
        StackPane.setAlignment(imageView, javafx.geometry.Pos.TOP_CENTER);
        StackPane.setAlignment(nameText, javafx.geometry.Pos.BOTTOM_CENTER);

        getChildren().addAll(imageView, nameText);
    }

    // Helper method to load image from file
    private Image loadImageFromFile(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            try {
                return new Image(new FileInputStream(filePath));
            } catch (FileNotFoundException e) {
                System.err.println("Profile picture not found: " + filePath);
            }
        }
        return null;
    }

    public Person getPerson() {
        return person;
    }

    // You can add a method to update the cell's view if the person data changes
    public void updateCell() {
        // Update the name text
        Text nameText = (Text) getChildren().stream().filter(n -> n instanceof Text).findFirst().orElse(null);
        if (nameText != null) {
            nameText.setText(person.getName());
        }

        // Update the image
        Image newImage = loadImageFromFile(person.getProfilePicturePath());
        if (newImage == null) {
            newImage = new Image(getClass().getResourceAsStream("/imagess/default-profile.png"));
        }
        imageView.setImage(newImage);
    }
}