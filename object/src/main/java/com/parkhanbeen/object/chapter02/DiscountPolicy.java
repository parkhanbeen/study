package com.parkhanbeen.object.chapter02;

import java.util.Arrays;
import java.util.List;

/**
 * 할인 정책.
 */
public abstract class DiscountPolicy {

    /**
     * 할인 조건들.
     */
    private List<DiscountCondition> conditions;

    public DiscountPolicy(DiscountCondition ... conditions) {
        this.conditions = Arrays.asList(conditions);
    }

    /**
     * 전체 할인 조건 중 만족된 조건을 찾아 할인 금액을 반환합니다.
     * 만약, 만족하는 할인 조건이 없다면 Money.ZERO 를 반환합니다.
     *
     * @param screening
     */
    public Money calculateDiscountAmount(Screening screening) {
        for (DiscountCondition each : conditions) {
            if (each.isSatisfiedBy(screening)) {
                return getDiscountAmount(screening);
            }
        }

        return Money.ZERO;
    }

    /**
     * 요금을 계산합니다.
     */
    abstract protected Money getDiscountAmount(Screening screening);
}
