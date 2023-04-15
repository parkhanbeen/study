package com.pakrhanbeen.modernjavainaction.chapter10;

/**
 * 주식으 시장을 지정하고 거래에 주식을 추가합니다.
 */
class StockBuilder {
    private final MethodChainingOrderBuilder builder;
    private final Trade trade;
    private final Stock stock = new Stock();

    StockBuilder(MethodChainingOrderBuilder builder,
                        Trade trade,
                        String symbol) {
        this.builder = builder;
        this.trade = trade;
        stock.setSymbol(symbol);
    }

    TradeBuilderWithStock on(String market) {
        stock.setMarket(market);
        trade.setStock(stock);
        return new TradeBuilderWithStock(builder, trade);
    }
}
