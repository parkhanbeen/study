package com.example.advanced.proxy.config.v1proxy.concrete_proxy;

import com.example.advanced.proxy.app.v2.OrderRepositoryV2;
import com.example.advanced.trace.LogTrace;
import com.example.advanced.trace.TraceStatus;

public class OrderRepositoryConcreteProxy extends OrderRepositoryV2 {

  private final OrderRepositoryV2 target;
  private final LogTrace logTrace;

  public OrderRepositoryConcreteProxy(OrderRepositoryV2 target,
                                      LogTrace logTrace) {
    this.target = target;
    this.logTrace = logTrace;
  }

  @Override
  public void save(String itemId) {
    TraceStatus status = null;
    try {
      status = logTrace.begin("OrderRepository.request()");
      target.save(itemId);
      logTrace.end(status);
    } catch (Exception e) {
      logTrace.exception(status, e);
      throw e;
    }
  }
}
