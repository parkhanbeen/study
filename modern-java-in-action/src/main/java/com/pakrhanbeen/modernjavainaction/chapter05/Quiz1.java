package com.pakrhanbeen.modernjavainaction.chapter05;

import java.util.List;
import java.util.stream.Collectors;

public class Quiz1 {

    public static void main(String[] args) {
        final var numbers = List.of(1, 2, 3, 4, 5);

        List<Integer> 제곱_숫자 = numbers.stream()
            .map(number -> number * number)
            .collect(Collectors.toList());

        System.out.println("제곱_숫자 = " + 제곱_숫자);

    }

}
