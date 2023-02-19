package com.pakrhanbeen.modernjavainaction.chapter04;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.pakrhanbeen.modernjavainaction.chapter04.Dish.Type;

import static java.util.stream.Collectors.*;

public class Client {
    public static void main(String[] args) {
        List<Dish> menu = Arrays.asList(
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

        List<String> threeHighCaloricDishNames = menu.stream()  // 메뉴(요리 리스트)에서 스트림을 얻는다.
            .filter(dish -> dish.getCalories() > 300)  // 파이프라인 연산 만들기. 첫 번째로 고칼로리 요리를 필터링한다.
            .map(Dish::getName)   // 요리명 추출
            .limit(3)    // 선착순 세 개만 선택
            .collect(toList());  // 결과를 다른 리스트로 저장

//        System.out.println(threeHighCaloricDishNames);

        List<String> names = menu.stream()
            .filter(dish -> {
                System.out.println("filtering: " + dish.getName());
                return dish.getCalories() > 300;
            })
            .map(dish -> {
                System.out.println("mapping: " + dish.getName());
                return dish.getName();
            })
            .limit(3)
            .collect(toList());

        System.out.println("names = " + names);

    }
}
