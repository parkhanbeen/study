package com.example.advanced.proxy.config.v1proxy.concrete_proxy;

import com.example.advanced.proxy.app.v2.OrderControllerV2;
import com.example.advanced.proxy.app.v2.OrderServiceV2;
import com.example.advanced.trace.LogTrace;
import com.example.advanced.trace.TraceStatus;

public class OrderControllerConcreteProxy extends OrderControllerV2 {

  private final OrderControllerV2 target;
  private final LogTrace logTrace;

  public OrderControllerConcreteProxy(OrderControllerV2 target,
                                      LogTrace logTrace) {
    super(null);
    this.target = target;
    this.logTrace = logTrace;
  }

  @Override
  public String request(String itemId) {
    TraceStatus status = null;
    try {
      status = logTrace.begin("OrderController.request()");
      String result = target.request(itemId);
      logTrace.end(status);
      return result;
    } catch (Exception e) {
      logTrace.exception(status, e);
      throw e;
    }
  }

  @Override
  public String noLog() {
    return target.noLog();
  }
}
