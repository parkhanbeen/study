package com.pakrhanbeen.modernjavainaction.chapter05;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.pakrhanbeen.modernjavainaction.chapter05.Dish.Type;

public class Client {

    public static void main(String[] args) {
        final var words = List.of("Hello", "World", "park", "han", "been");

        List<String> uniqueCharacters = words.stream()
            .map(word -> word.split(""))
            .flatMap(Arrays::stream)
            .distinct()
            .collect(Collectors.toList());

        System.out.println("uniqueCharacters = " + uniqueCharacters);

        List<Dish> dishes = Arrays.asList(
            new Dish("pork", false, 800, Type.MEAT),
            new Dish("beef", false, 700, Type.MEAT),
            new Dish("chicken", false, 400, Type.MEAT),
            new Dish("french fries", false, 530, Type.OTHER),
            new Dish("rice", true, 350, Type.OTHER),
            new Dish("season fruit", true, 120, Type.OTHER),
            new Dish("pizza", true, 550, Type.OTHER),
            new Dish("prawns", false, 300, Type.FISH),
            new Dish("salmon", false, 450, Type.FISH)
        );

        int summary = dishes.stream()
            .map(dish -> 1)
            .reduce(0, Integer::sum);

        long summary2 = dishes.stream().count();

        System.out.println("summary = " + summary);
        System.out.println("summary2 = " + summary2);
    }

}
