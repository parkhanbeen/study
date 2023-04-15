package com.pakrhanbeen.modernjavainaction.chapter10;

import java.util.function.Consumer;
import java.util.stream.Stream;

class MixedBuilder {
    static Order forCustomer(String customer,
                             TradeBuilder... builders) {
        Order order = new Order();
        order.setCustomer(customer);
        Stream.of(builders).forEach(b -> order.addTrade(b.trade));
        return order;
    }

    static TradeBuilder buy(Consumer<TradeBuilder> consumer) {
        return buildTrade(consumer, Trade.Type.BUY);
    }

    static TradeBuilder sell(Consumer<TradeBuilder> consumer) {
        return buildTrade(consumer, Trade.Type.SELL);
    }

    private static TradeBuilder buildTrade(Consumer<TradeBuilder> consumer, Trade.Type type) {
        TradeBuilder builder = new TradeBuilder();
        builder.trade.setType(type);
        consumer.accept(builder);
        return builder;
    }

    static class TradeBuilder {
        private Trade trade = new Trade();

        TradeBuilder quantity(int quantity) {
            trade.setQuantity(quantity);
            return this;
        }

        TradeBuilder at(double price) {
            trade.setPrice(price);
            return this;
        }

        StockBuilder stock(String symbol) {
            return new StockBuilder(this, trade, symbol);
        }
    }

    static class StockBuilder {
        private final TradeBuilder builder;
        private final Trade trade;
        private final Stock stock = new Stock();

        private StockBuilder(TradeBuilder builder, Trade trade, String symbol) {
            this.builder = builder;
            this.trade = trade;
            stock.setSymbol(symbol);
        }

        TradeBuilder on(String market) {
            stock.setMarket(market);
            trade.setStock(stock);
            return builder;
        }
    }


}
