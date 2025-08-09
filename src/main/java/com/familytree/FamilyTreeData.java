package com.familytree;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.*;

public class FamilyTreeData {
    private static final String FILE_NAME = "family_tree.dat";
    private static FamilyTreeData instance;

    private final Map<String, Person> people;
    private final Map<String, Position> layoutPositions;

    private final ObservableList<Person> personList;

    private FamilyTreeData() {
        this.people = new HashMap<>();
        this.layoutPositions = new HashMap<>();
        this.personList = FXCollections.observableArrayList();
        loadData();
    }

    public static FamilyTreeData getInstance() {
        if (instance == null) {
            instance = new FamilyTreeData();
        }
        return instance;
    }

    // Getters
    public ObservableList<Person> getPersonList() {
        return personList;
    }

    public List<Person> getAllPeople() {
        return new ArrayList<>(people.values());
    }

    public Person getPerson(String id) {
        return people.get(id);
    }

    public Position getLayoutPosition(String personId) {
        return layoutPositions.get(personId);
    }

    public List<Person> getSpouses(Person person) {
        List<Person> spouses = new ArrayList<>();
        for (String spouseId : person.getSpouseIds()) {
            spouses.add(getPerson(spouseId));
        }
        return spouses;
    }

    public List<Person> getChildren(Person person) {
        List<Person> children = new ArrayList<>();
        for (String childId : person.getChildIds()) {
            children.add(getPerson(childId));
        }
        return children;
    }

    // Setters / Modifiers
    public void addPerson(Person person) {
        people.put(person.getId(), person);
        personList.add(person);
    }

    public void removePerson(String id) {
        Person person = people.remove(id);
        if (person != null) {
            // Remove from other peoples' relationships
            if (person.getFatherId() != null) {
                getPerson(person.getFatherId()).removeChildId(id);
            }
            if (person.getMotherId() != null) {
                getPerson(person.getMotherId()).removeChildId(id);
            }
            for (String spouseId : person.getSpouseIds()) {
                getPerson(spouseId).removeSpouseId(id);
            }
            for (String childId : person.getChildIds()) {
                Person child = getPerson(childId);
                if (child != null) {
                    child.setFatherId(null);
                    child.setMotherId(null);
                }
            }
            personList.remove(person);
        }
    }

    public void setLayoutPosition(String personId, Position position) {
        layoutPositions.put(personId, position);
    }

    public void clearData() {
        people.clear();
        layoutPositions.clear();
        personList.clear();
    }

    public void linkAllRelationships() {
        // This method ensures relationships are consistent in both directions
        for (Person person : people.values()) {
            // Link to father
            if (person.getFatherId() != null) {
                Person father = people.get(person.getFatherId());
                if (father != null) {
                    father.addChildId(person.getId());
                }
            }
            // Link to mother
            if (person.getMotherId() != null) {
                Person mother = people.get(person.getMotherId());
                if (mother != null) {
                    mother.addChildId(person.getId());
                }
            }

            // Link spouses
            for (String spouseId : person.getSpouseIds()) {
                Person spouse = people.get(spouseId);
                if (spouse != null) {
                    spouse.addSpouseId(person.getId());
                }
            }
        }
    }

    // Persistence
    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(people);
            oos.writeObject(layoutPositions);
        } catch (IOException e) {
            System.err.println("Failed to save data: " + e.getMessage());
        }
    }
    // This is the new method you need to add
    public void updatePerson(Person person) {
        // We simply replace the old Person object with the new one.
        // The ID remains the same, but other properties are updated.
        if (people.containsKey(person.getId())) {
            people.put(person.getId(), person);

            // The personList also needs to be updated.
            // This is a simple way to do it.
            int index = personList.indexOf(person);
            if (index != -1) {
                personList.set(index, person);
            }
        }
    }
    @SuppressWarnings("unchecked")
    public void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            Map<String, Person> loadedPeople = (Map<String, Person>) ois.readObject();
            Map<String, Position> loadedLayout = (Map<String, Position>) ois.readObject();

            this.people.clear();
            this.people.putAll(loadedPeople);
            this.layoutPositions.clear();
            this.layoutPositions.putAll(loadedLayout);

            this.personList.clear();
            this.personList.addAll(people.values());

            // Re-establish consistent relationships after loading
            linkAllRelationships();

        } catch (FileNotFoundException e) {
            System.out.println("No saved data found. Starting with an empty tree.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load data: " + e.getMessage());
        }
    }

    // Add this method to FamilyTreeData.java
    public void setAllData(FamilyTreeData other) {
        this.people.clear();
        this.people.putAll(other.people);

        this.layoutPositions.clear();
        this.layoutPositions.putAll(other.layoutPositions);

        this.personList.clear();
        this.personList.addAll(this.people.values());
    }

    public void setParentChildRelationship(Person parent, Person child) {
        if (child.getFatherId() != null && child.getMotherId() != null) {
            throw new IllegalStateException("This person already has two parents set.");
        }

        // Set parent and child relationships
        if ("Male".equals(parent.getGender())) {
            child.setFatherId(parent.getId());
        } else {
            child.setMotherId(parent.getId());
        }
        parent.addChildId(child.getId());
    }

    public void setSpouseRelationship(Person spouse1, Person spouse2) {
        // Add each person to the other's spouse list
        spouse1.addSpouseId(spouse2.getId());
        spouse2.addSpouseId(spouse1.getId());
    }
}