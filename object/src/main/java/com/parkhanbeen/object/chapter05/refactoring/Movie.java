package com.parkhanbeen.object.chapter05.refactoring;

import java.util.List;

public class Movie {
    private Money fee;
    private MovieType movieType;
    private Money discountAmount;
    private double discountPercent;
    private List<DiscountCondition> discountConditions;

    public Money getFee() {
        return fee;
    }

    public MovieType getMovieType() {
        return movieType;
    }

    public Money getDiscountAmount() {
        return discountAmount;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public List<DiscountCondition> getDiscountConditions() {
        return discountConditions;
    }
}
