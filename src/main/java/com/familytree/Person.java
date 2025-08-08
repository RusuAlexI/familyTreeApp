package com.familytree;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Person {
    private String id;
    private String name;
    private String birthDate;
    private String deathDate;
    private String gender;
    private String bio;
    private String occupation;
    private String profilePicturePath;

    private String fatherId;
    private String motherId;

    private final Set<String> spouseIds;
    private final Set<String> childIds;

    // A simple constructor for creating new people
    public Person(String id, String name) {
        this.id = id;
        this.name = name;
        this.spouseIds = new HashSet<>();
        this.childIds = new HashSet<>();
        this.birthDate = "";
        this.deathDate = "";
        this.gender = "";
        this.bio = "";
        this.occupation = "";
        this.fatherId = null;
        this.motherId = null;
        this.profilePicturePath = null;
    }

    // A more comprehensive constructor for loading data
    public Person(String id, String name, String birthDate, String deathDate, String gender,
                  String bio, String occupation, String fatherId, String motherId,
                  String profilePicturePath, Set<String> spouseIds, Set<String> childIds) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.deathDate = deathDate;
        this.gender = gender;
        this.bio = bio;
        this.occupation = occupation;
        this.fatherId = fatherId;
        this.motherId = motherId;
        this.profilePicturePath = profilePicturePath;
        this.spouseIds = spouseIds != null ? new HashSet<>(spouseIds) : new HashSet<>();
        this.childIds = childIds != null ? new HashSet<>(childIds) : new HashSet<>();
    }

    // Getters
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getBirthDate() {
        return birthDate;
    }
    public String getDeathDate() {
        return deathDate;
    }
    public String getGender() {
        return gender;
    }
    public String getBio() {
        return bio;
    }
    public String getOccupation() {
        return occupation;
    }
    public String getProfilePicturePath() {
        return profilePicturePath;
    }
    public String getFatherId() {
        return fatherId;
    }
    public String getMotherId() {
        return motherId;
    }
    public Set<String> getSpouseIds() {
        return spouseIds;
    }
    public Set<String> getChildIds() {
        return childIds;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
    public void setDeathDate(String deathDate) {
        this.deathDate = deathDate;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }
    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }
    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }
    public void setFatherId(String fatherId) {
        this.fatherId = fatherId;
    }
    public void setMotherId(String motherId) {
        this.motherId = motherId;
    }

    // Relationship methods
    public void addSpouseId(String spouseId) {
        this.spouseIds.add(spouseId);
    }
    public void removeSpouseId(String spouseId) {
        this.spouseIds.remove(spouseId);
    }
    public void addChildId(String childId) {
        this.childIds.add(childId);
    }
    public void removeChildId(String childId) {
        this.childIds.remove(childId);
    }

    // Override equals and hashCode for proper collection usage
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Override toString for debugging
    @Override
    public String toString() {
        return "Person{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}