package com.parkhanbeen.object.chapter10;

import java.util.ArrayList;
import java.util.List;

public abstract class Phone {

    private double taxRate;

    private List<Call> calls = new ArrayList<>();

    public Phone(double taxRate) {
        this.taxRate = taxRate;
    }

    public Money calculateFee() {
        // 부모 클래스의 calculateFee 호출
        Money result = Money.ZERO;

        for (Call call : calls) {
            result = result.plus(
                calculateCallFee(call));
        }

        return result;
    }

    abstract protected Money calculateCallFee(Call call);

}
