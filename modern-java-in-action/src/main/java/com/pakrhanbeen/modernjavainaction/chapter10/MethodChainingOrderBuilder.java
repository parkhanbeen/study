package com.pakrhanbeen.modernjavainaction.chapter10;

class MethodChainingOrderBuilder {
    /**
     * 빌더로 감싼 주문.
     */
    final Order order = new Order();

    private MethodChainingOrderBuilder(String customer) {
        order.setCustomer(customer);
    }

    /**
     * 고객의 주문을 만드는 정적 팩터리 메서드
     */
    static MethodChainingOrderBuilder forCustomer(String customer) {
        return new MethodChainingOrderBuilder(customer);
    }

    /**
     * 주식을 사는 TradeBuilder.
     */
    TradeBuilder buy(int quantity) {
        return new TradeBuilder(this, Trade.Type.BUY, quantity);
    }

    /**
     * 주식을 파는 TradeBuilder.
     */
    TradeBuilder sell(int quantity) {
        return new TradeBuilder(this, Trade.Type.SELL, quantity);
    }

    /**
     * 주문에 주식 추가.
     * 유연하게 추가 주문을 만들어 추가할 수 있도록 주문 빌더 자체 반환.
     */
    MethodChainingOrderBuilder addTrade(Trade trade) {
        order.addTrade(trade);
        return this;
    }

    /**
     * 주문 만들기 종료.
     */
    Order end() {
        return order;
    }
}
