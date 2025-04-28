package com.familytree;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TreeDataManager {
    private static final String FILE_NAME = "family_tree.json";

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static void save(FamilyTreeData data) {
        try (FileWriter writer = new FileWriter(FILE_NAME)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FamilyTreeData load() {
        try (FileReader reader = new FileReader(FILE_NAME)) {
            return gson.fromJson(reader, FamilyTreeData.class);
        } catch (IOException e) {
            // File may not exist yet — return empty tree
            return new FamilyTreeData();
        }
    }
}
