package com.example.advanced.strategy;

import com.example.advanced.strategy.code.strategy.ContextV2;
import com.example.advanced.strategy.code.strategy.StrategyLogic1;
import com.example.advanced.strategy.code.strategy.StrategyLogic2;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ContextV2Test {

  @Test
  void strategyV1() {
    ContextV2 contextV2 = new ContextV2();
    contextV2.execute(new StrategyLogic1());
    contextV2.execute(new StrategyLogic2());
  }
}
