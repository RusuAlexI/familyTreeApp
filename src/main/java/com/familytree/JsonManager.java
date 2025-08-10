package com.familytree;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JsonManager {
    // We now build the Gson instance with the custom FamilyTreeDataAdapter
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(FamilyTreeData.class, new FamilyTreeDataAdapter())
            .create();

    /**
     * Saves the current FamilyTreeData instance to a user-specified file.
     * @param file The File object representing the save location.
     * @throws IOException if an I/O error occurs.
     */
    public static void saveTree(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Get the singleton instance to save its state
            gson.toJson(FamilyTreeData.getInstance(), writer);
        }
    }

    /**
     * Loads FamilyTreeData from a user-specified file into the singleton instance.
     * @param file The File object representing the load location.
     * @return The FamilyTreeData singleton instance with the loaded data.
     * @throws IOException if an I/O error occurs.
     */
    public static FamilyTreeData loadTree(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            // The custom TypeAdapter will handle populating the singleton instance.
            // We can now call fromJson directly and it will work as expected.
            FamilyTreeData loadedData = gson.fromJson(reader, FamilyTreeData.class);
            return loadedData;
        }
    }
}
