package com.familytree;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonDTO {
    private String id;
    private String name;
    private String dateOfBirth;
    private String dateOfDeath;
    private String gender;
    private List<String> parentIds = new ArrayList<>();
    private String photoPath; // Store path/URL instead of Image

    // Default constructor for Jackson
    public PersonDTO() {}

    // Conversion from domain Person to DTO
    public static PersonDTO fromDomain(Person person) {
        PersonDTO dto = new PersonDTO();
        dto.setId(person.getId());
        dto.setName(person.getName());
        dto.setDateOfBirth(person.getDateOfBirth());
        dto.setDateOfDeath(person.getDateOfDeath());
        dto.setGender(person.getGender());

        for (Person parent : person.getParents()) {
            dto.getParentIds().add(parent.getId());
        }

        // Optionally include photo path
        // dto.setPhotoPath(person.getPhotoPath());

        return dto;
    }

    // Conversion back to domain Person
    public Person toDomain() {
        Person person = new Person();
        person.setId(this.getId());
        person.setName(this.getName());
        person.setDateOfBirth(this.getDateOfBirth());
        person.setDateOfDeath(this.getDateOfDeath());
        person.setGender(this.getGender());

        // Optionally load photo
        // person.setPhotoPath(this.getPhotoPath());

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
    public void setParentIds(List<String> parentIds) { this.parentIds = parentIds; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
}
