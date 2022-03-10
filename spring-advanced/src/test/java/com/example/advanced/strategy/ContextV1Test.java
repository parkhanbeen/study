package com.example.advanced.strategy;

import com.example.advanced.strategy.code.strategy.ContextV1;
import com.example.advanced.strategy.code.strategy.Strategy;
import com.example.advanced.strategy.code.strategy.StrategyLogic1;
import com.example.advanced.strategy.code.strategy.StrategyLogic2;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ContextV1Test {

  @Test
  void strategyV0() {
    logic1();
    logic2();
  }

  private void logic1() {
    long startTime = System.currentTimeMillis();

    log.info("비지니스 로직1 실행");
    long endTime = System.currentTimeMillis();
    long resultTime = endTime - startTime;
    log.info("resultTime = {}", resultTime);
  }

  private void logic2() {
    long startTime = System.currentTimeMillis();

    log.info("비지니스 로직2 실행");
    long endTime = System.currentTimeMillis();
    long resultTime = endTime - startTime;
    log.info("resultTime = {}", resultTime);
  }

  @Test
  void strategyV1() {
    Strategy strategy = new StrategyLogic1();
    ContextV1 context1 = new ContextV1(strategy);
    context1.execute();

    Strategy strategy2 = new StrategyLogic2();
    ContextV1 context2 = new ContextV1(strategy2);
    context2.execute();
  }

  @Test
  void strategyV2() {
    Strategy strategyLogic1 = () -> log.info("비지니스 로직1 실행");

    ContextV1 context1 = new ContextV1(strategyLogic1);
    context1.execute();

    ContextV1 context2 = new ContextV1(() -> log.info("비지니스 로직2 실행"));
    context2.execute();
  }
}
