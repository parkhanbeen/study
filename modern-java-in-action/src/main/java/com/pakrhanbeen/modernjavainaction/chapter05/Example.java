package com.pakrhanbeen.modernjavainaction.chapter05;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.*;

public class Example {

    public static void main(String[] args) {
        Trader raoul = new Trader("Raoul", "Cambridge");
        Trader mario = new Trader("Mario", "Milan");
        Trader alan = new Trader("Alan", "Cambridge");
        Trader brian = new Trader("Brian", "Cambridge");

        List<Transaction> transactions = Arrays.asList(
            new Transaction(brian, 2011, 300),
            new Transaction(raoul, 2012, 1000),
            new Transaction(raoul, 2011, 400),
            new Transaction(mario, 2012, 710),
            new Transaction(mario, 2012, 700),
            new Transaction(alan, 2012, 950)
        );

        List<Transaction> q1 = transactions.stream()
            .filter(transaction -> transaction.getYear() == 2011)
            .sorted(Comparator.comparing(Transaction::getValue))
            .collect(toList());

        System.out.println("q1 = " + q1);

        List<String> cities = transactions.stream()
            .map(transaction -> transaction.getTrader().getCity())
            .distinct()
            .collect(toList());

        System.out.println("cities = " + cities);

        List<Trader> q3 = transactions.stream()
            .map(Transaction::getTrader)
            .filter(trader -> trader.getCity().equals("Cambridge"))
            .distinct()
            .sorted(Comparator.comparing(Trader::getName))
            .collect(toList());

        System.out.println("q3 = " + q3);

        String q4 = transactions.stream()
            .map(transaction -> transaction.getTrader().getName())
            .distinct()
            .sorted()   // 이름을 알파벳 순으로 정렬
            .collect(joining());

        System.out.println("q4 = " + q4);

        boolean q5 = transactions.stream()
            .anyMatch(transaction -> transaction.getTrader().getCity().equals("Milan"));

        System.out.println("q5 = " + q5);

        transactions.stream()
            .filter(transaction -> transaction.getTrader().getCity().equals("Cambridge"))
            .map(Transaction::getValue)
            .forEach(System.out::println);

        Integer q7 = transactions.stream()
            .map(Transaction::getValue)
            .reduce(0, Integer::max);

        System.out.println("q7 = " + q7);

        Optional<Transaction> min = transactions.stream()
            .min(Comparator.comparing(Transaction::getValue));

        System.out.println("min = " + min.get());
    }
}
