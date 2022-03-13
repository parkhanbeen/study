package com.example.advanced.proxy.config.v1proxy;

import com.example.advanced.proxy.app.v2.OrderControllerV2;
import com.example.advanced.proxy.app.v2.OrderRepositoryV2;
import com.example.advanced.proxy.app.v2.OrderServiceV2;
import com.example.advanced.proxy.config.v1proxy.concrete_proxy.OrderControllerConcreteProxy;
import com.example.advanced.proxy.config.v1proxy.concrete_proxy.OrderRepositoryConcreteProxy;
import com.example.advanced.proxy.config.v1proxy.concrete_proxy.OrderServiceConcreteProxy;
import com.example.advanced.trace.LogTrace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConcreteProxyConfig {

  @Bean
  public OrderControllerV2 orderControllerV2(LogTrace logTrace) {
    OrderControllerV2 controllerImpl = new OrderControllerV2(orderServiceV2(logTrace));
    return new OrderControllerConcreteProxy(controllerImpl, logTrace);
  }

  @Bean
  public OrderServiceV2 orderServiceV2(LogTrace logTrace) {
    OrderServiceV2 serviceImpl = new OrderServiceV2(orderRepositoryV2(logTrace));
    return new OrderServiceConcreteProxy(serviceImpl, logTrace);
  }

  @Bean
  public OrderRepositoryV2 orderRepositoryV2(LogTrace logTrace) {
    OrderRepositoryV2 repositoryImpl = new OrderRepositoryV2();
    return new OrderRepositoryConcreteProxy(repositoryImpl, logTrace);

  }
}
