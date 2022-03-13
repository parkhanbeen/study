package com.example.advanced.proxy.config.v1proxy.interface_proxy;

import com.example.advanced.proxy.app.v1.OrderRepositoryV1;
import com.example.advanced.trace.LogTrace;
import com.example.advanced.trace.TraceStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderRepositoryInterfaceProxy implements OrderRepositoryV1 {

  private final OrderRepositoryV1 target;
  private final LogTrace logTrace;

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
