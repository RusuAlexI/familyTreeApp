package com.familytree;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;

public class FamilyTreeIO {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
}