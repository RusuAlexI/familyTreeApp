package com.familytree;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.util.Base64;
import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.IntSequenceGenerator.class,
        property = "@id"
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Person {
    private String id;
    private String name;
    private String dateOfBirth;
    private String dateOfDeath;
    private String gender;
    private List<Person> children = new ArrayList<>();


    private String photoBase64;
    private List<Person> parents = new ArrayList<>();

    public Person() {    this.id = UUID.randomUUID().toString();
    }

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDateOfDeath() {
        return dateOfDeath;
    }

    public void setDateOfDeath(String dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public List<Person> getParents() {
        return parents;
    }

    public void setParents(List<Person> parents) {
        this.parents = parents;
    }

    public void addParent(Person parent) {
        if (!parents.contains(parent)) {
            parents.add(parent);
        }
    }


    public List<Person> getChildren() {
        return children;
    }

    public void addChild(Person child) {
        if (!children.contains(child)) {
            children.add(child);
        }
    }
    public String getPhotoBase64() {
        return photoBase64;
    }

    public void setPhotoBase64(String photoBase64) {
        this.photoBase64 = photoBase64;
    }

    public Image getPhotoAsImage() {
        if (photoBase64 == null || photoBase64.isEmpty()) return null;
        byte[] bytes = Base64.getDecoder().decode(photoBase64);
        return new Image(new ByteArrayInputStream(bytes));
    }

    public void setPhotoFromImage(Image image) {
        if (image == null) {
            this.photoBase64 = null;
            return;
        }
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", outputStream);
            this.photoBase64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            this.photoBase64 = null;
        }
    }

    @Override
    public String toString() {
        return name;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Person person = (Person) obj;
        return id != null && id.equals(person.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}