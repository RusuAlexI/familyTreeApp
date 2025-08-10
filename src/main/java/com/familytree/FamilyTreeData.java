package com.familytree;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FamilyTreeData {
    private static FamilyTreeData instance;

    private final Map<String, Person> people;
    private final Map<String, Position> layoutPositions;
    private final ObservableList<Person> personList;

    private FamilyTreeData() {
        this.people = new HashMap<>();
        this.layoutPositions = new HashMap<>();
        this.personList = FXCollections.observableArrayList();
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
            Person spouse = getPerson(spouseId);
            if (spouse != null) {
                spouses.add(spouse);
            }
        }
        return spouses;
    }

    public List<Person> getChildren(Person person) {
        List<Person> children = new ArrayList<>();
        if (person != null) {
            for (String childId : person.getChildIds()) {
                Person child = getPerson(childId);
                if (child != null) {
                    children.add(child);
                }
            }
        }
        return children;
    }

    // New setters and methods
    public void addPerson(Person person) {
        people.put(person.getId(), person);
        personList.add(person);
    }

    public void removePerson(Person person) {
        people.remove(person.getId());
        personList.remove(person);
    }

    public void setLayoutPosition(String personId, Position position) {
        layoutPositions.put(personId, position);
    }

    public void updatePerson(Person person) {
        this.people.put(person.getId(), person);
        int index = personList.indexOf(person);
        if (index >= 0) {
            personList.set(index, person);
        }
    }


    public void linkAllRelationships() {
        for (Person person : people.values()) {
            for (String spouseId : person.getSpouseIds()) {
                Person spouse = people.get(spouseId);
                if (spouse != null && !spouse.getSpouseIds().contains(person.getId())) {
                    spouse.addSpouseId(person.getId());
                }
            }
            if (person.getFatherId() != null) {
                Person father = people.get(person.getFatherId());
                if (father != null && !father.getChildIds().contains(person.getId())) {
                    father.addChildId(person.getId());
                }
            }
            if (person.getMotherId() != null) {
                Person mother = people.get(person.getMotherId());
                if (mother != null && !mother.getChildIds().contains(person.getId())) {
                    mother.addChildId(person.getId());
                }
            }
        }
    }

    public void setAllData(FamilyTreeData other) {
        this.people.clear();
        this.people.putAll(other.people);

        this.layoutPositions.clear();
        this.layoutPositions.putAll(other.layoutPositions);

        this.personList.clear();
        this.personList.addAll(other.personList);
    }

    public void setParentChildRelationship(Person parent, Person child) {
        if (child.getFatherId() != null && child.getMotherId() != null) {
            throw new IllegalStateException("This person already has two parents set.");
        }

        if ("Male".equals(parent.getGender())) {
            child.setFatherId(parent.getId());
        } else {
            child.setMotherId(parent.getId());
        }
        parent.addChildId(child.getId());
    }

    public void setSpouseRelationship(Person spouse1, Person spouse2) {
        spouse1.addSpouseId(spouse2.getId());
        spouse2.addSpouseId(spouse1.getId());
    }

    public Map<String, Person> getPeople() {
        return people;
    }

    public Map<String, Position> getLayoutPositions() {
        return layoutPositions;
    }
}
