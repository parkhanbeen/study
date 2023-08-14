package com.cleanarchitectureexample.account.domain;

public class Money {

    private final Long amount;

    public Money(Long amount) {
        this.amount = amount;
    }

    public static Money add(Money baselineBalance, Object calculateBalance) {
        return null;
    }

    public static Money of(Long amount) {
        return new Money(amount);
    }

    public Object negate() {
        return null;
    }

    public boolean isPositive() {
        return false;
    }
}
