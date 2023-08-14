package com.cleanarchitectureexample.account.domain;

import java.math.BigInteger;

import lombok.Getter;
import lombok.NonNull;

public class Money {

    public static Money ZERO = Money.of(0L);

    @Getter
    @NonNull
    private final BigInteger amount;

    public Money(@NonNull BigInteger amount) {
        this.amount = amount;
    }

    public boolean isPositiveOrZero(){
        return this.amount.compareTo(BigInteger.ZERO) >= 0;
    }

    public boolean isNegative(){
        return this.amount.compareTo(BigInteger.ZERO) < 0;
    }

    public boolean isPositive(){
        return this.amount.compareTo(BigInteger.ZERO) > 0;
    }

    public boolean isGreaterThanOrEqualTo(Money money){
        return this.amount.compareTo(money.amount) >= 0;
    }

    public boolean isGreaterThan(Money money){
        return this.amount.compareTo(money.amount) >= 1;
    }

    public static Money of(long value) {
        return new Money(BigInteger.valueOf(value));
    }

    public static Money add(Money a, Money b) {
        return new Money(a.amount.add(b.amount));
    }

    public Money minus(Money money){
        return new Money(this.amount.subtract(money.amount));
    }

    public Money plus(Money money){
        return new Money(this.amount.add(money.amount));
    }

    public static Money subtract(Money a, Money b) {
        return new Money(a.amount.subtract(b.amount));
    }

    public Money negate(){
        return new Money(this.amount.negate());
    }
}
