package com.familytree;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FamilyTreeData {

    private static FamilyTreeData instance = new FamilyTreeData();

    private List<Person> persons = new ArrayList<>();

    // Required for Jackson
    public FamilyTreeData() {}

    public FamilyTreeData(List<Person> persons) {
        this.persons = persons;
    }

    // Singleton getter
    public static FamilyTreeData getInstance() {
        return instance;
    }

    // Setter for replacing instance (e.g., on import)
    public static void setInstance(FamilyTreeData newData) {
        if (newData != null && newData.getPersons() != null) {
            System.out.println(">>> DEBUG: Replacing singleton with " + newData.getPersons().size() + " persons.");
            instance = newData;
        } else {
            System.out.println(">>> WARNING: Attempted to set null or empty FamilyTreeData instance.");
        }
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

    public void addPerson(Person person) {
        if (person.getId() == null || person.getId().isEmpty()) {
            person.setId(UUID.randomUUID().toString());
        }
        persons.add(person);
    }

    public void removePerson(Person person) {
        persons.remove(person);
        for (Person p : persons) {
            p.getParents().remove(person);
//            p.getChildren().remove(person);
        }
    }

    public Person findById(String id) {
        return persons.stream()
                .filter(p -> p.getId() != null && p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }


}