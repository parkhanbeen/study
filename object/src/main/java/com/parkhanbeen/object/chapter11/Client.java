package com.parkhanbeen.object.chapter11;

import java.time.Duration;
import java.time.LocalDateTime;

import com.parkhanbeen.object.chapter10.Call;

public class Client {

  public void enter() {
    Phone phone = new RegularPhone(Money.wons(5), Duration.ofSeconds(10));

    phone.calculateCallFee(new com.parkhanbeen.object.chapter10.Call(
        LocalDateTime.of(2018, 1, 1, 12, 10, 0),
        LocalDateTime.of(2018, 1, 1, 12, 11, 0))
    );

    phone.calculateCallFee(new Call(
        LocalDateTime.of(2018, 1, 2, 12, 10, 0),
        LocalDateTime.of(2018, 1, 2, 12, 11, 0))
    );

    phone.calculateFee(); // Money.wons(60)
  }
}
