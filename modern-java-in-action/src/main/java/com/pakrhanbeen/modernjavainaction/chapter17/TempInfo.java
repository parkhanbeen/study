package com.pakrhanbeen.modernjavainaction.chapter17;

import java.util.Random;

/**
 * 원격 온도계 (0에서 99 사이의 화씨 온도를 임의로 만들어 연속적으로 보고).
 */
public class TempInfo {
    public static final Random random = new Random();

    private final String town;
    private final int temp;

    public TempInfo(String town, int temp) {
        this.town = town;
        this.temp = temp;
    }

    public static TempInfo fetch(String town) {
        // 10분의 1확률로 온도 가져오기 작업이 실패한다.
        if (random.nextInt(10) == 0) {
            throw new RuntimeException("Error!");
        }

        // 0에서 99 사이에서 임의의 화씨 온도를 반환한다.
        return new TempInfo(town, random.nextInt(100));
    }

    @Override
    public String toString() {
        return town + " : " + temp;
    }

    public String getTown() {
        return town;
    }

    public int getTemp() {
        return temp;
    }
}
