package com.example.advanced.proxy.config.v1proxy.concrete_proxy;

import com.example.advanced.proxy.app.v2.OrderRepositoryV2;
import com.example.advanced.proxy.app.v2.OrderServiceV2;
import com.example.advanced.trace.LogTrace;
import com.example.advanced.trace.TraceStatus;

public class OrderServiceConcreteProxy extends OrderServiceV2 {

  private final OrderServiceV2 target;
  private final LogTrace logTrace;

  public OrderServiceConcreteProxy(OrderServiceV2 target,
                                   LogTrace logTrace) {
    super(null);
    this.target = target;
    this.logTrace = logTrace;
  }

  @Override
  public void orderItem(String orderItem) {
    TraceStatus status = null;
    try {
      status = logTrace.begin("OrderService.orderItem()");
      target.orderItem(orderItem);
      logTrace.end(status);
    } catch (Exception e) {
      logTrace.exception(status, e);
      throw e;
    }
  }
}
