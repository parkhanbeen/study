package com.example.advanced.proxy.config.v1proxy.interface_proxy;

import com.example.advanced.proxy.app.v1.OrderServiceV1;
import com.example.advanced.trace.LogTrace;
import com.example.advanced.trace.TraceStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderServiceInterfaceProxy implements OrderServiceV1 {

  private final OrderServiceV1 target;
  private final LogTrace logTrace;

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
