package com.example.advanced.proxy.app.v2;


public class OrderServiceV2 {

  private final OrderRepositoryV2 orderRepository;

  public OrderServiceV2(OrderRepositoryV2 orderRepository) {
    this.orderRepository = orderRepository;
  }

  public void orderItem(String orderItem) {
    orderRepository.save(orderItem);
  }
}
