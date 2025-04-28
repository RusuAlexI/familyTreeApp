package com.familytree;

public class Position {
    private double x;
    private double y;
    private String personId;

    public Position(double x, double y, String personId) {
        this.x = x;
        this.y = y;
        this.personId = personId;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getPersonId() {
        return personId;
    }
}
