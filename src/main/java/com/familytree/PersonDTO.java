package com.familytree;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonDTO {
    private String id;
    private String name;
    private String dateOfBirth;
    private String dateOfDeath;
    private String gender;
    private List<String> parentIds = new ArrayList<>();
    private List<String> childrenIds = new ArrayList<>();

    private String photoPath;
    private String placeOfBirth;
    private String occupation;
    private String notes;

    // Default constructor
    public PersonDTO() {}

    // From domain
    public static PersonDTO fromDomain(Person person) {
        PersonDTO dto = new PersonDTO();
        dto.setId(person.getId());
        dto.setName(person.getName());
        dto.setDateOfBirth(person.getDateOfBirth());
        dto.setDateOfDeath(person.getDateOfDeath());
        dto.setGender(person.getGender());
        dto.setPhotoPath(person.getPhotoPath());
        dto.setPlaceOfBirth(person.getPlaceOfBirth());
        dto.setOccupation(person.getOccupation());
        dto.setNotes(person.getNotes());
        dto.setChildrenIds(person.getChildren().stream()
                .map(Person::getId)
                .collect(Collectors.toList()));
        for (Person parent : person.getParents()) {
            dto.getParentIds().add(parent.getId());
        }

        return dto;
    }

    // To domain
    public Person toDomain() {
        Person person = new Person();
        person.setId(this.id);
        person.setName(this.name);
        person.setDateOfBirth(this.dateOfBirth);
        person.setDateOfDeath(this.dateOfDeath);
        person.setGender(this.gender);
        person.setPhotoPath(this.photoPath);
        person.setPlaceOfBirth(this.placeOfBirth);
        person.setOccupation(this.occupation);
        person.setNotes(this.notes);
        return person;
    }

    // Getters and setters
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
    public List<String> getParentIds() { return parentIds; }
    public List<String> getChildrenIds() { return childrenIds; }
    public void setChildrenIds(List<String> childrenIds) { this.childrenIds = childrenIds; }
    public void setParentIds(List<String> parentIds) { this.parentIds = parentIds; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public String getPlaceOfBirth() { return placeOfBirth; }
    public void setPlaceOfBirth(String placeOfBirth) { this.placeOfBirth = placeOfBirth; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
