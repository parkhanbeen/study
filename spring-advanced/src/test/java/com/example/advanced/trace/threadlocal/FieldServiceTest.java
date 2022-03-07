package com.example.advanced.trace.threadlocal;

import com.example.advanced.trace.threadlocal.code.FieldService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class FieldServiceTest {
  private FieldService fieldService = new FieldService();

  @Test
  void field() {
    log.info("start!!!!");
    Runnable userA = () -> fieldService.logic("userA");
    Runnable userB = () -> fieldService.logic("userB");

    Thread threadA = new Thread(userA);
    threadA.setName("thread-A");
    Thread threadB = new Thread(userB);
    threadB.setName("thread-B");

    threadA.start();
//    sleep(2000); // 동시성 문제 없음
    sleep(100); // 동시성 문제 발생
    threadB.start();

    sleep(3000);
    log.info("exit!!!");
  }

  private void sleep(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
