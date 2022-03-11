package com.example.advanced.app.v4;

import com.example.advanced.trace.LogTrace;
import com.example.advanced.trace.template.AbstractTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class OrderControllerV4 {

  private final OrderServiceV4 orderService;
  private final LogTrace trace;

  @GetMapping("/v4/request")
  public String request(String itemId) {
    AbstractTemplate<String> template = new AbstractTemplate<>(trace) {
      @Override
      protected String call() {
        orderService.orderItem(itemId);
        return "ok";
      }
    };
    return template.execute("orderController.request()");
  }
}
