package com.pakrhanbeen.modernjavainaction.chapter10;

/**
 * 적용할 세금을 유창하게 정의하는 세금 계산기.
 */
public class TaxCalculator {
    private boolean useRegional;
    private boolean useGeneral;
    private boolean useSurcharge;

    TaxCalculator withTaxRegional() {
        useRegional = true;
        return this;
    }

    TaxCalculator withTaxGeneral() {
        useGeneral = true;
        return this;
    }

    TaxCalculator withTaxSurcharge() {
        useSurcharge = true;
        return this;
    }

    double calculate(Order order) {
        return calculate(order, useRegional, useGeneral, useSurcharge);
    }

    static double calculate(Order order,
                            boolean useRegional,
                            boolean useGeneral,
                            boolean useSurcharge) {
        double value = order.getValue();
        if (useRegional) {
            value = Tax.regional(value);
        }

        if (useGeneral) {
            value = Tax.general(value);
        }

        if (useSurcharge) {
            value = Tax.surcharge(value);
        }
        return value;
    }

}
