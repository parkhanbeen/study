package com.parkhanbeen.object.chapter02;

/**
 * 할인 요금이 없는 정책.
 */
public class NoneDefaultDiscountPolicy implements DiscountPolicy {

    @Override
    public Money calculateDiscountAmount(Screening screening) {
        return Money.ZERO;
    }
}
