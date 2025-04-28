package com.familytree;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FamilyTreeIO {

    public static void saveTree(FamilyTreeData treeData, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            // Create a Gson object for serialization
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // Serialize the object to JSON and write to file
            gson.toJson(treeData, writer);
            System.out.println("Tree data saved successfully to " + fileName);
            System.out.println("Saving to file: " + new File("familytree.json").getAbsolutePath());
// Check this print
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FamilyTreeData loadTree(String fileName) {
        try (FileReader reader = new FileReader(fileName)) {
            // Create a Gson object for deserialization
            Gson gson = new Gson();
            FamilyTreeData data = gson.fromJson(reader, FamilyTreeData.class);
            return data != null ? data : new FamilyTreeData();
        } catch (IOException e) {
            System.out.println("No saved file found, starting fresh.");
            return new FamilyTreeData();  // Return empty tree if file doesn't exist or there's an error
        }
    }
}
