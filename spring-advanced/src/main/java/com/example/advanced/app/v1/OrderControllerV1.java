package com.example.advanced.app.v1;

import com.example.advanced.trace.TraceStatus;
import com.example.advanced.trace.TraceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class OrderControllerV1 {

  private final OrderServiceV1 orderService;
  private final TraceV1 trace;

  @GetMapping("/v1/request")
  public String request(String itemId) {
    TraceStatus status = null;
    try {
      status = trace.begin("orderController.request()");
      orderService.orderItem(itemId);
      trace.end(status);
      return "ok";
    } catch (Exception e) {
      trace.exception(status, e);
      throw e;
    }
  }
}
