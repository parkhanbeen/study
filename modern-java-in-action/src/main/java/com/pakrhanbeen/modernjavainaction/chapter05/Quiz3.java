package com.pakrhanbeen.modernjavainaction.chapter05;

import java.util.List;
import java.util.stream.Collectors;

public class Quiz3 {

    public static void main(String[] args) {
        final var numbers1 = List.of(1, 2, 3);
        final var numbers2 = List.of(3, 4);

        final var pairs = numbers1.stream()
            .flatMap(i -> numbers2.stream()
                .filter(j -> (i + j) % 3 == 0)
                .map(j -> new int[]{i, j}))
            .collect(Collectors.toList());

        pairs.stream()
                .forEach(System.out::println);
    }

}
