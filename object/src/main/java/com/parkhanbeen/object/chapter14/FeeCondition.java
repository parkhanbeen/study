package com.parkhanbeen.object.chapter14;

import java.util.List;

/**
 * 적용조건.
 */
public interface FeeCondition {

    /**
     * 적용조건을 만족하는 기간을 구합니다.
     */
    List<DateTimeInterval> findTimeIntervals(Call call);
}
