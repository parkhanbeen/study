package com.pakrhanbeen.modernjavainaction.chapter17;

import java.util.concurrent.Flow.Publisher;

public class Main {

    public static void main(String[] args) {
        // 뉴욕에 새 Publisher를 만들고 TempSubscriber를 구독시킴
        getTemperatures("New York").subscribe(new TempSubscriber());
        getCelsiusTemperatures("New York").subscribe(new TempSubscriber());
    }

    private static Publisher<TempInfo> getCelsiusTemperatures(String town) {
        return subscriber -> {
            // TempProcessor를 만들고 Subscriber와 반환된 Publisher 사이로 연결
            TempProcessor processor = new TempProcessor();
            processor.subscribe(subscriber);
            processor.onSubscribe(new TempSubscription(processor, town));
        };
    }

    private static Publisher<TempInfo> getTemperatures(String town) {
        // 구독한 Subscriber에게 TempSubscription을 전송하는 Publisher를 반환
        return subscriber -> subscriber.onSubscribe(
            new TempSubscription(subscriber, town)
        );
    }
}
