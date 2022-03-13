package com.example.advanced.proxy.pureproxy.concreteproxy.code;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConcreteLogic {

  public String operation() {
    log.info("concreteLogic 실행");
    return "data";
  }
}
