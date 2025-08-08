package com.familytree;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JsonManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Now accepts a File object to save to a user-specified location
    public static void saveTree(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(FamilyTreeData.getInstance(), writer);
        }
    }

    // Now accepts a File object to load from a user-specified location
    public static FamilyTreeData loadTree(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            FamilyTreeData loadedData = gson.fromJson(reader, FamilyTreeData.class);
            if (loadedData != null) {
                FamilyTreeData.getInstance().setAllData(loadedData);
            }
            return FamilyTreeData.getInstance();
        }
    }
}