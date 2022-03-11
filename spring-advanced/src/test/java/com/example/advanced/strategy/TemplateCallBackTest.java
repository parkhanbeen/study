package com.example.advanced.strategy;

import com.example.advanced.strategy.code.template.CallBack;
import com.example.advanced.strategy.code.template.TimeLogTemplate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TemplateCallBackTest {

  @Test
  void callBackV1() {
    TimeLogTemplate template = new TimeLogTemplate();
    template.execute(new CallBack() {
      @Override
      public void call() {
        log.info("비즈니스 로직1 실행");
      }
    });
    
    template.execute(new CallBack() {
      @Override
      public void call() {
        log.info("비즈니스 로직2 실행");
      }
    });
  }

  @Test
  void callBackV2() {
    TimeLogTemplate template = new TimeLogTemplate();
    template.execute(() -> log.info("비즈니스 로직1 실행"));

    template.execute(() -> log.info("비즈니스 로직2 실행"));
  }
}
