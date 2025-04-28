package com.familytree;

import java.io.FileWriter;
import java.io.IOException;

public class TreeResetter {
    public static void resetFamilyTree() {
        try (FileWriter writer = new FileWriter("familytree.json")) {
            writer.write("{\"persons\":[],\"relationships\":[]}");
            System.out.println("Family tree reset successfully!");
        } catch (IOException e) {
            System.out.println("Error resetting family tree: " + e.getMessage());
        }
    }
}
