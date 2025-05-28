package com.familytree;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Person {
    private String id;
    private String name;
    private String dateOfBirth;
    private String dateOfDeath;
    private String gender;
    private String placeOfBirth;
    private String occupation;
    private String notes;
    private String photoPath;
    private List<Person> parents = new ArrayList<>();
    private List<Person> children = new ArrayList<>();

    public Person() {
        this.id = UUID.randomUUID().toString();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<Person> getChildren() {
        return children;
    }

    public void setChildren(List<Person> children) {
        this.children = children;
    }

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
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public List<Person> getParents() { return parents; }
    public void setParents(List<Person> parents) { this.parents = parents; }
    public void addParent(Person parent) { this.parents.add(parent); }
}
