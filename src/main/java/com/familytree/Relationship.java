package com.familytree;

import lombok.Data;

@Data
public class Relationship {
    private String fromId;
    private String toId;
    private String type; // e.g., Parent, Spouse, etc.

    public Relationship(String fromId, String toId, String type) {
        this.fromId = fromId;
        this.toId = toId;
        this.type = type;
    }

    public String getFromId() {
        return fromId;
    }

    public String getToId() {
        return toId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}