package com.familytree;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FamilyTreeData {

    private List<Relationship> relationships = new ArrayList<>();
    private static FamilyTreeData instance;
    private List<Person> persons;
    private final String FILE = "family_tree.json";

    private FamilyTreeData() {
        persons = new ArrayList<>();
        loadData();
    }

    public static FamilyTreeData getInstance() {
        if (instance == null) {
            instance = new FamilyTreeData();
        }
        return instance;
    }

    public void addRelationship(Relationship r) {
        relationships.add(r);
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void saveData() {
        try (Writer writer = new FileWriter(FILE)) {
            new Gson().toJson(persons, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        File file = new File(FILE);
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                Type type = new TypeToken<List<Person>>() {}.getType();
                persons = new Gson().fromJson(reader, type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addPerson(Person person) {
        persons.add(person);
    }
    public void removePerson(Person person) {
        persons.remove(person);
    }

}
