package com.familytree;

import java.util.List;
import java.util.Map;

public class FamilyTreeDataDTO {

    private List<Person> persons;
    private Map<String, Position> layoutPositions;

    public FamilyTreeDataDTO() {}

    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

    public Map<String, Position> getLayoutPositions() {
        return layoutPositions;
    }

    public void setLayoutPositions(Map<String, Position> layoutPositions) {
        this.layoutPositions = layoutPositions;
    }
}