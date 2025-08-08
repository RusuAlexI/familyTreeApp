package com.familytree;

public class Position {
    private double x;
    private double y;
    // The 'id' field might have been added for specific use cases,
    // but for simple (x,y) coordinates, it's often not needed in the constructor.
    // If it's truly optional, we can remove it from the simpler constructor.
    // Let's assume for now it's only needed for specific serialization or context.
    private String id; // Keep the ID field if it's used elsewhere for serialization/deserialization

    // Constructor with ID (if needed for serialization or specific contexts)
    public Position(double x, double y, String id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    // NEW: Constructor for simple X, Y coordinates, as expected by TreeVisualizer
    public Position(double x, double y) {
        this.x = x;
        this.y = y;
        this.id = null; // Or some default if ID is always required
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                (id != null ? (", id='" + id + "'") : "") +
                '}';
    }
}