package com.familytree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FamilyTreeDataDTO {
    private List<PersonDTO> persons = new ArrayList<>();

    // Default constructor for Jackson
    public FamilyTreeDataDTO() {}

    // Conversion from domain FamilyTreeData to DTO
    public static FamilyTreeDataDTO fromDomain(FamilyTreeData domain) {
        FamilyTreeDataDTO dto = new FamilyTreeDataDTO();
        dto.setPersons(domain.getPersons().stream()
                .map(PersonDTO::fromDomain)
                .collect(Collectors.toList()));
        return dto;
    }

    // Conversion back to domain FamilyTreeData
    public FamilyTreeData toDomain() {
        FamilyTreeData domain = new FamilyTreeData();

        // First convert all persons without relationships
        List<Person> domainPersons = this.persons.stream()
                .map(PersonDTO::toDomain)
                .collect(Collectors.toList());

        domain.setPersons(domainPersons);

        // Then establish relationships
        for (int i = 0; i < this.persons.size(); i++) {
            PersonDTO dto = this.persons.get(i);
            Person domainPerson = domainPersons.get(i);

            // Rebuild parent relationships
            for (String parentId : dto.getParentIds()) {
                Person parent = domain.findById(parentId);
                if (parent != null) {
                    domainPerson.addParent(parent);
                }
            }
        }

        return domain;
    }

    // Getters and setters
    public List<PersonDTO> getPersons() { return persons; }
    public void setPersons(List<PersonDTO> persons) { this.persons = persons; }
}