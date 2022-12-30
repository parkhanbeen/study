package com.parkhanbeen.object.chapter02;

/**
 * 할인 정책.
 */
public interface DiscountPolicy {

    Money calculateDiscountAmount(Screening screening);
}
