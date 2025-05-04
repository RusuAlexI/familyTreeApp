package com.familytree;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@AllArgsConstructor
public class Person {

    private String id;
    private String name;
    private String dateOfBirth;
    private String dateOfDeath;
    private String gender;

    private List<Person> children = new ArrayList<>();
    private List<Person> parents = new ArrayList<>();

    public Person() {} // Required for Jackson

    // All getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getDateOfDeath() { return dateOfDeath; }
    public void setDateOfDeath(String dateOfDeath) { this.dateOfDeath = dateOfDeath; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public List<Person> getChildren() { return children; }
    public void setChildren(List<Person> children) { this.children = children; }

    public List<Person> getParents() { return parents; }
    public void setParents(List<Person> parents) { this.parents = parents; }

    public void addParent(Person parent) {
        if (!parents.contains(parent)) {
            parents.add(parent);
        }
        if (!parent.getChildren().contains(this)) {
            parent.getChildren().add(this);
        }
    }

    @Override
    public String toString() {
        return name + " (" + gender + ")";
    }
}