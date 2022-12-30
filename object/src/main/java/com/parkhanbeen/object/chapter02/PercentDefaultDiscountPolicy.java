package com.parkhanbeen.object.chapter02;

/**
 * 비율 할인 정책.
 */
public class PercentDefaultDiscountPolicy extends DefaultDiscountPolicy {

    /**
     * 할인 비율.
     */
    private double percent;

    public PercentDefaultDiscountPolicy(double percent, DiscountCondition... conditions) {
        super(conditions);
        this.percent = percent;
    }

    @Override
    protected Money getDiscountAmount(Screening screening) {
        return screening.getMovieFee().times(percent);
    }
}
