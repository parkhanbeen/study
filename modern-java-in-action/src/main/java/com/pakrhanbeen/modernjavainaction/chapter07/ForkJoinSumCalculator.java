package com.pakrhanbeen.modernjavainaction.chapter07;

import java.util.concurrent.RecursiveTask;

/**
 * 포크 조인 프레임워크를 이용한 병렬 합계 계산기.
 */
public class ForkJoinSumCalculator extends RecursiveTask<Long> {
    /**
     * 이 값 이하의 서브태스크는 더 이상 분할할 수 없다.
     */
    private static final long THRESHOLD = 10_000;

    private final long[] numbers;

    /**
     * 서브태스크에서 처리할 배열의 초기 위치.
     */
    private final int start;

    /**
     * 서브태스크에서 처리할 배열의 최종 위치.
     */
    private final int end;

    public ForkJoinSumCalculator(long[] numbers) {
        this(numbers, 0, numbers.length);
    }

    private ForkJoinSumCalculator(long[] numbers, int start, int end) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        int length = end - start;
        if (length <= THRESHOLD) {
            // 기준값과 같거나 작으면 순차적으로 결과를 계산한다.
            return computeSequentially();
        }
        // 배열의 첫 번째 절반을 더하도록 서브태스크를 생성한다.
        ForkJoinSumCalculator leftTask = new ForkJoinSumCalculator(numbers, start, start + length/2);

        // ForkJoinPool의 다른 스레드로 새로 생선한 태스크를 비동기로 실행한다.
        leftTask.fork();

        // 배열의 나머지 절반을 더하도록 서브태스크를 생성한다.
        ForkJoinSumCalculator rightTask = new ForkJoinSumCalculator(numbers, start + length/2, end);

        // 두 번째 서브태스크를 동기 실행한다.
        // 이때 추가로 분할이 일어날 수 있다.
        Long rightResult = rightTask.compute();

        // 첫 번째 서브태스크의 결과를 읽거나 아직 결과가 없으면 기다린다.
        Long leftResult = leftTask.join();

        // 두 서브태스크의 결과를 조합한 값이 이 태스크의 결과다.
        return leftResult + rightResult;
    }

    /**
     * 서브태스크의 결과를 계산.
     */
    private Long computeSequentially() {
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += numbers[i];
        }
        return sum;
    }
}
