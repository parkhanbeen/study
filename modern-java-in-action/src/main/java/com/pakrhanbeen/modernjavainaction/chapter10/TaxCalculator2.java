package com.pakrhanbeen.modernjavainaction.chapter10;

import java.util.function.DoubleUnaryOperator;

/**
 * 적용할 세금을 유창하게 정의하는 세금 계산기 리팩터링 버전.
 */
public class TaxCalculator2 {

    /**
     * 주문 값에 적용된 모든 세금을 계산하는 함수.
     */
    DoubleUnaryOperator taxFunction = d -> d;

    /**
     * 새로운 세금 계산 함수를 얻어서 인수로 전달된 함수와 현재 함수를 합침.
     * 세금 함수가 연결될 수 있도록 결과 반환.
     */
    TaxCalculator2 with(DoubleUnaryOperator function) {
        taxFunction = taxFunction.andThen(function);
        return this;
    }

    /**
     * 주문의 총 합에 세금 계산 함수를 적용해 최종 주문값을 계산.
     */
    double calculate(Order order) {
        return taxFunction.applyAsDouble(order.getValue());
    }
}
