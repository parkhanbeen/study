package com.pakrhanbeen.modernjavainaction.chapter10;

import java.util.function.Consumer;

class LambdaOrderBuilder {
    private Order order = new Order();

    static Order order(Consumer<LambdaOrderBuilder> consumer) {
        LambdaOrderBuilder builder = new LambdaOrderBuilder();
        consumer.accept(builder);
        return builder.order;
    }

    /**
     * 주문을 요청한 고객 설정.
     */
    void forCustomer(String customer) {
        order.setCustomer(customer);
    }

    /**
     * 주식 매수 주문을 만들도록 TradeBuilder 소비.
     */
    void buy(Consumer<TradeBuilder> consumer) {
        trade(consumer, Trade.Type.BUY);
    }

    /**
     * 주식 매도 주문을 만들도록 TradeBuilder 소비.
     */
    void sell(Consumer<TradeBuilder> consumer) {
        trade(consumer, Trade.Type.BUY);
    }

    private void trade(Consumer<TradeBuilder> consumer, Trade.Type type) {
        TradeBuilder builder = new TradeBuilder();
        builder.trade.setType(type);

        // TradeBuilder로 전달할 람다 표현식 실행
        consumer.accept(builder);

        // TradeBuilder의 Consumer를 실행해 만든 거래를 주문에 추가
        order.addTrade(builder.trade);
    }

    static class TradeBuilder {
        private Trade trade = new Trade();

        void quantity(int quantity) {
            trade.setQuantity(quantity);
        }

        void price(double price) {
            trade.setPrice(price);
        }

        void stock(Consumer<StockBuilder> consumer) {
            StockBuilder builder = new StockBuilder();
            consumer.accept(builder);
            trade.setStock(builder.stock);
        }
    }

    static class StockBuilder {
        private Stock stock = new Stock();

        void symbol(String symbol) {
            stock.setSymbol(symbol);
        }

        void market(String market) {
            stock.setMarket(market);
        }
    }
}
