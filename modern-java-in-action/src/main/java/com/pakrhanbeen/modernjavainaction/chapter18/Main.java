package com.pakrhanbeen.modernjavainaction.chapter18;

import java.util.stream.LongStream;

public class Main {

    public static void main(String[] args) {

    }

    static int factorialIterative(int n) {
        // 반복 방식의 팩토리얼
        int r = 1;
        for (int i = 1; i <= n; i++) {
            r *= i;
        }
        return r;
    }

    static long factorialRecursive(long n) {
        // 재귀 방식의 팩토리얼
        return n == 1 ? 1 : n * factorialRecursive(n - 1);
    }

    static long factorialStreams(long n) {
        // 스트림 팩토리얼
        return LongStream.rangeClosed(1, n)
            .reduce(1, (long a, long b) -> a * b);
    }

    static long factorialTailRecursive(long n) {
        // 꼬리 재귀 팩토리얼
        return factorialHelper(1, n);
    }

    static long factorialHelper(long acc, long n) {
        return n == 1 ? acc : factorialHelper(acc * n, n - 1);
    }
}
