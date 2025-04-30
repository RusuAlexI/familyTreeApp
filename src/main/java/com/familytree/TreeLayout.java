package com.familytree;

import java.util.*;

public class TreeLayout {

    public static class Position {
        private double x;
        private double y;

        public Position(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() { return x; }

        public double getY() { return y; }

        public void setX(double x) { this.x = x; }

        public void setY(double y) { this.y = y; }
    }

    private static final double NODE_WIDTH = 120;
    private static final double NODE_HEIGHT = 60;
    private static final double H_SPACING = 40;
    private static final double V_SPACING = 100;

    public static Map<Person, Position> computeLayout(List<Person> persons) {
        Map<Person, Position> layout = new HashMap<>();

        double x = 50;
        double y = 50;

        for (Person p : persons) {
            layout.put(p, new Position(x, y));
            x += NODE_WIDTH + H_SPACING;
        }

        return layout;
    }
}