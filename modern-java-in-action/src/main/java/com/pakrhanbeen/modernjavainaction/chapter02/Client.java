package com.pakrhanbeen.modernjavainaction.chapter02;

import java.util.List;

public class Client {

    public static void main(String[] args) {
        final var apples = List.of(
            new Apple(150, Color.RED),
            new Apple(100, Color.GREEN)
        );

        prettyPrintApple(apples, new AppleSimpleFormatter());
        prettyPrintApple(apples, new AppleFancyFormatter());
    }

    private static void prettyPrintApple(List<Apple> inventory, AppleFormatter formatter) {
        for (Apple apple : inventory) {
            String output = formatter.accept(apple);
            System.out.println(output);
        }
    }
}
