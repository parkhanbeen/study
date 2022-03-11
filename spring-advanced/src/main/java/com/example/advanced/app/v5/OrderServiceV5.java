package com.example.advanced.app.v5;

import com.example.advanced.trace.LogTrace;
import com.example.advanced.trace.callback.TraceTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceV5 {

  private final OrderRepositoryV5 orderRepository;
  private final TraceTemplate template;

  public OrderServiceV5(OrderRepositoryV5 orderRepository, LogTrace trace) {
    this.orderRepository = orderRepository;
    this.template = new TraceTemplate(trace);
  }

  public void orderItem(String itemId) {
    template.execute("OrderService.orderItem()", () -> {
      orderRepository.save(itemId);
      return null;
    });

  }
}
