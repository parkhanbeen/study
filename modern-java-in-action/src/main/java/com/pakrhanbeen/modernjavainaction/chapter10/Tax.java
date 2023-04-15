package com.pakrhanbeen.modernjavainaction.chapter10;

/**
 * 주문의 총 합에 적용할 세금.
 */
public class Tax {
    static double regional(double value) {
        return value * 1.1;
    }

    static double general(double value) {
        return value * 1.3;
    }

    static double surcharge(double value) {
        return value * 1.05;
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
