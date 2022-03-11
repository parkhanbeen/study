package com.example.advanced.app.v5;

import com.example.advanced.trace.LogTrace;
import com.example.advanced.trace.callback.TraceCallBack;
import com.example.advanced.trace.callback.TraceTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderControllerV5 {

  private final OrderServiceV5 orderService;
  private final TraceTemplate template;

  public OrderControllerV5(OrderServiceV5 orderService,
                           LogTrace trace) {
    this.orderService = orderService;
    this.template = new TraceTemplate(trace);
  }

  @GetMapping("/v5/request")
  public String request(String itemId) {
    return template.execute("orderController.request()", new TraceCallBack<>() {
      @Override
      public String call() {
        orderService.orderItem(itemId);
        return "ok";
      }
    });
  }
}
