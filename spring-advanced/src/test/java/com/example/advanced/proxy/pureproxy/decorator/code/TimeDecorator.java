package com.example.advanced.proxy.pureproxy.decorator.code;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeDecorator implements Component {

  private final Component component;

  public TimeDecorator(Component component) {
    this.component = component;
  }

  @Override
  public String operation() {
    log.info("TImeDecorator 실행");
    long startTime = System.currentTimeMillis();
    String result = component.operation();
    long endTime = System.currentTimeMillis();
    long resultTime = endTime - startTime;
    log.info("TImeDecorator 종료 resultTime = {} ", resultTime);
    return null;
  }
}
