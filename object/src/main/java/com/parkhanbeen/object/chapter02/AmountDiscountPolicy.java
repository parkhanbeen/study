package com.parkhanbeen.object.chapter02;

/**
 * 금액 할인 정책.
 */
public class AmountDiscountPolicy extends DiscountPolicy {

    /**
     * 할인 요금.
     */
    private Money discountAmount;

    public AmountDiscountPolicy(Money discountAmount, DiscountCondition... conditions) {
        super(conditions);
        this.discountAmount = discountAmount;
    }

    @Override
    protected Money getDiscountAmount(Screening screening) {
        return discountAmount;
    }
}
