package com.familytree;

import java.util.ArrayList;
import java.util.List;

public class FamilyTreeData {
    private static FamilyTreeData instance = new FamilyTreeData();

    private List<Person> persons = new ArrayList<>();

    private FamilyTreeData() {}

    public static FamilyTreeData getInstance() {
        return instance;
    }

    public static void setInstance(FamilyTreeData newInstance) {
        instance = newInstance;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

    public void addPerson(Person person) {
        this.persons.add(person);
    }
}