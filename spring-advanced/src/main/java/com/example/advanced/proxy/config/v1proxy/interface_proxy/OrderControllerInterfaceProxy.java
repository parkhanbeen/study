package com.example.advanced.proxy.config.v1proxy.interface_proxy;

import com.example.advanced.proxy.app.v1.OrderControllerV1;
import com.example.advanced.trace.LogTrace;
import com.example.advanced.trace.TraceStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderControllerInterfaceProxy implements OrderControllerV1 {

  private final OrderControllerV1 target;
  private final LogTrace logTrace;

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
