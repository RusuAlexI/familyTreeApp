package com.familytree;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FamilyTreeData {
    private static FamilyTreeData instance;
    private List<Person> persons;
    private List<Relationship> relationships;

    FamilyTreeData() {
        persons = new ArrayList<>();
        relationships = new ArrayList<>();
    }

    public static FamilyTreeData getInstance() {
        if (instance == null) {
            instance = new FamilyTreeData();
        }
        return instance;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public void addPerson(Person person) {
        persons.add(person);
        saveToFile();
    }

    public void deletePerson(Person person) {
        persons.remove(person);
        relationships.removeIf(r -> r.getFromId().equals(person.getId()) || r.getToId().equals(person.getId()));
        saveToFile();
    }

    public void updatePerson(Person person) {
        for (int i = 0; i < persons.size(); i++) {
            if (persons.get(i).getId().equals(person.getId())) {
                persons.set(i, person);
                break;
            }
        }
        saveToFile();
    }

    public void addRelationship(String fromId, String toId, String type) {
        relationships.add(new Relationship(fromId, toId, type));
        saveToFile();
    }

    public void saveToFile() {
        try (FileWriter writer = new FileWriter("familytree.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromFile() {
        try (FileReader reader = new FileReader("familytree.json")) {
            Gson gson = new Gson();
            Type dataType = new TypeToken<FamilyTreeData>() {}.getType();
            FamilyTreeData loadedData = gson.fromJson(reader, dataType);
            if (loadedData != null) {
                this.persons = loadedData.persons != null ? loadedData.persons : new ArrayList<>();
                this.relationships = loadedData.relationships != null ? loadedData.relationships : new ArrayList<>();
            }
        } catch (IOException e) {
            System.out.println("No existing file, starting fresh.");
        }
    }
}
