package com.familytree;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty; // Import JsonProperty

import java.util.*;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FamilyTreeData {

    private static FamilyTreeData instance = new FamilyTreeData(); // Singleton instance

    @JsonProperty("allPeople") // <-- ADD THIS ANNOTATION
    private List<Person> persons = new ArrayList<>();
    // A map for quick lookup of persons by ID
    private transient Map<String, Person> personMap = new HashMap<>();

    // Required for Jackson for deserialization
    public FamilyTreeData() {
        // Initialize map from persons list upon creation/deserialization
        initializePersonMap();
    }

    public FamilyTreeData(List<Person> persons) {
        this.persons = persons;
        initializePersonMap();
    }

    // Singleton getter
    public static FamilyTreeData getInstance() {
        return instance;
    }

    // Setter for replacing instance (e.g., on import/load)
    public static void setInstance(FamilyTreeData newData) {
        if (newData != null && newData.getAllPeople() != null) {
            System.out.println(">>> DEBUG: Replacing singleton with " + newData.getAllPeople().size() + " persons.");
            instance = newData;
            instance.initializePersonMap(); // Re-initialize map for new instance
        } else {
            System.out.println(">>> WARNING: Attempted to set null or empty FamilyTreeData instance.");
        }
    }

    // Initialize/Re-initialize the personMap from the persons list
    private void initializePersonMap() {
        personMap = new HashMap<>();
        for (Person person : persons) {
            personMap.put(person.getId(), person);
        }
    }

    public List<Person> getAllPeople() {
        return new ArrayList<>(persons); // Return a copy to prevent external modification
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
        initializePersonMap(); // Update map when persons list is set
    }

    public void addPerson(Person person) {
        if (person.getId() == null || person.getId().isEmpty()) {
            person.setId(UUID.randomUUID().toString()); // Ensure ID is set if not already
        }
        persons.add(person);
        personMap.put(person.getId(), person); // Add to map
    }

    /**
     * Removes a person by their ID and updates all related relationships.
     * @param personId The ID of the person to remove.
     */
    public void removePerson(String personId) {
        Person personToRemove = personMap.get(personId);
        if (personToRemove == null) return;

        // Remove from main list
        persons.remove(personToRemove);
        // Remove from map
        personMap.remove(personId);

        // Update relationships of other persons
        for (Person p : persons) {
            // Remove as a child
            p.getChildrenIds().remove(personId);

            // Remove as a spouse
            p.getSpouseIds().remove(personId);

            // If removed person was a mother or father, clear their parent ID
            if (Objects.equals(p.getMotherId(), personId)) {
                p.setMotherId(null);
            }
            if (Objects.equals(p.getFatherId(), personId)) {
                p.setFatherId(null);
            }
        }
    }


    public Person findById(String id) {
        return personMap.get(id);
    }

    /**
     * Establishes a parent-child relationship.
     * @param parentId The ID of the parent.
     * @param childId The ID of the child.
     * @param type "mother" or "father"
     */
    public void setParentChildRelationship(String parentId, String childId, String type) {
        Person parent = findById(parentId);
        Person child = findById(childId);

        if (parent == null || child == null) return;

        // Ensure child points to parent
        if ("mother".equalsIgnoreCase(type)) {
            // If child already has a mother, remove the old relationship first
            if (child.getMotherId() != null && !child.getMotherId().equals(parentId)) {
                removeParentChildRelationship(child.getMotherId(), childId);
            }
            child.setMotherId(parentId);
        } else if ("father".equalsIgnoreCase(type)) {
            // If child already has a father, remove the old relationship first
            if (child.getFatherId() != null && !child.getFatherId().equals(parentId)) {
                removeParentChildRelationship(child.getFatherId(), childId);
            }
            child.setFatherId(parentId);
        }

        // Ensure parent lists child
        if (!parent.getChildrenIds().contains(childId)) {
            parent.getChildrenIds().add(childId);
        }
    }

    /**
     * Removes a parent-child relationship.
     * @param parentId The ID of the parent.
     * @param childId The ID of the child.
     */
    public void removeParentChildRelationship(String parentId, String childId) {
        Person parent = findById(parentId);
        Person child = findById(childId);

        if (parent == null || child == null) return;

        // Remove child's reference to parent
        if (Objects.equals(child.getMotherId(), parentId)) {
            child.setMotherId(null);
        }
        if (Objects.equals(child.getFatherId(), parentId)) {
            child.setFatherId(null);
        }

        // Remove parent's reference to child
        parent.getChildrenIds().remove(childId);
    }

    /**
     * Establishes a spouse relationship (mutual).
     * @param person1Id The ID of the first person.
     * @param person2Id The ID of the second person.
     */
    public void setSpouseRelationship(String person1Id, String person2Id) {
        Person p1 = findById(person1Id);
        Person p2 = findById(person2Id);

        if (p1 == null || p2 == null) return;

        if (!p1.getSpouseIds().contains(person2Id)) {
            p1.getSpouseIds().add(person2Id);
        }
        if (!p2.getSpouseIds().contains(person1Id)) {
            p2.getSpouseIds().add(person1Id);
        }
    }

    /**
     * Removes a spouse relationship (mutual).
     * @param person1Id The ID of the first person.
     * @param person2Id The ID of the second person.
     */
    public void removeSpouseRelationship(String person1Id, String person2Id) {
        Person p1 = findById(person1Id);
        Person p2 = findById(person2Id);

        if (p1 == null || p2 == null) return;

        p1.getSpouseIds().remove(person2Id);
        p2.getSpouseIds().remove(person1Id);
    }
}