package com.familytree;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Person {
    private String name;
    private LocalDate dateOfBirth;
    private LocalDate dateOfDeath;
    private String gender;

    private final List<Person> parents = new ArrayList<>();
    private final List<Person> children = new ArrayList<>();

    public Person(String name, LocalDate dateOfBirth, LocalDate dateOfDeath, String gender) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.dateOfDeath = dateOfDeath;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getDateOfDeath() {
        return dateOfDeath;
    }

    public void setDateOfDeath(LocalDate dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public List<Person> getParents() {
        return parents;
    }

    public List<Person> getChildren() {
        return children;
    }

    public void addParent(Person parent) {
        if (!parents.contains(parent)) {
            parents.add(parent);
            parent.addChild(this); // maintain bidirectional relationship
        }
    }

    public void addChild(Person child) {
        if (!children.contains(child)) {
            children.add(child);
            child.addParent(this); // avoid infinite recursion via check
        }
    }

    @Override
    public String toString() {
        return name;
    }
}