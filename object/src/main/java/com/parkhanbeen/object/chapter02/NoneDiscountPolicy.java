package com.parkhanbeen.object.chapter02;

/**
 * 할인 요금이 없는 정책.
 */
public class NoneDiscountPolicy extends DiscountPolicy {
    @Override
    protected Money getDiscountAmount(Screening screening) {
        return Money.ZERO;
    }
}
