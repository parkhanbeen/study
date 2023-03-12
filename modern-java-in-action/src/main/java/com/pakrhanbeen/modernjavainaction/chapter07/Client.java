package com.pakrhanbeen.modernjavainaction.chapter07;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.LongStream;

public class Client {
    public static void main(String[] args) {
        System.out.println("ForkJoin sum done in = " + forkJoinSum(10_000_000));
    }

    public static long forkJoinSum(long n) {
        long[] numbers = LongStream.rangeClosed(1, n).toArray();
        ForkJoinTask<Long> task = new ForkJoinSumCalculator(numbers);
        return new ForkJoinPool().invoke(task);
    }
}
