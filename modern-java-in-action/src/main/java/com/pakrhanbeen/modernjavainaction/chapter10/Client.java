package com.pakrhanbeen.modernjavainaction.chapter10;

import static com.pakrhanbeen.modernjavainaction.chapter10.MethodChainingOrderBuilder.forCustomer;
import static com.pakrhanbeen.modernjavainaction.chapter10.NestedFunctionOrderBuilder.at;
import static com.pakrhanbeen.modernjavainaction.chapter10.NestedFunctionOrderBuilder.buy;
import static com.pakrhanbeen.modernjavainaction.chapter10.NestedFunctionOrderBuilder.on;
import static com.pakrhanbeen.modernjavainaction.chapter10.NestedFunctionOrderBuilder.order;
import static com.pakrhanbeen.modernjavainaction.chapter10.NestedFunctionOrderBuilder.sell;
import static com.pakrhanbeen.modernjavainaction.chapter10.NestedFunctionOrderBuilder.stock;

public class Client {
    public static void main(String[] args) {
        // 장황한 코드
        Order order = new Order();
        order.setCustomer("BigBank");

        Trade trade1 = new Trade();
        trade1.setType(Trade.Type.BUY);

        Stock stock1 = new Stock();
        stock1.setSymbol("IBM");
        stock1.setMarket("NYSE");

        trade1.setStock(stock1);
        trade1.setPrice(125.00);
        trade1.setQuantity(80);
        order.addTrade(trade1);

        Trade trade2 = new Trade();
        trade2.setType(Trade.Type.BUY);

        Stock stock2 = new Stock();
        stock2.setSymbol("GOOGLE");
        stock2.setMarket("NASDAQ");

        trade2.setStock(stock2);
        trade2.setPrice(375.00);
        trade2.setQuantity(50);
        order.addTrade(trade2);


        // 메서드 체인 방법
        Order methodChainingUsingOrder = forCustomer("BigBank")
            .buy(80)
            .stock("IBM")
            .on("NYSE")
            .at(125.00)
            .sell(50)
            .stock("GOOGLE")
            .on("NASDAQ")
            .at(375.00)
            .end();


        // 중첩된 함수 이용
        Order nestedFunctionUsingOrder = order("BigBank",
            buy(80,
                stock("IBM", on("NYSE")), at(125.00)),
            sell(50,
                stock("GOOGLE", on("NASDAQ")), at(375.00))
            );

        // 람다 표현식
        Order lambdaUsingOrder = LambdaOrderBuilder.order(o -> {
            o.forCustomer("BigBank");
            o.buy(t -> {
                t.quantity(80);
                t.price(125.00);
                t.stock(s -> {
                    s.symbol("IBM");
                    s.market("NYSE");
                });
            });
            o.sell(t -> {
                t.quantity(50);
                t.price(375.00);
                t.stock(s -> {
                    s.symbol("GOOGLE");
                    s.market("NASDAQ");
                });
            });
        });

        // 조합하여 사용
        Order combinationUsingOrder = MixedBuilder.forCustomer("BigBank",  // 최상위 수준 주문의 속성을 지정하는 중첩 함수
            MixedBuilder.buy(t -> t.quantity(80)   // 한 개의 주문을 만드는 람다 표현식
                    .stock("IBM")     // 거래 객체를 만드는 람다 표현식 바디의 메서드 체인
                    .on("NYSE")
                    .at(125.00)),
            MixedBuilder.sell(t -> t.quantity(50)   // 한 개의 주문을 만드는 람다 표현식
                .stock("GOOGLE")     // 거래 객체를 만드는 람다 표현식 바디의 메서드 체인
                .on("NASDAQ")
                .at(125.00))
        );

        // 메서드 참조 예제
        // 주문에 세금 적용
//        double value = Tax.calculate(combinationUsingOrder, true, false, true);
        double value = new TaxCalculator()
            .withTaxGeneral()
            .withTaxRegional()
            .calculate(combinationUsingOrder);

        double value2 = new TaxCalculator2()
            .with(Tax::regional)
            .with(Tax::surcharge)
            .calculate(combinationUsingOrder);
    }
}
