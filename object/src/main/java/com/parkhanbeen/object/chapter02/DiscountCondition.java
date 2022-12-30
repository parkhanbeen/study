package com.parkhanbeen.object.chapter02;

public interface DiscountCondition {

    /**
     * 할인 조건을 만족할 경우 true.
     *
     * @param screening 상영
     */
    boolean isSatisfiedBy(Screening screening);
}
