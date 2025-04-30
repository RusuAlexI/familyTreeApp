package com.familytree;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Person {
    private String id;
    private String name;
    private String dateOfBirth;
    private String dateOfDeath;
    private String gender;
    private List<String> parentIds;

    public Person(String id, String name, String dateOfBirth, String dateOfDeath, String gender) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.dateOfDeath = dateOfDeath;
        this.gender = gender;
        this.parentIds = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getDateOfDeath() {
        return dateOfDeath;
    }

    public String getGender() {
        return gender;
    }

    public List<String> getParentIds() {
        return parentIds;
    }

    public void addParentId(String parentId) {
        if (!parentIds.contains(parentId)) {
            parentIds.add(parentId);
        }
    }

    public void removeParentId(String parentId) {
        parentIds.remove(parentId);
    }
}
