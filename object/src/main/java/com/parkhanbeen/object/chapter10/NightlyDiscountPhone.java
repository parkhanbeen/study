package com.parkhanbeen.object.chapter10;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class NightlyDiscountPhone extends Phone {
  private static final int LATE_NIGHT_HOUR = 22;

  private Money nightlyAmount;

  public NightlyDiscountPhone(Money amount, Duration seconds,
                              Money nightlyAmount, double taxRate) {
    super(amount, seconds, taxRate);
    this.nightlyAmount = nightlyAmount;
  }

  @Override
  public Money calculateFee() {
    // 부모 클래스의 calculateFee 호출
    Money result = super.calculateFee();

    Money nightlyFee = Money.ZERO;
    for (Call call : getCalls()) {
      if (call.getFrom().getHour() >= LATE_NIGHT_HOUR) {
        nightlyFee = nightlyFee.plus(
            getAmount().minus(nightlyAmount).times(
                call.getDuration().getSeconds() / getSeconds().getSeconds()));
      }
    }

    // 중복된 코드를 수정하다 발생하는 이슈 minus -> plus
    return result.minus(nightlyFee.plus(nightlyFee.times(getTaxRate())));
  }
}
