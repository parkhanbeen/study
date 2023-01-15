package com.parkhanbeen.object.chapter11;

import java.time.Duration;

public class TaxableNightlyDiscountPhone extends NightlyDiscountPhone {
  private double taxRate;

  public TaxableNightlyDiscountPhone(Money nightlyAmount, Money regularAmount, Duration seconds, double taxRate) {
    super(nightlyAmount, regularAmount, seconds);
    this.taxRate = taxRate;
  }

  @Override
  protected Money afterCalculated(Money fee) {
    return fee.plus(fee.times(taxRate));
  }
}
