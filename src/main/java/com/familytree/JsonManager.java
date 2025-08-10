package com.familytree;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JsonManager {
    // We now build the Gson instance with our custom TypeAdapter
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(FamilyTreeData.class, new FamilyTreeDataAdapter())
            .create();

    public static void saveTree(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(FamilyTreeData.getInstance(), writer);
        }
    }

    public static FamilyTreeData loadTree(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            // Gson now knows how to correctly deserialize into the FamilyTreeData object
            FamilyTreeData loadedData = gson.fromJson(reader, FamilyTreeData.class);
            if (loadedData != null) {
                // We clear and set the data on the singleton instance
                // instead of returning a new one.
                FamilyTreeData.getInstance().setAllData(loadedData);
            }
            return FamilyTreeData.getInstance();
        }
    }
}
