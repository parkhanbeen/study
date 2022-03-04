package com.example.advanced.app.v2;

import com.example.advanced.trace.TraceStatus;
import com.example.advanced.trace.TraceV1;
import com.example.advanced.trace.TraceV2;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class OrderControllerV2 {

  private final OrderServiceV2 orderService;
  private final TraceV2 trace;

  @GetMapping("/v2/request")
  public String request(String itemId) {
    TraceStatus status = null;
    try {
      status = trace.begin("orderController.request()");
      orderService.orderItem(status.getTraceId(), itemId);
      trace.end(status);
      return "ok";
    } catch (Exception e) {
      trace.exception(status, e);
      throw e;
    }
  }
}
