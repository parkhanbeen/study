package com.pakrhanbeen.modernjavainaction.chapter10;

class TradeBuilderWithStock {
    private final MethodChainingOrderBuilder builder;
    private final Trade trade;

    TradeBuilderWithStock(MethodChainingOrderBuilder builder,
                                 Trade trade) {
        this.builder = builder;
        this.trade = trade;
    }

    /**
     * 거래되는 주식의 단위 가격을 설정한 후 주문 빌더를 반환합니다.
     *
     * @param price 주식의 단위 가격
     */
    MethodChainingOrderBuilder at(double price) {
        trade.setPrice(price);
        return builder.addTrade(trade);
    }
}
