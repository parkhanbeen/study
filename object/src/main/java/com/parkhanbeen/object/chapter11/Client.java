package com.parkhanbeen.object.chapter11;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class Client {

  public void enter() {

    // 일반 요금제에 세금 정책 조합
    Phone basicAmountAndTaxPolicyPhone = new Phone(
        new TaxablePolicy(
            new RegularPolicy(
                new Money(new BigDecimal(1000)),
                Duration.ofSeconds(10000)),
            0.05));

    // 일반 요금제에 기본 요금 할인 정책을 조합한 결과에 세금 정책 조합
    Phone basicAmountAndRateDiscountAndTaxPolicyPhone = new Phone(
        new TaxablePolicy(
            new RateDiscountablePolicy(
                new RegularPolicy(
                    new Money(new BigDecimal(1000)), Duration.ofSeconds(10000)),
                Money.wons(1000)),
            0.05));

    // 일반 요금제에 세금 정책을 조합한 결과에 기본 요금 할인 정책 조합
    Phone basicAmountAndTaxPolicyAndRateDiscountPhone = new Phone(
        new RateDiscountablePolicy(
            new TaxablePolicy(
                new RegularPolicy(
                    new Money(new BigDecimal(1000)), Duration.ofSeconds(10000)), 0.05),
            new Money(new BigDecimal(1000))
        ));

    // 심야 요금제에 세금 정책을 조합한 결과에 기본 요금 할인 정책 조합
    Phone nightlyAmountAndTaxPolicyAndRateDiscountPhone = new Phone(
        new RateDiscountablePolicy(
            new TaxablePolicy(
                new NightlyDiscountPolicy(
                    new Money(new BigDecimal(1500)),
                    new Money(new BigDecimal(1000)),
                    Duration.ofSeconds(10000)),
                0.05),
            new Money(new BigDecimal(1000))
        ));
  }
}
