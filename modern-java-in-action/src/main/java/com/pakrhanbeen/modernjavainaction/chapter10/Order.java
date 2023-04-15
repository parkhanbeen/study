package com.pakrhanbeen.modernjavainaction.chapter10;

import java.util.ArrayList;
import java.util.List;

/**
 * 주문.
 */
class Order {
    private String customer;
    private List<Trade> trades = new ArrayList<>();

    void addTrade(Trade trade) {
        trades.add(trade);
    }

    String getCustomer() {
        return customer;
    }

    void setCustomer(String customer) {
        this.customer = customer;
    }

    double getValue() {
        return trades.stream()
            .mapToDouble(Trade::getValue)
            .sum();
    }
}
