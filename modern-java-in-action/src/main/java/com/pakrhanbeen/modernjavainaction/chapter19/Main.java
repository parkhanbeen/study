package com.pakrhanbeen.modernjavainaction.chapter19;

public class Main {

    public static void main(String[] args) {
        MyList<Integer> l = new MyLinkedList<>(5, new MyLinkedList<>(10, new Empty<>()));

        // 게으른 리스트
        LazyList<Integer> numbers = from(2);
        int two = numbers.head();
        int three = numbers.tail().head();
        int four = numbers.tail().tail().head();

        System.out.println(two + " " + three + " " + four);
    }

    static LazyList<Integer> from(int n) {
        return new LazyList<>(n, () -> from(n + 1));
    }

}
