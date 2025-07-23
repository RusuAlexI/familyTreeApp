package com.familytree;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.List;

public class FamilyTreeIO {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public FamilyTreeIO() {
    }

    public static void save(List<Person> persons, String filename) {
        try (Writer writer = new FileWriter(filename)) {
            gson.toJson(persons, writer);
            System.out.println("Saved to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Person> load(String filename) {
        try (Reader reader = new FileReader(filename)) {
            Type listType = new TypeToken<List<Person>>() {}.getType();
            List<Person> data = gson.fromJson(reader, listType);
            System.out.println("Loaded from " + filename);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveToFile(File file, FamilyTreeData data) throws IOException {
        mapper.writeValue(file, data);
    }

    public static FamilyTreeData loadFromFile(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = new String(Files.readAllBytes(file.toPath()));
        System.out.println("DEBUG raw JSON: " + json);
        FamilyTreeData loaded = mapper.readValue(file, FamilyTreeData.class);
        System.out.println("DEBUG: loaded.getPersons().size() = " + loaded.getAllPeople().size());
        for (Person p : loaded.getAllPeople()) {
            System.out.println("DEBUG: Person = " + p.getName());
        }
        return loaded;
    }
}