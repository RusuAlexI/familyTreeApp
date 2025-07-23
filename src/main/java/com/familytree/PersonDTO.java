package com.familytree;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonDTO {
    private String id;
    private String name;
    // Removed dateOfBirth, dateOfDeath, gender as they are not present in the Person.java class you provided
    // If these are desired, they should be added to the Person.java domain model first.
    private String placeOfBirth;
    private String occupation;
    private String notes;
    private String photoBase64; // Changed from photoPath to match Person.java's photoBase64

    private String motherId; // Direct mapping to Person's motherId
    private String fatherId; // Direct mapping to Person's fatherId
    private List<String> childrenIds = new ArrayList<>();
    private List<String> spouseIds = new ArrayList<>(); // Added to match Person.java's spouseIds

    private double x; // Added to match Person.java's x coordinate
    private double y; // Added to match Person.java's y coordinate

    // Default constructor
    public PersonDTO() {}

    /**
     * Creates a PersonDTO from a Person domain object.
     * @param person The Person domain object.
     * @return A new PersonDTO.
     */
    public static PersonDTO fromDomain(Person person) {
        PersonDTO dto = new PersonDTO();
        dto.setId(person.getId());
        dto.setName(person.getName());
        dto.setPlaceOfBirth(person.getPlaceOfBirth());
        dto.setOccupation(person.getOccupation());
        dto.setNotes(person.getNotes());
        dto.setPhotoBase64(person.getPhotoBase64()); // Match photo field

        dto.setMotherId(person.getMotherId());
        dto.setFatherId(person.getFatherId());
        dto.getChildrenIds().addAll(person.getChildrenIds());
        dto.getSpouseIds().addAll(person.getSpouseIds());

        dto.setX(person.getX());
        dto.setY(person.getY());

        return dto;
    }

    /**
     * Converts this PersonDTO back into a Person domain object.
     * Note: When converting back to domain, relationships (motherId, fatherId, childrenIds, spouseIds)
     * are set as IDs. The FamilyTreeData manager is responsible for linking actual Person objects.
     * @return A new Person domain object populated from the DTO.
     */
    public Person toDomain() {
        // Person constructor generates ID, but we set it from DTO for existing persons to preserve it.
        Person person = new Person();
        person.setId(this.id);
        person.setName(this.name);
        person.setPlaceOfBirth(this.placeOfBirth);
        person.setOccupation(this.occupation);
        person.setNotes(this.notes);
        person.setPhotoBase64(this.photoBase64); // Match photo field

        person.setMotherId(this.motherId);
        person.setFatherId(this.fatherId);
        // Add all children IDs and spouse IDs to the Person object's lists
        person.getChildrenIds().addAll(this.childrenIds);
        person.getSpouseIds().addAll(this.spouseIds);

        person.setX(this.x);
        person.setY(this.y);

        return person;
    }

    // Getters and setters for all fields

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPlaceOfBirth() { return placeOfBirth; }
    public void setPlaceOfBirth(String placeOfBirth) { this.placeOfBirth = placeOfBirth; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPhotoBase64() { return photoBase64; }
    public void setPhotoBase64(String photoBase64) { this.photoBase64 = photoBase64; }

    public String getMotherId() { return motherId; }
    public void setMotherId(String motherId) { this.motherId = motherId; }

    public String getFatherId() { return fatherId; }
    public void setFatherId(String fatherId) { this.fatherId = fatherId; }

    public List<String> getChildrenIds() { return childrenIds; }
    public void setChildrenIds(List<String> childrenIds) { this.childrenIds = childrenIds; }

    public List<String> getSpouseIds() { return spouseIds; }
    public void setSpouseIds(List<String> spouseIds) { this.spouseIds = spouseIds; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
}