package com.familytree;

public class Person {
    private String id;
    private String name;
    private String dateOfBirth;
    private String dateOfDeath;
    private String gender;

    public Person(String id, String name, String dateOfBirth, String dateOfDeath, String gender) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.dateOfDeath = dateOfDeath;
        this.gender = gender;
    }

    public Person(String name, String dateOfBirth, String dateOfDeath, String gender) {
        this(java.util.UUID.randomUUID().toString(), name, dateOfBirth, dateOfDeath, gender);
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

    public void setName(String name) {
        this.name = name;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setDateOfDeath(String dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
