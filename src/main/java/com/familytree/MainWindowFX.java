package com.familytree;

// MainWindowFX.java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainWindowFX extends Application {
//    private FamilyTree familyTree = new FamilyTree();
//
    @Override
    public void start(Stage stage) {
//        stage.setTitle("Family Tree (JavaFX)");
//
//        // Layout
//        BorderPane root = new BorderPane();
//        VBox inputPanel = new VBox(10);
//        TreeView<String> treeView = new TreeView<>();
//
//        // Input Fields
//        TextField nameField = new TextField();
//        nameField.setPromptText("Name");
//        TextField ageField = new TextField();
//        ageField.setPromptText("Age");
//
//        // Buttons
//        Button addButton = new Button("Add Person");
//        addButton.setOnAction(e -> {
//            Person p = new Person(nameField.getText(),
//                    Integer.parseInt(ageField.getText()),
//                    "male"); // Default gender
//            familyTree.addPerson(p);
//            updateTreeView(treeView); // Refresh UI
//        });
//
//        // Assemble UI
//        inputPanel.getChildren().addAll(
//                new Label("Add Member"), nameField, ageField, addButton
//        );
//        root.setLeft(inputPanel);
//        root.setCenter(treeView);
//
//        // Show Window
//        stage.setScene(new Scene(root, 800, 600));
//        stage.show();
//    }
//
//    private void updateTreeView(TreeView<String> treeView) {
//        TreeItem<String> rootItem = new TreeItem<>("Family Tree");
//        for (Person person : familyTree.getPeople()) {
//            TreeItem<String> personItem = new TreeItem<>(person.getName());
//            rootItem.getChildren().add(personItem);
//        }
//        treeView.setRoot(rootItem);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
