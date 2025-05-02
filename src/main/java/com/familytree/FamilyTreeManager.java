package com.familytree;

import java.util.ArrayList;
import java.util.List;

public class FamilyTreeManager {

    private List<Person> people;

    public FamilyTreeManager() {
        this.people = FamilyTreeData.getInstance().getPersons();
    }

    public void addPerson(Person p) {
        people.add(p);
//        FamilyTreeData.getInstance().saveData();
    }

    public void removePerson(Person p) {
        people.remove(p);
//        FamilyTreeData.getInstance().saveData();
    }

    public List<Person> getAllPeople() {
        return new ArrayList<>(people);
    }
}
