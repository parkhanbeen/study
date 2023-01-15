package com.parkhanbeen.object.chapter11;

import java.util.ArrayList;
import java.util.List;

import com.parkhanbeen.object.chapter10.Call;

public abstract class Phone {

    private List<Call> calls = new ArrayList<>();

    public Money calculateFee() {
        // 부모 클래스의 calculateFee 호출
        Money result = Money.ZERO;

        for (Call call : calls) {
            result = result.plus(
                calculateCallFee(call));
        }

        return result;
    }

    // 훅 메서드
    protected Money afterCalculated(Money fee) {
        return fee;
    }

    abstract protected Money calculateCallFee(Call call);

}
