package com.familytree;
import java.util.ArrayList;
import java.util.List;

public class TreeDataManager {
    private static TreeDataManager instance;
    private final List<Person> persons;

    private TreeDataManager() {
        this.persons = new ArrayList<>();
    }

    public static TreeDataManager getInstance() {
        if (instance == null) {
            instance = new TreeDataManager();
        }
        return instance;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void addPerson(Person person) {
        persons.add(person);
    }

    public void removePerson(Person person) {
        persons.remove(person);
    }

    public void clearAll() {
        persons.clear();
    }

    public void loadFromFile(String filename) {
        List<Person> loaded = FamilyTreeIO.load(filename);
        if (loaded != null) {
            persons.clear();
            persons.addAll(loaded);
        }
    }

    public void saveToFile(String filename) {
        FamilyTreeIO.save(persons, filename);
    }
}
