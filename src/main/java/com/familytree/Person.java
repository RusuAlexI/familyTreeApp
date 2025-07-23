package com.familytree;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException; // Added import for IOException
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Person {
    private String id;
    private String name;
    private String dateOfBirth;
    private String dateOfDeath;
    private String gender;
    private String placeOfBirth;
    private String occupation;
    private String notes;
    private String photoBase64;

    // --- Relationship fields (ID-based) ---
    private String motherId;
    private String fatherId;
    private final List<String> childrenIds = new ArrayList<>();
    private final List<String> spouseIds = new ArrayList<>();

    // --- Position fields for visualization ---
    private double x; // Added for X coordinate
    private double y; // Added for Y coordinate


    public Person() {
        this.id = UUID.randomUUID().toString();
    }

    public Person(String name) {
        this(); // Call default constructor to initialize ID
        this.name = name;
    }

    // Getters and Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getDateOfDeath() { return dateOfDeath; }
    public void setDateOfDeath(String dateOfDeath) { this.dateOfDeath = dateOfDeath; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPlaceOfBirth() { return placeOfBirth; }
    public void setPlaceOfBirth(String placeOfBirth) { this.placeOfBirth = placeOfBirth; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPhotoBase64() { return photoBase64; }
    public void setPhotoBase64(String photoBase64) { this.photoBase64 = photoBase64; }

    // --- Getters and Setters for ID-based relationships ---
    public String getMotherId() { return motherId; }
    public void setMotherId(String motherId) { this.motherId = motherId; }

    public String getFatherId() { return fatherId; }
    public void setFatherId(String fatherId) { this.fatherId = fatherId; }

    public List<String> getChildrenIds() { return childrenIds; }
    public List<String> getSpouseIds() { return spouseIds; }

    // --- Getters and Setters for Position ---
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }


    // Utilities for photo conversion
    public static String imageToBase64(Image image) throws Exception {
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bImage, "png", bos);
        byte[] imageBytes = bos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    public static Image base64ToImage(String base64) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64);
            ByteArrayInputStream bis = new ByteArrayInputStream(decoded);
            BufferedImage bImage = ImageIO.read(bis);
            return SwingFXUtils.toFXImage(bImage, null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Copy method to update personal details (X,Y are copied here as they are part of Person's state)
    public void copyFrom(Person other) {
        this.name = other.getName();
        this.dateOfBirth = other.getDateOfBirth();
        this.dateOfDeath = other.getDateOfDeath();
        this.gender = other.getGender();
        this.placeOfBirth = other.getPlaceOfBirth();
        this.occupation = other.getOccupation();
        this.notes = other.getNotes();
        this.photoBase64 = other.getPhotoBase64();
        // Copy position as well when copying person details
        this.x = other.getX();
        this.y = other.getY();
        // Relationship IDs (motherId, fatherId, childrenIds, spouseIds) are handled by FamilyTreeData's methods
        // and are updated explicitly in PersonDialog's callback, not via copyFrom for Person's internal fields.
    }
}