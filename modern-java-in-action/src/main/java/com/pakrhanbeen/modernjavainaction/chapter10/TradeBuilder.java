package com.pakrhanbeen.modernjavainaction.chapter10;

class TradeBuilder {
    private final MethodChainingOrderBuilder builder;
    final Trade trade = new Trade();

    TradeBuilder(MethodChainingOrderBuilder builder,
                         Trade.Type type,
                         int quantity) {
        this.builder = builder;
        trade.setType(type);
        trade.setQuantity(quantity);
    }

    StockBuilder stock(String symbol) {
        return new StockBuilder(builder, trade, symbol);
    }
}
