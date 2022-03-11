package com.example.advanced.strategy.code.template;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeLogTemplate {

  public void execute(CallBack callBack) {
    long startTime = System.currentTimeMillis();

    callBack.call();

    long endTime = System.currentTimeMillis();
    long resultTime = endTime - startTime;
    log.info("resultTime = {}", resultTime);
  }
}
