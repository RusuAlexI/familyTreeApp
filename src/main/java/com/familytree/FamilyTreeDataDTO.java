package com.familytree;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@JsonIgnoreProperties(ignoreUnknown = true)
public class FamilyTreeDataDTO {
    private List<PersonDTO> persons = new ArrayList<>();

    public FamilyTreeDataDTO() {}

    public static FamilyTreeDataDTO fromDomain(FamilyTreeData domain) {
        FamilyTreeDataDTO dto = new FamilyTreeDataDTO();
        dto.setPersons(domain.getPersons().stream()
                .map(PersonDTO::fromDomain)
                .collect(Collectors.toList()));
        return dto;
    }

    public FamilyTreeData toDomain() {
        FamilyTreeData domain = new FamilyTreeData();
        List<Person> domainPersons = this.persons.stream()
                .map(PersonDTO::toDomain)
                .collect(Collectors.toList());
        domain.setPersons(domainPersons);

        for (int i = 0; i < this.persons.size(); i++) {
            PersonDTO dto = this.persons.get(i);
            Person domainPerson = domainPersons.get(i);
            for (String parentId : dto.getParentIds()) {
                Person parent = domain.findById(parentId);
                if (parent != null) {
                    domainPerson.addParent(parent);
                }
            }
        }

        return domain;
    }

    public List<PersonDTO> getPersons() { return persons; }
    public void setPersons(List<PersonDTO> persons) { this.persons = persons; }
}
