package com.example.advanced.proxy.pureproxy.decorator.code;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageDecorator implements Component {

  private final Component component;

  public MessageDecorator(Component component) {
    this.component = component;
  }

  @Override
  public String operation() {
    log.info("message decorator 실행");
    String operation = component.operation();
    String decoratorResult =  "*****" +  operation + "******";

    log.info("message decorator 전 = {}, 후 = {}", operation, decoratorResult);
    return decoratorResult;
  }
}
