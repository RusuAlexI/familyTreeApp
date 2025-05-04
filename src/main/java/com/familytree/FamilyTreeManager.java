package com.familytree;

import java.util.ArrayList;
import java.util.List;

public class FamilyTreeManager {

    private List<Person> persons;

    public FamilyTreeManager() {
        this.persons = FamilyTreeData.getInstance().getPersons();
    }

    public void addPerson(Person p) {
        persons.add(p);
//        FamilyTreeData.getInstance().saveData();
    }

    public void removePerson(Person p) {
        persons.remove(p);
//        FamilyTreeData.getInstance().saveData();
    }

    public List<Person> getAllPersons() {
        return new ArrayList<>(persons);
    }
}
