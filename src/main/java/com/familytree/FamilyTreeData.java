package com.familytree;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class FamilyTreeData {

    private static FamilyTreeData instance;
    private final ObservableList<Person> persons;
    private final Map<Person, Relationship> relationships;

    private FamilyTreeData() {
        persons = FXCollections.observableArrayList();
        relationships = new HashMap<>();
    }

    public static FamilyTreeData getInstance() {
        if (instance == null) {
            instance = new FamilyTreeData();
        }
        return instance;
    }

    public ObservableList<Person> getPersons() {
        return persons;
    }

    public void addPerson(Person person) {
        persons.add(person);
        relationships.putIfAbsent(person, new Relationship(null,null,null));
    }

    public void removePerson(Person person) {
        persons.remove(person);
        relationships.remove(person);
            relationships.values().remove(person);
    }

    public Relationship getRelationship(Person person) {
        return relationships.get(person);
    }

    public void addParentChildRelation(Person parent, Person child) {
        relationships.putIfAbsent(parent, new Relationship(null,null,null));
        relationships.putIfAbsent(child, new Relationship(null,null,null));
        relationships.get(parent).setToId(child.getName());
        relationships.get(child).setFromId(parent.getName());
    }

    public void clear() {
        persons.clear();
        relationships.clear();
    }
}