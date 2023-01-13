package com.parkhanbeen.object.chapter10;

import java.time.Duration;

public class RegularPhone extends Phone {
  private Money amount;
  private Duration seconds;

  public RegularPhone(Money amount, Duration seconds, double taxRate) {
    super(taxRate);
    this.amount = amount;
    this.seconds = seconds;
  }

  public Money getAmount() {
    return amount;
  }

  public Duration getSeconds() {
    return seconds;
  }

  @Override
  protected Money calculateCallFee(Call call) {
    return amount.times(
        call.getDuration().getSeconds() / seconds.getSeconds());
  }
}
