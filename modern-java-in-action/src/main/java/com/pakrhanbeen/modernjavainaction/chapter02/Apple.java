package com.pakrhanbeen.modernjavainaction.chapter02;

public class Apple {
    private final int weight;

    private final Color color;

    public Apple(int weight, Color color) {
        this.weight = weight;
        this.color = color;
    }

    public int getWeight() {
        return weight;
    }

    public Color getColor() {
        return color;
    }
}
