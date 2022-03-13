package com.example.advanced.proxy.pureproxy.concreteproxy.code;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeProxy extends ConcreteLogic {
  private final ConcreteLogic concreteLogic;

  public TimeProxy(ConcreteLogic concreteLogic) {
    this.concreteLogic = concreteLogic;
  }


  @Override
  public String operation() {
    log.info("TImeDecorator 실행");
    long startTime = System.currentTimeMillis();
    String result = concreteLogic.operation();
    long endTime = System.currentTimeMillis();
    long resultTime = endTime - startTime;
    log.info("TImeDecorator 종료 resultTime = {} ", resultTime);
    return result;
  }
}
