package com.familytree;

import java.util.List;

public class PersonDTO {
    private String id;
    private String name;
    private String gender;
    private String dateOfBirth;
    private String dateOfDeath;
    private String photoBase64;
    private String fatherId;
    private String motherId;
    private List<String> spouseIds;
    private List<String> childIds;

    // Default constructor for GSON
    public PersonDTO() {}

    // Getters and setters for all fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getDateOfDeath() { return dateOfDeath; }
    public void setDateOfDeath(String dateOfDeath) { this.dateOfDeath = dateOfDeath; }
    public String getPhotoBase64() { return photoBase64; }
    public void setPhotoBase64(String photoBase64) { this.photoBase64 = photoBase64; }
    public String getFatherId() { return fatherId; }
    public void setFatherId(String fatherId) { this.fatherId = fatherId; }
    public String getMotherId() { return motherId; }
    public void setMotherId(String motherId) { this.motherId = motherId; }
    public List<String> getSpouseIds() { return spouseIds; }
    public void setSpouseIds(List<String> spouseIds) { this.spouseIds = spouseIds; }
    public List<String> getChildIds() { return childIds; }
    public void setChildIds(List<String> childIds) { this.childIds = childIds; }
}